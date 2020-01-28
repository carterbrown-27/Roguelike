import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.*;

public class Player extends Creature {
	public BufferedImage img;
	public Map map;
	
	public int ViewDistance = 5;
	public int Luminosity = 4;
	
	public Ability ability = Ability.BASIC;
	public int step = 0;

	public ActionLibrary lib;
	
	public boolean resting = false;
	
	private ArrayList<Direction> dirs = new ArrayList<>();
	
	// String = item.getDisplayName();
	private Set<String> identifiedItems = new HashSet<>();
	
	Player(int x, int y, Map _map){
		super();
		map = _map;
		map.entities.remove(this.getName());
		map.player = this;
		lib = new ActionLibrary(this);
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
	
	public void act_adj(Direction dir){
		if(ability.equals(Ability.BASIC)){
			basic(dir);
		}else if(ability.equals(Ability.SLASH)){
			if(melee(dir,1.5)){
				changeSP(-Ability.SLASH.s);
				Main.takeTurn();
				deselect();
			}
		}else if(ability.equals(Ability.LUNGE)){
			dirs.add(dir);
			if(dirs.size()==2){
				Point start = getPos();
				if(lunge(dirs.get(0),dirs.get(1))){
					changeSP(-Ability.LUNGE.s);
					System.out.println(getSP());
					Main.takeTurn();
					deselect();
				}else{
					x = start.x;
					y = start.y;
					deselect();
					error();
				}
			}
		}
	}
	
	public boolean isItemIdentified(Item i) {
		return identifiedItems.contains(i.getTypeName());
	}
	
	
	public void basic(Direction dir){
		if(!move(dir)){
			if(melee(dir,1)){
				Main.takeTurn();
			}else{

			}
		}else{
			Main.takeTurn();
		}
	}
	
	public boolean enemiesNearby(){
		// TODO: creature map
		for(Entity e: map.entities){
			if(e.awakeCheck()){
				return true;
			}
		}
		return false;
	}
	
	public void rest(){
		while(!enemiesNearby()){
			super.changeHP(+1);
			Main.takeTurn();
		}
		
		resting = false;
		Main.appendText("You feel better - HP: "+getHP());
		Main.refreshText();
	}
	
	public void startRest(){
		if (getHP() < getHP_max()) {
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
	
	public boolean lunge(Direction movedir, Direction attackdir){
		return (move(movedir) && melee(attackdir,0.5));
	}
	
	public boolean melee(Creature target, double modifier){
		if(target!=null){
			// System.out.println("player swing");
			boolean hit = lib.melee(target, modifier);
			if(hit){
				Main.appendText("You hit the "+target.getName());								
				Main.appendText(target.getName()+" 's HP: "+target.getHP());				
			}else{
				Main.appendText("You miss the "+target.getName());				
			}
			return true;
		}
		return false;
	}
	
	public Entity targetAdjacent(Direction dir){
		for(Entity i: map.entities){
			if(isAdjacentTo(i.getPos()) && getDir(i.getPos()) ==  dir){
				return i;
			}
		}
		return null;
	}
	
	public void identify(Item i) {
		identifiedItems.add(i.getTypeName());
		Main.appendText("It was a "+i.getTypeName()+".");
	}
	
	
	public void pickUp(char c, Inventory origInv){
		Main.appendText("You pick up the " + origInv.getItem(c).getDisplayName());
		origInv.pickUp(c,this);
		Main.takeTurn();
	}
	
	// TODO: implement
	public <T extends Equippable> void equip(T item){
		// temp
		item.equip(this);
	}
	
	public <T extends Equippable> void unequip(T item){
		item.unequip(this);
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
