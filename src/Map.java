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


	public static boolean undercity = true;

	Map(){
		this(height,width,randFillPercent);
	}
	Map(int _height, int _width, int _randFillPercent){
		this(_height,_width,_randFillPercent,smooths);
	}

	Map(int _height, int _width, int _randFillPercent, int _smooths){
		height = _height;
		width = _width;
		randFillPercent = _randFillPercent;
		smooths = _smooths;

		map = new int[height][width];
		tileMap = new BufferedImage[height][width];

		if(undercity){
			generateUndercity();
		}else{
			generateCaves();
		}
		placeExits();
		printMap();
		buildTileMap();
	}

	Map(int[][] _map, HashMap<String,Entity> _entities){
		map = _map;
		entities = _entities;
	}

	Map(Map that){
		this(that.getMap(),that.entities);
	}

	// methods

	public int[][] getMap(){
		return map;
	}

	public boolean isOpen(int x, int y){
		if(x>=height || y>= width || x<0 || y<0) return false;
		if(map[x][y]!=1) return true;
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

			// TODO: starting point
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
				if (rng.nextInt(9)<7 || start) {
					do {
						r = new Room(p, start, RoomType.REGULAR);
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
					map[p.point.x][p.point.y] = 0;
				}
			}
			// System.out.println(doorCount);
		}while(doorCount<25);

		printMap();
		System.out.println("F:");
		// smoothMap(3); // 3 or 4 TODO: constant
	}

	public ArrayList<PointDir> buildCorridor(PointDir door){
		ArrayList<PointDir> doors = new ArrayList<PointDir>();

		char dir = door.dir;
		int length = 0; 
		PointDir end = door;
		boolean obstructed = false;
		while(!obstructed && length<8 && (rng.nextInt(7)<=5 || length <=3)){
			Point temp = aheadTile(end);
			if(isOpen(temp.x,temp.y) || end.point.x <= 1 || end.point.x >= height-2 || end.point.y <= 1 || end.point.y >= width-2){
				obstructed = true;
			}
			if(!obstructed){
				map[end.point.x][end.point.y] = 0;
				// doors on sides
				if(rng.nextInt(8)>6){
					if (dir=='T' || dir=='D') {
						if (rng.nextBoolean()) {
							// left of path
							if (!isOpen(end.point.x, end.point.y - 1) && end.point.y - 1 > 1) {
								doors.add(new PointDir(new Point(end.point.x, end.point.y - 1),
										'L'));

								// right of path	
							} else if (!isOpen(end.point.x, end.point.y + 1)
									&& end.point.y + 1 < width-1) {
								doors.add(new PointDir(new Point(end.point.x, end.point.y + 1),
										'R'));
							}
						} else {
							// right of path
							if (!isOpen(end.point.x, end.point.y + 1)
									&& end.point.y + 1 < width-1) {
								doors.add(new PointDir(new Point(end.point.x, end.point.y + 1),
										'R'));

								// left of path
							} else if (!isOpen(end.point.x, end.point.y - 1)
									&& end.point.y - 1 > 1) {
								doors.add(new PointDir(new Point(end.point.x, end.point.y - 1),
										'L'));
							}
						}
					}else if(dir=='L' || dir=='R'){
						if (rng.nextBoolean()) {
							// up of path
							if (!isOpen(end.point.x-1, end.point.y) && end.point.x - 1 > 1) {
								doors.add(new PointDir(new Point(end.point.x-1, end.point.y),
										'T'));

								// down of path	
							} else if (!isOpen(end.point.x+1, end.point.y)
									&& end.point.x + 1 < height-1) {
								doors.add(new PointDir(new Point(end.point.x+1, end.point.y),
										'D'));
							}
						} else {
							// down of path
							if (!isOpen(end.point.x+1, end.point.y)
									&& end.point.x + 1 < height-1) {
								doors.add(new PointDir(new Point(end.point.x+1, end.point.y),
										'D'));
								// up of path
							}else if (!isOpen(end.point.x-1, end.point.y) && end.point.x - 1 > 1) {
								doors.add(new PointDir(new Point(end.point.x-1, end.point.y),
										'T'));
							}
						}
					}
				}
				end.point = temp;
				length++;
			}
		}
		// map[door.point.x][door.point.y] = 5;
		doors.add(end);

		if(length>3){
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
		REGULAR,
		CORRIDOR;
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


		Room(PointDir _door, boolean _start, RoomType type){
			boolean works = true;
			int count = 0;
			start = _start;
			door = _door;
			dir = door.dir;

			do{
				works = true;
				if(type.equals(RoomType.CORRIDOR)){
					w = rng.nextInt(8)+4;
					h = rng.nextInt(8)+4;
				}else if(type.equals(RoomType.REGULAR)){
					w = rng.nextInt(7)+3;
					h = rng.nextInt(7)+3;
				}
				// TODO: check correctness
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
				if(type.equals(RoomType.REGULAR)) buildNormal();
			}else{
				valid = false;
			}
		}

		Room(int _w, int _h){
			w = _w;
			h = _h;
			start = true;
			buildNormal();
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
			pickDoors();
			placeDoors();
			if(valid) cutOut();
		}

		public void pickDoors(){
			// (T) top = x,y -> x,y+w
			// (L) left = x,y -> x+h,y
			// (R) right = x,y+w -> x+h,y+w
			// (B) bottom = x+h,y -> x+h,y+w
			int[][] chart = {{ 0,10 },{ 1, 35 }, { 2, 30 }, { 3, 25 }};
			RandomChart rnd = new RandomChart(chart);
			doors = rnd.pick();
			if(start) doors = 3;
			if(dir=='T') opens[0] = false;
			if(dir=='L') opens[1] = false;
			if(dir=='R') opens[2] = false;
			if(dir=='D') opens[3] = false;

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
			if (opens[0]) {
				localDoorPoints[0] = new PointDir(new Point(0, rng.nextInt(w-1)),'T');

				doorPoints[0] = new PointDir(new Point(x, rng.nextInt(w-1) + y),'T');
			}
			if (opens[1]) {
				localDoorPoints[1] = new PointDir(new Point(rng.nextInt(h-1), 0),'L');

				doorPoints[1] = new PointDir(new Point(rng.nextInt(h-1) + x, y),'L');
			}
			if (opens[2]) {
				localDoorPoints[2] = new PointDir(new Point(rng.nextInt(h-1), w-1),'R');

				doorPoints[2] = new PointDir(new Point(rng.nextInt(h-1) + x, y + w-1),'R');
			}
			if (opens[3]) {
				localDoorPoints[3] = new PointDir(new Point(h-1, rng.nextInt(w-1)),'D');

				doorPoints[3] = new PointDir(new Point(x + h-1, rng.nextInt(w-1) + y),'D');
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
			x = rng.nextInt(height);
			y = rng.nextInt(width);
		}while(!isOpen(x,y));
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
				} while (map[x1][y1] == 1);
				do {
					x2 = rng.nextInt(height);
					y2 = rng.nextInt(width);
				} while (map[x2][y2] == 1);
			} while (!(Math.abs(x2-x1)+Math.abs(y2-y1)>=Math.min(Math.max(width, height)/2.25,Math.min(width,height))));
			tries++;
			if(tries>=200){
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
				if(isOpen(cx,cy)) return true;
			}
		}
		return false;
	}


	// TODO **END OF GENERATION** TODO

	public void buildTileMap(){
		for(int x = 0; x < height; x++){
			for(int y = 0; y < width; y++){
				tileMap[x][y] = type.pickImage(map[x][y]);
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

	public BufferedImage renderArea(int x1, int y1, int x2, int y2){
		int areaHeight = Math.abs(x2-x1)+1;
		int areaWidth = Math.abs(y2-y1)+1;

		BufferedImage area = new BufferedImage(areaWidth*tileSize,areaHeight*tileSize,BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) area.getGraphics();
		BufferedImage dark;
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
					if(x>= height || y>= width || x<0 || y<0 || (map[x][y] == 1 && !isTouchingVisible(x,y))){
						tile = dark;
					}else{
						tile = tileMap[x][y];
					}
					if(!(tile==null)){
						if(tile!=dark && map[x][y] == 5) g.drawImage(type.pickImage(0),y_ofs*tileSize, x_ofs*tileSize,null);
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

}
