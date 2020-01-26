import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Creature extends Entity {

	// fields
	
	public double HP;
	public double SP;
	public double EV = 1.0; // MAX = 1.5 percent, 1.0 = 50/50 to dodge attack of same acc
	public double strength = 1;
	public double HP_max = 1; // 6 dmg = player with dagger
	public double SP_max = 1;
	public double SP_regen = 1;
	public double speed = 1.0;
	
	public AI ai;
	public boolean hasAI = true;
	
	public BufferedImage sprite;
	public String name;
	
	public boolean isFlying = false;
	public boolean isAmphibious = false;
	
	// constructors
	Creature(){
		super();
	}
	
	// methods
	
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