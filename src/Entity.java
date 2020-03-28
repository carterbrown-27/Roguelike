import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;

public class Entity extends GameObject {
	
	// TODO (M) move all creature logic to creature class
	private String name;
	private Point pos;
	
	@Deprecated
	public Map map;
	
	private Inventory inv;
	private FOV fov;
	
	private boolean waiting = false;
	private boolean inPlayerView = false;
	private boolean awake = false;
	private boolean passable = false;
	private boolean indestructible = false;
	
	private double HP = 1.0;
	private double HP_max = 1; // 6 dmg = player with dagger
	
	Entity(String id, Point _pos, Map _map){
		// TODO (I) Implement
		super();
		
		inv = new Inventory();
		fov = new FOV();
		
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
		
		// TODO (R) Review, switch to door Interactable when implemented
		if(map.getMap()[getY()][getX()] == 5){
			map.getMap()[getY()][getX()] = 7;
			map.tileMap[getY()][getX()].setValue(7);
			
			// TODO (R) Refactor, add to "sound" system when added. (currently global message)
			Main.view.appendText("The door creaks open.");
		}
		return sxs;
	}

	public boolean isOpen(int x, int y){
		if(!map.isOnMap(x,y)) return false;
		int[][] openMap = map.buildOpenMap();
		return canOccupySpace(openMap,x,y);
	}
	
	public boolean canOccupySpace(int[][] openMap, int x, int y) {
		if(!map.isOnMap(x,y)) return false;
		return openMap[y][x] == 0;
	}
	
	public boolean isOpen(Point p) {
		return isOpen(p.x,p.y);
	}

	public boolean isFullOpen(int x, int y){
		return isFullOpen(new Point(x,y));

	}
	
	public boolean isFullOpen(Point p) {
		for(Entity e: map.entities){
			if(!e.isPassable() && p.equals(e.getPos())) return false;
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
	
	// TODO: override with status (e.g. flying) check in Creature.
	public boolean checkMove(Direction dir){
		Point translated = getTranslatedPos(dir);
		return isFullOpen(translated);
	}
	
	public Point getTranslatedPos(Direction dir) {
		Point delta = dir.p;
		Point translated = new Point(pos);
		translated.translate(delta.x,delta.y);
		return translated;
	}
	
	// check if in square bound range.
	public boolean inRange(Point other, int range) {
		return (Math.abs(other.x-pos.x) <= range) && (Math.abs(other.y-pos.y) <= range);
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

	public Inventory getInv() {
		return inv;
	}

	public FOV getFov() {
		return fov;
	}

	public boolean isAwake() {
		return awake;
	}

	public void setAwake(boolean b) {
		awake = b;
	}

	public boolean isPassable() {
		return passable;
	}
}