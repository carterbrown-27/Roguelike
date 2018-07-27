import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

public class Player extends KeyAdapter {
	public Entity e;
	public BufferedImage img;
	public Map map;
	
	public int ViewDistance = 6;
	public int Luminosity = 4;
	
	public Ability ability = Ability.BASIC;
	public int step = 0;
	
	public Random rng = new Random();
	
	public Entity ghost;
	
	Player(int x, int y, Map _map){
		map = _map;
		this.e = new Entity(Creature.PLAYER,x,y,map);
		map.player = this.e;
	}
	
	public enum Ability{
		BASIC (0),
		SLASH (1),
		LUNGE (2);
		
		
		public int k;
		public int s;
		
		Ability (int e){
			if(e == 0){
				k = KeyEvent.VK_ESCAPE;
				s = 0;
			}else if(e == 1){
				k = KeyEvent.VK_1;
				s = 4;
			}else if(e == 2){
				k = KeyEvent.VK_2;
				s = 2;
			}
		}
	}
	
	public void deselect(){
		select(Ability.BASIC);
	}
	
	public void select(Ability a){
		dirs.clear();
		step = 0;
		this.ability = a;
	}
	
	public void error(){
		System.out.println("Action not possible.");
	}
	
	ArrayList<Integer> dirs = new ArrayList<Integer>();
	public void act_adj(int dir){
		if(ability.equals(Ability.BASIC)){
			basic(dir);
		}else if(ability.equals(Ability.SLASH)){
			if(melee(dir,1.5)){
				e.SP-=Ability.SLASH.s;
				Main.takeTurn();
				deselect();
			}
		}else if(ability.equals(Ability.LUNGE)){
			dirs.add(dir);
			if(dirs.size()==2){
				Point start = e.getPos();
				if(lunge(dirs.get(0),dirs.get(1))){
					e.SP-=Ability.LUNGE.s;
					System.out.println(e.SP);
					Main.takeTurn();
					deselect();
				}else{
					e.x = start.x;
					e.y = start.y;
					deselect();
					error();
				}
			}
		}
	}
	
	@SuppressWarnings("unused")
	public void basic(int dir){
		if(!e.move(dir)){
			if(melee(dir,1)){
				Main.takeTurn();
			}else if(false /* TODO: interact */){
				
			}
		}else{
			Main.takeTurn();
		}
	}
	
	public boolean lunge(int movedir, int attackdir){
		return (e.move(movedir) && melee(attackdir,0.5));
	}
	
	public boolean melee(int dir, double modifier){
		Entity target = targetAdjacent(dir);
		if(target!=null){
			target.HP -= Math.round((e.STRENGTH/2 + (rng.nextDouble()*e.STRENGTH) * modifier));
			System.out.println(target.name+" 's HP: "+target.HP);
			return true;
		}
		return false;
	}
	
	public Entity targetAdjacent(int dir){
		for(Entity i: map.entities.values()){
			if(e.isAdjacentTo(i.getPos()) && e.getDir(i.getPos()) ==  dir){
				return i;
			}
		}
		return null;
	}
	
//	public ArrayList<Entity> getAdjacents() {
//		ArrayList<Entity> targets = new ArrayList<Entity>();
//		for(Entity i: map.entities.values()){
//			if(i.isAdjacentTo(e.getPos())){
//				targets.add(i);
//			}
//		}
//		return targets;
//	}
	

}
