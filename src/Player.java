import java.awt.Point;
import java.awt.event.KeyEvent;
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
		boolean[][] vision = getFov().calculate(map.buildOpacityMap(), getX(), getY(), luminosity);
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
		Main.getView().appendText(String.format("You feel better - HP: %d", (int) getHP()));
		Main.getView().refreshText();
	}
	
	public void startRest(){
		if (getHP() < getHP_max()) {
			if (!enemiesNearby()) {
				if (!resting) {
					Main.getView().appendText("You start resting.");
					resting = true;
					rest();
				} else {
					Main.getView().appendText("You stop resting.");
					resting = false;
				}
			} else {
				Main.getView().appendText("You cannot rest now, there are enemies nearby.");
			}
		}else{
			Main.getView().appendText("You aren't tired.");			
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
				Main.getView().appendText("You hit the "+target.getName());
				
				// floor rounding except when below 1.
				int oppHP = (int) Math.max(0,target.getHP());
				Main.getView().appendText(String.format("%s's HP: %d", target.getName(), oppHP > 0 ? Math.max(oppHP, 1) : 0));				
			}else{
				Main.getView().appendText("You miss the "+target.getName());				
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
		Main.getView().appendText(String.format("It was a %s.", i.getTypeName()));
	}
	
	
	public void pickUp(char c, Inventory origInv){
		Main.getView().appendText("You pick up the " + origInv.getItem(c).getDisplayName());
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
}
