import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class Entity extends GameObject {
	
	// TODO: move all creature logic to creature class
	private Point pos;
	
	@Deprecated
	public int x;
	@Deprecated
	public int y;
	@Deprecated
	public Map map;

	private String name;
	
	private boolean waiting = false;
	private boolean inPlayerView = false;
	
	public Inventory inv = new Inventory();
	public boolean awake = false;
	public FOV fov = new FOV();
	public boolean isPassable = false;

	Entity(String id, Point _pos){
		// TODO: init by id
		this.pos = _pos;
		// TODO: eliminate
		if(NOT_PLAYER){
			// TODO: move map logic to Map.
			name = map.addEntity(this);
		}else{
			name = "player"; 
		}
	}
	
	public void die(){
		map.entities.remove(this);
	}

	@Deprecated
	public enum turnEnding{
		DEAD,
		WAITING,
		NOTACREATURE,
		NORMAL;
	}


	public int getX(){ return pos.x; }
	public int getY(){ return pos.y; }
	public Point getPos(){ return pos; }
	public String getName(){ return name; } 

	// u:0,r:1,d:2,l:3
	// ur: 4,rd: 5, dl: 6, lu: 7
	public boolean move(Direction dir){
		boolean sxs = true;
		if(checkMove(dir)){
			this.pos = getTranslatedPos(dir);
		}else{
			sxs = false;
		}
		if(map.map[y][x] == 5){
			map.map[y][x] = 7;
			map.tileMap[y][x].setValue(7);

			Main.appendText("The door creaks open.");
		}
		return sxs;
	}

	public boolean isOpen(int x, int y){
		if(!map.isOnMap(x,y)) return false;
		int[][] m = map.buildOpenMap();
		// wall			// water 		// fly-over-able
		if(m[y][x]!=1 && m[y][x]!=2 && m[y][x]!=3) return true;
		return false;
	}
	

	public boolean isFullOpen(int x, int y){
		for(Entity e: map.entities){
			if(!e.isPassable && e.getX()==x && e.getY()==y) return false;
		}
		if(map.player.x == x && map.player.y == y) return false;
		return isOpen(x,y);
	}
	
	public boolean isFullOpen(Point p) {
		return isFullOpen(p.x,p.y);
	}
	
	@Deprecated
	public boolean isInPlayerView() {
		return inPlayerView;
	}
	
	public boolean checkMove(Direction dir){
		Point translated = getTranslatedPos(dir);
		return isFullOpen(translated);
	}
	
	private Point getTranslatedPos(Direction dir) {
		Point delta = dir.p;
		Point translated = new Point(pos);
		translated.translate(delta.x,delta.y);
		return translated;
	}

	public boolean isAdjacentTo(Point p){
		for(Direction d: Direction.values()) {
			if(p.equals(getTranslatedPos(d))) {
				return true;
			}
		}
		return false;
	}
	
	public Point[] adjacentPositions(){
		Point[] p = new Point[8];
		int pos = 0;
		for(int cx = x-1; cx<=x+1; cx++){
			for(int cy = y-1; cy<=y+1; cy++){
				if(cx == x && cy == y) continue;
				p[pos] = new Point(x,y);
				pos++;
			}
		}
		return p;
	}
	
	// TODO: keys that match floors, inv print keylist
	// TODO: move this to Inv class
}