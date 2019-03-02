import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;


public class Item{

	/* fields */
	/**TODO turn this into json**/
	/* An item refers to a stack of items of the same exact type
	 * stackable types: MISSILE SCROLL POTION FOOD
	 * multiple item stacks can occupy the same spot on the grid
	 */


	Item(Items type, int amount, int floor){
		this.type = type;
		this.amount = amount;
		this.floorFoundOn = floor;

		this.name = type.name;
		this.sprite = type.sprite;
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
	
	// weapon
	public boolean weilded = false;

	// armour/rings/amulets
	public boolean worn = false;

	// projectiles
	public boolean quivered = false;

	public Items type;
	public int amount = 1;
	public String name;
	private static BufferedImage potionImages;
	private static BufferedImage sourcedItems;

	public BufferedImage sprite;
	public int floorFoundOn;

	boolean isStackable = true;

	
	// TODO: REPLACE STATS WITH TSV
	public enum Items {

		DAGGER		(0),
		HAND_AXE	(1),
		CLUB			(2),
		SPKD_CLUB (3),

		LTR_BOOTS	(10),

		DART			(20),
		
		SCR_ID		(30),
		SCR_TELE	(31),

		POT_HEAL	(40),
		POT_MIGHT	(41),
		POT_FLIGHT(42),

		BREAD			(50),
		
		SLVR_KEY	(60);
		

		Items(int itemNo){
			if(itemNo == 0){
				dagger();
			}else if(itemNo == 1){
				axe();
			}else if(itemNo == 2){
				club();
			}else if(itemNo == 3){
				spiked_club();
			}else if(itemNo == 10){
				leatherBoots();
			}else if(itemNo == 20){
				dart();
			}else if(itemNo == 30){
				scrollOfIdentify();
			}else if(itemNo == 31){
				scrollOfTeleportation();
			}else if(itemNo == 40){
				potionOfHealing();
			}else if(itemNo == 41){
				potionOfMight();
			}else if(itemNo == 42){
				potionOfFlight();
			}else if(itemNo == 50){
				bread();
			}else if(itemNo == 60){
				silverKey();
			}
		}
		

		/** item classes **/
		public static final Items[] scrolls = {SCR_ID,SCR_TELE};
		public static final Items[] potions = {POT_HEAL,POT_MIGHT,POT_FLIGHT};
		public static final Items[] keys = {SLVR_KEY};

		public static enum potionColours{
			RED		 		 (0),
			ORANGE 		 (1),
			GREEN	 		 (2),
			BLUE 	 		 (3),
			VIOLET 		 (4),
			PINK	 		 (5),
			MAHOGANY 	 (6),
			AQUAMARINE (7),
			GOLDEN 		 (8),
			SILVER 		 (9),
			CHARCOAL 	 (10),
			BROWN  		 (11);

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

		public static final Items[] helmets = {};
		public static final Items[] chestplates = {};
		public static final Items[] greaves = {};
		public static final Items[] boots = {LTR_BOOTS};
		public static final Items[] gloves = {};
		public static final Items[] rings = {};
		public static final Items[] amulets = {};

		public static enum Item_Supertype{

			WEAPON (0),
			ARMOUR (1),
			MISSILE(2),
			SCROLL (3),
			POTION (4),
			FOOD	 (5),
			SPECIAL(6); // not in player inventory ie (key, gold)

			boolean isStackable = true;
			boolean isUnknown = false;
			Item_Supertype(int i){
				if(i==0||i==1){
					isStackable = false;
				}
				if(i==3||i==4){
					isUnknown = true;
				}
			}
		}

		Item_Supertype supertype;

		String name;
		BufferedImage sprite;
		int tier;
		String description = "<description>";
		int commonStackSize = 1;

		// weapon/missile
		double baseDamage;
		double baseAccuracy;
		// armour
		double baseDefense;

		double weight; // at this strength, penalty is removed, like a log scale

		// food
		double foodValue;
		
		// TODO: item tiers & balanced generation
		// TODO: change item methods into json or other data storage method.
		
		/** TEMPORARY **/
		public static Items randomItemType(int level /*, LevelType type*/){
			
			// pick any but a special item.
			int r;
			do{
				r = Main.rng.nextInt(Items.values().length);
			} while (Items.values()[r].supertype.equals(Item_Supertype.SPECIAL));
			
			return Items.values()[r];
		}

		public boolean isUnknown(){
			return supertype.isUnknown;
		}

		public void dagger(){
			supertype = Item_Supertype.WEAPON;
			name = "dagger";
			sprite = subImage(5,1);

			baseDamage = 3.0;
			baseAccuracy = 6.5;
			weight = 2;

			tier = 2;

			description = "A short metal blade that is effective\nin short ranged combat." +
					"Due to its compactness,\nit also serves as an adequate throwing weapon.";
		}

		public void axe(){
			supertype = Item_Supertype.WEAPON;
			name = "hand axe";
			sprite = subImage(6,1);

			baseDamage = 9.0;
			baseAccuracy = 5.5;
			weight = 6.0;

			tier = 4;

			description = "A small battleaxe that can be effectively\nweilded using one hand by stronger warriors.";
		}

		public void club(){
			supertype = Item_Supertype.WEAPON;
			name = "club";
			sprite = subImage(0,9);

			baseDamage = 6.0;
			baseAccuracy = 4.5;
			weight = 4.5;

			tier = 1;
		}	

		public void spiked_club(){
			supertype = Item_Supertype.WEAPON;
			name = "spiked club";
			sprite = subImage(4,1);

			baseDamage = 7.0;
			baseAccuracy = 4.5;
			weight = 5.0;

			tier = 2;
		}

		public void leatherBoots(){
			supertype = Item_Supertype.ARMOUR;
			name = "pair of leather boots";
			sprite = subImage(0,4);

			baseDefense = 1.5;
			tier = 2;
		}

		/** evokables/consumables **/


		public void dart(){
			supertype = Item_Supertype.MISSILE;
			name = "throwing dart";
			sprite = subImage(8,1);
			
			tier = 1;
			commonStackSize = 5;
		}
		
		/** SCROLLS **/

		public void scrollOfIdentify(){
			supertype = Item_Supertype.SCROLL;
			name = "scroll of Identify";
			sprite = subImage(0,3);

			tier = 2;
		}

		public void scrollOfTeleportation(){
			supertype = Item_Supertype.SCROLL;
			name = "scroll of Teleportation";
			sprite = subImage(0,3);

			tier = 2;
		}


		/** POTIONS **/

		public void potionOfHealing(){
			supertype = Item_Supertype.POTION;
			name = "potion of Healing";
			sprite = subImage(2,2);

			tier = 3;
		}

		public void potionOfMight(){
			supertype = Item_Supertype.POTION;
			name = "potion of Might";
			sprite = subImage(2,2);

			tier = 2;
		}
		
		public void potionOfFlight(){
			supertype = Item_Supertype.POTION;
			name = "potion of Flight";
			sprite = subImage(2,2);
			
			tier = 2;
		}

		/** FOOD **/

		public void bread(){
			supertype = Item_Supertype.FOOD;
			name = "bread ration";
			sprite = subImage(5,5);

			foodValue = 10;
			tier = 1;
		}
		
		/** KEYS **/
		
		public void silverKey(){
			supertype = Item_Supertype.SPECIAL;
			name = "silver key";
			sprite = subImage(4,0);
			
		}
	}
}