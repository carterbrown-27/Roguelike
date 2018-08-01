import java.awt.Point;
import java.awt.image.BufferedImage;
public class Entity {
	
	public int x;
	public int y;
	
	public String name;
	// public static int hp;
	
	public Map map;
	public BufferedImage img;
	
	public int speed = 1;
	
	// TODO: add class Type for specific
	
	public Creature creature;
	public AI ai;
	
	
	public double HP;
	public double SP;
	public double STRENGTH;
	
	public Item weapon;
	public Item quivered;
	public Item helmet;
	public Item boots;
	public Item chestplate;
	public Item greaves;
	public Item gloves;
	public Item ring_left;
	public Item ring_right;
	public Item[] armour = {weapon,quivered,helmet,boots,chestplate,greaves,gloves};
	public Item amulet;
	
	public boolean awake = false;
	
	public FOV fov = new FOV();
	
	Entity(Creature _creature, int _x, int _y, Map _map){
		x = _x;
		y =_y;
		creature = _creature;
		img = _creature.SPRITE;
		map = _map;
		if(!creature.equals(Creature.PLAYER)){
			name = map.addEntity(this);
			ai = new AI(this);
			System.out.println("AI attached.");
		}else{
			name = "player";
		}
		this.HP = creature.HP_MAX;
		this.SP = creature.SP_MAX;
		this.STRENGTH = creature.STRENGTH;
	}
	
	
	public void die(){
		map.entities.remove(name);
	}
	
	public boolean awakeCheck(){
		// TODO: upgrade
		if(awake) return true;
		boolean[][] vision = fov.calculate(map.buildOpacityMap(), x, y, Main.player.Luminosity);
		if(vision[Main.player.e.y][Main.player.e.x]){
			awake = true;
			return true;
		}
		return false;
	}
	
	public boolean takeTurn(){
		if(ai == null) return true; // not a creature
		if (HP<0.1) {
			Main.appendText("You defeated the " + creature.NAME + ".");
			return false; // dead
		}
		if(awakeCheck()){
			if(!ai.takeTurn()){
				Main.appendText("You defeated the "+creature.NAME+".");
				return false; // dead
			}
		}
		return true; // not dead
	}
	
	public int getX(){ return x; }
	public int getY(){ return y; }
	public Point getPos(){ return (new Point(x,y)); }
	public String getName(){ return creature.NAME; } 
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
		if(map.map[y][x]!=1 && (map.map[y][x]!=6 || creature.isAmphibious || creature.isFlying)) return true;
		return false;
	}

	public boolean isFullOpen(int x, int y){
		for(Entity e: map.entities.values()){
			if(e.x==x && e.y==y) return false;
		}
		if(map.player.x == x && map.player.y == y) return false;
		return isOpen(x,y);
	}
	
	public boolean diagonalCheck(int dir){
		int blocks = 0;
		if((dir == 4 || dir == 5)&& !isFullOpen(x+1,y)) blocks++;
		if((dir == 5 || dir == 6)&& !isFullOpen(x, y+1)) blocks++;
		if((dir == 6 || dir == 7)&& !isFullOpen(x-1, y)) blocks++;
		if((dir == 7 || dir == 4)&& !isFullOpen(x, y-1)) blocks++;
		
		if(blocks>=2) return false;
		
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
	
	public int getDir(Point d){
		if(d.x==x+1 && d.y == y-1) return 4;
		if(d.x==x+1 && d.y == y+1) return 5;
		if(d.x==x-1 && d.y == y+1) return 6;
		if(d.x==x-1 && d.y == y-1) return 7;
		if(d.y==y-1) return 0;
		if(d.x==x+1) return 1;
		if(d.y==y+1) return 2;
		if(d.x==x-1) return 3;
		
		return -1;
	}

	public Point getPoint(int dir){
		if(dir == 0) return new Point(x,y-1);
		if(dir == 1) return new Point(x+1,y);
		if(dir == 2) return new Point(x,y+1);
		if(dir == 3) return new Point(x-1,y);
		
		if(dir == 4) return new Point(x+1,y-1);
		if(dir == 5) return new Point(x+1,y+1);
		if(dir == 6) return new Point(x-1,y+1);
		if(dir == 7) return new Point(x-1,y-1);
		
		return null;
	}
	/** rf **/
}