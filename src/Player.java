import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
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
	
	public Inventory inv = new Inventory();
	
	// String = item name (ie blue potion) <> Item.Items = item real name (ie potion of flight)
	public HashMap<String,Item.Items> identifiedItems = new HashMap<String,Item.Items>();
	
	Player(int x, int y, Map _map){
		map = _map;
		this.e = new Entity(Creature.PLAYER,x,y,map);
		map.entities.remove(this.e);
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
				s = 2;
			}else if(e == 2){
				k = KeyEvent.VK_2;
				s = 4;
			}
		}
	}
	
	public double getAttackDamage(){
		double baseDamage = e.STRENGTH;
		if(e.weapon!=null) baseDamage += e.weapon.type.baseDamage;
		return baseDamage/2 + (rng.nextDouble()*baseDamage);
	}
	
	public double getDefense(){
		double defense = 0;
		for(Item i: e.armour){
			if(i!=null){
				defense += i.type.baseDefense;
			}
		}
		return defense;
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
			target.HP -= ActionLibrary.round(getAttackDamage() * modifier, 1);
			Main.appendText(target.name+" 's HP: "+ ActionLibrary.round(target.HP,1));
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
	
	
	public void pickUp(char c){
		Main.appendText("You pick up the " + map.tileMap[e.y][e.x].inventory.inv.get(c).getDisplayName());
		map.tileMap[e.y][e.x].inventory.pickUp(c, inv);
		Main.takeTurn();
	}
	
	public void weild(Item i){
		if(e.weapon!=null) unweild(e.weapon);
		i.weilded = true;
		e.weapon = i;
		Main.appendText("You are now weilding your "+i.name+".");
	}
	
	public void unweild(Item i){
		if(i!=null) i.weilded = false;
		e.weapon = null;
		Main.appendText("You unweild your "+i.name+".");
	}
	
	public void takeOff(Item i){
		if(i!=null) i.worn = false;
		//TODO: remove armour
		Main.appendText("You take off your "+i.name+".");
	}
	
	public void putOn(Item i){
		if(i.isInClass(Item.Items.helmets)){
			if(e.helmet!=null) takeOff(e.helmet);
			e.helmet = i;
		}else if(i.isInClass(Item.Items.chestplates)){
			if(e.chestplate!=null) takeOff(e.chestplate);
			e.chestplate = i;
		}else if(i.isInClass(Item.Items.greaves)){
			if(e.greaves!=null) takeOff(e.greaves);
			e.greaves = i;
		}else if(i.isInClass(Item.Items.boots)){
			if(e.boots!=null) takeOff(e.boots);
			e.boots = i;
		}else if(i.isInClass(Item.Items.gloves)){
			if(e.gloves!=null) takeOff(e.gloves);
			e.gloves = i;
		}else if(i.isInClass(Item.Items.rings)){
			if(e.ring_left == null){
				e.ring_left = i;
			}else if(e.ring_right == null){
				e.ring_right = i;
			}else{
				// take off rings
				Main.appendText("you must take off one of your two rings first.");
			}
		}else if(i.isInClass(Item.Items.amulets)){
			if(e.amulet!=null) takeOff(e.amulet);
			e.amulet = i;
		}
		i.worn = true;
		Main.appendText("You put on your "+i.name+".");
	}
	
	public void quiver(Item i){
		if(e.quivered!=null) e.quivered.quivered = false;
		i.quivered = true;
		e.quivered = i;
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
