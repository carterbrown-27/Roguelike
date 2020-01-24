import java.awt.Point;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.imageio.ImageIO;


public class Item{

	/* fields */
	/** TODO turn this into json **/
	/* An item refers to a stack of items of the same exact type
	 * stackable types: MISSILE SCROLL POTION FOOD
	 * multiple item stacks can occupy the same spot on the grid
	 */

	public Items type;
	public char inventoryID;

	private static BufferedImage potionImages;
	private static BufferedImage sourcedItems;
	private static JSONObject masterJSON;

	public BufferedImage sprite;
	private JSONObject ObjectData;

	Item(String id){
		// TODO: improve
		if(masterJSON == null) {
			try{
				masterJSON = (JSONObject) (new JSONParser().parse(new FileReader("DATA/Items.json")));
			}catch(Exception e) {
				e.printStackTrace();
			};
		}
		this.ObjectData = (JSONObject) masterJSON.get(id);
		this.sprite = type.sprite;
	}

	public String toString() {
		String quantity;
		if(this.amount==1){
			if(Inventory.isVowelStart(this.getDisplayName())){
				quantity = "an";
			}else{
				quantity = "a";
			}
		}else{
			quantity = String.valueOf(this.amount);
		}

		String line = String.format("%s - %s %s", this.inventoryID, quantity, this.getDisplayName());

		//if(this.wielded) line+= " (weilded)";
		//if(this.quivered) line+= " (quivered)";
		//if(this.worn) line+= " (worn)" ;

		return line;
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

	public boolean isUnknown(){
		return type.isUnknown();
	}

	public boolean isStackable(){
		return type.supertype.isStackable;
	}

	public boolean isInClass(Items[] cl){
		for(Items t: cl){
			if(type.equals(t)){
				return true;
			}
		}
		return false;
	}

	// TODO: move, refactor
	public void quaff(Entity e, char c){
		if(type.supertype.equals(Items.Item_Supertype.POTION)){
			boolean discover = false;
			if(type.equals(Items.POT_HEAL)){
				e.HP = e.creature.HP_MAX; // TODO: replace creature.hp_max with dynamic max
				if(!e.creature.hasAI){
					Main.appendText("You feel rejuvenated.");
				}else{
					Main.appendText("The "+e.creature.NAME+" appears to be in much better health.");
				}
				discover = true;
			}else if(type.equals(Items.POT_MIGHT)){
				if(!e.creature.hasAI){
					Main.appendText("Your body shudders with newfound strength!");
					e.addStatus(Entity.Status.MIGHTY);
					Main.refreshStats();
				}else{
					e.addStatus(Entity.Status.MIGHTY);
					Main.appendText("The "+e.creature.NAME+"'s body shudders with newfound strength.");
				}
				discover = true;
			}else if(type.equals(Items.POT_FLIGHT)){
				if(!e.creature.hasAI){
					Main.appendText("You feel your feet leave the ground!");
					e.addStatus(Entity.Status.FLIGHT);
					Main.refreshStats();
				}else{
					e.addStatus(Entity.Status.FLIGHT);
					Main.appendText("The "+e.creature.NAME+"raises into the air.");
				}
			}
			if(discover && !Main.player.identifiedItems.containsKey(type)){
				identify();
			}
			e.inv.removeItem(c);
			Main.refreshText();
			Main.takeTurn();
		}
	}

	// TODO: move, refactor
	public void read(Entity e, char c){
		if(type.supertype.equals(Items.Item_Supertype.SCROLL)){
			boolean discover = true;
			if(type.equals(Items.SCR_ID)){
				if(!e.creature.hasAI){
					Main.appendText("Your pack glows blue and you are\noverwhelmed with a fleeting knowledge.");
					if(!Main.player.identifiedItems.containsKey(type)){
						identify();
					}
					discover = false;
					e.inv.removeItem(c);
					if(e.inv.containsUnidentified()){
						Main.appendText("Identify what?");
						e.inv.printContents(false);
						Main.pickItem = true;
						Main.identify = true;
					}else{
						Main.appendText("There is nothing in your pack to identify!");
					}
				}else{
					Main.appendText("");
				}
			}else if(type.equals(Items.SCR_TELE)){
				Point p = e.map.randomEmptySpace();
				e.x = p.x;
				e.y = p.y;

				if(!e.creature.hasAI){
					Main.appendText("You spin quickly and then abruptly stop.\nYour surroundings look different.");
				}else{
					Main.appendText("The "+e.creature.NAME+" vanishes with a loud thunk.");
				}
				e.inv.removeItem(c);
			}

			if(discover && !Main.player.identifiedItems.containsKey(type)){
				identify();
			}

			Main.refreshText();
			Main.takeTurn();
		}
	}

	public void eat(Entity e, char c){
		if(type.supertype!=Items.Item_Supertype.FOOD) return;

		String descriptor;
		if(type.foodValue >= 10){
			descriptor = "great!";
		}else if(type.foodValue >= 5){
			descriptor = "good.";
		}else if(false){
			descriptor = "off.";
		}else{
			descriptor = "fine.";
		}

		e.SAT += type.foodValue; // TODO: cap
		e.inv.removeItem(c);

		Main.appendText("That "+type.name+" tasted "+descriptor);
		Main.refreshText();
		Main.takeTurn();

	}

	public void identify(){
		Main.player.identifiedItems.put(type, type.name);
		Main.appendText("It was a "+type.name+".");
	}

	public String getDisplayName(){
		if(!isUnknown() || Main.player.identifiedItems.containsKey(type)){
			return name;
		}else if(type.supertype.equals(Items.Item_Supertype.SCROLL) || type.supertype.equals(Items.Item_Supertype.POTION)){
			return Main.randomNames.get(type);
		}
		return "";
	}
	
	public static enum ItemTypes {
		WEAPON ("Weapons"),
		ARMOUR ("Armour"),
		POTION ("Potions"),
		SCROLL ("Scrolls"),
		MISSILE ("Missiles"),
		SPECIAL ("Special");
		
		ItemTypes(String str){
			
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
	public static Items randomItemType(int level /*, LevelType type*/){

		// pick any but a special item.
		int r;
		do{
			r = Main.rng.nextInt(Items.values().length);
		} while (Items.values()[r].supertype.equals(Item_Supertype.SPECIAL));

		return Items.values()[r];
	}
}