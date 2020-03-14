import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

// TODO: deprecate entirely or move logic to new Interface: Interactable
public class StaticEntity extends Entity {
	
	public SEType type;
	public boolean isLocked;
	public int linkedKey;
	
	StaticEntity(String name, Point pos, Map map){
		super(name, pos, map);
		
		// TODO (X) Delete
		type = SEType.SILVER_CHEST;
		isLocked = type.isLocked;
		this.setSprite(type.spriteA);
		
		if(type.hasInventory){
			inv.makeRandomInventory(type.inventoryTierModifier + Main.floorNumber, type.inventoryAmount);
		}
	}
	
	public void interact(Entity e, char c){
		// TODO: action selection if there are multiple
		// TODO: non-player entity interaction

		if(c=='g' && inv!=null){
			
		}else if(c=='o' && isLocked){
			if(e.inv.hasKey(Main.floorNumber)){
				isLocked = false;
				e.inv.useKey(Main.floorNumber);
				if(inv.isEmpty()){
					Main.view.appendText("You open the "+type.name+" and find nothing.");
				}else{
					Main.view.appendText("You open the "+type.name+" to find:");
					inv.printContents(true);
					Main.setGameState(Main.GameState.PICKUP);
					Main.currentInventory = inv;
				}
			}else{
				Main.view.appendText("It's locked, and you don't have a key to open it.");
			}
		}
	}
	
	// TODO (J) JSONize
	@Deprecated
	public enum SEType {
		SILVER_CHEST	(0);

		SEType(int t){
			if(t==0){
				silverChest();
			}
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

		// TODO (R) Review: more fields

		public void silverChest(){
			name = "silver chest";
			spriteA = subImage(GameObject.SpriteSource.DEFAULT,5,0);
			spriteB = subImage(GameObject.SpriteSource.DEFAULT,6,0);
			indestructible = true;
			
			// TODO: plan out tier sys
			isLocked = true;
			hasInventory = true;
			inventoryTierModifier = 2;
			inventoryAmount = 2;
		}
	}
}
