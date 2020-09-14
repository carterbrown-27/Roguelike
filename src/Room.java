import java.awt.Point;
import java.util.ArrayList;
import java.util.Queue;
import java.util.logging.Logger;

// TODO (*) Create Room Hierarchy to handle roomtypes.
public class Room {

	private static final Logger logger = Logger.getLogger(Room.class.getName());
	
	public static enum RoomType{
		RANDOM,
		REGULAR,
		SEWER,
		PRECON;
	}

	// TODO (R) Refactor, clean up & document all of this

	char[][] roomMap;
	int[][] foreground;
	
	public int x;
	public int y;

	public int w;
	public int h;
	public int tw;
	public int th;

	public int offset;
	public int rotationNinety = 0; // int rN * 90 = degrees to rotate

	public Map.PointDir door;
	public int doors;
	public ArrayList<Map.PointDir> doorPoints = new ArrayList<>();
	
	//	public PointDir[] localDoorPoints = new PointDir[4];
	boolean[] opens = new boolean[4];
	boolean valid = true;
	boolean start = false; 
	char dir;
	
	Map map;
	RoomType roomType;

	boolean preconPicked = false;
	int precon_id;


	Room(Map.PointDir _door, boolean _start, RoomType _type, Map _map){
		start = _start;
		door = _door;
		if(!start) dir = door.dir;
		roomType = _type;
		map = _map;

		if(start){
			x = door.point.x;
			y = door.point.y;
			roomType = RoomType.REGULAR;
			setBounds();
			buildNormal();
			return;
		}

		if(roomType.equals(RoomType.RANDOM)){
			// TODO (T%) Tabulate %s
			logger.fine("picking type.");
			int a = Main.getRng().nextInt(10);
			if(a>=7){
				roomType = RoomType.SEWER;
			}else if(a>=4){
				roomType = RoomType.PRECON;
			}else{
				roomType = RoomType.REGULAR;
			}
		}

		if (findWorking()) {
			buildNormal();
		}else{
			valid = false;
		}
	}
	
	Room(Map.PointDir _door, RoomType _type, Map _map){
		this(_door, false, _type, _map);
	}
	
	private boolean findWorking() {
		boolean works = true;
		int count = 0;
		final int MAX_ATTEMPTS = 40;
		do{
			works = true;
			setBounds();

			// TODO (R) Rework system so that room is generated first, then offsets are found, and doors are placed etc
			if(roomType.equals(RoomType.PRECON)){
				boolean offsetWorks = false;
				int OFFSET_ATTEMPTS = 20;
				int offsetCount = 0;
				do{
					if(dir=='U' || dir=='D'){
						offset = Main.getRng().nextInt(w-1)+1; // from 1 -> w
					}else{
						offset = Main.getRng().nextInt(h-1)+1;
					}

					Point p = precon_getDoorstep();
					Point dp = precon_getDoorPos();
					char[][] r = map.getMapType().precons.get(precon_id);
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

			if(dir=='U'){
				x = door.point.x-offset;
				y = door.point.y-th+1;
			}else if(dir=='D'){
				x = door.point.x-offset;
				y = door.point.y;
			}else if(dir=='L'){
				y = door.point.y-offset;
				x = door.point.x-tw+1;
			}else if(dir=='R'){
				y = door.point.y-offset;
				x = door.point.x;
			}
			works = surroundCheck();
			count++;
		} while(!works && count < MAX_ATTEMPTS);
		
		return works;
	}

	public Map.PointDir getRandomDoor(){
		if(!doorPoints.isEmpty()){
			logger.fine("" + doorPoints.size());
			for(Map.PointDir d: doorPoints){
				logger.fine(String.valueOf(d.point));
			}
			int rand;
			if(doorPoints.size()>1){
				rand = Main.getRng().nextInt(doorPoints.size()-1);
			}else{
				rand = 0;
			}
			Map.PointDir p = doorPoints.get(rand);
			doorPoints.remove(rand);
			return p;
		}else{
			logger.fine("Picking random door...");
			Map.PointDir p = randomDoor();
			if(p==null){
				logger.fine("No more doors possible!");
			}
			return p;
		}
	}

	public void setBounds(){
		if (roomType.equals(RoomType.REGULAR)) {
			int min = Main.getRng().nextInt(1)+3;
			w = Main.getRng().nextInt(5) + min;
			h = Main.getRng().nextInt(5) + min;
		}else if (roomType.equals(RoomType.SEWER)){
			final int l_min = 8;
			final int l_var = 6;

			if(dir=='U' || dir=='D'){
				// wide
				w = Main.getRng().nextInt(l_var)+l_min;
				h = Main.getRng().nextInt(2)+4;
			}else{
				// tall
				h = Main.getRng().nextInt(l_var)+l_min;
				w = Main.getRng().nextInt(2)+4;
			}
		}else if(roomType.equals(RoomType.PRECON)){
			if(!preconPicked){
				// TODO (R) Refactor, move logic to MapType.
				precon_id = Main.getRng().nextInt(map.getMapType().precons.size());
				rotationNinety = Main.getRng().nextInt(3);

				h = map.getMapType().precons.get(precon_id).length - 2;
				w = map.getMapType().precons.get(precon_id)[0].length - 2;
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
			if(dir=='U' || dir=='D'){
				// System.out.println(w);
				offset = Main.getRng().nextInt(w-1)+1; // from 1 -> w
			}else{
				// System.out.println(h);
				offset = Main.getRng().nextInt(h-1)+1;
			}
		}
	}

	public Map.PointDir randomDoor(){
		int tries = 0;
		while(tries < 15){
			int d = Main.getRng().nextInt(3);
			Map.PointDir p = null;
			if(d==0 && (start || door.dir != 'U')){
				p = new Map.PointDir(new Point(Main.getRng().nextInt(w-1)+1 + x, y), 'U');
			}else if(d==1 && (start || door.dir != 'R')){
				p = new Map.PointDir(new Point(x + tw-1, Main.getRng().nextInt(h-1)+1 + y), 'R');					
			}else if(d==2 && (start || door.dir != 'D')){
				p = new Map.PointDir(new Point(Main.getRng().nextInt(w-1)+1 + x, y + th-1), 'D');					
			}else if(start || door.dir != 'L'){
				p = new Map.PointDir(new Point(x, Main.getRng().nextInt(h-1)+1 + y), 'L');
			}
			if(p == null || (roomMap != null && roomMap[p.point.y-y][p.point.x-x] == 'X')){
				tries++;
				continue;
			}
			for(int cx = p.point.x-1; cx <= p.point.x+1; cx++){
				for(int cy = p.point.y-1; cy <= p.point.y+1; cy++){
					if(cx<x || cy<y || cx>=x+tw || cy>=y+th || (cx!=p.point.x && cy!=p.point.y)) continue;
					if(map.isOpen(cx,cy)){
						return p;
					}
				}
			}

			tries++;					
		}
		return null;
	}


	public boolean precon_containsDoors(){
		char[][] r = map.getMapType().precons.get(precon_id);

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
		if(door.dir == 'U'){
			return new Point(offset,th-2);
		}else if(door.dir == 'D'){
			return new Point(offset,1);
		}else if(door.dir == 'L'){
			return new Point(tw-2,offset);
		}else if (door.dir == 'R'){
			return new Point(1,offset);
		}
		return null;
	}

	public Point precon_getDoorPos(){
		if(door.dir == 'U'){
			return new Point(offset,th-1);
		}else if(door.dir == 'D'){
			return new Point(offset,0);
		}else if(door.dir == 'L'){
			return new Point(tw-1,offset);
		}else if (door.dir == 'R'){
			return new Point(0,offset);
		}
		return null;
	}

	public void buildNormal(){
		if(roomType.equals(RoomType.REGULAR)){
			logger.fine("building regular...");
			pickDoors();
			//				placeDoors();
			map.cutOut(this);
		}else if(roomType.equals(RoomType.SEWER)){
			logger.fine("building sewer...");
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
			map.cutOut(this);
			buildSewer();
		}else if(roomType.equals(RoomType.PRECON)){
			logger.fine("building precon #"+precon_id);
			pickDoors();
			buildPrecon();
		}
	}

	//		public void customize(){
	//			cutOut();
	//			if(RoomType.SEWER.equals(this.roomType)){
	//				buildSewer();
	//			}
	//		}

	public void buildPrecon(){

		// TODO (+) add chests, keys, item spawns / monster spawns
		// TODO (+) add extra info for rooms; rarity, ool gen level etc
		// TODO (J) JSONize
		// TODO (R) Review: allow for rotation.
		int ow = map.getMapType().precons.get(precon_id)[0].length;
		int oh = map.getMapType().precons.get(precon_id).length;
		roomMap = new char[oh][ow];
		for(int cy = 0; cy < oh; cy++){
			for(int cx = 0; cx < ow; cx++){
				char c = map.getMapType().precons.get(precon_id)[cy][cx];
				roomMap[cy][cx] = c;
			}
		}

		for(int i = 0; i < rotationNinety; i++){
			roomMap = rotateNinety(roomMap);
		}

		ArrayList<Map.PointDir> possibleDoors = new ArrayList<>();
		boolean hitD = false;
		int temp = 0;
		for(int cy = 0; cy < th; cy++){
			for(int cx = 0; cx < tw; cx++){;
			Point p = new Point(cx,cy);
			// TODO (+) add non-edge doors
			if(roomMap[cy][cx]== 'D'){
				if(!hitD){
					hitD = true;
					doors = 0;
				}
				if(cy != door.point.y-y && cx != door.point.x-x){
					doors++;
					doorPoints.add(new Map.PointDir(new Point(p.x+x,p.y+y),getDoorDir(p)));							
				}
			}else if(roomMap[cy][cx] =='d' && (cy != door.point.y-y && cx != door.point.x-x)){
				if(hitD){
					temp = doors;
					pickDoors();
					doors+=temp;
				}
				possibleDoors.add(new Map.PointDir(new Point(p.x+x,p.y+y),getDoorDir(p)));
			}
			}
		}

		int doorCount = doors-temp;
		while(doorCount > 0 && possibleDoors.size()>1){
			int r = Main.getRng().nextInt(possibleDoors.size()-1);

			doorPoints.add(possibleDoors.get(r));
			possibleDoors.remove(r);
			doorCount--;
		}

		if(doorCount > 0 && possibleDoors.size()==1){
			doorPoints.add(possibleDoors.get(0));
			possibleDoors.remove(0);
		}

		for(Map.PointDir d: possibleDoors){
			roomMap[d.point.y-y][d.point.x-x] = '#';
		}

		//			if(doorPoints.isEmpty()){
		//				for(int i = doors; i > 0; i--){
		//					doorPoints.add(randomDoor());
		//				}
		//			}

		// TODO: place doors, chests, keys etc
		map.putIntoMap(this);
	}

	public char getDoorDir(Point p){
		if(p.x == 0){
			return 'L';
		}else if(p.y == 0){
			return 'U';
		}else if(p.x == tw-1){
			return 'R';
		}else if(p.y == th-1){
			return 'D';
		}
		return '!';
	}

	public void buildSewer(){
		if(h<=3 || w<=3) return;
		int waterWidth = 2;
		boolean noBridge = (doors==0 && Main.getRng().nextInt(10)<=7);
		if((door.dir == 'U' || door.dir == 'D') && w>4){
			// horizontal
			int river_h;
			if(h<=4){
				river_h = 1;
			}else{
				river_h = Main.getRng().nextInt(h-4)+1;
			}

			// - 2 for edges, -2 for width, -river_h, +1
			// 5h1 -> [0,1] (+2) -> [2,3]
			// 5h2 -> [0] (+2) -> [2]
			if(h-2-river_h+1 == 2){
				waterWidth = 2;
			}else{
				if(false){
					waterWidth = Main.getRng().nextInt(h-2-river_h+1-2)+2;
				}else{
					waterWidth = h-2;
				}
			}

			// width || x x x || = 7 = 3 possible = [0,1,2] + 2) -> 7-5 = [2,3,4]
			int bridge_x = Main.getRng().nextInt(w-3)+2;
			for(int i = 1; i <= w; i++){
				for(int t = 1; t <= waterWidth; t++){
					if (noBridge || i!=bridge_x) {
						map.map[y + river_h + t][x + i] = 6;
					}else{
						map.map[y + river_h + t][x + i] = 6;
						map.getTile(new Point(x + i, y + river_h + t)).setForeground(Tile.Fg_Type.BRIDGE, 0);
					}
				}
			}
		}else if((door.dir == 'L' || door.dir == 'R') && h>4){
			// vertical
			int river_w;
			if(w<=4){
				river_w = 1;
			}else{
				river_w = Main.getRng().nextInt(w-4)+1;
			}

			if(w-2-river_w+1 == 2){
				waterWidth = 2;
			}else{
				if(false){
					waterWidth = Main.getRng().nextInt(w-2-river_w+1-2)+2;
				}else{
					waterWidth = w-2;
				}
			}

			// TODO: move map altering logic to Map
			int bridge_y = Main.getRng().nextInt(h-3)+2;
			for(int i = 1; i <= h; i++){
				for(int t = 1; t <= waterWidth; t++){
					if (noBridge || i!=bridge_y) {
						map.map[y + i][x + river_w + t] = 6;
					}else{
						map.map[y + i][x + river_w + t] = 6;
						map.getTile(new Point(x + river_w + t, y + i)).setForeground(Tile.Fg_Type.BRIDGE);
					}
				}
			}
		}
	}

	public boolean surroundCheck(){
		for(int i = x; i <= x+tw-1; i++){
			for(int j = y; j <= y+th-1; j++){
				if(i == door.point.x && j == door.point.y) continue;
				if(!map.isOnMap(i,j)){
					return false;
				}else if(map.isOpen(i,j)){
					return false;
				}
			}
		}
		return true;
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

	public Map.PointDir[] getDoors(){
		Map.PointDir[] l = new Map.PointDir[doors];
		if(doorPoints!=null && !doorPoints.isEmpty()){
			for(int i = 0; i < doors; i++){
				l[i] = doorPoints.get(i);
			}
		}
		for(int i = 0; i < doors; i++){
			Map.PointDir d = randomDoor();
			if(d!=null) l[i] = d;
		}
		return l;
	}

	public Queue<Map.PointDir> addDoorsToQueue(Queue<Map.PointDir> q){
		for(Map.PointDir d: q){
			q.add(d);
		}
		return q;

	}
}