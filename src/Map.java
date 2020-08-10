import java.io.*;
import javax.imageio.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.logging.Logger;

public class Map {
	private static final Logger logger = Logger.getLogger(Map.class.getName());

	public int height = 52; // 52
	public int width = 90; // 90
	public final int MIN_DOORS = 32; // 32

	public int randFillPercent = 46; // 46 [+4 / -3]
	public boolean randSeed = true;
	
	// TODO: compress this all into tileMap
	public int[][] map;
	public int[][] foreground;
	public Tile[][] tileMap;
	public boolean[][] lightMap;
	private int[][] openMap;

	public static int smooths = 4;
	// public static Entity[][] entity_map = new Entity[height][width];

	// TODO (I) Populate w/ creatures.
	public HashSet<Entity> entities;

	public Player player;

	public int tileSize = 24;

	private FOV fov = new FOV();

	private MapType mapType = MapType.UNDERCITY;

	public final boolean undercity = true;

	public boolean debugFlag = false;

	Map(int _height, int _width, int _randFillPercent, int _smooths){
		height = _height;
		width = _width;
		randFillPercent = _randFillPercent;
		smooths = _smooths;

		map = new int[height][width];
		foreground = new int[height][width];
		entities = new HashSet<>();

		// TODO (R) Review
		tileMap = new Tile[height][width];
		openMap = new int[height][width];

		if(undercity){
			generateUndercity();
		}else{
			generateCaves();
		}

		logger.info("placing exits...");
		placeExits();
		logger.info("exits placed");
		printMap();
		logger.info("building tile map...");
		buildTileMap();
		// printPrecons();
		logger.info("Done.");
		if(debugFlag) logger.warning("debug flag raised");
	}
	
	Map(int _height, int _width, int _randFillPercent){
		this(_height,_width,_randFillPercent,smooths);
	}
	
	public void cutOut(Room r){
		for(int cx = r.x+1; cx < r.x+r.tw-1; cx++){
			for(int cy = r.y+1; cy < r.y+r.th-1; cy++){
				map[cy][cx] = 0;
			}
		}
	}

	public void reset(Room r){
		r.doorPoints.clear();
		for(int cx = r.x+1; cx < r.x+r.tw-1; cx++){
			for(int cy = r.y+1; cy < r.y+r.th-1; cy++){
				map[cy][cx] = 1;
				foreground[cy][cx] = 0;
			}
		}
	}

	public void putIntoMap(Room r){
		for(int cy = 0; cy < r.th; cy++){
			for(int cx = 0; cx < r.tw; cx++){
				char cc = r.roomMap[cy][cx];
				int t = -1;
				if(cc=='X'){
					cc='#';
				}else if(cc == '.'){
					cc = ' ';
				}else if(cc == '5'){
					cc = 'D';
				}
				// TODO (R) change to hashmap.
				for(int i = 0; i < getMapType().tile_characters.length; i++){
					char c = getMapType().tile_characters[i];
					if(c == cc){
						t = i;
						break;
					}
				}
				if(t!=-1){
					map[r.y+cy][r.x+cx] = t;
				}else{
					map[r.y+cy][r.x+cx] = 1;
				}
			}
		}
		
		if(getMapType().precons_fg.containsKey(r.precon_id)){
			for(String s: getMapType().precons_fg.get(r.precon_id)){
				String[] str = s.split(",");
				int fg_x = Character.getNumericValue(str[1].charAt(1));
				int fg_y = Character.getNumericValue(str[1].charAt(3));
				Point p;
				if(r.rotationNinety == 1 || r.rotationNinety == 3){
					p = new Point(fg_x, fg_y);
				}else{
					p = new Point(fg_y,fg_x);
				}

				char c = str[0].charAt(0);
				// TODO (R) Change to hashmap
				int t = 0;
				for(int i = 1; i < getMapType().foreground_characters.length; i++){
					if(getMapType().foreground_characters[i] == c){
						t = i;
						break;
					}
				}

				foreground[r.y+p.y][r.x+p.x] = t;
				// TODO: directions
			}
		}
	}

	// methods

	public void printPrecons() {
		for(char[][] r: getMapType().precons.values()){
			for(int cy = 0; cy < r.length; cy++){
				for(int cx = 0; cx < r[0].length; cx++){
					System.out.print(r[cy][cx]);
				}
				System.out.println();
			}
		}
	}

	public int[][] getMap(){
		return map;
	}

	public boolean isOnMap(int x, int y){
		if(x>=width || y>= height || x<0 || y<0) return false;
		return true;
	}

	public boolean isOnMap(Point p) {
		return isOnMap(p.x,p.y);
	}

	public boolean isOpen(int x, int y){
		if(!isOnMap(x,y)) return false;
		if(map[y][x]!=1 && map[y][x]!=6) return true;

		return false;
	}

	public boolean isOpen(Point p){
		return isOpen(p.x,p.y);
	}

	public boolean isFullOpen(int x, int y){
		return isFullOpen(new Point(x,y)); 
	}

	public boolean isFullOpen(Point p) {
		for(Entity e: entities){
			if(e.isPassable() == false && e.getPos().equals(p)) return false;
		}
		if(player.getPos().equals(p)) return false;
		return isOpen(p);
	}

	public boolean isEmpty(Point p) {
		for(Entity e: entities){
			if(e.getPos().equals(p)) return false;
		}
		if(player.getPos().equals(p)) return false;
		return isOpen(p);
	}

	public boolean isEmpty(int x, int y){
		return isEmpty(new Point(x,y));
	}

	public void generateCaves(){
		for(int x=0; x < width; x++){
			for(int y=0; y < height; y++){
				// if border, wall
				if(x==0||x==width-1||y==0||y==height-1){
					map[y][x] = 1;
					// random placement
				} else if(Main.getRng().nextInt(100)<=randFillPercent){
					map[y][x] = 1;
				}else{
					map[y][x] = 0;
				}
			}
		}
		for(int i = 0; i < smooths-1; i++){
			map = smoothMap(4);
		}
		map = smoothMap(4);
	}

	public void generateUndercity(){
		ArrayList<PointDir> workingDoors;
		ArrayList<PointDir> invalidDoors;
		int doorCount = 0;
		do {
			doorCount = 0;
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					map[y][x] = 1;
					foreground[y][x] = 0;
				}
			}
			workingDoors = new ArrayList<PointDir>();
			invalidDoors = new ArrayList<PointDir>();
			Queue<Room> q = new LinkedList<Room>();

			int start_x = Main.getRng().nextInt(width*2/3) + width/6 - 1;
			int start_y = Main.getRng().nextInt(height*2/3) + height/6 - 1;

			q.add(new Room(new PointDir(new Point(start_x,start_y),'R'), true, Room.RoomType.REGULAR, this));

			// starting point
			// Top = 0, Right = 1, Bottom = 2, Left = 3

			//			int quadrant = Main.getRng().nextInt(3);
			//			switch(quadrant){
			//			case 0:
			//				q.add(new PointDir(new Point(Main.getRng().nextInt(width-1), Main.getRng().nextInt(height/3)), dirs[DOWN]));
			//				break;
			//			case 1:
			//				q.add(new PointDir(new Point(width-Main.getRng().nextInt(width/3)-1, Main.getRng().nextInt(height-1)), dirs[LEFT]));
			//				break;
			//			case 2:
			//				q.add(new PointDir(new Point(Main.getRng().nextInt(width-1), height-Main.getRng().nextInt(height/3)-1), 'T'));
			//				break;
			//			case 3:
			//				q.add(new PointDir(new Point(Main.getRng().nextInt(width/3),Main.getRng().nextInt(height-1)), dirs[RIGHT]));
			//				break;
			//			}

			while (!q.isEmpty()) {
				Room R = q.remove();
				logger.fine("building OFF OF..."+R.roomType.name());
				// room vs corridor
				for(int t = 0; t < R.doors; t++){

					Room r;
					if (Main.getRng().nextInt(10)>=4) {
						int attempts = 0;
						Room.RoomType prev = null;
						final int ROOM_ATTEMPTS = 4;
						do {
							PointDir p = R.getRandomDoor();
							if(p==null) break;
							Point ahead = aheadTile(p);
							if(isOpen(ahead) && Main.getRng().nextInt(4)==4){
								workingDoors.add(p);
								break;
							}
							Room.RoomType type = Room.RoomType.RANDOM;
							if(prev!=null && attempts<=2){
								type = prev;
							}
							r = new Room(p, type, this);
							// printMap();
							if (r.valid) {
								q.add(r);
								workingDoors.add(p);
								break;
							}else{
								invalidDoors.add(p);
								prev = r.roomType;
								attempts++;
								if(attempts >= ROOM_ATTEMPTS){
									map[p.point.y][p.point.x] = 1;
								}
							}
						} while (attempts < ROOM_ATTEMPTS);
					}else{
						PointDir p = R.getRandomDoor();
						if(p==null) break;
						ArrayList<PointDir> doors = buildCorridor(p,false);
						// printMap();
						if (doors!=null) {
							workingDoors.add(p);
							for (PointDir i : doors) {
								if(Main.getRng().nextInt(10)>=5){
									r = new Room(i, Room.RoomType.RANDOM, this);
									workingDoors.add(i);
									if(r.valid){
										q.add(r);
									}
								}else{
									// corridor

									doors = buildCorridor(i,true);
									if (doors!=null) {
										workingDoors.add(i);
										for (PointDir i2 : doors) {
											r = new Room(i2, Room.RoomType.RANDOM, this);
											workingDoors.add(i2);
											if(r.valid){
												q.add(r);
											}
										}
									}
								}
							}
						}
					}

					//				if (attempts >= 50) {
					//					map[p.point.y][p.point.x] = 0;
					//				} else if(!start){
					//					workingDoors.add(p);
					//				}
					for (PointDir i : workingDoors) {
						map[i.point.y][i.point.x] = 5;
					}
					// printMap();
					// System.out.println("$$$");
				}
			}

			for(int i = 0; i < workingDoors.size(); i++){
				PointDir p = workingDoors.get(i);
				// if door leads nowhere, fill it in
				if(!isOpen(aheadTile(p))){
					if(isOnMap(p.point)) map[p.point.y][p.point.x] = 1;
					invalidDoors.add(workingDoors.get(i));
					workingDoors.remove(i);
					i--;
				}else if(isSandwich(p.point.x,p.point.y) ){
					doorCount++;
					if(true /*|| Main.getRng().nextInt(7)>=1*/){
						map[p.point.y][p.point.x] = 5;
					}
					//					else{
					//						map[p.point.y][p.point.x] = 7;
					//					}
				}else{
					// if door is not between two walls, destroy it
					if(isOnMap(p.point)) map[p.point.y][p.point.x] = 1;
					invalidDoors.add(workingDoors.get(i));
					workingDoors.remove(i);
					i--;
				}
			}

			for(PointDir d: invalidDoors){
				if(isOpen(aheadTile(d)));
			}
			// System.out.println(doorCount);
		}while(doorCount<MIN_DOORS);

		// doctorMap();
		encaseMap();
		logger.info("Done building Undercity");
		// printMap();
	}

	public ArrayList<PointDir> buildCorridor(PointDir door, boolean fromOtherC){
		logger.fine("building corridor...");
		ArrayList<PointDir> doors = new ArrayList<PointDir>();

		char dir = door.dir;
		int length = 0; 
		PointDir end = door;
		boolean obstructed = false;
		while(!obstructed && length<=5 && (Main.getRng().nextInt(7)<=5 || length <=3)){
			// TODO (F) fix obstruction detection.
			Point temp = aheadTile(end);
			if((!isOnMap(temp) || map[temp.y][temp.x] != 1) || end.point.x <= 1 || end.point.y >= height-2 || end.point.y <= 1 || end.point.x >= width-2){
				if(isOnMap(temp)){
					doors.add(end);
				}
				obstructed = true;
			}
			if(!obstructed){
				if(true /*|| length!=0 || fromOtherC */){
					map[end.point.y][end.point.x] = 0;
				}
				// doors on sides
				if(Main.getRng().nextInt(8)>=6){
					if (dir=='U' || dir=='D') {
						if (Main.getRng().nextBoolean()) {
							// left of path
							if (!isOpen(end.point.x-1, end.point.y) && end.point.x - 1 > 1)  {
								doors.add(new PointDir(new Point(end.point.x-1, end.point.y), 'L'));

								// right of path	
							} else if (!isOpen(end.point.x+1, end.point.y) && end.point.x + 1 < width-1) {
								doors.add(new PointDir(new Point(end.point.x+1, end.point.y), 'R'));
							}
						} else {
							// right of path
							if (!isOpen(end.point.x+1, end.point.y) && end.point.x + 1 < width-1) {
								doors.add(new PointDir(new Point(end.point.x+1, end.point.y), 'R'));

								// left of path
							} else if (!isOpen(end.point.x-1, end.point.y) && end.point.x - 1 > 1) {
								doors.add(new PointDir(new Point(end.point.x-1, end.point.y), 'L'));
							}
						}
					}else if(dir=='L' || dir=='R'){
						if (Main.getRng().nextBoolean()) {
							// up of path
							if (!isOpen(end.point.x, end.point.y-1) && end.point.y - 1 > 1) {
								doors.add(new PointDir(new Point(end.point.x, end.point.y-1), 'U'));

								// down of path	
							} else if (!isOpen(end.point.x, end.point.y+1) && end.point.y + 1 < height-1) {
								doors.add(new PointDir(new Point(end.point.x, end.point.y+1), 'D'));
							}
						} else {
							// down of path
							if (!isOpen(end.point.x, end.point.y+1) && end.point.y + 1 < height-1) {
								doors.add(new PointDir(new Point(end.point.x, end.point.y+1), 'D'));
								// up of path
							}else if (!isOpen(end.point.x, end.point.y-1) && end.point.y - 1 > 1) {
								doors.add(new PointDir(new Point(end.point.x, end.point.y-1), 'U'));
							}
						}
					}
				}
				end.point = temp;
				length++;
			}
		}
		// map[door.point.x][door.point.y] = 5;
		if(Main.getRng().nextInt(10)>=7){
			if(end.dir=='U' || end.dir=='D'){
				if(Main.getRng().nextBoolean()){
					end.dir = 'L';
				}else{
					end.dir = 'R';
				}
			}else{
				if(Main.getRng().nextBoolean()){
					end.dir = 'U';
				}else{
					end.dir = 'D';
				}
			}
			map[end.point.y][end.point.x] = 0;
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
		if(!(x>0 && x<width-1 && y>0 && y<height-1)) return false;
		if((map[y][x-1] == 1 && map[y][x+1] == 1) || (map[y-1][x] == 1 && map[y+1][x] == 1)) return true;
		return false;
	}

	/** Surrounds the map with a 1-tile wall, so that non-wall tiles cannot occupy the outer edges of the map.
	 */
	public void encaseMap() {
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				if(x==0 || y==0 || x==width-1 || y==height-1) map[y][x] = 1;
			}
		}
	}

	public static class PointDir {
		public Point point;
		public char dir;

		public Point getPos(){
			return point;
		}

		PointDir(Point p, char d){
			point = p;
			dir = d;
		}
		
		public static final int UP = 0;
		public static final int RIGHT = 1;
		public static final int DOWN = 2;
		public static final int LEFT = 3;
//		private static final char[] dirChars = new char[] {'T','R','D','L'};
//
//		public static char getDirChar(int i) {
//			return dirChars[i];
//		}
	}

	public Point aheadTile(PointDir p){
		Point point = p.point;
		char dir = p.dir;
		if(dir=='U'){
			return new Point(point.x,point.y-1);
		}else if(dir=='L'){
			return new Point(point.x-1,point.y);
		}else if(dir=='D'){
			return new Point(point.x,point.y+1);
		}else if(dir=='R'){
			return new Point(point.x+1,point.y);
		}
		return null;
	}

	// default 4
	public int[][] smoothMap(int threshold){
		int[][] temp = new int[height][width];
		for(int x=0; x < width; x++){
			for(int y=0; y < height; y++){
				int adj = surroundingWallCount(x,y);
				if(adj>threshold) temp[y][x] = 1;
				if(adj<threshold) temp[y][x] = 0;
				if(adj==threshold) temp[y][x] = map[y][x];
			}
		}
		return temp;
	}

	public int surroundingWallCount(int centerX, int centerY){
		int walls = 0;
		for(int x = centerX-1; x <= centerX+1; x++){
			for(int y = centerY-1; y <= centerY+1; y++){
				if(x>=0&&y>=0 && x<width && y<height){
					if(x!=centerX||y!=centerY){
						if(map[x][y] == 1) walls++;
					}
				}else{
					walls++;
				}
			}
		}
		return walls;
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
		if(!(centerY<=0) && map[centerY][centerX] != map[centerY-1][centerX]) type+=1;
		// right
		if(!(centerX>=width-1) && map[centerY][centerX] != map[centerY][centerX+1]) type+=2;
		// down
		if(!(centerY>=height-1) && map[centerY][centerX] != map[centerY+1][centerX]) type+=4;
		// left
		if(!(centerX<=0) && map[centerY][centerX] != map[centerY][centerX-1]) type+=8;

		return type;
	}

	public int fgTypeAdj(int centerX, int centerY){
		// generates binary numbers (8421=urdl)
		int type = 0;
		int f = foreground[centerY][centerX];

		// up 
		if(!(centerY<=0) && f != foreground[centerY-1][centerX]) type+=1;
		// right
		if(!(centerX>=width-1) && f != foreground[centerY][centerX+1]) type+=2;
		// down
		if(!(centerY>=height-1) && f != foreground[centerY+1][centerX]) type+=4;
		// left
		if(!(centerX<=0) && f!= foreground[centerY][centerX-1]) type+=8;

		// System.out.println(type);
		return type;
	}

	public Point randomEmptySpace(){
		int x;
		int y;
		do{
			// System.out.println("picking point...");
			x = Main.getRng().nextInt(width-1);
			y = Main.getRng().nextInt(height-1);
		}while(!isEmpty(x,y) || map[y][x] == 5);
		return (new Point(x,y));
	}

	public Point randomOpenSpace(){
		int x;
		int y;
		do{
			// System.out.println("picking point...");
			x = Main.getRng().nextInt(width-1);
			y = Main.getRng().nextInt(height-1);
		}while(!isFullOpen(x,y) || map[y][x] == 5);
		return (new Point(x,y));
	}

	public Point getPosition(int uniqueTile){
		for(int x = 0; x < width; x++){
			for(int y = 0; y < height; y++){
				if(map[y][x]==uniqueTile){
					return (new Point(x,y));
				}
			}
		}
		return null;
	}

	public int valueAt(Point p){
		return map[p.y][p.x];
	}


	// TODO (R) Refactor to FW, pick 2 points whose edge is not null, and exceeds a certain distance.
	public void placeExits(){
		buildOpenMap();

		int x1=0;
		int y1=0;
		int x2=0;
		int y2=0;

		int tries = 0;
		int bigtries=0;
		Pathfinder.PointBFS p = null;

		final double criterion = Math.min(Math.max(width, height)/2.25,Math.min(width,height));

		// TODO: change this to FW lookup (V^3 = very fast here)
		do {
			do {
				do {
					x1 = Main.getRng().nextInt(width-1);
					y1 = Main.getRng().nextInt(height-1);
				} while (!isOpen(x1,y1));
				do {
					x2 = Main.getRng().nextInt(width-1);
					y2 = Main.getRng().nextInt(height-1);
				} while (!isOpen(x2,y2));
				tries++;
			} while (Math.abs(x1-x2) + Math.abs(y1-y2) < criterion && tries<=300);
			logger.fine("Placed exits.");
			if(tries<=150){
				p = Pathfinder.pathfindBFS(new Point(x1,y1), new Point(x2,y2), openMap, entities, player, true, true);
				if(p==null || p.getParent() == null){
					logger.warning("Connecting exits failed.");
				}
			}else{
				break;
			}
			bigtries++;
		} while (p==null || p.getParent() == null && bigtries < 10);

		if(tries>300){
			logger.severe("Connecting exits failed completely.");
			if(undercity){
				generateUndercity();
			}else{
				generateCaves();
			}
			placeExits();
			return;
		}else{
			while(p.getParent() != null){
				// map[p.x][p.y] = 4;
				p = p.getParent();
			}

			map[y1][x1] = 2;
			map[y2][x2] = 3;
		}
	}

	public int[][] dijkstra(Pathfinder pf, Point src){
		int[][] dijk = new int[this.height][this.width];

		Queue<Point> q = new LinkedList<Point>();
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				dijk[y][x] = Integer.MAX_VALUE;
				q.add(new Point(x,y));
			}
		}

		dijk[src.y][src.x] = 0;

		while(/*!*/q.isEmpty()) {
			// TODO (A) Implement
		}

		return dijk;
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
		if(map[y][x] != 1 && map[y][x] != 5) return true;
		return false;
	}

	public void doctorMap(){
		// this is where all janky map fixes go
		// cleans up errors in map

		for(int x = 0; x < width; x++){
			for(int y = 0; y < height; y++){
				// fixes bad water tiles
				if(map[y][x] == 6){
					if(tileTypeAdj(x,y) == 11 || tileTypeAdj(x,y) == 14){
						if(map[y+1][x] == 0){
							map[y+1][x] = 6;
						}else{
							map[y-1][x] = 6;
						}
					}else if(tileTypeAdj(x,y) == 10){
						//						map[y][x] = 1;
						//						foreground[y][x] = 0;
					}
				}else if(foreground[y][x] == 1){
					foreground[y][x] = 0;
				}
			}
		}
	}


	/**END OF GENERATION**/
	// 0 = open
	// 1 = closed
	// 2 = amph/fly open
	// 3 = fly open
	public void buildOpenMap(){
		for(int x = 0; x < width; x++){
			for(int y = 0; y < height; y++){
				if(map[y][x] == 1){
					openMap[y][x] = 1;
				}else if(map[y][x] == 6){
					if(foreground[y][x] == 1){
						openMap[y][x] = 0;
					}else{
						openMap[y][x] = 2;
					}
				}else{
					openMap[y][x] = 0;
				}
				//System.out.print(openMap[y][x]+" ");
			}
			//System.out.println();
		}
	}

	public int[][] getOpenMap(){
		buildOpenMap(); // TODO: bug, why does this need to be called here??
		return openMap;
	}

	public void buildTileMap(){
		for(int x = 0; x < width; x++){
			for(int y = 0; y < height; y++){
				tileMap[y][x] = new Tile(map[y][x],getMapType(),tileTypeAdj(x,y));
				int f = foreground[y][x];
				// if(f==1 && tileMap[y][x].value != 6) f = 0;
				if(f!=0) tileMap[y][x].setForeground(f,fgTypeAdj(x,y));
			}
		}
	}

	// returns unique name
	public void addEntity(Entity entity){
		entities.add(entity);
	}

	// prints map to console
	public void printMap(){
		for(int y=0; y < height; y++){
			for(int x=0; x < width; x++){
				if(foreground[y][x] != 0){
					System.out.print(getMapType().foreground_characters[foreground[y][x]]+" ");
				}else{
					System.out.print(getMapType().tile_characters[map[y][x]]+" ");
				}
			}
			System.out.println();
		}
	}

	public BufferedImage renderMap() {
		return renderArea(0,0,width-1,height-1,true); 
	}

	public boolean[][] buildOpacityMap(){
		boolean[][] temp = new boolean[height][width];
		for(int x=0; x < width; x++){
			for(int y=0; y < height; y++){
				temp[y][x] = isTransparent(x,y);
			}
		}
		return temp;
	}

	//	public boolean[][] skew(boolean[][] map){
	//		boolean[][] temp = new boolean[map[0].length][map.length];
	//		for(int x=0; x < map[0].length; x++){
	//			for(int y=0; y < map.length; y++){
	//				temp[y][x] = map[y][y];
	//			}
	//		}
	//		return temp;
	//	}

	public void updateFOV(){
		lightMap = fov.calculate(buildOpacityMap(), player.getX(), player.getY(), Main.getPlayer().luminosity);

		for(Entity e: entities){
			if(e instanceof Creature) {
				Creature c = (Creature) e;
				if(c.isInPlayerView()){
					c.setInPlayerView(lightMap[c.getY()][c.getX()]);
					if(!c.isInPlayerView()) Main.getView().appendText("The "+c.getName()+" is no longer in view.");
				}else{
					c.setInPlayerView(lightMap[c.getY()][c.getX()]);
					if(c.isInPlayerView()) Main.getView().appendText("A "+c.getName()+" moves into view.");
				}
			}
		}
		//		updateViewed();
	}

	//	public void updateViewed(){
	//		for(int x = 0; x < width; x++){
	//			for(int y = 0; y < height; y++){
	//				if(!visitedTiles[y][x] && lightMap[y][x]) visitedTiles[y][x] = true; 
	//			}
	//		}
	//	}

	public ArrayList<StaticEntity> getInteractablesInRange(Point p) {
		// TODO: interact-able checks handled by "Interactable" interface
		// TODO: switch to Set<Interactable>
		ArrayList<StaticEntity> interactablesInRange = new ArrayList<>(); 
		for(Entity n: entities) {
			if(n instanceof StaticEntity) {
				StaticEntity se = (StaticEntity) n;
				if(se.isAdjacentTo(p)){
					interactablesInRange.add(se);
				}
			}
		}
		return interactablesInRange;
	}

	// TODO (R) Render Layers (seperately, instead of tile-based)
	public BufferedImage renderArea(int x1, int y1, int x2, int y2, boolean noLighting){
		int areaHeight = Math.abs(y2-y1)+1;
		int areaWidth = Math.abs(x2-x1)+1;

		BufferedImage area = new BufferedImage(areaWidth*tileSize,areaHeight*tileSize,BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) area.getGraphics();
		BufferedImage dark;

		updateFOV();

		// BufferedImage SouthWall;
		try {
			dark = ImageIO.read(new File(getMapType().PATH+"dark.png"));
			// SouthWall = ImageIO.read(new File(type.PATH+"Floor.png"));

			// draw
			for(int x=Math.min(x1,x2); x <= Math.max(x1,x2); x++){ // column #
				for(int y=Math.min(y1,y2); y <= Math.max(y1,y2); y++){ // row #
					BufferedImage tile;
					int x_ofs = x - Math.min(x1,x2);
					int y_ofs = y - Math.min(y1,y2);
					if(x>= width || y>= height || x<0 || y<0 || (map[y][x] == 1 && !isTouchingVisible(x,y))){
						tile = dark;
					}else{
						tile = tileMap[y][x].image;
					}

					if(tile==null) tile = dark;

					if(tile!=dark && !noLighting && !lightMap[y][x]){
						if (tileMap[y][x].visited) {
							BufferedImage image = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_4BYTE_ABGR);
							Graphics tileG = image.getGraphics();
							tileG.drawImage(tileMap[y][x].asLastSeen, 0, 0, null);
							Entity LEH = tileMap[y][x].lastEntityHere;
							if(LEH!=null && isOnMap(LEH.getPos()) && !lightMap[LEH.getY()][LEH.getX()]){
								// if last creature here has died, don't show
								if(!(LEH instanceof Creature) || ((Creature) LEH).getHP() > 0) {
									tileG.drawImage(LEH.getSprite(), 0, 0, null);
								}
							}

							final float percentBrightness = .60f;

							tileG.setColor(new Color(0,0,0, (int) (255*(1-percentBrightness)) ));
							tileG.fillRect(0, 0, image.getWidth(), image.getHeight());
							tileG.dispose();
							tile = image;
						}else{
							tile = dark;
						}
					}
					g.drawImage(tile,x_ofs*tileSize, y_ofs*tileSize,null);

					BufferedImage fullImg = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
					Graphics fg = fullImg.getGraphics();
					fg.drawImage(tile, 0, 0, null);

					Entity lastEntity = null;
					if (isOnMap(x,y) && (noLighting || lightMap[y][x])) {

						// draw foreground
						if(tileMap[y][x].getFgImage()!=null){
							fg.drawImage(tileMap[y][x].getFgImage(),0,0,null);
						}

						// draw items
						Inventory inv = tileMap[y][x].inventory;
						if(!inv.isEmpty()){
							fg.drawImage(inv.drawPile(),0,0,null);
						}
					}


					// get Entity image

					if (isOnMap(x,y) && (noLighting || lightMap[y][x])) {
						for (Entity e : entities) {
							if (e.getX() == x && e.getY() == y) {
								lastEntity = e;
								break;
							}
						}
					}

					// draw tile. items. fg.
					fg.dispose();
					g.drawImage(fullImg, x_ofs * tileSize, y_ofs * tileSize, null);

					// update tile memory
					if(!noLighting && isOnMap(x,y) && lightMap[y][x]) tileMap[y][x].view(fullImg,lastEntity);

					// finally, draw entities
					if(lastEntity!=null) g.drawImage(lastEntity.getSprite(), x_ofs*tileSize, y_ofs*tileSize, null);

					// and draw player
					if(player != null && player.getX() == x && player.getY() == y){
						g.drawImage(player.getSprite(),x_ofs*tileSize,y_ofs*tileSize,null);
						// fg.drawImage(player.getImg(),0,0,null);
					}
				}
			}
			g.dispose();
		} catch (Exception e) {
			e.printStackTrace();
		};
		return area;
	}

	public void addItemToSpace(Item i, Point p) {
		tileMap[p.y][p.x].inventory.addItem(i);
	}

	public BufferedImage renderRadius(int x, int y, int r){
		BufferedImage area = renderArea(x-r,y-r,x+r,y+r,false);
		// circle shading
		return area;
	}

	@Deprecated
	public BufferedImage addVignette(BufferedImage image, int x, int y, int r){
		int d = r*2*tileSize;
		Graphics g = (Graphics2D) image.getGraphics();
		g.setColor(new Color(225,185,85,45));
		g.fillOval(x,y,d,d);
		return image;
	}

	@Deprecated
	public BufferedImage addCenterVignette(BufferedImage image, int r){
		int pixr = r*tileSize;
		return addVignette(image,image.getWidth()/2 - pixr,image.getHeight()/2 - pixr,r);
	}

	public BufferedImage render_vig(int x, int y, int r, int vr){
		BufferedImage image = renderRadius(x,y,r);
		// image = addCenterVignette(image,vr);
		return image;
	}

	public MapType getMapType() {
		return mapType;
	}

	//	public int[] getEntityPriority(){
	//		int[] order = new int[entities.size()];
	//		for (int x = 0; x < order.length; x++) {
	//			int min_distance = Integer.MAX_VALUE;
	//			int min_pos = -1;
	//			for (int i = 0; i < entities.size(); i++) {
	//				entities.get(i).ai.lib.updatePath();
	//				int distance = entities.get(i).ai.lib.distance;
	//				if(distance < min_distance){
	//					min_distance = distance;
	//					min_pos = i;
	//				}
	//			}
	//			order[x] = min_pos;
	//		}
	//		return order;
	//	}
}