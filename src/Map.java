import java.io.*;
import javax.imageio.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

public class Map {
	public static int height = 52; // 52
	public static int width = 90; // 90
	public static int MIN_DOORS = 32; // 32

	public static int randFillPercent = 46; // 46 [+4 / -3]
	public boolean randSeed = true;

	public int[][] map;
	public Tile[][] tileMap;
	public int[][] foreground;

	public static int smooths = 4;
	public int entityNumber = 0;
	// public static Entity[][] entity_map = new Entity[height][width];
	public HashMap<String,Entity> entities = new HashMap<String,Entity>();

	public Entity player;

	public int tileSize = 24;

	public  static Random rng = new Random();
	private Pathfinder pf = new Pathfinder();

	private MapTypes type = MapTypes.UNDERCITY;

	public static final boolean undercity = true;

	public boolean debugFlag = false;

	public final int UP = 0;
	public final int RIGHT = 1;
	public final int DOWN = 2;
	public final int LEFT = 3;

	public final char[] dirs = {'T','R','D','L'};

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

		// kill
		tileMap = new Tile[height][width];

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

		for(char[][] r: type.precons.values()){
			for(int cy = 0; cy < r.length; cy++){
				for(int cx = 0; cx < r[0].length; cx++){
					System.out.print(r[cy][cx]);
				}
				System.out.println();
			}
		}
		System.out.println("done.");
		if(debugFlag) System.out.println("debug flag raised");
	}

	// methods

	public int[][] getMap(){
		return map;
	}

	public boolean isOnMap(int x, int y){
		if(x>=width || y>= height || x<0 || y<0) return false;
		return true;
	}

	public boolean isOpen(int x, int y){
		if(!isOnMap(x,y)) return false;
		if(map[y][x]!=1 && map[y][x]!=6) return true;
		
		return false;
	}
	
	// overload
	public boolean isOpen(Point p){
		return isOpen(p.x,p.y);
	}

	public boolean isFullOpen(int x, int y){
		for(Entity e: entities.values()){
			if(e.isPassable == false && e.x==x && e.y==y) return false;
		}
		if(player.x == x && player.y == y) return false;
		return isOpen(x,y);
	}

	public boolean isEmpty(int x, int y){
		for(Entity e: entities.values()){
			if(e.x==x && e.y==y) return false;
		}
		if(player.x == x && player.y == y) return false;
		return isOpen(x,y);
	}

	public void generateCaves(){
		for(int x=0; x < width; x++){
			for(int y=0; y < height; y++){
				// if border, wall
				if(x==0||x==width-1||y==0||y==height-1){
					map[y][x] = 1;
					// random placement
				} else if(rng.nextInt(100)<=randFillPercent){
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

			int start_x = rng.nextInt(width*2/3) + width/6 - 1;
			int start_y = rng.nextInt(height*2/3) + height/6 - 1;

			q.add(new Room(new PointDir(new Point(start_x,start_y),dirs[RIGHT]),true,RoomType.REGULAR));

			// starting point
			// Top = 0, Right = 1, Bottom = 2, Left = 3

			//			int quadrant = rng.nextInt(3);
			//			switch(quadrant){
			//			case 0:
			//				q.add(new PointDir(new Point(rng.nextInt(width-1), rng.nextInt(height/3)), dirs[DOWN]));
			//				break;
			//			case 1:
			//				q.add(new PointDir(new Point(width-rng.nextInt(width/3)-1, rng.nextInt(height-1)), dirs[LEFT]));
			//				break;
			//			case 2:
			//				q.add(new PointDir(new Point(rng.nextInt(width-1), height-rng.nextInt(height/3)-1), 'T'));
			//				break;
			//			case 3:
			//				q.add(new PointDir(new Point(rng.nextInt(width/3),rng.nextInt(height-1)), dirs[RIGHT]));
			//				break;
			//			}

			while (!q.isEmpty()) {
				Room R = q.remove();
				System.out.println("building OFF OF..."+R.roomType.name());
				// room vs corridor
				for(int t = 0; t < R.doors; t++){

					Room r;
					if (rng.nextInt(10)>=4) {
						int attempts = 0;
						RoomType prev = null;
						final int ROOM_ATTEMPTS = 4;
						do {
							PointDir p = R.getRandomDoor();
							if(p==null) break;
							Point ahead = aheadTile(p);
							if(isOpen(ahead) && rng.nextInt(4)==4){
								workingDoors.add(p);
								break;
							}
							RoomType type = RoomType.RANDOM;
							if(prev!=null && attempts<=2){
								type = prev;
							}
							r = new Room(p, false, type);
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
								if(rng.nextInt(10)>=5){
									r = new Room(i, false, RoomType.RANDOM);
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
											r = new Room(i2, false, RoomType.RANDOM);
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
					if(isOnMap(p.point.x,p.point.y)) map[p.point.y][p.point.x] = 1;
					invalidDoors.add(workingDoors.get(i));
					workingDoors.remove(i);
					i--;
				}else if(isSandwich(p.point.x,p.point.y) ){
					doorCount++;
					if(true || rng.nextInt(7)>=1){
						map[p.point.y][p.point.x] = 5;
					}else{
						map[p.point.y][p.point.x] = 7;
					}
				}else{
					// if door is not between two walls, destroy it
					if(isOnMap(p.point.x,p.point.y)) map[p.point.y][p.point.x] = 1;
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
		printMap();
		System.out.println("F:");
	}

	public ArrayList<PointDir> buildCorridor(PointDir door, boolean fromOtherC){
		System.out.println("building corridor...");
		ArrayList<PointDir> doors = new ArrayList<PointDir>();

		char dir = door.dir;
		int length = 0; 
		PointDir end = door;
		boolean obstructed = false;
		while(!obstructed && length<=5 && (rng.nextInt(7)<=5 || length <=3)){
			// TODO: fix obstruction detection.
			Point temp = aheadTile(end);
			if((!isOnMap(temp.x,temp.y) || map[temp.y][temp.x] != 1) || end.point.x <= 1 || end.point.y >= height-2 || end.point.y <= 1 || end.point.x >= width-2){
				if(isOnMap(temp.x,temp.y)){
					doors.add(end);
				}
				obstructed = true;
			}
			if(!obstructed){
				if(true || length!=0 || fromOtherC){
					map[end.point.y][end.point.x] = 0;
				}
				// doors on sides
				if(rng.nextInt(8)>=6){
					if (dir==dirs[UP] || dir==dirs[DOWN]) {
						if (rng.nextBoolean()) {
							// left of path
							if (!isOpen(end.point.x-1, end.point.y) && end.point.x - 1 > 1)  {
								doors.add(new PointDir(new Point(end.point.x-1, end.point.y), dirs[LEFT]));

								// right of path	
							} else if (!isOpen(end.point.x+1, end.point.y) && end.point.x + 1 < width-1) {
								doors.add(new PointDir(new Point(end.point.x+1, end.point.y), dirs[RIGHT]));
							}
						} else {
							// right of path
							if (!isOpen(end.point.x+1, end.point.y) && end.point.x + 1 < width-1) {
								doors.add(new PointDir(new Point(end.point.x+1, end.point.y), dirs[RIGHT]));

								// left of path
							} else if (!isOpen(end.point.x-1, end.point.y) && end.point.x - 1 > 1) {
								doors.add(new PointDir(new Point(end.point.x-1, end.point.y), dirs[LEFT]));
							}
						}
					}else if(dir==dirs[LEFT] || dir==dirs[RIGHT]){
						if (rng.nextBoolean()) {
							// up of path
							if (!isOpen(end.point.x, end.point.y-1) && end.point.y - 1 > 1) {
								doors.add(new PointDir(new Point(end.point.x, end.point.y-1), dirs[UP]));

								// down of path	
							} else if (!isOpen(end.point.x, end.point.y+1) && end.point.y + 1 < height-1) {
								doors.add(new PointDir(new Point(end.point.x, end.point.y+1), dirs[DOWN]));
							}
						} else {
							// down of path
							if (!isOpen(end.point.x, end.point.y+1) && end.point.y + 1 < height-1) {
								doors.add(new PointDir(new Point(end.point.x, end.point.y+1), dirs[DOWN]));
								// up of path
							}else if (!isOpen(end.point.x, end.point.y-1) && end.point.y - 1 > 1) {
								doors.add(new PointDir(new Point(end.point.x, end.point.y-1), dirs[UP]));
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
			if(end.dir==dirs[UP] || end.dir==dirs[DOWN]){
				if(rng.nextBoolean()){
					end.dir = dirs[LEFT];
				}else{
					end.dir = dirs[RIGHT];
				}
			}else{
				if(rng.nextBoolean()){
					end.dir = dirs[UP];
				}else{
					end.dir = dirs[DOWN];
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
		if(dir==dirs[UP]){
			return new Point(point.x,point.y-1);
		}else if(dir==dirs[LEFT]){
			return new Point(point.x-1,point.y);
		}else if(dir==dirs[DOWN]){
			return new Point(point.x,point.y+1);
		}else if(dir==dirs[RIGHT]){
			return new Point(point.x+1,point.y);
		}
		return null;
	}

	public enum RoomType{
		RANDOM,
		REGULAR,
		SEWER,
		PRECON;
	}

	public class Room {

		public int x;
		public int y;

		public int w;
		public int h;
		public int tw;
		public int th;

		public int offset;
		public int rotationNinety = 0; // int rN * 90 = degrees to rotate
		
		public PointDir door;
		public int doors;
		public ArrayList<PointDir> doorPoints = new ArrayList<PointDir>();
		//		public PointDir[] localDoorPoints = new PointDir[4];
		boolean[] opens = new boolean[4];
		boolean works = true;
		boolean valid = true;
		boolean start = false; 
		char dir;
		RoomType roomType;

		boolean preconPicked = false;
		int precon_id;


		Room(PointDir _door, boolean _start, RoomType _type){
			boolean works = true;
			int count = 0;
			start = _start;
			door = _door;
			if(!start) dir = door.dir;
			roomType = _type;

			if(start){
				x = door.point.x;
				y = door.point.y;
				roomType = RoomType.REGULAR;
				setBounds();
				buildNormal();
				return;
			}

			if(roomType.equals(RoomType.RANDOM)){
				// TODO: Tabulate %s
				System.out.println("picking type.");
				int a = rng.nextInt(10);
				if(a>=7){
					roomType = RoomType.SEWER;
				}else if(a>=4){
					roomType = RoomType.PRECON;
				}else{
					roomType = RoomType.REGULAR;
				}
			}

			final int ATTEMPTS = 40;
			do{
				works = true;
				setBounds();

				// TODO: rework system so that room is generated first, then offsets are found, and doors are placed etc
				if(roomType.equals(RoomType.PRECON)){
					boolean offsetWorks = false;
					int OFFSET_ATTEMPTS = 20;
					int offsetCount = 0;
					do{
						if(dir==dirs[UP] || dir==dirs[DOWN]){
							offset = rng.nextInt(w-1)+1; // from 1 -> w
						}else{
							offset = rng.nextInt(h-1)+1;
						}

						Point p = precon_getDoorstep();
						Point dp = precon_getDoorPos();
						char[][] r = type.precons.get(precon_id);
						for(int i = 0; i < rotationNinety; i++){
							r = rotateNinety(r);
						}
//						if(rotationNinety == 1 || rotationNinety == 3){
//							p = new Point(p.y,p.x);
//							dp = new Point(dp.y,dp.x);
//						}
						if((precon_containsDoors() 
								&& (r[dp.y][dp.x] == 'D' || r[dp.y][dp.x] == 'd')) 
								|| (!precon_containsDoors() && r[p.y][p.x] == '.')){
							offsetWorks = true;
						}
						offsetCount++;
					}while(!offsetWorks && offsetCount < OFFSET_ATTEMPTS);
					if(!offsetWorks){
						works = false;
						break;
					}
				}

				if(dir==dirs[UP]){
					x = door.point.x-offset;
					y = door.point.y-th+1;
				}else if(dir==dirs[DOWN]){
					x = door.point.x-offset;
					y = door.point.y;
				}else if(dir==dirs[LEFT]){
					y = door.point.y-offset;
					x = door.point.x-tw+1;
				}else if(dir==dirs[RIGHT]){
					y = door.point.y-offset;
					x = door.point.x;
				}
				works = surroundCheck();
				count++;
			} while(!works && count < ATTEMPTS);


			if (works) {
				buildNormal();
			}else{
				valid = false;
			}
		}

		public PointDir getRandomDoor(){
			if(!doorPoints.isEmpty()){
				System.out.println(doorPoints.size());
				for(PointDir d: doorPoints){
					System.out.println(d.point);
				}
				int rand;
				if(doorPoints.size()>1){
					rand = rng.nextInt(doorPoints.size()-1);
				}else{
					rand = 0;
				}
				PointDir p = doorPoints.get(rand);
				doorPoints.remove(rand);
				return p;
			}else{
				System.out.println("picking random door");
				PointDir p = randomDoor();
				if(p==null){
					System.out.println("no more doors possible");
				}
				return p;
			}
		}

		public void setBounds(){
			if (roomType.equals(RoomType.REGULAR)) {
				int min = rng.nextInt(1)+3;
				w = rng.nextInt(5) + min;
				h = rng.nextInt(5) + min;
			}else if (roomType.equals(RoomType.SEWER)){
				final int l_min = 8;
				final int l_var = 6;

				if(dir==dirs[UP] || dir==dirs[DOWN]){
					// wide
					w = rng.nextInt(l_var)+l_min;
					h = rng.nextInt(2)+4;
				}else{
					// tall
					h = rng.nextInt(l_var)+l_min;
					w = rng.nextInt(2)+4;
				}
			}else if(roomType.equals(RoomType.PRECON)){
				if(!preconPicked){
					precon_id = rng.nextInt(type.precons.size());
					rotationNinety = rng.nextInt(3);
					
					h = type.precons.get(precon_id).length - 2;
					w = type.precons.get(precon_id)[0].length - 2;
					if(rotationNinety == 1 || rotationNinety == 3){
						int temp = h;
						h = w;
						w = temp;
					}
					preconPicked = true;

				}
			}

			tw = w+2;
			th = h+2;
			if(!roomType.equals(RoomType.PRECON)){
				if(dir==dirs[UP] || dir==dirs[DOWN]){
					// System.out.println(w);
					offset = rng.nextInt(w-1)+1; // from 1 -> w
				}else{
					// System.out.println(h);
					offset = rng.nextInt(h-1)+1;
				}
			}
		}

		public boolean surroundCheck(){
			for(int _x = x; _x <= x+tw-1; _x++){
				for(int _y = y; _y <= y+th-1; _y++){
					if(_x == door.point.x && _y == door.point.y) continue;
					if(!isOnMap(_x,_y)){
						return false;
					}else if(isOpen(_x,_y)){
						return false;
					}
				}
			}
			return true;
		}

		public PointDir randomDoor(){
			int tries = 0;
			while(tries < 15){
				int d = rng.nextInt(3);
				PointDir p = null;
				if(d==0 && (start || door.dir!=dirs[UP])){
					p = new PointDir(new Point(rng.nextInt(w-1)+1 + x, y),dirs[UP]);
				}else if(d==1 && (start || door.dir!=dirs[RIGHT])){
					p = new PointDir(new Point(x + tw-1, rng.nextInt(h-1)+1 + y),dirs[RIGHT]);					
				}else if(d==2 && (start || door.dir!=dirs[DOWN])){
					p = new PointDir(new Point(rng.nextInt(w-1)+1 + x, y + th-1),dirs[DOWN]);					
				}else if(start || door.dir!=dirs[LEFT]){
					p = new PointDir(new Point(x, rng.nextInt(h-1)+1 + y),dirs[LEFT]);
				}
				if(p == null || (room != null && room[p.point.y-y][p.point.x-x] == 'X')){
					tries++;
					continue;
				}
				for(int cx = p.point.x-1; cx <= p.point.x+1; cx++){
					for(int cy = p.point.y-1; cy <= p.point.y+1; cy++){
						if(cx<x || cy<y || cx>=x+tw || cy>=y+th || (cx!=p.point.x && cy!=p.point.y)) continue;
						if(isOpen(cx,cy)){
							return p;
						}
					}
				}

				tries++;					
			}
			return null;
		}


		public boolean precon_containsDoors(){
			char[][] r = type.precons.get(precon_id);
			
			for(int cy = 0; cy < r.length; cy++){
				for(int cx = 0; cx < r[0].length; cx++){
					if(r[cy][cx] == 'D' || r[cy][cx] == 'd'){
						return true;
					}
				}
			}
			return false;
		}

		public Point precon_getDoorstep(){
			if(door.dir == dirs[UP]){
				return new Point(offset,th-2);
			}else if(door.dir == dirs[DOWN]){
				return new Point(offset,1);
			}else if(door.dir == dirs[LEFT]){
				return new Point(tw-2,offset);
			}else if (door.dir == dirs[RIGHT]){
				return new Point(1,offset);
			}
			return null;
		}

		public Point precon_getDoorPos(){
			if(door.dir == dirs[UP]){
				return new Point(offset,th-1);
			}else if(door.dir == dirs[DOWN]){
				return new Point(offset,0);
			}else if(door.dir == dirs[LEFT]){
				return new Point(tw-1,offset);
			}else if (door.dir == dirs[RIGHT]){
				return new Point(0,offset);
			}
			return null;
		}

		public void buildNormal(){
			if(roomType.equals(RoomType.REGULAR)){
				System.out.println("building regular...");
				pickDoors();
				//				placeDoors();
				cutOut();
			}else if(roomType.equals(RoomType.SEWER)){
				System.out.println("building sewer...");
				pickDoors();

				//				if(dir==dirs[UP]){
				//					opens[UP] = true;
				//				}else if(dir==dirs[DOWN]){
				//					opens[DOWN] = true;
				//				}else if(dir==dirs[LEFT]){
				//					opens[LEFT] = true;
				//				}else if(dir==dirs[RIGHT]){
				//					opens[RIGHT] = true;
				//				}
				//				placeDoors();
				cutOut();
				buildSewer();
			}else if(roomType.equals(RoomType.PRECON)){
				System.out.println("building precon #"+precon_id);
				pickDoors();
				buildPrecon();
			}
		}

		//		public void customize(){
		//			cutOut();
		//			if(RoomType.SEWER.equals(this.type)){
		//				buildSewer();
		//			}
		//		}

		char[][] room;
		public void buildPrecon(){

			// TODO: allow for rotation.
			// TODO: add chests, keys, item spawns / monster spawns
			// TODO: add extra info for rooms; rarity, ool gen level etc
			int ow = type.precons.get(precon_id)[0].length;
			int oh = type.precons.get(precon_id).length;
			room = new char[oh][ow];
			for(int cy = 0; cy < oh; cy++){
				for(int cx = 0; cx < ow; cx++){
					char c = type.precons.get(precon_id)[cy][cx];
					room[cy][cx] = c;
				}
			}
			
			for(int i = 0; i < rotationNinety; i++){
				room = rotateNinety(room);
			}

			ArrayList<PointDir> possibleDoors = new ArrayList<PointDir>();
			boolean hitD = false;
			int temp = 0;
			for(int cy = 0; cy < th; cy++){
				for(int cx = 0; cx < tw; cx++){;
				Point p = new Point(cx,cy);
				// TODO: add non-edge doors
				if(room[cy][cx]== 'D'){
					if(!hitD){
						hitD = true;
						doors = 0;
					}
					if(cy != door.point.y-y && cx != door.point.x-x){
						doors++;
						doorPoints.add(new PointDir(new Point(p.x+x,p.y+y),getDoorDir(p)));							
					}
				}else if(room[cy][cx] =='d' && (cy != door.point.y-y && cx != door.point.x-x)){
					if(hitD){
						temp = doors;
						pickDoors();
						doors+=temp;
					}
					possibleDoors.add(new PointDir(new Point(p.x+x,p.y+y),getDoorDir(p)));
				}
				}
			}

			int doorCount = doors-temp;
			while(doorCount > 0 && possibleDoors.size()>1){
				int r = rng.nextInt(possibleDoors.size()-1);

				doorPoints.add(possibleDoors.get(r));
				possibleDoors.remove(r);
				doorCount--;
			}

			if(doorCount > 0 && possibleDoors.size()==1){
				doorPoints.add(possibleDoors.get(0));
				possibleDoors.remove(0);
			}

			for(PointDir d: possibleDoors){
				room[d.point.y-y][d.point.x-x] = '#';
			}

			//			if(doorPoints.isEmpty()){
			//				for(int i = doors; i > 0; i--){
			//					doorPoints.add(randomDoor());
			//				}
			//			}

			// TODO: place doors, chests, keys etc
			for(int cy = 0; cy < th; cy++){
				for(int cx = 0; cx < tw; cx++){
					char cc = room[cy][cx];
					int t = -1;
					if(cc=='X'){
						cc='#';
					}else if(cc == '.'){
						cc = ' ';
					}else if(cc == '5'){
						cc = 'D';
					}
					for(int i = 0; i < type.tile_characters.length; i++){
						char c = type.tile_characters[i];
						if(c == cc){
							t = i;
							break;
						}
					}
					if(t!=-1){
						map[y+cy][x+cx] = t;
					}else{
						map[y+cy][x+cx] = 1;
					}
				}
			}
			if(type.precons_fg.containsKey(precon_id)){
				for(String s: type.precons_fg.get(precon_id)){
					String[] str = s.split(",");
					int fg_x = Character.getNumericValue(str[1].charAt(1));
					int fg_y = Character.getNumericValue(str[1].charAt(3));
					Point p;
					if(rotationNinety == 1 || rotationNinety == 3){
						p = new Point(fg_x, fg_y);
					}else{
						p = new Point(fg_y,fg_x);
					}
					char c = str[0].charAt(0);
					int t = 0;
					for(int i = 1; i < type.foreground_characters.length; i++){
						if(type.foreground_characters[i] == c){
							t = i;
						}
					}

					foreground[y+p.y][x+p.x] = t;
					// TODO: directions
				}
			}
		}

		public char getDoorDir(Point p){
			if(p.x == 0){
				return dirs[LEFT];
			}else if(p.y == 0){
				return dirs[UP];
			}else if(p.x == tw-1){
				return dirs[RIGHT];
			}else if(p.y == th-1){
				return dirs[DOWN];
			}
			return '!';
		}

		@SuppressWarnings("unused")
		public void buildSewer(){
			if(h<=3 || w<=3) return;
			int waterWidth = 2;
			boolean noBridge = (doors==0 && rng.nextInt(10)<=7);
			if((door.dir == dirs[UP] || door.dir == dirs[DOWN]) && w>4){
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
					if(false){
						waterWidth = rng.nextInt(h-2-river_h+1-2)+2;
					}else{
						waterWidth = h-2;
					}
				}

				// width || x x x || = 7 = 3 possible = [0,1,2] + 2) -> 7-5 = [2,3,4]
				int bridge_x = rng.nextInt(w-3)+2;
				for(int i = 1; i <= w; i++){
					for(int t = 1; t <= waterWidth; t++){
						if (noBridge || i!=bridge_x) {
							map[y + river_h + t][x + i] = 6;
						}else{
							map[y + river_h + t][x + i] = 6;
							foreground[y + river_h + t][x + i] = 1;
						}
					}
				}
			}else if((door.dir == dirs[LEFT] || door.dir == dirs[RIGHT]) && h>4){
				// vertical
				int river_w;
				if(w<=4){
					river_w = 1;
				}else{
					river_w = rng.nextInt(w-4)+1;
				}

				if(w-2-river_w+1 == 2){
					waterWidth = 2;
				}else{
					if(false){
						waterWidth = rng.nextInt(w-2-river_w+1-2)+2;
					}else{
						waterWidth = w-2;
					}
				}

				int bridge_y = rng.nextInt(h-3)+2;
				for(int i = 1; i <= h; i++){
					for(int t = 1; t <= waterWidth; t++){
						if (noBridge || i!=bridge_y) {
							map[y + i][x + river_w + t] = 6;
						}else{
							map[y + i][x + river_w + t] = 6;
							foreground[y + i][x + river_w + t] = 1;
						}
					}
				}
			}
		}

		public boolean compareMaps(char[][] map, char[][] t_map){
			for(int x=0; x<map.length; x++){
				for(int y=0; y<map[x].length; y++){
					if(map[x][y] != t_map[x][y]){
						return false;
					}
				}
			}
			return true;
		}

		public char[][] rotateNinety(char[][] map){

			int width = map[0].length;
			int height = map.length;
			char[][] newmap = new char[width][height];
			for(int y=0; y<width; y++){
				for(int x=0; x<height; x++){
					newmap[y][x] = map[x][width-1-y];					
				}
			}
			return newmap;
		}

		public void pickDoors(){
			// x=y && y=x
			// (T) top = x,y -> x,y+w
			// (L) left = x,y -> x+h,y
			// (R) right = x,y+w -> x+h,y+w
			// (D) bottom = x+h,y -> x+h,y+w
			int[][] chart = {{ 0,10 },{ 1, 25 }, { 2, 40 }, { 3, 25 }};
			RandomChart rnd = new RandomChart(chart);
			doors = rnd.pick();
			if(start) doors = 3;
		}

		public PointDir[] getDoors(){
			PointDir[] l = new PointDir[doors];
			if(doorPoints!=null && !doorPoints.isEmpty()){
				for(int i = 0; i < doors; i++){
					l[i] = doorPoints.get(i);
				}
			}
			for(int i = 0; i < doors; i++){
				PointDir d = randomDoor();
				if(d!=null) l[i] = d;
			}
			return l;
		}

		public Queue<PointDir> addDoorsToQueue(Queue<PointDir> q){
			for(PointDir d: q){
				q.add(d);
			}
			return q;

		}

		public void cutOut(){
			for(int cx = x+1; cx < x+tw-1; cx++){
				for(int cy = y+1; cy < y+th-1; cy++){
					map[cy][cx] = 0;
				}
			}
		}

		public void reset(){
			doorPoints.clear();
			for(int cx = x+1; cx < x+tw-1; cx++){
				for(int cy = y+1; cy < y+th-1; cy++){
					map[cy][cx] = 1;
					foreground[cy][cx] = 0;
				}
			}
		}

		public void putIntoMap(int[][] room){
			for(int cx = 0; cx < tw; cx++){
				for(int cy = 0; cy < th; cy++){
					// map[_y+y+1][_x+x+1] = room[_y][_x];
					map[cy+y][cx+x] = room[cy][cx];
				}
			}
		}
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

		System.out.println(type);
		return type;
	}

	public Point randomEmptySpace(){
		int x;
		int y;
		do{
			// System.out.println("picking point...");
			x = rng.nextInt(width-1);
			y = rng.nextInt(height-1);
		}while(!isEmpty(x,y) || map[y][x] == 5);
		return (new Point(x,y));
	}

	public Point randomOpenSpace(){
		int x;
		int y;
		do{
			// System.out.println("picking point...");
			x = rng.nextInt(width-1);
			y = rng.nextInt(height-1);
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

	public void placeExits(){
		int x1=0;
		int y1=0;
		int x2=0;
		int y2=0;

		int tries = 0;
		int bigtries=0;
		Pathfinder.PointBFS p = null;
		do {
			do {
				do {
					x1 = rng.nextInt(width-1);
					y1 = rng.nextInt(height-1);
				} while (!isOpen(x1,y1));
				do {
					x2 = rng.nextInt(width-1);
					y2 = rng.nextInt(height-1);
				} while (!isOpen(x2,y2));
				tries++;
			} while (!(Math.abs(x2-x1)+Math.abs(y2-y1)>=Math.min(Math.max(width, height)/2.25,Math.min(width,height))) && tries<=300);
			System.out.println("placed exits");
			if(tries<=150){
				// TODO: change this to flood fill from every unvisited node source
				p = pf.pathfindBFS(new Point(x1,y1), new Point(x2,y2), buildOpenMap(), entities, true, true);
				if(p==null || p.getParent() == null){
					System.out.println("connecting exits failed.");
				}
			}else{
				break;
			}
			bigtries++;
		} while (p==null || p.getParent() == null && bigtries < 10);

		if(tries>300){
			System.out.println("connecting exits failed.");
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
	public int[][] buildOpenMap(){
		int[][] openMap = new int[height][width];
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
			}
		}
		return openMap;
	}

	public void buildTileMap(){
		for(int x = 0; x < width; x++){
			for(int y = 0; y < height; y++){
				tileMap[y][x] = new Tile(map[y][x],type,tileTypeAdj(x,y));
				int f = foreground[y][x];
				// if(f==1 && tileMap[y][x].value != 6) f = 0;
				if(f!=0) tileMap[y][x].setForeground(f,fgTypeAdj(x,y));
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
		for(int y=0; y < height; y++){
			for(int x=0; x < width; x++){
				if(foreground[y][x] != 0){
					System.out.print(type.foreground_characters[foreground[y][x]]+" ");
				}else{
					System.out.print(type.tile_characters[map[y][x]]+" ");
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

	public boolean[][] lightMap;
	private FOV fov = new FOV();

	public void updateFOV(){
		lightMap = fov.calculate(buildOpacityMap(), player.x, player.y, Main.player.Luminosity);

		for(Entity e: entities.values()){
			if(e.ai == null) continue;
			if(e.inPlayerView){
				e.inPlayerView = lightMap[e.y][e.x];
				if(!e.inPlayerView) Main.appendText("The "+e.creature.NAME+" is no longer in view.");
			}else{
				e.inPlayerView = lightMap[e.y][e.x];
				if(e.inPlayerView) Main.appendText("A "+e.creature.NAME+" moves into view.");
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

	public BufferedImage renderArea(int x1, int y1, int x2, int y2, boolean noLighting){
		int areaHeight = Math.abs(y2-y1)+1;
		int areaWidth = Math.abs(x2-x1)+1;

		BufferedImage area = new BufferedImage(areaWidth*tileSize,areaHeight*tileSize,BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) area.getGraphics();
		BufferedImage dark;

		updateFOV();

		// BufferedImage SouthWall;
		try {
			dark = ImageIO.read(new File(type.PATH+"dark.png"));
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

					if(tile!=dark && !noLighting){
						if(!lightMap[y][x]){
							if (tileMap[y][x].visited) {
								BufferedImage image = new BufferedImage(tileSize, tileSize,
										BufferedImage.TYPE_BYTE_GRAY);
								Graphics tileG = image.getGraphics();
								tileG.drawImage(tileMap[y][x].asLastSeen, 0, 0, null);
								Entity LEH = tileMap[y][x].lastEntityHere;
								if(LEH!=null && isOnMap(LEH.x,LEH.y) && !lightMap[LEH.y][LEH.x]){
									tileG.drawImage(LEH.getImg(), 0, 0, null);
								}
								tileG.dispose();
								tile = image;
							}else{
								tile = dark;
							}
						}else{

						}
					}
					g.drawImage(tile,x_ofs*tileSize, y_ofs*tileSize,null);

					BufferedImage fullImg = new BufferedImage(tileSize,tileSize,BufferedImage.TYPE_INT_ARGB);
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
						for (Entity e : entities.values()) {
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
					if(lastEntity!=null) g.drawImage(lastEntity.getImg(), x_ofs*tileSize, y_ofs*tileSize, null);

					// and draw player
					if(player != null && player.getX() == x && player.getY() == y){
						g.drawImage(player.getImg(),x_ofs*tileSize,y_ofs*tileSize,null);
						//					fg.drawImage(player.getImg(),0,0,null);
					}
				}
			}
			g.dispose();
		} catch (Exception e) {
			e.printStackTrace();
		};
		return area;
	}

	public BufferedImage renderRadius(int x, int y, int r){
		BufferedImage area = renderArea(x-r,y-r,x+r,y+r,false);
		// circle shading
		return area;
	}

	public BufferedImage addVignette(BufferedImage image, int x, int y, int r){
		int d = r*2*tileSize;
		Graphics g = (Graphics2D) image.getGraphics();
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