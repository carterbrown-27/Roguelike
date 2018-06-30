import java.awt.event.KeyAdapter;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

public class Player extends KeyAdapter {
	public Entity e;
	public BufferedImage img;
	public Map map;
	
	public int ViewDistance = 4;
	public int Luminosity = 3;
	
	public Random rng = new Random();
	
	Player(int x, int y, Map _map){
		map = _map;
		this.e = new Entity(Creature.PLAYER,x,y,map);
		map.player = this.e;
	}
	
	public void basic(int dir){
		if(!e.move(dir)){
			if(melee(dir)) Main.takeTurn();
		}else{
			Main.takeTurn();
		}
	}
	
	public boolean melee(int dir){
		Entity target = targetAdjacent(dir);
		if(target!=null){
			target.HP -= Math.round(e.STRENGTH/2 + (rng.nextDouble()*e.STRENGTH));
			System.out.println(target.name+" 's HP: "+target.HP);
			return true;
		}
		return false;
	}
	
	public Entity targetAdjacent(int dir){
		for(Entity i: map.entities.values()){
			if(e.getDir(i.getPos()) ==  dir){
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
