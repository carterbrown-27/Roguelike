import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.json.JSONObject;
import org.json.JSONTokener;

public class Creature extends Entity {

	private static JSONObject masterJSON;
	
	private JSONObject creatureData;
	
	// fields

	private double HP_regen = 1;
	
	private double SP;
	private double SP_max = 1;
	private double SP_regen = 1;
	
	private double EV = 1.0; // MAX = 1.5 percent, 1.0 = 50/50 to dodge attack of same acc
	private double strength = 1;
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
	
	// TODO (A) Implement, read data from JSON.
	// constructors
	Creature(String id, Point p, Map map){
		super(id,p,map);
		initMasterJSON();
		
		// (R) Review, move some of this to entity potentially
		creatureData = masterJSON.getJSONObject(id);
		this.setName(creatureData.getString("name"));
		JSONObject spriteIndex = creatureData.getJSONObject("spriteIndex");
		super.setSprite(GameObject.SpriteSource.DEFAULT, spriteIndex.getInt("x"), spriteIndex.getInt("y"));
		
		// creature specific
		
		this.strength = creatureData.getDouble("strength");
		this.EV = creatureData.getDouble("evasiveness");
		this.speed = creatureData.getDouble("speed");
		
		JSONObject HP_Data = creatureData.getJSONObject("HP");
		super.setHP_max(HP_Data.getDouble("max"));
		super.setHP(super.getHP_max());
		this.HP_regen = HP_Data.getDouble("regenRate");
		
		JSONObject SP_Data = creatureData.getJSONObject("SP");
		this.SP_max = SP_Data.getDouble("max");
		this.SP = SP_max;
		this.SP_regen = SP_Data.getDouble("regenRate");

	}
	
	Creature(int tier, Point p, Map map){
		this(pickRandomType(tier),p,map);
	}
	// methods
	
	public static void initMasterJSON() {
		if(masterJSON == null) {
			try{
				JSONTokener in = new JSONTokener(new FileReader("DATA/Creatures.json"));
				masterJSON = new JSONObject(in);
			}catch(Exception e) {
				e.printStackTrace();
			};
		}
	}
	
	// TODO (V) Add rat to DEFAULT sprite sheet.
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
				// TODO (M) move this data elsewhere.
				if(s.equals(Status.RESTING)){
					changeHP(+0.5);
				}else if(s.equals(Status.POISONED)){
					changeHP(-1);
				}
				// TODO (R) Review: add static regen
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
		// TODO (+) add creature viewDis
		boolean[][] vision = fov.calculate(map.buildOpacityMap(), getX(), getY(), Main.player.Luminosity); 
		if(vision[Main.player.getY()][Main.player.getX()]){
			awake = true;
			return true;
		}
		return false;
	}
	
	// TODO (F) Fix
	public turnEnding takeTurn(){
		//waiting = false;
		if(ai == null){
			return turnEnding.NOTACREATURE; // not a creature
		}
		if(awakeCheck()){
			upkeep();
			
			// TODO (R) Refactor
			if (getHP()<0.05) {
				Main.appendText("You kill the " + super.getName() + ".");
				return turnEnding.DEAD; // dead
			}
			if(!ai.takeTurn()){
				// waiting = true;
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
	
	public static String pickRandomType(int tier) {
		// TODO (A) Implement
		return Main.rng.nextBoolean() ? "rat" : "bat";
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

	public double getSP() {
		return SP;
	}
	public double getSP_max() {
		return SP_max;
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
	
	// TODO (X) remove these, only allow immutable gets.
	public HashMap<Status,Integer> getStatuses() {
		return statuses;
	}
	
	public ArmourSet getArmourSet() {
		return armourSet;
	}
	
	public double getDefence() {
		return armourSet.getDefence();
	}
	
	public double getSAT() {
		return satiation;
	}
	
	public void changeSAT(double amt) {
		satiation += amt;
	}
}