import java.io.*;
import javax.imageio.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

public class Map {
	public static int height = 52; // 52
	public static int width = 90; // 90

	public static int randFillPercent = 46; // 46 [+4 / -3]
	public boolean randSeed = true;

	public int[][] map = new int[height][width];
	public BufferedImage[][] tileMap = new BufferedImage[height][width];
	public int[][] foreground = new int[height][width];
	public static int smooths = 4;

	public int entityNumber = 0;
	// public static Entity[][] entity_map = new Entity[height][width];
	public HashMap<String,Entity> entities = new HashMap<String,Entity>();

	public Entity player;

	public int tileSize = 24;

	private Random rng = new Random();
	private Pathfinder pf = new Pathfinder();

	public String maptype = "sourcedCave";
	private MapTypes type = new MapTypes(maptype);

	public static final boolean undercity = true;

	public boolean debugFlag = false;

	public final int UP = 0;
	public final int RIGHT = 1;
	public final int DOWN = 2;
	public final int LEFT = 3;


	Map(int _height, int _width, int _randFillPercent, Random _rng){
		this(_height,_width,_randFillPercent,smooths,_rng);
	}

	Map(int _height, int _width, int _randFillPercent, int _smooths, Random _rng){
		height = _height;
		width = _width;
		randFillPercent = _randFillPercent;
		smooths = _smooths;
		rng = _rng;

		map = new int[height][width];
		foreground = new int[height][width];
		tileMap = new BufferedImage[height][width];

		if(undercity){
			generateUndercity();
		}else{
			generateCaves();
		}
		
		System.out.println("placing exits...");
		placeExits();
		System.out.println("exits placed");
		printMap();
		System.out.println("building tile map...");
		buildTileMap();
		
		System.out.println("done.");
		if(debugFlag) System.out.println("debug flag raised");
	}

	// methods

	public int[][] getMap(){
		return map;
	}

	public boolean isOnMap(int x, int y){
		if(x>=height || y>= width || x<0 || y<0) return false;
		return true;
	}

	public boolean isOpen(int x, int y){
		if(!isOnMap(x,y)) return false;
		if(map[x][y]!=1 && map[x][y]!=6) return true;
		return false;
	}

	public boolean isFullOpen(int x, int y){
		for(Entity e: entities.values()){
			if(e.x==x && e.y==y) return false;
		}
		if(player.x == x && player.y == y) return false;
		return isOpen(x,y);
	}


	public void generateCaves(){
		for(int x=0; x < height; x++){
			for(int y=0; y < width; y++){
				// if border, wall
				if(x==0||x==height-1||y==0||y==width-1){
					map[x][y] = 1;
					// random placement
				} else if(rng.nextInt(100)<=randFillPercent){
					map[x][y] = 1;
				}else{
					map[x][y] = 0;
				}
			}
		}
		for(int i = 0; i < smooths-1; i++){
			map = smoothMap(4);
		}
		map = smoothMap(4);
	}


	public void generateUndercity(){
		ArrayList<Room> rooms;
		ArrayList<PointDir> workingDoors;
		int doorCount = 0;
		do {
			doorCount = 0;
			for (int x = 0; x < height; x++) {
				for (int y = 0; y < width; y++) {
					map[x][y] = 1;
				}
			}
			rooms = new ArrayList<Room>();
			workingDoors = new ArrayList<PointDir>();
			Queue<PointDir> q = new LinkedList<PointDir>();

			// starting point
			// Top = 0, Right = 1, Bottom = 2, Left = 3
			int quadrant = rng.nextInt(3);
			switch(quadrant){
			case 0:
				q.add(new PointDir(new Point(rng.nextInt(height/3), rng.nextInt(width-1)), 'D'));
				break;
			case 1:
				q.add(new PointDir(new Point(rng.nextInt(height-1), width-rng.nextInt(width/3)-1), 'L'));
				break;
			case 2:
				q.add(new PointDir(new Point(height-rng.nextInt(height/3)-1, rng.nextInt(width-1)), 'T'));
				break;
			case 3:
				q.add(new PointDir(new Point(rng.nextInt(height-1), rng.nextInt(width/3)), 'R'));
				break;
			}



			boolean start = true;
			while (!q.isEmpty()) {
				PointDir p = q.remove();
				Room r;
				int attempts = 0;
				// room vs corridor
				if (rng.nextInt(10)>=3 || start) {
					do {
						r = new Room(p, start, RoomType.RANDOM);
						if (r.valid) {
							rooms.add(r);
							q = r.addDoorsToQueue(q);
						}
						attempts++;
					} while (!r.valid && attempts < 50);
				}else{
					ArrayList<PointDir> doors = buildCorridor(p);
					if (doors!=null) {
						for (PointDir i : doors) {
							q.add(i);
						}
					}
				}
				if (attempts >= 50) {
					map[p.point.x][p.point.y] = 0;
				} else if(!start){
					workingDoors.add(p);
				}
				for (PointDir i : q) {
					map[i.point.x][i.point.y] = 5;
				}
				start = false;
				// printMap();
				// System.out.println("$$$");
			}

			for(PointDir p: workingDoors){
				Point temp = aheadTile(p);
				PointDir newDoor = new PointDir(temp,p.dir);
				// if door leads nowhere, fill it in
				if(!isOpen(aheadTile(newDoor).x,aheadTile(newDoor).y)){
					if(!(temp.x<0 || temp.y<0 || temp.x>height-1 || temp.y>width-1)) map[p.point.x][p.point.y] = 1;

					// if door is not between two walls, destroy it
				}else if(isSandwich(temp.x,temp.y) ){
					map[temp.x][temp.y] = 5;
					doorCount++;
					map[p.point.x][p.point.y] = 0;
				}else{
					map[p.point.x][p.point.y] = 1;
				}
			}
			// System.out.println(doorCount);
		}while(doorCount<28);

		doctorMap();
		printMap();
		System.out.println("F:");
	}

	public ArrayList<PointDir> buildCorridor(PointDir door){
		System.out.println("building corridor...");
		ArrayList<PointDir> doors = new ArrayList<PointDir>();

		char dir = door.dir;
		int length = 0; 
		PointDir end = door;
		boolean obstructed = false;
		while(!obstructed && length<=5 && (rng.nextInt(7)<=5 || length <=3)){
			Point temp = aheadTile(end);
			if(isOpen(temp.x,temp.y) || end.point.x <= 1 || end.point.x >= height-2 || end.point.y <= 1 || end.point.y >= width-2){
				obstructed = true;
			}
			if(!obstructed){
				map[end.point.x][end.point.y] = 0;
				// doors on sides
				if(rng.nextInt(8)>=5){
					if (dir=='T' || dir=='D') {
						if (rng.nextBoolean()) {
							// left of path
							if (!isOpen(end.point.x, end.point.y - 1) && end.point.y - 1 > 1) {
								doors.add(new PointDir(new Point(end.point.x, end.point.y - 1), 'L'));

								// right of path	
							} else if (!isOpen(end.point.x, end.point.y + 1) && end.point.y + 1 < width-1) {
								doors.add(new PointDir(new Point(end.point.x, end.point.y + 1), 'R'));
							}
						} else {
							// right of path
							if (!isOpen(end.point.x, end.point.y + 1) && end.point.y + 1 < width-1) {
								doors.add(new PointDir(new Point(end.point.x, end.point.y + 1), 'R'));

								// left of path
							} else if (!isOpen(end.point.x, end.point.y - 1) && end.point.y - 1 > 1) {
								doors.add(new PointDir(new Point(end.point.x, end.point.y - 1), 'L'));
							}
						}
					}else if(dir=='L' || dir=='R'){
						if (rng.nextBoolean()) {
							// up of path
							if (!isOpen(end.point.x-1, end.point.y) && end.point.x - 1 > 1) {
								doors.add(new PointDir(new Point(end.point.x-1, end.point.y), 'T'));

								// down of path	
							} else if (!isOpen(end.point.x+1, end.point.y) && end.point.x + 1 < height-1) {
								doors.add(new PointDir(new Point(end.point.x+1, end.point.y), 'D'));
							}
						} else {
							// down of path
							if (!isOpen(end.point.x+1, end.point.y) && end.point.x + 1 < height-1) {
								doors.add(new PointDir(new Point(end.point.x+1, end.point.y), 'D'));
								// up of path
							}else if (!isOpen(end.point.x-1, end.point.y) && end.point.x - 1 > 1) {
								doors.add(new PointDir(new Point(end.point.x-1, end.point.y), 'T'));
							}
						}
					}
				}
				end.point = temp;
				length++;
			}
		}
		// map[door.point.x][door.point.y] = 5;
		if(rng.nextInt(10)>=7){
			if(end.dir=='T' || end.dir=='D'){
				if(rng.nextBoolean()){
					end.dir = 'L';
				}else{
					end.dir = 'R';
				}
			}else{
				if(rng.nextBoolean()){
					end.dir = 'T';
				}else{
					end.dir = 'D';
				}
			}
			map[end.point.x][end.point.y] = 0;
			end.point = aheadTile(end);
		}
		doors.add(end);

		if(length>=3){
			return doors;
		}else{
			return null;
		}

	}

	public boolean isSandwich(int x, int y){
		if(!(x>0 && x<height-1 && y>0 && y<width-1)) return false;
		if((map[x-1][y] == 1 && map[x+1][y] == 1) || (map[x][y-1] == 1 && map[x][y+1] == 1)) return true;
		return false;
	}

	public class PointDir {
		public Point point;
		public char dir;

		public Point getPos(){
			return point;
		}

		PointDir(Point p, char d){
			point = p;
			dir = d;
		}
	}

	public Point aheadTile(PointDir p){
		Point point = p.point;
		char dir = p.dir;
		if(dir=='T'){
			return new Point(point.x-1,point.y);
		}else if(dir=='L'){
			return new Point(point.x,point.y-1);
		}else if(dir=='D'){
			return new Point(point.x+1,point.y);
		}else if(dir=='R'){
			return new Point(point.x,point.y+1);
		}
		return null;
	}

	public enum RoomType{
		RANDOM,
		REGULAR,
		SEWER;
	}

	public class Room {

		public int x;
		public int y;
		public int w;
		public int h;
		public PointDir door;
		public int doors;
		public PointDir[] doorPoints = new PointDir[4];
		public PointDir[] localDoorPoints = new PointDir[4];
		boolean[] opens = new boolean[4];
		boolean works = true;
		boolean valid = true;
		boolean start = false; 
		char dir;
		RoomType type;


		Room(PointDir _door, boolean _start, RoomType _type){
			boolean works = true;
			int count = 0;
			start = _start;
			door = _door;
			dir = door.dir;
			type = _type;

			if(type.equals(RoomType.RANDOM)){
				// TODO: pick random
				if(rng.nextInt(10)>=6){
					type = RoomType.SEWER;
				}else{
					type = RoomType.REGULAR;
				}
			}

			do{
				works = true;
				setBounds();

				if(dir=='T'){
					x = door.point.x-h-1;
					y = door.point.y;
				}else if(dir=='D'){
					x = door.point.x+1;
					y = door.point.y;
				}else if(dir=='L'){
					x = door.point.x;
					y = door.point.y-w-1;
				}else if(dir=='R'){
					x = door.point.x;
					y = door.point.y+1;
				}
				works = surroundCheck();
				count++;
			} while(!works && count < 100);


			if (count<100) {
				buildNormal();
			}else{
				valid = false;
			}
		}

		public void setBounds(){
			if (type.equals(RoomType.REGULAR)) {
				int min = rng.nextInt(1)+3;
				w = rng.nextInt(5) + min;
				h = rng.nextInt(5) + min;
			}else if (type.equals(RoomType.SEWER)){
				final int l_min = 7;
				final int l_var = 5;

				if(dir=='T' || dir=='D'){
					// wide
					w = rng.nextInt(l_var)+l_min;
					h = rng.nextInt(3)+4;
				}else{
					// tall
					h = rng.nextInt(l_var)+l_min;
					w = rng.nextInt(3)+4;
				}
			}
		}

		public boolean surroundCheck(){
			for(int _x = x-1; _x <= x+h; _x++){
				for(int _y = y-1; _y <= y+w; _y++){
					if(_x>=height-1||_x<=0||_y>=width-1||_y<=0){
						return false;
					}else if(isOpen(_x,_y)){
						return false;
					}
				}
			}
			return true;
		}

		public void buildNormal(){
			if(type.equals(RoomType.REGULAR)){
				System.out.println("building regular...");
				pickDoors();
				placeDoors();
				cutOut();
			}else if(type.equals(RoomType.SEWER)){
				System.out.println("building sewer...");
				if(dir=='T'){
					opens[UP] = true;
				}else if(dir=='D'){
					opens[DOWN] = true;
				}else if(dir=='L'){
					opens[LEFT] = true;
				}else if(dir=='R'){
					opens[RIGHT] = true;
				}
				placeDoors();
				cutOut();
				buildSewer();
			}
		}

		public void customize(){
			cutOut();
			if(RoomType.SEWER.equals(this.type)){
				buildSewer();
			}
		}

		public void buildSewer(){

			// TODO: add varying bridge placements and water h/w
			// TODO: add precons

			if(h<=3 || w<=3) return;
			int waterWidth = 2;
			boolean noBridge = false; // (doors==0 && rng.nextInt(10)<=7);
			if(doorPoints[1] == null && doorPoints[3] == null && w>4){
				// horizontal
				int river_h;
				if(h<=4){
					river_h = 1;
				}else{
					river_h = rng.nextInt(h-4)+1;
				}

				// - 2 for edges, -2 for width, -river_h, +1
				// 5h1 -> [0,1] (+2) -> [2,3]
				// 5h2 -> [0] (+2) -> [2]
				if(h-2-river_h+1 == 2){
					waterWidth = 2;
				}else{
					if(rng.nextBoolean()){
						waterWidth = rng.nextInt(h-2-river_h+1-2)+2;
					}else{
						waterWidth = river_h-2;
					}
				}

				int bridge_w = w/2;
				for(int i = 0; i < w; i++){
					if (noBridge || i!=bridge_w) {
						for(int t = 0; t < waterWidth; t++){
							map[x + river_h + t][y + i] = 6;
						}
					}
				}
			}else if(doorPoints[0] == null && doorPoints[2] == null && h>4){
				// width
				int river_w;
				if(w<=4){
					river_w = 1;
				}else{
					river_w = rng.nextInt(w-4)+1;
				}

				if(w-2-river_w+1 == 2){
					waterWidth = 2;
				}else{
					if(rng.nextBoolean()){
						waterWidth = rng.nextInt(w-2-river_w+1-2)+2;
					}else{
						waterWidth = river_w-2;
					}
				}

				int bridge_h = rng.nextInt(h-2)+1;
				for(int i = 0; i < h; i++){
					if (noBridge || i!=bridge_h) {
						for(int t = 0; t < waterWidth; t++){
							map[x + i][y + river_w + t] = 6;
						}
					}
				}
			}
		}

		public void pickDoors(){
			// (T) top = x,y -> x,y+w
			// (L) left = x,y -> x+h,y
			// (R) right = x,y+w -> x+h,y+w
			// (D) bottom = x+h,y -> x+h,y+w
			int[][] chart = {{ 0,5 },{ 1, 30 }, { 2, 40 }, { 3, 25 }};
			RandomChart rnd = new RandomChart(chart);
			doors = rnd.pick();
			if(start) doors = 3;
			if(dir=='T') opens[UP] = false;
			if(dir=='L') opens[LEFT] = false;
			if(dir=='D') opens[DOWN] = false;
			if(dir=='R') opens[RIGHT] = false;

			for(int i = 0; i < doors; i++) {
				boolean flag = false;
				do {
					int temp = rng.nextInt(3);
					if (!opens[temp]) {
						flag = true;
						opens[temp] = true;
					}
				} while (!flag);
			}
		}

		public void placeDoors(){
			if (opens[UP]) {
				localDoorPoints[UP] = new PointDir(new Point(0, rng.nextInt(w-1)),'T');

				doorPoints[UP] = new PointDir(new Point(x, rng.nextInt(w-1) + y),'T');
			}
			if (opens[RIGHT]) {
				localDoorPoints[RIGHT] = new PointDir(new Point(rng.nextInt(h-1), w-1),'R');

				doorPoints[RIGHT] = new PointDir(new Point(rng.nextInt(h-1) + x, y + w-1),'R');
			}
			if (opens[DOWN]) {
				localDoorPoints[DOWN] = new PointDir(new Point(h-1, rng.nextInt(w-1)),'D');

				doorPoints[DOWN] = new PointDir(new Point(x + h-1, rng.nextInt(w-1) + y),'D');
			}
			if (opens[LEFT]) {
				localDoorPoints[LEFT] = new PointDir(new Point(rng.nextInt(h-1), 0),'L');

				doorPoints[LEFT] = new PointDir(new Point(rng.nextInt(h-1) + x, y),'L');
			}
		}

		public Queue<PointDir> addDoorsToQueue(Queue<PointDir> q){
			for(int i = 0; i < doorPoints.length; i++){
				if(doorPoints[i]!=null) q.add(doorPoints[i]);
			}
			return q;
		}

		public void cutOut(){
			for(int _x = x; _x < x+h; _x++){
				for(int _y = y; _y < y+w; _y++){
					map[_x][_y] = 0;
				}
			}
		}

		public void putIntoMap(int[][] room){
			for(int _x = 0; _x < h; _x++){
				for(int _y = 0; _y < w; _y++){
					map[_x+x+1][_y+y+1] = room[_x][_y];
				}
			}
		}

	}

	// default 4
	public int[][] smoothMap(int threshold){
		int[][] temp = new int[height][width];
		for(int x=0; x < height; x++){
			for(int y=0; y < width; y++){
				int adj = surroundingWallCount(x,y);
				if(adj>threshold) temp[x][y] = 1;
				if(adj<threshold) temp[x][y] = 0;
				if(adj==threshold) temp[x][y] = map[x][y];
			}
		}
		return temp;
	}

	public int surroundingWallCount(int centerX, int centerY){
		int walls = 0;
		for(int x = centerX-1; x <= centerX+1; x++){
			for(int y = centerY-1; y <= centerY+1; y++){
				if(x>=0&&y>=0 && x<height&&y<width){
					if(x!=centerX||y!=centerY){
						walls+= map[x][y];
					}
				}else{
					walls++;
				}
			}
		}
		return walls;
	}


	public boolean connectedPoints(int[][] temp, int x1, int y1, int x2, int y2){
		if(x1==x2&&y1==y2){
			return true;
		}
		if(temp[x1][y1] == 1 || temp[x1][y1] == 4) return false;

		temp[x1][y1] = 4;
		// down
		if(connectedPoints(temp,x1+1,y1,x2,y2)) return true;
		// right
		if(connectedPoints(temp,x1,y1+1,x2,y2)) return true;
		// up
		if(connectedPoints(temp,x1-1,y1,x2,y2)) return true;
		// left
		if(connectedPoints(temp,x1,y1-1,x2,y2)) return true;

		return false;
	}

	public int[][] copyMap(){
		int[][] temp = new int[height][width];
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				temp[y][x] = map[y][x];
			}
		}
		return temp;
	}

	public int tileTypeAdj(int centerX, int centerY){
		// generates binary numbers (8421=urdl)
		int type = 0;
		// up 
		if(!(centerX<=0) && map[centerX][centerY]!=map[centerX-1][centerY]) type+=1;
		// right
		if(!(centerY>=width-1) && map[centerX][centerY]!=map[centerX][centerY+1]) type+=2;
		// down
		if(!(centerX>=height-1) && map[centerX][centerY]!=map[centerX+1][centerY]) type+=4;
		// left
		if(!(centerY<=0) && map[centerX][centerY]!=map[centerX][centerY-1]) type+=8;

		return type;
	}

	public Point randomOpenSpace(){
		int x;
		int y;
		do{
			System.out.println("picking point...");
			x = rng.nextInt(height-1);
			y = rng.nextInt(width-1);
		}while(!isFullOpen(x,y));
		return (new Point(x,y));
	}

	public Point getPosition(int uniqueTile){
		for(int x = 0; x < height; x++){
			for(int y = 0; y < width; y++){
				if(map[x][y]==uniqueTile){
					return (new Point(x,y));
				}
			}
		}
		return null;
	}

	public int valueAt(Point p){
		return map[p.x][p.y];
	}

	public void placeExits(){
		int x1=0;
		int y1=0;
		int x2=0;
		int y2=0;

		int tries = 0;
		Pathfinder.PointBFS p;
		do{
			do {
				do {
					x1 = rng.nextInt(height);
					y1 = rng.nextInt(width);
				} while (!isOpen(x1,y1));
				do {
					x2 = rng.nextInt(height);
					y2 = rng.nextInt(width);
				} while (!isOpen(x2,y2));
				tries++;
			} while (!(Math.abs(x2-x1)+Math.abs(y2-y1)>=Math.min(Math.max(width, height)/2.25,Math.min(width,height))) && tries<=300);
			if(tries>=300){
				System.out.println("placing exits failed.");
				if(undercity){
					generateUndercity();
				}else{
					generateCaves();
				}
				placeExits();
				return;
			}
			p = pf.pathfindBFS(new Point(x1,y1), new Point(x2,y2), copyMap(), true);
		} while(p == null || p.getParent()==null);

		while(p.getParent() != null){
			// map[p.x][p.y] = 4;
			p = p.getParent();
		}

		map[x1][y1] = 2;
		map[x2][y2] = 3;
	}

	public boolean hasSouthFace(int x, int y){
		int t = tileTypeAdj(x,y);
		if((t>=4 && t<=7)||(t>=12 && t<=15)) return true;
		return false;
	}

	public boolean isTouchingVisible(int x, int y){
		for(int cx = x-1; cx<=x+1; cx++){
			for(int cy = y-1; cy<=y+1; cy++){
				if(isOpen(cx,cy) || isTransparent(cx,cy)) return true;
			}
		}
		return false;
	}


	public boolean isTransparent(int x, int y){
		if(!isOnMap(x,y)) return false;
		if(map[x][y] != 1) return true;
		return false;
	}

	public void doctorMap(){
		// this is where all janky map fixes go
		// cleans up errors in map

		for(int x = 0; x < height; x++){
			for(int y = 0; y < width; y++){
				// fixes bad water tiles
				if(map[x][y] == 6){
					if(tileTypeAdj(x,y) == 11 || tileTypeAdj(x,y) == 14){
						if(map[x][y+1] == 0){
							map[x][y+1] = 6;
						}else{
							map[x][y-1] = 6;
						}
					}else if(tileTypeAdj(x,y) == 10){
						map[x][y] = 1;
					}
				}
			}
		}
	}


	/**END OF GENERATION**/

	public void buildTileMap(){
		for(int x = 0; x < height; x++){
			for(int y = 0; y < width; y++){
				tileMap[x][y] = type.pickImage(map[x][y],tileTypeAdj(x,y));
			}
		}
	}

	public String addEntity(Entity entity){
		String name = entity.getName()+"/"+entityNumber;
		entities.put(name,entity);
		entityNumber++;
		System.out.println(name);
		return name;
	}

	// prints map to console
	public void printMap(){
		for(int x=0; x < height; x++){
			for(int y=0; y < width; y++){
				System.out.print(type.tile_characters[map[x][y]]+" ");
			}
			System.out.println();
		}
	}

	public BufferedImage renderMap() {
		return renderArea(0,0,height-1,width-1); 
	}
	
	public boolean[][] buildOpacityMap(){
		boolean[][] temp = new boolean[height][width];
		for(int x=0; x < height; x++){
			for(int y=0; y < width; y++){
				temp[x][y] = isTransparent(x,y);
			}
		}
		return temp;
	}

//	public boolean[][] skew(boolean[][] map){
//		boolean[][] temp = new boolean[map[0].length][map.length];
//		for(int x=0; x < map[0].length; x++){
//			for(int y=0; y < map.length; y++){
//				temp[x][y] = map[y][y];
//			}
//		}
//		return temp;
//	}

	public BufferedImage renderArea(int x1, int y1, int x2, int y2){
		int areaHeight = Math.abs(x2-x1)+1;
		int areaWidth = Math.abs(y2-y1)+1;

		BufferedImage area = new BufferedImage(areaWidth*tileSize,areaHeight*tileSize,BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) area.getGraphics();
		BufferedImage dark;
		
		FOV fov = new FOV();
		boolean[][] lightMap = fov.calculate(buildOpacityMap(), player.x, player.y);
		// BufferedImage SouthWall;
		try {
			dark = ImageIO.read(new File(type.PATH+"dark.png"));
			// SouthWall = ImageIO.read(new File(type.PATH+"Floor.png"));

			// draw
			for(int x=Math.min(x1,x2); x <= Math.max(x1,x2); x++){ // row #
				for(int y=Math.min(y1,y2); y <= Math.max(y1,y2); y++){ // column #
					BufferedImage tile;
					int x_ofs = x - Math.min(x1,x2);
					int y_ofs = y - Math.min(y1,y2);
					if(x>= height || y>= width || x<0 || y<0 || (map[x][y] == 1 && !isTouchingVisible(x,y)) || /* !lightMap[x][y] */){
						tile = dark;
					}else{
						tile = tileMap[x][y];
					}
					if(!(tile==null)){
						if(tile!=dark && map[x][y] == 5) g.drawImage(type.usedImages[1],y_ofs*tileSize, x_ofs*tileSize,null);
						g.drawImage(tile,y_ofs*tileSize, x_ofs*tileSize,null);
					}
					for(Entity e: entities.values()){
						if(e.getX() == x && e.getY() == y){
							g.drawImage(e.getImg(),y_ofs*tileSize,x_ofs*tileSize,null);
						}
					}
					if(player != null && player.getX() == x && player.getY() == y){
						g.drawImage(player.getImg(),y_ofs*tileSize,x_ofs*tileSize,null);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		};
		return area;
	}

	public BufferedImage renderRadius(int x, int y, int r){
		BufferedImage area = renderArea(x-r,y-r,x+r,y+r);
		// circle shading
		return area;
	}

	public BufferedImage addVignette(BufferedImage image, int x, int y, int r){
		int d = r*2*tileSize;
		Graphics g = (Graphics2D) image.getGraphics();
		//g.setColor(new Color(18,28,38,50));
		//g.fillRect(0,0,image.getWidth(), image.getHeight());
		g.setColor(new Color(225,185,85,45));
		g.fillOval(x,y,d,d);
		return image;
	}

	public BufferedImage addCenterVignette(BufferedImage image, int r){
		int pixr = r*tileSize;
		return addVignette(image,image.getWidth()/2 - pixr,image.getHeight()/2 - pixr,r);
	}

	public BufferedImage render_vig(int x, int y, int r, int vr){
		BufferedImage image = renderRadius(x,y,r);
		// image = addCenterVignette(image,vr);
		return image;
	}

	// TODO: FOV

}