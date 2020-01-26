import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import Main.Direction;
public class Entity extends GameObject {
	
	// TODO: move all creature logic to creature class
	
	public int x;
	public int y;

	public String name;
	// public static int hp;

	public Map map;
	public BufferedImage img;

	
	public double SAT = 10; // TEMP
	
	public boolean waiting = false;

	public boolean inPlayerView = false;
	

	public HashMap<Status,Integer> statuses = new HashMap<Status,Integer>();

	public Inventory inv = new Inventory();	
	
	// TODO: move these to creature
	public Weapon weapon;
	public Missile quivered;	

	public boolean awake = false;

	public FOV fov = new FOV();
	
	public boolean isPassable = false;

	Entity(Creature _creature, int _x, int _y, Map _map){
		x = _x;
		y =_y;
		img = _creature.SPRITE;
		map = _map;
		if(NOT_PLAYER){
			name = map.addEntity(this);
			ai = new AI(this);
			System.out.println("AI attached.");
		}else{
			name = "player"; 
		}
		inheritFromCreature();
	}

	Entity(StaticEntity.SEType _SEType, int _x, int _y, Map _map){
		SE = new StaticEntity(_SEType);
		x = _x;
		y = _y;
		map = _map;
		img = SE.sprite;
		name = SE.type.name;
		inv = SE.inv;
		isPassable = true;
		map.addEntity(this);
	}

	public void inheritFromCreature(){
		this.HP = creature.HP_MAX;
		this.SP = creature.SP_MAX;
		this.STRENGTH = creature.STRENGTH;
		this.EV = creature.EVASIVENESS;
		this.isFlying = creature.isFlying;
		this.isAmphibious = creature.isAmphibious;
	}

	public void die(){
		map.entities.remove(name);
	}

	public boolean awakeCheck(){
		// TODO: upgrade
		if(awake) return true;
		if(ai == null) return false;
		boolean[][] vision = fov.calculate(map.buildOpacityMap(), x, y, Main.player.Luminosity); //TODO; add creature viewDis
		if(vision[Main.player.e.y][Main.player.e.x]){
			awake = true;
			return true;
		}
		return false;
	}

	public void upkeep(){
		SAT = Math.max(SAT-0.01,0);
		for(Status s: statuses.keySet()){
			if(s.upkeep){
				// TODO: do tier effect
				if(s.t == 0){
					HP+=0.5;
				}else if(s.t == 2){
					HP--;
				}
				// TODO: add static regen
			}
			statuses.replace(s, statuses.get(s)-1);
			if(statuses.get(s) <= 0){
				removeStatus(s);
			}
		}
	}

	
	public enum turnEnding{
		DEAD,
		WAITING,
		NOTACREATURE,
		NORMAL;
	}
	public turnEnding takeTurn(){
		waiting = false;
		if(ai == null){
			return turnEnding.NOTACREATURE; // not a creature
		}
		if(awakeCheck()){
			upkeep();

			if (HP<0.05) {
				Main.appendText("You kill the " + creature.NAME + ".");
				return turnEnding.DEAD; // dead
			}
			if(!ai.takeTurn()){
				waiting = true;
				return turnEnding.WAITING;
			}
		}
		return turnEnding.NORMAL; // not dead
	}

	public void addStatus(Status s){
		if(!statuses.containsKey(s)){
			toggle(s, true);
			if(ai!=null){
				Main.appendText("The "+creature.NAME+" is "+s.name+"!");
			}else{
				Main.appendText("You are "+s.name+"!");
			}
			statuses.put(s, (int) ActionLibrary.round(s.baseDuration*2/3*Main.rng.nextDouble() + s.baseDuration*2/3,0));
		}else{
			statuses.replace(s, statuses.get(s) + (int) ActionLibrary.round(s.baseDuration*2/3*Main.rng.nextDouble() + s.baseDuration*2/3,0));
		}
	}

	public void removeStatus(Status s){
		toggle(s, false);
		if(ai!=null){
			Main.appendText("The "+creature.NAME+" is no longer "+s.name+".");
		}else{
			Main.appendText("You are no longer "+s.name+".");
		}
		statuses.remove(s);
	}

	public void toggle(Status s, boolean start){
		int t = s.t;
		int mod = 1;
		if(!start) mod = -1;
		if(t==1){
			STRENGTH += Math.max(5,STRENGTH/2)*mod;
		}else if(t==3){
			isFlying = start;
		}
	}
	

	public int getX(){ return x; }
	public int getY(){ return y; }
	public Point getPos(){ return (new Point(x,y)); }
	public String getName(){
		if(creature == null) return SE.type.name;
		return creature.NAME; 
	} 
	public BufferedImage getImg(){ return img; } 



	// u:0,r:1,d:2,l:3
	// ur: 4,rd: 5, dl: 6, lu: 7
	public boolean move(int dir){
		boolean sxs = true;
		if(dir == 0 && isFullOpen(x,y-1)){
			y--;
		} else if(dir == 1 && isFullOpen(x+1,y)){
			x++;
		} else if(dir == 2 && isFullOpen(x,y+1)){
			y++;
		} else if(dir == 3 && isFullOpen(x-1,y)){
			x--;
		} else if(dir == 4 && diagonalCheck(4)){
			y--;
			x++;
		} else if(dir == 5 && diagonalCheck(5)){
			y++;
			x++;
		} else if(dir == 6 && diagonalCheck(6)){
			y++;
			x--;
		} else if(dir == 7 && diagonalCheck(7)){
			y--;
			x--;
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
		for(Entity e: map.entities.values()){
			if(!e.isPassable && e.x==x && e.y==y) return false;
		}
		if(map.player.x == x && map.player.y == y) return false;
		return isOpen(x,y);
	}

	public boolean diagonalCheck(int dir){
		//		int blocks = 0;
		//		if((dir == 4 || dir == 5)&& !isFullOpen(x+1,y)) blocks++;
		//		if((dir == 5 || dir == 6)&& !isFullOpen(x, y+1)) blocks++;
		//		if((dir == 6 || dir == 7)&& !isFullOpen(x-1, y)) blocks++;
		//		if((dir == 7 || dir == 4)&& !isFullOpen(x, y-1)) blocks++;
		//		
		//		if(blocks>=2) return false;
		//		
		if(dir == 4 && !isFullOpen(x+1,y-1)) return false;
		if(dir == 5 && !isFullOpen(x+1,y+1)) return false;
		if(dir == 6 && !isFullOpen(x-1,y+1)) return false;
		if(dir == 7 && !isFullOpen(x-1,y-1)) return false;

		return true;
	}

	public boolean isAdjacentTo(Point p){
		for(int cx = x-1; cx<=x+1; cx++){
			for(int cy = y-1; cy<=y+1; cy++){
				if(cx == x && cy == y) continue;
				if(cx == p.x && cy == p.y) return true;
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
	
	public int getDir(Point d){
		Main.Direction[] dirs = Main.Direction.values();
		for(int i = 0; i < dirs.length; i++) {
			if(d.equals(dirs[i].p)) return i;
		}

		return -1;
	}

	public Point getPoint(int dir) {
		return Main.Direction.values()[dir].p;
	}

	public double getDefense(){
		double defense = 0;
		for(Armour a: armour){
			if(a!=null){
				defense += a.getDefence();
			}
		}
		return defense;
	}
	
	// TODO: keys that match floors, inv print keylist
	public void pickupKey(int floor){
		if(inv.keys.containsKey(floor)){
			inv.keys.replace(floor,inv.keys.get(floor)+1);
		}else{
			inv.keys.put(floor,1);
		}
	}
	
	public void useKey(int floor){
		if(inv.keys.containsKey(floor)){
			inv.keys.replace(floor,inv.keys.get(floor)-1);
			if(inv.keys.get(floor) <= 0){
				inv.keys.remove(floor);
			}
		}
	}
}