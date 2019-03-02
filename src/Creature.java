import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public enum Creature {
	PLAYER (0),
	RAT		 (1), 
	BAT		 (2),
	GOBLIN (3),
	OOZE	 (4);
//	SNAKE  (4),
//	FROG	 (5),
//	SLIME	 (6),
//	SPIDER (7);
	
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
	
	Creature(int type){
		if(type==0){
			createPlayer();
		}else if(type==1){
			createRat();
		}else if(type==2){
			createBat();
		}else if(type==3){
			createGoblin();
		}else if(type==4){
			createOoze();
		}
	}
	
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
	
	private void createPlayer(){
		NAME = "player";
		SPRITE = subImage(2,6);
		
		STRENGTH = 2.5;
		HP_MAX = 15;
		SP_MAX = 4;
		
		EVASIVENESS = 1.0;
		
		hasAI = false;
	}
	
	private void createRat(){
		NAME = "rat";
		try {
			SPRITE = ImageIO.read(new File("imgs/rat.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		STRENGTH = 2;
		HP_MAX = 6;
		SP_MAX = 3;
		SPEED = 1;
		EVASIVENESS = 0.8;
	}
	
	private void createBat(){
		NAME = "bat";
		SPRITE = subImage(3,8);
		
		STRENGTH = 2;
		HP_MAX = 4;
		SP_MAX = 3;
		SPEED = 0.8;
		EVASIVENESS = 1.5;
		
		isFlying = true;
	}
	
	private void createGoblin(){
		NAME = "goblin";
		SPRITE = subImage(7,9);
		
		STRENGTH = 3;
		HP_MAX = 14;
		SP_MAX = 3;
		SPEED = 1;
		EVASIVENESS = 1.0;
	}
	
	private void createOoze(){
		NAME = "ooze";
		SPRITE = subImage(8,9);
		
		STRENGTH = 3;
		HP_MAX = 10;
		SP_MAX = 2;
		SPEED = 1.2;
		
		EVASIVENESS = 0.9;
		
		isAmphibious = true; // TODO: invis while swimming
	}
	
	
//	private BufferedImage getSprite(String path){
//		
//	}
}