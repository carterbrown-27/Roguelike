import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

public class Creature extends Entity {

	// fields
	private double HP;
	private double HP_max = 1; // 6 dmg = player with dagger
	private double SP;
	private double EV = 1.0; // MAX = 1.5 percent, 1.0 = 50/50 to dodge attack of same acc
	private double strength = 1;
	private double SP_max = 1;
	private double SP_regen = 1;
	private double speed = 1.0;
	private double satiation = 10; // TEMP
	
	private ArmourSet armourSet = new ArmourSet();
	
	private AI ai;
	
	@Deprecated
	private boolean hasAI = true;
	
	private HashMap<Status,Integer> statuses = new HashMap<Status,Integer>();
	
	public Weapon weapon;
	public Missile quivered;	
	
	// TODO: create data struct. - potentially extend Status (attribute)
	private boolean flying = false;
	private boolean amphibious = false;
	
	// constructors
	Creature(String id, Point p){
		super(id,p);
	}
	
	// methods
	
	@Deprecated
	private void createRat(){
		// TODO: edit sprites
		try {
			setSprite(ImageIO.read(new File("imgs/rat.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void upkeep(){
		satiation = Math.max(satiation-0.01,0);
		for(Status s: getStatuses().keySet()){
			if(s.upkeep){
				// TODO: move this data elsewhere.
				if(s.equals(Status.RESTING)){
					HP += 0.5;
				}else if(s.equals(Status.POISONED)){
					HP--;
				}
				// TODO: add static regen
			}
			getStatuses().replace(s, getStatuses().get(s)-1);
			if(getStatuses().get(s) <= 0){
				removeStatus(s);
			}
		}
	}
	
	public void endStep() {
		// recover HP, SP
		// hunger
		// etc.
	}
	
	public boolean awakeCheck(){
		// TODO: upgrade
		if(awake) return true;
		if(ai == null) return false;
		// TODO add creature viewDis
		boolean[][] vision = fov.calculate(map.buildOpacityMap(), getX(), getY(), Main.player.Luminosity); 
		if(vision[Main.player.getY()][Main.player.getX()]){
			awake = true;
			return true;
		}
		return false;
	}
	
	public turnEnding takeTurn(){
		waiting = false;
		if(ai == null){
			return turnEnding.NOTACREATURE; // not a creature
		}
		if(awakeCheck()){
			upkeep();

			if (HP<0.05) {
				Main.appendText("You kill the " + super.getName() + ".");
				return turnEnding.DEAD; // dead
			}
			if(!ai.takeTurn()){
				waiting = true;
				return turnEnding.WAITING;
			}
		}
		return turnEnding.NORMAL; // not dead
	}
	
	// TODO: instead of the branching, use polymorphism (Creature < Player)
	public void addStatus(Status s){
		if(!getStatuses().containsKey(s)){
			toggle(s, true);
			if(ai!=null){
				Main.appendText(String.format("The %s is %s!", getName(), s.name));
			}else{
				Main.appendText(String.format("You are %s!", s.name));
			}
			getStatuses().put(s, (int) ActionLibrary.round(s.baseDuration*2/3*Main.rng.nextDouble() + s.baseDuration*2/3,0));
		}else{
			getStatuses().replace(s, getStatuses().get(s) + (int) ActionLibrary.round(s.baseDuration*2/3*Main.rng.nextDouble() + s.baseDuration*2/3,0));
		}
	}

	public void removeStatus(Status s){
		toggle(s, false);
		if(ai!=null){
			Main.appendText("The "+getName()+" is no longer "+s.name+".");
		}else{
			Main.appendText("You are no longer "+s.name+".");
		}
		getStatuses().remove(s);
	}

	public void toggle(Status s, boolean start){
		int mod = 1;
		if(!start) mod = -1;
		if(s.equals(Status.MIGHTY)){
			strength += Math.max(5,strength/2)*mod;
		}else if(s.equals(Status.FLIGHT)){
			flying = start;
		}
	}
	
	// GETTERS, SETTERS, MUTATORS
	
	@Deprecated
	public boolean isFlying() {
		return flying;
	}
	
	@Deprecated
	public boolean isAmphibious() {
		return amphibious;
	}
	
	public double getHP() {
		return HP;
	}
	
	public void changeHP(double delta) {
		setHP(getHP() + delta);
	}
	
	public void setHP(double value) {
		HP = value;
		HP = ActionLibrary.round(HP, 1);
		
		if(HP <= 0) {
			// die
		}else if(HP > getHP_max()) {
			HP = getHP_max();
		}
	}
	
	public double getSP() {
		return SP;
	}
	
	public void changeSP(double delta) {
		setSP(getSP() + delta);
	}
	
	public void setSP(double value) {
		SP = value;
		SP = ActionLibrary.round(SP, 1);
		SP = Math.min(SP_max, SP);
	}
	
	public double getEV() {
		return EV;
	}
	
	public double getStrength() {
		return strength;
	}
	
	// TODO: remove these, only allow immutable gets.
	public HashMap<Status,Integer> getStatuses() {
		return statuses;
	}
	
	public ArmourSet getArmourSet() {
		return armourSet;
	}

	public double getHP_max() {
		return HP_max;
	}

	
	public double getDefence() {
		return armourSet.getDefence();
	}
	
	public double getSatiation() {
		return satiation;
	}
}