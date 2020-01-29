import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Potion extends Item implements Consumable {
	private static BufferedImage potionImages;
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
		this.setAmount(_amount);
		super.setSprite(Main.potionColours.get(this.getTypeName()).image);
	}

	Potion(String id){
		this(id,1);
	}

	// TODO (J) JSONize.
	public static enum PotionColours {
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

		public String colour;
		public BufferedImage image;

		public static final String[] colours = {"red","orange","green","blue","violet","pink","mahogany",
				"aquamarine","golden","silver","charcoal","brown"};

		public static final BufferedImage[] images = {subImagePotion(0,0),subImagePotion(0,1),subImagePotion(2,0),subImagePotion(1,0),subImagePotion(3,0),subImagePotion(4,1),
				subImagePotion(5,1), subImagePotion(2,1),subImagePotion(4,0),subImagePotion(1,1),subImagePotion(5,0),subImagePotion(3,1)};

		PotionColours(int n){
			colour = getColour(n);
			image = getImage(n);
		}

		public String getColour(int i){
			return colours[i];
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
		return Potion.descriptors[Main.rng.nextInt(Potion.descriptors.length)];
	}

	@Override
	public void use(Entity e) {

	}

	@Override
	public String[] listPrompts() {
		// TODO (A) Implement
		return new String[] {};
	}
}
