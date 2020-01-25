import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.*;

// TODO: extends entity
public class Player {
	public Entity e;
	public BufferedImage img;
	public Map map;
	
	public int ViewDistance = 5;
	public int Luminosity = 4;
	
	public Ability ability = Ability.BASIC;
	public int step = 0;

	public ActionLibrary lib;
	
	public boolean resting = false;
	
	ArrayList<Integer> dirs = new ArrayList<Integer>();
	
	// String = item name (ie blue potion) <> Item.Items = item real name (ie potion of flight)
	// TODO: REFACTOR
	public Set<String> identifiedItems = new HashSet<String>();
	
	Player(int x, int y, Map _map){
		map = _map;
		this.e = new Entity(Creature.PLAYER,x,y,map);
		map.entities.remove(this.e);
		map.player = this.e;
		lib = new ActionLibrary(e);
	}
	
	public enum Ability{
		BASIC (0),
		SLASH (1),
		LUNGE (2);
		/* TODO: PARRY: if you would be hit by a non-magic attack instead take no damage
		 * and hit back for slash damage (guarantee hit) only works if the attack is under
		 * "x" damage. projectiles means no hit-back only block
		 * else: ?
		 * neat combat like piratey stuff
		*/
		
		public int k;
		public int s;
		public String name;
		
		Ability (int e){
			if(e == 0){
				k = KeyEvent.VK_ESCAPE;
				s = 0;
				name = "basic";
			}else if(e == 1){
				k = KeyEvent.VK_1;
				s = 3;
				name = "slash";
			}else if(e == 2){
				k = KeyEvent.VK_2;
				s = 3;
				name = "lunge";
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
	
	public void basic(int dir){
		if(!e.move(dir)){
			if(melee(dir,1)){
				Main.takeTurn();
			}else{

			}
		}else{
			Main.takeTurn();
		}
	}
	
	public boolean enemiesNearby(){
		for(Entity e: map.entities.values()){
			if(e.awakeCheck()){
				return true;
			}
		}
		return false;
	}
	
	public void rest(){
		while(!enemiesNearby() && e.HP < e.creature.HP_MAX){
			e.HP = Math.min(e.HP+1, e.creature.HP_MAX);
			Main.takeTurn();
		}
		resting = false;
		Main.appendText("You feel better - HP: "+e.HP);
		Main.refreshText();
	}
	
	public void startRest(){
		if (e.HP < e.creature.HP_MAX) {
			if (!enemiesNearby()) {
				if (!resting) {
					Main.appendText("You start resting.");
					resting = true;
					rest();
				} else {
					Main.appendText("You stop resting.");
					resting = false;
				}
			} else {
				Main.appendText("You cannot rest now, there are enemies nearby.");
			}
		}else{
			Main.appendText("You aren't tired.");			
		}
	}
	
	public boolean lunge(int movedir, int attackdir){
		return (e.move(movedir) && melee(attackdir,0.5));
	}
	
	public boolean melee(int dir, double modifier){
		Entity target =  targetAdjacent(dir);
		if(target!=null && target.SE==null){
			// System.out.println("player swing");
			boolean hit = lib.melee(target, modifier);
			if(hit){
				Main.appendText("You hit the "+target.creature.NAME);								
				Main.appendText(target.creature.NAME+" 's HP: "+ ActionLibrary.round(target.HP,1));				
			}else{
				Main.appendText("You miss the "+target.creature.NAME);				
			}
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
	
	
	public void pickUp(char c, Inventory origInv){
		Main.appendText("You pick up the " + origInv.inv.get(c).getDisplayName());
		origInv.pickUp(c,e);
		Main.takeTurn();
	}
	
	// TODO: implement
	public <T extends Equippable> void equip(T item){
		
	}
	
	public <T extends Equippable> void unequip(T item){
		
	}
	
//	public void weild(Weapon i){
//		if(e.weapon!=null) unweild(e.weapon);
//		i.equipped = true;
//		e.weapon = i;
//		Main.appendText("You are now weilding your "+i.name+".");
//	}
//	
//	public void unweild(Weapon i){
//		if(i!=null) i.equipped = false;
//		e.weapon = null;
//		Main.appendText("You unweild your "+i.name+".");
//	}
//	
//	public void quiver(Missile i){
//		if(i!=null){
//			unquiver(e.quivered);
//		}else{
//			return;
//		}
//		i.equipped = true;
//		e.quivered = i;
//		Main.appendText("You quiver your "+i.name+"(s).");
//	}
//	
//	public void unquiver(Missile i){
//		if(i!=null){
//			i.equipped = false;
//		}else{
//			return;
//		}
//		e.quivered = null;
//		Main.appendText("You put away your "+i.name+"(s).");
//	}
	
//	public void takeOff(Item i){
//		if(i==null) return;
//		i.worn = false;
//		if(e.helmet.equals(i)){
//			e.helmet = null;
//		}else if(e.chestplate.equals(i)){
//			e.chestplate = null;
//		}else if(e.greaves.equals(i)){
//			e.greaves = null;
//		}else if(e.boots.equals(i)){
//			e.boots = null;
//		}else if(e.gloves.equals(i)){
//			e.gloves = null;
//		}else if(e.ring_left.equals(i)){
//			e.ring_left = null;
//		}else if(e.ring_right.equals(i)){
//			e.ring_right = null;
//		}
//		Main.appendText("You take off your "+i.name+".");
//	}
	
//	public void putOn(Item i){
//		if(i.isInClass(Item.Items.helmets)){
//			if(e.helmet!=null) takeOff(e.helmet);
//			e.helmet = i;
//		}else if(i.isInClass(Item.Items.chestplates)){
//			if(e.chestplate!=null) takeOff(e.chestplate);
//			e.chestplate = i;
//		}else if(i.isInClass(Item.Items.greaves)){
//			if(e.greaves!=null) takeOff(e.greaves);
//			e.greaves = i;
//		}else if(i.isInClass(Item.Items.boots)){
//			if(e.boots!=null) takeOff(e.boots);
//			e.boots = i;
//		}else if(i.isInClass(Item.Items.gloves)){
//			if(e.gloves!=null) takeOff(e.gloves);
//			e.gloves = i;
//		}else if(i.isInClass(Item.Items.rings)){
//			if(e.ring_left == null){
//				e.ring_left = i;
//			}else if(e.ring_right == null){
//				e.ring_right = i;
//			}else{
//				// take off rings
//				Main.appendText("you must take off one of your two rings first.");
//			}
//		}else if(i.isInClass(Item.Items.amulets)){
//			if(e.amulet!=null) takeOff(e.amulet);
//			e.amulet = i;
//		}
//		i.worn = true;
//		Main.appendText("You put on your "+i.name+".");
//	}
	
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
