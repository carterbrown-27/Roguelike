import org.json.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

public abstract class Item extends GameObject {
	private static JSONObject masterJSON;
	// private static boolean initialized;
	
	private static final int MIN_TIER = 1;
	private static final int MAX_TIER = 10;
	
	private JSONObject supertypeData;
	private JSONObject itemData;
	
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
				// TODO (L) Log: System.out.println(typeName+" found under " + k);
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
	
	protected JSONObject getMasterJSON() {
		return masterJSON;
	}
	
	protected JSONObject getItemData() {
		return itemData;
	}
	
	public boolean actionsContains(char c) {
		return actions.containsKey(c);
	}
	
	public void initBasicPrompts() {
		// reverse order.
		addPrompt('a', "re(a)ssign");
		addPrompt('d', "(d)rop");
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
		return false;
	}

	public boolean isStackable(){
		return false;
	}
	
	public BufferedImage getSprite() {
		return super.getSprite();
	}

	public String getDisplayName(){
		return displayName;
	}
	
	// TODO (R) Review, is this good practice? resolve unchecked warnings.
	private static final Class[] itemClasses = {Weapon.class,Armour.class,Potion.class,Scroll.class,Missile.class,Food.class,Item.class};
	private static final String[] itemTypeNames = {"Weapons","Armour","Potions","Scrolls","Missiles","Food","Special"};

	public static enum ItemType {
		WEAPON (0),
		ARMOUR (1),
		POTION (2),
		SCROLL (3),
		MISSILE (4),
		FOOD (5),
		SPECIAL(6);
		
		String name;
		Class<? extends Item> _class;

		ItemType(int n){
			this.name = itemTypeNames[n];
			this._class = itemClasses[n];
		}
	}
	
	public static <E extends Item> Item createItemByID(Class<E> _class, String id) {
		try {
			return (Item) _class.getDeclaredConstructor(String.class).newInstance(id);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Item randomItem(int tierMean) {
		// TODO (A) Implement
		// TODO (+) More Sophisticated Random Calculations.
		float plusMinus = 1/3f * tierMean;
		int tier = Math.round((Main.getRng().nextFloat()*2*plusMinus) + tierMean - plusMinus);
		
		ItemType type = randomItemType(0);
		
		List<String> ids = getAllItemIDs(type, tier);
		String id = ids.get(Main.getRng().nextInt(ids.size()));
		
		return createItemByID(type._class,id);
	}
	
	
	public static List<String> getAllItemIDs(ItemType type){
		if(masterJSON == null) initMasterJSON();
 		JSONObject itemListObj = masterJSON.getJSONObject(type.name).getJSONObject("list");
		return new ArrayList<String>(itemListObj.keySet());
	}
	
	public static List<String> getAllItemIDs(ItemType type, int tier){
		tier = Math.max(tier, 1);
		
		List<String> items = getAllItemIDs(type);
		List<String> result = new ArrayList<>();
		
		if(masterJSON == null) initMasterJSON();
		JSONObject itemListObj = masterJSON.getJSONObject(type.name).getJSONObject("list");
		
		// decrease to 0.
		for(;result.isEmpty() && tier >= MIN_TIER; tier--) {
			for(String id: items) {
				if(itemListObj.getJSONObject(id).getInt("tier") == tier) {
					result.add(id);
				}
			}
		}
		
		// then go up
		for(;result.isEmpty() && tier <= MAX_TIER; tier++) {
			for(String id: items) {
				if(itemListObj.getJSONObject(id).getInt("tier") == tier) {
					result.add(id);
				}
			}
		}
		
		return result;
	}
	
	// TODO (T) Temp
	public static ItemType randomItemType(int level /*, LevelType type*/){

		// pick any but a special item.
		int r;
		do{
			r = Main.getRng().nextInt(ItemType.values().length);
		} while (ItemType.values()[r].equals(ItemType.SPECIAL));

		return ItemType.values()[r];
	}
	
	// TODO (F) Fix
	public void drop(Creature from) {
		Main.getView().appendText("You drop the "+this.getDisplayName());
		// TODO (R) Refactor
		from.getInv().deleteItem(this);
		from.map.addItemToSpace(this, from.getPos());
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