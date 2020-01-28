import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

// TODO: extend Entity class, or deprecte entirely.

@Deprecated
public class StaticEntity {
	
	public SEType type;
	public Inventory inv = new Inventory();
	public boolean isLocked;
	public int linkedKey;
	public BufferedImage sprite;
	
	StaticEntity(SEType _type){
		type = _type;
		isLocked = type.isLocked;
		sprite = type.spriteA;
		
		if(type.hasInventory){
			inv.makeRandomInventory(type.inventoryTierModifier + Main.cF, type.inventoryAmount);
		}
	}
	
	public void interact(Entity e, char c){
		// TODO: action selection if there are multiple
		// TODO: non-player entity interaction

		if(c=='g' && inv!=null){
			
		}else if(c=='o' && isLocked){
			if(e.inv.hasKey(Main.cF)){
				isLocked = false;
				e.inv.useKey(Main.cF);
				if(inv.isEmpty()){
					Main.appendText("You open the "+type.name+" and find nothing.");
				}else{
					Main.appendText("You open the "+type.name+" to find:");
					inv.printContents(true);
					Main.itemPickup = true;
					Main.currentInventory = inv;
				}
			}else{
				Main.appendText("It's locked, and you don't have a key to open it.");
			}
		}
	}
	
	public enum SEType {
		SILVER_CHEST	(0);


		SEType(int t){
			if(t==0){
				silverChest();
			}
		}


		public BufferedImage sourcedItems;

		public BufferedImage subImage(int x, int y){
			if(sourcedItems == null){
				try {
					sourcedItems = ImageIO.read(new File("imgs/sourcedItems.png"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return sourcedItems.getSubimage(x*24+x, y*24+y, 24, 24);
		}

		public BufferedImage spriteA;
		public BufferedImage spriteB;
		
		public String name;
		public boolean hasInventory = false;
		public int inventoryTierModifier;
		public int inventoryAmount;
		public boolean isLocked;

		public double HP;
		public boolean indestructible;

		// TODO: more fields

		public void silverChest(){
			name = "silver chest";
			spriteA = subImage(5,0);
			spriteB = subImage(6,0);
			indestructible = true;
			
			// TODO: plan out tier sys
			isLocked = true;
			hasInventory = true;
			inventoryTierModifier = 2;
			inventoryAmount = 2;
		}
	}
}
