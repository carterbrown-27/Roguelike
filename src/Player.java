import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.*;

public class Player extends Creature {
	public int viewDis = 4;
	public int luminosity = 3;
	
	public Ability ability = Ability.BASIC;

	public ActionLibrary lib;	
	public boolean resting = false;
	
	private ArrayList<Direction> dirs = new ArrayList<>();
	
	// String = item.getDisplayName();
	private Set<String> identifiedItems = new HashSet<>();
	
	Player(int x, int y, Map _map){
		super("player", new Point(x,y),_map);
		map = _map;
		
		// TODO (X) Remove
		map.entities.remove(this);
		
		map.player = this;
		lib = new ActionLibrary(this);
	}
	
	// TODO: consider moving some of this data
	public enum Ability{
		BASIC (0),
		SLASH (1),
		LUNGE (2);
		/* TODO (R) Review: PARRY
		 * if you would be hit by a non-magic attack instead take no damage
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
		this.ability = a;
	}
	
	public void error(){
		System.out.println("Action not possible.");
	}
	
	public void act_adj(Direction dir){
		// TODO (F) Refactor, Switch
		if(ability == Ability.BASIC){
			basic(dir);
		}else if(ability == Ability.SLASH){
			if(melee(getTarget(dir),1.5)){
				changeSP(-Ability.SLASH.s);
				Main.takeTurn();
				deselect();
			}
		}else if(ability == Ability.LUNGE){
			dirs.add(dir);
			if(dirs.size()==2){
				Point start = getPos();
				if(lunge(dirs.get(0),dirs.get(1))){
					changeSP(-Ability.LUNGE.s);
					System.out.println(getSP());
					Main.takeTurn();
					deselect();
				}else{
					setPos(start);
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
			if(melee(getTarget(dir),1)){
				Main.takeTurn();
			}else{

			}
		}else{
			Main.takeTurn();
		}
	}
	
	public boolean enemiesNearby(){
		boolean[][] vision = fov.calculate(map.buildOpacityMap(), getX(), getY(), luminosity);
		for(Entity e: map.entities){
			if(e instanceof Creature) {
				Creature c = (Creature) e;
				// check closeness
				if(vision[c.getY()][c.getX()]){
					return true;
				}	
			}
		}
		return false;
	}
	
	public void rest(){
		while(!enemiesNearby() && getHP() < getHP_max()){
			super.changeHP(+1);
			Main.takeTurn();
		}
		
		resting = false;
		Main.view.appendText(String.format("You feel better - HP: %d", (int) getHP()));
		Main.view.refreshText();
	}
	
	public void startRest(){
		if (getHP() < getHP_max()) {
			if (!enemiesNearby()) {
				if (!resting) {
					Main.view.appendText("You start resting.");
					resting = true;
					rest();
				} else {
					Main.view.appendText("You stop resting.");
					resting = false;
				}
			} else {
				Main.view.appendText("You cannot rest now, there are enemies nearby.");
			}
		}else{
			Main.view.appendText("You aren't tired.");			
		}
	}
	
	public boolean lunge(Direction movedir, Direction attackdir){
		return (move(movedir) && melee(getTarget(attackdir),0.5));
	}
	
	public boolean melee(Creature target, double modifier){
		if(target!=null){
			// System.out.println("player swing");
			boolean hit = lib.melee(target, modifier);
			if(hit){
				Main.view.appendText("You hit the "+target.getName());
				
				// floor rounding except when below 1.
				int oppHP = (int) Math.max(0,target.getHP());
				Main.view.appendText(String.format("%s's HP: %d", target.getName(), oppHP > 0 ? Math.max(oppHP, 1) : 0));				
			}else{
				Main.view.appendText("You miss the "+target.getName());				
			}
			return true;
		}
		return false;
	}
	
	public Creature getTarget(Direction dir){
		for(Entity e: map.entities){
			if(e instanceof Creature) {
				Creature c = (Creature) e;
				if( Direction.translate(getPos(), dir).equals(c.getPos()) ){
					return c;
				}
			}
		}
		return null;
	}
	
	public void identify(Item i) {
		identifiedItems.add(i.getTypeName());
		Main.view.appendText("It was a "+i.getTypeName()+".");
	}
	
	
	public void pickUp(char c, Inventory origInv){
		Main.view.appendText("You pick up the " + origInv.getItem(c).getDisplayName());
		origInv.pickUp(c,this);
		Main.takeTurn();
	}
	
	// TODO (R) Review
	public <T extends Item & Equippable> void equip(T item){
		// temp
		item.equip(this);
	}
	
	public <T extends Item & Equippable> void unequip(T item){
		item.unequip(this);
	}
	
//	public void weild(Weapon i){
//		if(e.weapon!=null) unweild(e.weapon);
//		i.equipped = true;
//		e.weapon = i;
//		Main.view.appendText("You are now weilding your "+i.name+".");
//	}
//	
//	public void unweild(Weapon i){
//		if(i!=null) i.equipped = false;
//		e.weapon = null;
//		Main.view.appendText("You unweild your "+i.name+".");
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
//		Main.view.appendText("You quiver your "+i.name+"(s).");
//	}
//	
//	public void unquiver(Missile i){
//		if(i!=null){
//			i.equipped = false;
//		}else{
//			return;
//		}
//		e.quivered = null;
//		Main.view.appendText("You put away your "+i.name+"(s).");
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
//		Main.view.appendText("You take off your "+i.name+".");
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
//				Main.view.appendText("you must take off one of your two rings first.");
//			}
//		}else if(i.isInClass(Item.Items.amulets)){
//			if(e.amulet!=null) takeOff(e.amulet);
//			e.amulet = i;
//		}
//		i.worn = true;
//		Main.view.appendText("You put on your "+i.name+".");
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
