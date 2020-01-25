import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Potion extends Item implements Consumable {
	private static BufferedImage potionImages;
	
	Potion(String id, int _amount){
		super(id);
		this.setAmount(_amount);
	}
	
	Potion(String id){
		this(id,1);
	}
	
	// TODO: JSONize.
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

		PotionColours(int n){
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
	
	@Override
	public void use(Entity e) {
		
	}
	
	@Override
	public String[] listPrompts() {
		// TODO: implement
		return new String[] {};
	}
}
