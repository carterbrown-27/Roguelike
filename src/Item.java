// TODO: switch from json-simple to java-json.
import org.json.simple.*;
import org.json.simple.parser.JSONParser;

import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;


public abstract class Item extends GameObject {
	private static Scroll[] scrolls;
	private static Potion[] potions;
	
	private static JSONObject masterJSON;
	private static boolean initialized;
	
	private JSONObject supertypeData;
	private JSONObject itemData;
	
	private ItemType superType;
	private String typeName;
	private String displayName;
	private String description;
	private int amount;
	
	private char inventoryID = '?';

	Item(String id){
		// TODO: improve
		if(masterJSON == null) {
			try{
				masterJSON = (JSONObject) (new JSONParser().parse(new FileReader("DATA/Items.json")));
			}catch(Exception e) {
				e.printStackTrace();
			};
		}
		
		// TODO: init SuperType.
		for(Object o: masterJSON.keySet()) {
			JSONObject j = (JSONObject) masterJSON.get(o);
			if(j.values().contains(id)) {
				this.supertypeData = j;
				
				break;
			}
		}
		
		this.itemData = (JSONObject) supertypeData.get(id);
		
		JSONObject spriteIndex = (JSONObject) getSpecValue("spriteIndex");
		
		this.setSprite((int) spriteIndex.get("x"), (int) spriteIndex.get("y"));
		this.displayName = (String) itemData.getOrDefault("name","<none>");
		this.description = (String) itemData.getOrDefault("description","<none>");
	}
	
	public String toString() {
//		String quantity;
//		if(this.amount==1){
//			if(Inventory.isVowelStart(this.getDisplayName())){
//				quantity = "an";
//			}else{
//				quantity = "a";
//			}
//		}else{
//			quantity = String.valueOf(this.amount);
//		}

		// String line = String.format("%s - %s %s", this.inventoryID, quantity, this.getDisplayName());

		//if(this.wielded) line+= " (weilded)";
		//if(this.quivered) line+= " (quivered)";
		//if(this.worn) line+= " (worn)" ;

		return String.format("%s - %s", this.inventoryID, this.getDisplayName());
	}
	
	public String[] listPrompts() {
		 return new String[] {
			"(d)rop",
			"(r)eassign",
			"(ESC) exit"
		};
	}

	public static JSONObject getJSONbyID(String id) {
		JSONObject obj = new JSONObject();
		return obj;
	}
	
	// returns jsonobject if its found in the child of the item object, otherwise, return the default value.
	public Object getSpecValue(String key){
		return itemData.getOrDefault(key, supertypeData.get(key));
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
		return (boolean) getSpecValue("unknown");
	}

	public boolean isStackable(){
		return (boolean) getSpecValue("stackable");
	}
	
	public BufferedImage getSprite() {
		return super.getSprite();
	}
	
	public ItemType getSuperType() {
		return superType;
	}
	
	public static Potion[] getPotions() {
		return potions;
	}
	
	public static Scroll[] getScrolls() {
		return scrolls;
	}


	// TODO: move, refactor
//	public void quaff(Entity e, char c){
//		boolean discover = true;
//		if(discover && !Main.player.identifiedItems.containsKey(type)){
//			identify();
//		}
//		e.inv.removeItem(c);
//		Main.refreshText();
//		Main.takeTurn();
//	}

	// TODO: move, refactor
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
//		e.SAT += type.foodValue; // TODO: cap
//		e.inv.removeItem(c);
//
//		Main.appendText("That "+type.name+" tasted "+descriptor);
//		Main.refreshText();
//		Main.takeTurn();
//
//	}
	
	public String getDisplayName(){
		if(!isUnknown() || Main.player.isItemIdentified(this)){
			return displayName;
		}else {
			return Main.randomNames.getOrDefault((typeName),"<random name not found>");
		}
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

	/** TEMPORARY **/
	public static ItemType randomItemType(int level /*, LevelType type*/){

		// pick any but a special item.
		int r;
		do{
			r = Main.rng.nextInt(ItemType.values().length);
		} while (ItemType.values()[r].equals(ItemType.SPECIAL));

		return ItemType.values()[r];
	}
}