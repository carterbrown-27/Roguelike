import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import org.json.JSONObject;

/** Represents a Potion. Potions have a random effect, which uniquely correspond to a random potion colour. Potions apply Statuses to entities that quaff them.
 */
public class Potion extends Item implements Consumable {
	
	private static Random rng;
	private static HashMap<String,String> potionNames = new HashMap<>();
	private static HashMap<String,PotionColour> potionColours = new HashMap<>();
	private static BufferedImage potionImages;
	
	// string = name of effect, integer = potency (i.e. hp regained, duration, tier of effect, etc).
	// TODO (R) Review, potentially add Effect Class that describes functionality, tier, duration, etc. --> HashSet
	private HashMap<String,Integer> effects = new HashMap<>();
	private String fakeName;
	
	public static final String[] descriptors = {
			"bubbly",
			"foggy",
			"smoking",
			"flat",
			"swirling",
			"percipitated",
			"thick",
			"glowing",
			"shimmering",
			"frosted"
	};

	// Master Constructor
	Potion(String id, int _amount){
		super(id);
		super.addPrompt('q', "(q)uaff");
		
		// seed this rng.
		if(rng == null) rng = new Random(Main.rng.nextInt());
		this.setAmount(_amount);
		this.fakeName = randomPotionName(this.getTypeName());
		
		JSONObject effectData = super.getItemData().getJSONObject("effects");
		
		for(String k: effectData.keySet()) {
			Integer v = effectData.getInt(k);
			effects.put(k, v);
		}
		
		super.setSprite(potionColours.get(this.getTypeName()).image);
		// randomPotionName inits the colour. TODO (R) Review
	}

	Potion(String id){
		this(id,1);
	}

//	public String toString() {
//		// handle plurals here potion(s) of ... vs 3 <colour> potion(s)
//		return String.format("%s - %s %s%s", this.getInventoryID(), this.getQuantityString(), this.getDisplayName());
//	}
	
	// Not inside enum because of static field instantiation order.
	public static final String[] colourNames = {"red","orange","green","blue","violet","pink","mahogany",
			"aquamarine","golden","silver","charcoal","brown"};

	public static final BufferedImage[] images = {subImagePotion(0,0),subImagePotion(0,1),subImagePotion(2,0),subImagePotion(1,0),subImagePotion(3,0),subImagePotion(4,1),
			subImagePotion(5,1), subImagePotion(2,1),subImagePotion(4,0),subImagePotion(1,1),subImagePotion(5,0),subImagePotion(3,1)};
	
	// TODO (J) JSONize.
	/** Enumerates all the possible colours a Potion could have, with the sprites and names for these colours.
	 */
	public static enum PotionColour {
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

		public String colourName;
		public BufferedImage image;


		PotionColour(int n){
			colourName = getColour(n);
			image = getImage(n);
		}

		public String getColour(int i){
			return colourNames[i];
		}

		public BufferedImage getImage(int i){
			return images[i];
		}

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
	
	public static String getRandomDescriptor() {
		return Potion.descriptors[rng.nextInt(Potion.descriptors.length)];
	}

	// TODO (R) Review, potentially modularize
	public static String randomPotionName(String realName){
		if(potionNames.containsKey(realName)) {
			return potionNames.get(realName);
		}
		
		String descriptor = "";
		if(rng.nextBoolean()){
			descriptor = Potion.getRandomDescriptor();
		}
		
		PotionColour colour = randomPotionColour(realName);
		String veiledName = String.format("%s%s%s potion", descriptor, descriptor.length() > 0 ? " " : "", colour.colourName);
		
		potionNames.put(realName, veiledName);
		return veiledName;
	}
	
	public static PotionColour randomPotionColour(String realName) {
		if(potionColours.containsKey(realName)) {
			return potionColours.get(realName);
		}
		int r;
		do{
			r = rng.nextInt(PotionColour.values().length);
		}
		// makes sure this colour hasn't been chosen before.
		while(potionColours.containsValue(PotionColour.values()[r]));
		
		PotionColour colour = PotionColour.values()[r];
		
		potionColours.put(realName, colour);
		return colour;
	}
	
	// TODO (R) Review
	@Override
	public String getDisplayName() {
		if(Main.player.isItemIdentified(this)){
			return super.getDisplayName();
		}else {
			return fakeName;
		}
	}
	
	@Override
	public String getDescription() {
		if(Main.player.isItemIdentified(this)){
			return super.getDescription();
		}else {
			return "An unknown potion.";
		}		
	}
	
	
	@Override
	public void use(Creature c) {
		// TODO (A) Implement iterating over potion effects.
		// TODO (A) Implement text output.
		// TEMP
		c.changeHP(effects.getOrDefault("HP_CHANGE", 0));
		if(effects.containsKey("FLIGHT")) c.addStatus(Status.FLIGHT);
		if(effects.containsKey("MIGHTY")) c.addStatus(Status.MIGHTY);
		
		if(c instanceof Player) {
			((Player) c).identify(this);
		}
		
		super.delete(c);
		Main.takeTurn();

	}
	
	// TODO (T) TEMP
	@Override
	public boolean isStackable() {
		return true;
	}
	
	@Override
	public boolean isUnknown() {
		return true;
	}
}
