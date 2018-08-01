import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;


public class Item{
	
	/* fields */
	/* An item refers to a stack of items of the same exact type
	 * stackable types: MISSILE SCROLL POTION FOOD
	 * multiple item stacks can occupy the same spot on the grid
	 */
	
	
	Item(Items type, int amount){
		this.type = type;
		this.amount = amount;
		
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
	
	
	public String getDisplayName(){
		if(!isUnknown() || Main.player.identifiedItems.containsValue(type)){
			return name;
		}else if(type.supertype.equals(Items.Item_Supertype.SCROLL)){
			return Main.randomNames.get(type);
		}else if(type.supertype.equals(Items.Item_Supertype.POTION)){
			return name;
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
	private static BufferedImage sourcedItems;
	
	public BufferedImage sprite;
	
	boolean isStackable = true;
	
	public enum Items {

		DAGGER		(0),
		HAND_AXE	(1),
		SPKD_CLUB	(2),
		
		LTR_BOOTS	(10),
		
		SCR_ID		(30),
		SCR_TELE	(31),
		
		POT_HEAL	(40),
		
		BREAD			(50);
		
		/** item classes **/
		public static final Items[] scrolls = {SCR_ID,SCR_TELE};
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
		String description;
		
		// weapon/missile
		double baseDamage;
		// armour
		double baseDefense;

		Items(int itemNo){
			if(itemNo == 0){
				dagger();
			}else if(itemNo == 1){
				axe();
			}else if(itemNo == 2){
				club();
			}else if(itemNo == 10){
				leatherBoots();
			}else if(itemNo == 30){
				scrollOfIdentify();
			}else if(itemNo == 31){
				scrollOfTeleportation();
			}else if(itemNo == 40){
				potionOfHealing();
			}else if(itemNo == 50){
				bread();
			}
		}
		// TODO: item tiers & balanced generation

		/** TEMPORARY **/
		static Random rng = new Random();
		public static Items randomItemType(int level /*, LevelType type*/){
			int r = rng.nextInt(Items.values().length);
			return Items.values()[r];
		}
		
		public boolean isUnknown(){
			return supertype.isUnknown;
		}
		
		public void dagger(){
			supertype = Item_Supertype.WEAPON;
			name = "dagger";
			sprite = subImage(5,1);
			
			baseDamage = 3.5;
			tier = 0;
			
			description = "A short metal blade that is effective in short ranged combat.\n" +
										"Due to its compactness, it also serves as an adequate throwing weapon.";
		}
		
		public void axe(){
			supertype = Item_Supertype.WEAPON;
			name = "hand axe";
			sprite = subImage(6,1);
			
			baseDamage = 7.0;
			tier = 3;
			
			description = "A small battleaxe that can be effectively weilded using one hand by stronger warriors.";
		}
		
		public void club(){
			supertype = Item_Supertype.WEAPON;
			name = "spiked club";
			sprite = subImage(4,1);
			
			baseDamage = 6.0;
			tier = 3;
		}
		
		public void leatherBoots(){
			supertype = Item_Supertype.ARMOUR;
			name = "pair of leather boots";
			sprite = subImage(0,4);
			
			baseDefense = 1.5;
			tier = 2;
		}
		
		/** evokables/consumables **/
		
		
		/** SCROLLS **/
		
		public void scrollOfIdentify(){
			supertype = Item_Supertype.SCROLL;
			name = "scroll of identify";
			sprite = subImage(0,3);
			
			tier = 2;
		}
		
		public void scrollOfTeleportation(){
			supertype = Item_Supertype.SCROLL;
			name = "scroll of teleportation";
			sprite = subImage(0,3);
			
			tier = 2;
		}
		
		
		/** POTIONS **/
		
		public void potionOfHealing(){
			supertype = Item_Supertype.POTION;
			name = "potion of healing";
			sprite = subImage(2,2);
			
			tier = 3;
		}
		
		/** FOOD **/
		
		public void bread(){
			supertype = Item_Supertype.FOOD;
			name = "bread ration";
			sprite = subImage(5,5);
			
			tier = 1;
		}
	}
}