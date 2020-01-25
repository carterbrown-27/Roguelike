import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Creature extends Entity {

	// fields
	public BufferedImage sourcedItems;
	
	public double STRENGTH = 1;
	public double HP_MAX = 1; // 6 dmg = player with dagger
	public double SP_MAX = 1;
	public double SP_REGEN = 1;
	public double SPEED = 1.0;
	public double EVASIVENESS = 1.0; // MAX = 1.5 percent, 1.0 = 50/50 to dodge attack of same acc
	
	
	public BufferedImage SPRITE;
	public String NAME;
	
	// attributes
	public boolean hasAI = true;
	
	public boolean isFlying = false;
	public boolean isAmphibious = false;
	// methods
	
	@Deprecated
	public static Creature randomType(){
		return Creature.values()[Main.rng.nextInt(Creature.values().length)];
	}
	
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
	
	@Deprecated
	private void createRat(){
		// TODO: edit sprites
		try {
			SPRITE = ImageIO.read(new File("imgs/rat.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}