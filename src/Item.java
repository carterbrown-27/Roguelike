import org.json.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.logging.Logger;

public abstract class Item extends GameObject {
	private static JSONObject masterJSON;
	// private static boolean initialized;
	
	private JSONObject supertypeData;
	private JSONObject itemData;
	
	private ItemType superType;
	private String typeName;
	private String displayName;
	private String description;
	private int amount = 1;
	
	private char inventoryID = '?';

	Item(String id){
		typeName = id.toLowerCase();
		// TODO: improve
		initMasterJSON();
		// TODO: init SuperType
		for(String k: masterJSON.keySet()) {
			JSONObject j = masterJSON.getJSONObject(k);
			if(j.getJSONObject("list").has(typeName)) {
				this.supertypeData = j;
				System.out.println(typeName+" found under " + k);
				break;
			}
		}
		
		if(supertypeData == null) {
			System.out.printf("no supertype found. check spelling of item \"%s\".\n", id);
		}
		
		JSONObject itemList = supertypeData.getJSONObject("list");
		this.itemData = itemList.getJSONObject(typeName);
		
		JSONObject spriteIndex = itemData.optJSONObject("spriteIndex");
		if(spriteIndex == null) {
			spriteIndex = supertypeData.getJSONObject("generalProperties").getJSONObject("defaultSpriteIndex");
		}
		
		this.setSprite(GameObject.SpriteSource.DEFAULT, spriteIndex.getInt("x"), spriteIndex.getInt("y"));
		
		this.displayName = itemData.optString("name");
		this.description = itemData.optString("description");
	}
	
	public String toString() {
// 		TODO: (A) Implement properly
		//if(this.wielded) line+= " (weilded)";
		//if(this.quivered) line+= " (quivered)";
		//if(this.worn) line+= " (worn)" ;

		// String.format("%s - %s", this.inventoryID, this.getDisplayName())
		return String.format("%s - %s %s", this.getInventoryID(), this.getQuantityString(), this.getDisplayName());
	}
	
	public String getQuantityString() {
		String quantity;
		if(this.amount==1){
			if(StringHelper.isVowelStart(this.getDisplayName())){
				quantity = "an";
			}else{
				quantity = "a";
			}
		}else{
			quantity = String.valueOf(this.amount);
		}
		
		return quantity;
	}
	
	public String[] listPrompts() {
		 return new String[] {
			"(d)rop",
			"(r)eassign",
			"(ESC) exit"
		};
	}
	
	public static void initMasterJSON() {
		if(masterJSON == null) {
			try{
				JSONTokener in = new JSONTokener(new FileReader("DATA/Items.json"));
				masterJSON = new JSONObject(in);
			}catch(Exception e) {
				e.printStackTrace();
			};
		}
	}
	
	public static JSONObject getJSONbyID(String id) {
		// TODO (A) Implement
		JSONObject obj = new JSONObject();
		return obj;
	}
	
	// returns jsonobject if its found in the child of the item object, otherwise, return the default value.
	public Object getSpecValue(String key){
		Object specific = itemData.opt(key);
		if(specific != null) {
			return specific;
		}else {
			return supertypeData.opt(key);
		}
	}
	
	public String getTypeName() {
		return typeName;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public int getAmount() {
		return amount;
	}
	
	public void setAmount(int amt) {
		this.amount = amt;
	}

	public void changeAmount(int amt) {
		this.amount += amt;
		if(this.amount < 0) {
			this.amount = 0;
		}
	}
	
	public boolean isUnknown(){
		Object o = getSpecValue("unknown");
		if(o == null) {
			return false;
		}else{
			return (boolean) o;
		}
	}

	// TODO: (R) Fix
	public boolean isStackable(){
		Object o = getSpecValue("stackable");
		if(o == null) {
			return false;
		}else{
			return (boolean) o;
		}
	}
	
	public BufferedImage getSprite() {
		return super.getSprite();
	}
	
	public ItemType getSuperType() {
		return superType;
	}
	
	// TODO (I) move, refactor
//	public void quaff(Entity e, char c){
//		boolean discover = true;
//		if(discover && !Main.player.identifiedItems.containsKey(type)){
//			identify();
//		}
//		e.inv.removeItem(c);
//		Main.refreshText();
//		Main.takeTurn();
//	}

	// TODO (I) move, refactor
//	public void read(Entity e, char c){                                
//		if(type.supertype.equals(Items.Item_Supertype.SCROLL)){
//			boolean discover = true;
//			if(type.equals(Items.SCR_ID)){
//				e.inv.removeItem(c);
//				if(e.inv.containsUnidentified()){
//					Main.appendText("Identify what?");
//					e.inv.printContents(false);
//					Main.pickItem = true;
//					Main.identify = true;
//				}else{
//					Main.appendText("There is nothing in your pack to identify!");
//				}
//			}else if(type.equals(Items.SCR_TELE)){
//				Point p = e.map.randomEmptySpace();
//				e.x = p.x;
//				e.y = p.y;
//
//				if(!e.creature.hasAI){
//					Main.appendText("You spin quickly and then abruptly stop.\nYour surroundings look different.");
//				}else{
//					Main.appendText("The "+e.creature.NAME+" vanishes with a loud thunk.");
//				}
//				e.inv.removeItem(c);
//			}
//
//			if(discover && !Main.player.identifiedItems.containsKey(type)){
//				identify();
//			}
//
//			Main.refreshText();
//			Main.takeTurn();
//		}
//	}

//	public void eat(Entity e, char c){
//		if(type.supertype!=Items.Item_Supertype.FOOD) return;
//
//		String descriptor;
//		if(type.foodValue >= 10){
//			descriptor = "great!";
//		}else if(type.foodValue >= 5){
//			descriptor = "good.";
//		}else if(false){
//			descriptor = "off.";
//		}else{
//			descriptor = "fine.";
//		}
//
//		e.SAT += type.foodValue; // xTODO: cap
//		e.inv.removeItem(c);
//
//		Main.appendText("That "+type.name+" tasted "+descriptor);
//		Main.refreshText();
//		Main.takeTurn();
//
//	}

	public String getDisplayName(){
		return displayName;
	}

	public static enum ItemType {
		WEAPON ("Weapons"),
		ARMOUR ("Armour"),
		POTION ("Potions"),
		SCROLL ("Scrolls"),
		MISSILE ("Missiles"),
		SPECIAL ("Special");
		
		String name;

		ItemType(String _name){
			this.name = _name;
		}
	}

	public static Item randomItem(int tier) {
		// TODO (A) Implement
		return Main.rng.nextBoolean() ? new Scroll("scroll of teleportation"): new Scroll("scroll of identification");
	}
	
	// TODO (T) Temp
	public static ItemType randomItemType(int level /*, LevelType type*/){

		// pick any but a special item.
		int r;
		do{
			r = Main.rng.nextInt(ItemType.values().length);
		} while (ItemType.values()[r].equals(ItemType.SPECIAL));

		return ItemType.values()[r];
	}
	
	public void drop(Entity from) {
		// TODO (A) Implement
	}
	
	public void setInventoryID(char c) {
		this.inventoryID = c;
	}

	public char getInventoryID() {
		return inventoryID;
	}
}