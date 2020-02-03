import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class Entity extends GameObject {
	
	// TODO (M) move all creature logic to creature class
	private Point pos;
	@Deprecated
	public Map map;

	private String name;
	
	private boolean waiting = false;
	private boolean inPlayerView = false;
	
	public Inventory inv = new Inventory();
	public boolean awake = false;
	public FOV fov = new FOV();
	public boolean isPassable = false;
	
	private double HP = 1.0;
	private double HP_max = 1; // 6 dmg = player with dagger
	private boolean indestructible = false;

	Entity(String id, Point _pos, Map _map){
		// TODO (I) Implement
		super();
		// TODO: init by id
		this.pos = _pos;
		this.map = _map;
		
		// TODO (X) Eliminate
		if(!id.equals("player")){
			// TODO (M) move map logic to Map.
			map.addEntity(this);
		}
		name = id.toLowerCase();
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

	// u:0,r:1,d:2,l:3
	// ur: 4,rd: 5, dl: 6, lu: 7
	public boolean move(Direction dir){
		boolean sxs = true;
		if(checkMove(dir)){
			this.pos = getTranslatedPos(dir);
		}else{
			sxs = false;
		}
		
		// TODO (R) review
		if(map.map[getY()][getX()] == 5){
			map.map[getY()][getX()] = 7;
			map.tileMap[getY()][getX()].setValue(7);

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
	
	public boolean isOpen(Point p) {
		return isOpen(p.x,p.y);
	}

	public boolean isFullOpen(int x, int y){
		return isFullOpen(new Point(x,y));

	}
	
	public boolean isFullOpen(Point p) {
		for(Entity e: map.entities){
			if(!e.isPassable && p.equals(e.getPos())) return false;
		}
		if(p.equals(map.player.getPos())) return false;
		return isOpen(p);
	}
	
	public boolean isInPlayerView() {
		return inPlayerView;
	}
	
	public void setInPlayerView(boolean val) {
		this.inPlayerView = val;
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
		return getAdjacentDir(p) != null;
	}
	
	public Direction getAdjacentDir(Point p) {
		for(Direction d: Direction.values()) {
			if(p.equals(getTranslatedPos(d))) {
				return d;
			}
		}
		return null;
	}
	
	public Point[] adjacentPositions(){
		Point[] p = new Point[Direction.values().length];
		for(int i = 0; i < p.length; i++) {
			p[i] = Direction.translate(getPos(), Direction.values()[i]);
		}
		return p;
	}
	// TODO (A) Implement: keys that match floors, inv print keylist
	// TODO (M) move this to Inv class
	
	// getters
	public int getX(){ return pos.x; }
	public int getY(){ return pos.y; }
	public Point getPos(){ return pos; }
	public String getName(){ return name; } 
	
	public double getHP() {
		return HP;
	}
	
	public void changeHP(double delta) {
		setHP(getHP() + delta);
	}
	
	public void setHP(double value) {
		HP = value;
		HP = ActionLibrary.round(HP, 1);
		
		if(HP <= 0) {
			// die
		}else if(HP > getHP_max()) {
			HP = getHP_max();
		}
	}
	
	public double getHP_max() {
		return HP_max;
	}
	
	public void setName(String _name) {
		this.name = _name;
	}
	
	public void setPos(Point p) {
		pos = p;
	}

	public void setHP_max(double hP_max) {
		HP_max = hP_max;
	}
}