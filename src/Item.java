import org.json.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public abstract class Item extends GameObject {
	private static JSONObject masterJSON;
	// private static boolean initialized;
	
	private JSONObject supertypeData;
	private JSONObject itemData;
	
	private ItemType superType;
	private String typeName;
	private String displayName = "<name>";
	private String description = "<description>";
	private int amount = 1;
	
	private LinkedHashMap<Character,String> actions = new LinkedHashMap<>();
	
	private char inventoryID = '?';

	Item(String id){
		typeName = id.toLowerCase();

		// TODO: improve
		initMasterJSON();
		initBasicPrompts();
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
	
	public boolean actionsContains(char c) {
		return actions.containsKey(c);
	}
	
	public void initBasicPrompts() {
		// reverse order.
		addPrompt('A', "re(a)ssign");
		addPrompt('D', "(d)rop");
		// ESC is handled differently, see main.		
	}
	
	public List<String> listPrompts() {
		ArrayList<String> result = new ArrayList<>();
		actions.forEach((c,t) -> result.add(c+ " - " + t));
		Collections.reverse(result);
		result.add("ESC - exit");
		return result;
	}
	
	public void addPrompt(char c, String s) {
		actions.put(c,s);
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

	public String getDisplayName(){
		return displayName;
	}
	
	public static <E extends Item> Item createItemByID(Class<E> _class, String id) {
		try {
			return (Item) _class.getDeclaredConstructor(String.class).newInstance(id);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	// TODO (R) Review, is this good practice? resolve unchecked errors.
	private static final Class[] itemClasses = {Weapon.class,Armour.class,Potion.class,Scroll.class,Missile.class,Item.class};
	private static final String[] itemTypeNames = {"Weapons","Armour","Potions","Scrolls","Missiles","Special"};

	public static enum ItemType {
		WEAPON (0),
		ARMOUR (1),
		POTION (2),
		SCROLL (3),
		MISSILE (4),
		SPECIAL (5);
		
		String name;
		Class<? extends Item> _class;

		ItemType(int n){
			this.name = itemTypeNames[n];
			this._class = itemClasses[n];
		}
	}

	public static Item randomItem(int tierMean) {
		// TODO (A) Implement
		// TODO (+) More Sophisticated Random Calculations.
		float plusMinus = 1/3f * tierMean;
		int tier = (int) ((Main.rng.nextFloat()*2*plusMinus) + tierMean - plusMinus);
		
		ItemType type = randomItemType(0);
		
		List<String> ids = getAllItemIDs(type, tier);
		String id = ids.get(Main.rng.nextInt(ids.size()));
		
		return createItemByID(type._class,id);
	}
	
	
	public static List<String> getAllItemIDs(ItemType type){
		JSONObject itemListObj = masterJSON.getJSONObject(type.name).getJSONObject("list");
		return new ArrayList<String>(itemListObj.keySet());
	}
	
	public static List<String> getAllItemIDs(ItemType type, int tier){
		tier = Math.max(tier, 1);
		
		List<String> items = getAllItemIDs(type);
		List<String> result = new ArrayList<>();
		JSONObject itemListObj = masterJSON.getJSONObject(type.name).getJSONObject("list");
		
		// decrease to 0.
		do {
			for(String id: items) {
				if(itemListObj.getJSONObject(id).getInt("tier") == tier) {
					result.add(id);
				}
			}
			tier--;
		} while(result.isEmpty() && tier > 0);
		
		// then go up
		while(result.isEmpty() && tier <= 5) {
			for(String id: items) {
				if(itemListObj.getJSONObject(id).getInt("tier") == tier) {
					result.add(id);
				}
			}
			tier++;
		}
		
		return result;
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
}