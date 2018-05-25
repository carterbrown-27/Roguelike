import java.io.*;
import javax.imageio.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

public class Map {
	public static int height = 52; // 52
	public static int width = 90; // 90
	
	public static int randFillPercent = 46; // 46 [+4 / -3]
	public static boolean randSeed = true;
	
	public static int[][] map = new int[height][width];
	public static int smooths = 4;
	
	public static int entityNumber = 0;
	// public static Entity[][] entity_map = new Entity[height][width];
	public static HashMap<String,Entity> entities = new HashMap<String,Entity>();
	
	public static int tileSize = 24;
	
	private static Random rng = new Random();
	private static Pathfinder pf = new Pathfinder();
	
	public static String maptype = "Cave";
	private static Map_Types type = new Map_Types(maptype);
	
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
	
		generateMap();
		placeExits();
	}
	
	Map(int[][] _map, HashMap<String,Entity> _entities){
		map = _map;
		entities = _entities;
	}
	
	Map(Map that){
		this(that.getMap(),that.entities);
	}
	
	public static int[][] getMap(){
		return map;
	}
	
	public static boolean isOpen(int x, int y){
		if(x>=height || y>= width || x<0 || y<0) return false;
		if(map[x][y]!=1) return true;
		return false;
	}
	
	public static String addEntity(Entity entity){
//		int x = entity.getX();
//		int y = entity.getY();
//		if(entity_map[x][y] == null){
//			entity_map[x][y] = entity;
//		}
		String name = entity.getType()+"/"+String.valueOf(entityNumber);
		entities.put(name,entity);
		entityNumber++;
		return name;
	}
	
//	public static void buildEntityMap(){
//		for(Entity e: entities.values()){
//			entity_map[e.getX()][e.getY()] = e;
//		}
//	}
	
	public static void generateMap(){
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
	
	// default 4
	public static int[][] smoothMap(int threshold){
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
	
	public static int surroundingWallCount(int centerX, int centerY){
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
	
	// 	if(map[x][y] >= tile_characters.length){
	// System.out.println(foreground_characters[map[x][y]-tile_characters.length]+" ");
	public static void printMap(){
		for(int x=0; x < height; x++){
			for(int y=0; y < width; y++){
				System.out.print(type.tile_characters[map[x][y]]+" ");
			}
			System.out.println();
		}
	}
	
	public static BufferedImage renderMap() {
		return renderArea(0,0,height-1,width-1); 
	}
	
	public static BufferedImage renderArea(int x1, int y1, int x2, int y2){
		int areaHeight = Math.abs(x2-x1)+1;
		int areaWidth = Math.abs(y2-y1)+1;
		
		BufferedImage area = new BufferedImage(areaWidth*tileSize,areaHeight*tileSize,BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) area.getGraphics();
		BufferedImage[] imgs = new BufferedImage[type.tiles.length];
		BufferedImage dark;
		BufferedImage SouthWall;
		try {
			// image loading
			for(int i = 0; i < type.tiles.length; i++){
				imgs[i] = ImageIO.read(type.tiles[i]);
			}
			dark = ImageIO.read(new File("dark.png"));
			//dark = ImageIO.read(new File("RedWall.png"));
			SouthWall = ImageIO.read(new File("Floor.png"));
			
			// draw
			for(int x=Math.min(x1,x2); x <= Math.max(x1,x2); x++){ // row #
				for(int y=Math.min(y1,y2); y <= Math.max(y1,y2); y++){ // column #
					BufferedImage tile;
					int x_ofs = x - Math.min(x1, x2);
					int y_ofs = y - Math.min(y1,y2);
					if((x>=height || y>= width || x<0 || y<0) || map[x][y] == 1 && tileTypeAdj(x,y) == 0){
						tile = dark;
					}else{
						tile = imgs[map[x][y]];
					}
					if(!(tile==null)){
						g.drawImage(tile,y_ofs*tileSize, x_ofs*tileSize,null);
					}
					for(Entity e: entities.values()){
						if(e.getX() == x && e.getY() == y){
							g.drawImage(e.getImg(),y_ofs*tileSize,x_ofs*tileSize,null);
//							System.out.println(e.name);
						}
					}
				}
			}
		} catch (Exception e) {};
		return area;
	}
	
	public static boolean hasSouthFace(int x, int y){
		int t = tileTypeAdj(x,y);
		if((t>=4 && t<=7)||(t>=12 && t<=15)) return true;
		return false;
	}
	
	public static BufferedImage renderRadius(int x, int y, int r){
		BufferedImage area = renderArea(x-r,y-r,x+r,y+r);
		// circle shading
		return area;
	}
	
	public static BufferedImage addVignette(BufferedImage image, int x, int y, int r){
		int d = r*2*tileSize;
		Graphics g = (Graphics2D) image.getGraphics();
		//g.setColor(new Color(18,28,38,50));
		//g.fillRect(0,0,image.getWidth(), image.getHeight());
		g.setColor(new Color(225,185,85,45));
		g.fillOval(x,y,d,d);
		return image;
	}
	
	public static BufferedImage addCenterVignette(BufferedImage image, int r){
		int pixr = r*tileSize;
		return addVignette(image,image.getWidth()/2 - pixr,image.getHeight()/2 - pixr,r);
	}
	
	public static BufferedImage render_vig(int x, int y, int r, int vr){
		BufferedImage image = renderRadius(x,y,r);
		image = addCenterVignette(image,vr);
		return image;
	}
	
	public static boolean connectedPoints(int[][] temp, int x1, int y1, int x2, int y2){
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
	
	public static int[][] copyMap(){
		int[][] temp = new int[height][width];
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				temp[y][x] = map[y][x];
			}
		}
		return temp;
	}
	
	public static int tileTypeAdj(int centerX, int centerY){
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
	
	public static Point randomOpenSpace(){
		int x;
		int y;
		do{
			x = rng.nextInt(height);
			y = rng.nextInt(width);
		}while(!isOpen(x,y));
		return (new Point(x,y));
	}

	public static Point getPosition(int uniqueTile){
		for(int x = 0; x < height; x++){
			for(int y = 0; y < width; y++){
				if(map[x][y]==uniqueTile){
					return (new Point(x,y));
				}
			}
		}
		return null;
	}
	
	public static int valueAt(Point p){
		return map[p.x][p.y];
	}
	
	public static void placeExits(){
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
			} while (!(Math.abs(x2-x1)+Math.abs(y2-y1)>=Math.min(Math.max(width, height)/2,Math.min(width,height))));
			tries++;
			if(tries>=100){
				generateMap();
				placeExits();
				return;
			}
			p = pf.pathfindBFS(new Point(x1,y1), new Point(x2,y2), copyMap());
		} while(p == null || p.getParent()==null);
		
		while(p.getParent() != null){
			map[p.x][p.y] = 4;
			p = p.getParent();
		}
		
		map[x1][y1] = 2;
		map[x2][y2] = 3;
	}
}
