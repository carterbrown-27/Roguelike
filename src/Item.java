import java.awt.Point;
// TODO: switch from json-simple to java-json.
import org.json.simple.*;
import org.json.simple.parser.JSONParser;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.imageio.ImageIO;


public class Item{
	
	private static BufferedImage potionImages;
	private static BufferedImage sourcedItems;
	private static JSONObject masterJSON;
	
	private JSONObject supertypeData;
	private JSONObject itemData;
	
	private ItemType superType;
	private BufferedImage sprite;
	private String typeName;
	private String displayName;
	private String description;
	
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
		
		this.sprite = subImage((int) spriteIndex.get("x"), (int) spriteIndex.get("y"));
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

	public static BufferedImage subImage(int x, int y){
		if(sourcedItems==null){
			try {
				sourcedItems = ImageIO.read(new File("imgs/sourcedItems.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sourcedItems.getSubimage(x*24+x, y*24+y, 24, 24);
	}

	public static BufferedImage subImagePotion(int x, int y){
		if(potionImages==null){
			try {
				potionImages = ImageIO.read(new File("imgs/potions.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return potionImages.getSubimage(x*24+x, y*24+y, 24, 24);
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
	
	public boolean isUnknown(){
		return (boolean) getSpecValue("unknown");
	}

	public boolean isStackable(){
		return (boolean) getSpecValue("stackable");
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

	public void identify(){
		Main.player.identifiedItems.add(typeName);
		Main.appendText("It was a "+typeName+".");
	}

	public String getDisplayName(){
		if(!isUnknown() || Main.player.identifiedItems.contains(typeName)){
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

	public static enum potionColours {
		RED		 		(0),
		ORANGE 		 	(1),
		GREEN	 		(2),
		BLUE 	 		(3),
		VIOLET 		 	(4),
		PINK	 		(5),
		MAHOGANY 	 	(6),
		AQUAMARINE		(7),
		GOLDEN 			(8),
		SILVER 		 	(9),
		CHARCOAL 	 	(10),
		BROWN  		 	(11);

		potionColours(int n){
			colour = colours[n];
			image = images[n];
		}

		public String getName(int i){
			return colours[i];
		}
		public BufferedImage getImage(int i){
			return images[i];
		}

		public String colour;
		public BufferedImage image;

		public String[] colours = {"red","orange","green","blue","violet","pink",
				"mahogany","aquamarine","golden","silver","charcoal","brown"};

		public BufferedImage[] images = {subImagePotion(0,0),subImagePotion(0,1),subImagePotion(2,0),subImagePotion(1,0),subImagePotion(3,0),subImagePotion(4,1),
				subImagePotion(5,1), subImagePotion(2,1),subImagePotion(4,0),subImagePotion(1,1),subImagePotion(5,0),subImagePotion(3,1)};
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