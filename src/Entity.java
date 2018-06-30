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
	
	
	public int HP;
	public int SP;
	public double STRENGTH;
	
	public boolean awake = false;
	
	Entity(Creature _creature, int _x, int _y, Map _map){
		x = _x;
		y =_y;
		creature = _creature;
		img = _creature.SPRITE;
		map = _map;
		if(!creature.equals(Creature.PLAYER)){
			name = map.addEntity(this);
			ai = new AI(this);
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
		if(Math.abs(map.player.x-x) <=5 && Math.abs(map.player.y-y) <= 5){
			awake = true;
			return true;
		}
		return false;
	}
	
	public boolean takeTurn(){
		if(awakeCheck()){
			if(!ai.takeTurn()){
				System.out.println("You defeated the "+creature.NAME+".");
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
		if(dir == 0 && map.isFullOpen(x-1,y)){
			x--;
		} else if(dir == 1 && map.isFullOpen(x,y+1)){
			y++;
		} else if(dir == 2 && map.isFullOpen(x+1, y)){
			x++;
		} else if(dir == 3 && map.isFullOpen(x, y-1)){
			y--;
		} else if(dir == 4 && diagonalCheck(4)){
			x--;
			y++;
		} else if(dir == 5 && diagonalCheck(5)){
			x++;
			y++;
		} else if(dir == 6 && diagonalCheck(6)){
			x++;
			y--;
		} else if(dir == 7 && diagonalCheck(7)){
			x--;
			y--;
		}else{
			sxs = false;
		}
		return sxs;
	}
	
	public boolean diagonalCheck(int dir){
		int blocks = 0;
		if((dir == 4 || dir == 5)&& !map.isFullOpen(x, y+1)) blocks++;
		if((dir == 5 || dir == 6)&& !map.isFullOpen(x+1, y)) blocks++;
		if((dir == 6 || dir == 7)&& !map.isFullOpen(x, y-1)) blocks++;
		if((dir == 7 || dir == 4)&& !map.isFullOpen(x-1, y)) blocks++;
		
		if(blocks>=2) return false;
		
		if(dir == 4 && !map.isFullOpen(x-1,y+1)) return false;
		if(dir == 5 && !map.isFullOpen(x+1,y+1)) return false;
		if(dir == 6 && !map.isFullOpen(x+1,y-1)) return false;
		if(dir == 7 && !map.isFullOpen(x-1,y-1)) return false;
		
		return true;
	}
	
	public boolean isAdjacentTo(Point p){
		// TODO: do
		for(int cx = x-1; cx<=x+1; cx++){
			for(int cy = y-1; cy<=y+1; cy++){
				if(cx == x && cy == y) continue;
				if(cx == p.x && cy == p.y) return true;
			}
		}
		return false;
	}
	
	public int getDir(Point d){
		if(d.x==x-1 && d.y == y+1) return 4;
		if(d.x==x+1 && d.y == y+1) return 5;
		if(d.x==x+1 && d.y == y-1) return 6;
		if(d.x==x-1 && d.y == y-1) return 7;
		if(d.x==x-1) return 0;
		if(d.y==y+1) return 1;
		if(d.x==x+1) return 2;
		if(d.y==y-1) return 3;
		return -1;
	}
}