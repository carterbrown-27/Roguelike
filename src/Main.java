/** @author carterbrown-27 **/

import java.io.*;
import javax.imageio.*;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;

public class Main {

	/** TODO: saves **/
	//	public static int seed = 12345678;
	public static Random rng = new Random();
	//	public static boolean randSeed = true;
	//	public static boolean render = true;

	public static boolean running = true;

	private static JFrame frame;
	private static JPanel panel = new JPanel();

	public static Player player;

	public static int ticks = 0;

	public static double lastPress = System.currentTimeMillis();
	public static double interval = 100;

	public static boolean itemPickup = false;
	public static boolean inventoryScreen = false;
	public static boolean itemScreen = false;
	public static boolean pickItem = false;

	public static Point targetPos;
	public static boolean aimScreen = false;

	public static boolean identify = false;
	public static boolean enchant = false;

	public static Inventory currentInventory;

	public static char selectedItem;

	public static HashMap<Integer,Map> floors = new HashMap<>();
	public static Map gen;

	public static int cF;
	public static Point ropePoint;

	public static final int MAP_H = 60;
	public static final int MAP_W = 60;
	public static final int MAP_FILL = 49;

	public static int seed;

	public static HashMap<String,String> randomNames = new HashMap<>();
	public static HashMap<String,Potion.PotionColours> potionColours = new HashMap<>();	

	public static int JFrame_WIDTH = 1500;
	public static int JFrame_HEIGHT = 1000;
	public static JPanel consolePanel = new JPanel();


	public static void main(String[] args){

		// Map.Room r = new Map.Room(8,8);
		// 52,90,46
		seed = rng.nextInt(Integer.MAX_VALUE);
		// seed = 356598179;
		rng = new Random(seed);
		startGame();
		System.out.println(seed);

		// controls

		/* y	(k/ua)	u
		 *(h/la) . (l/ra)
		 * b	(d/da) n
		 * or shift/ctrl+l/r 
		 * 
		 * 7 0 4
		 * 3 . 1
		 * 6 2 5
		 */

		frame.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e){
				if(!running){
					if(e.getKeyCode() == KeyEvent.VK_SPACE){
						// restart
						startGame();
					}
					return;
				}
				
				if (System.currentTimeMillis()-lastPress>=interval) {
					lastPress = System.currentTimeMillis();
					if(!itemPickup && !inventoryScreen){
						txt.clear(); /* TODO: TEMP */
						refreshText();
					}
					if(itemPickup){
						for (char c = 'a'; c <= 'z'; c++) {
							if(currentInventory==null){
								currentInventory = floors.get(cF).tileMap[player.getY()][player.getX()].inventory;
							}

							if (e.getKeyChar() == c && currentInventory.contains(c)) {
								player.pickUp(c,currentInventory);
								if(currentInventory.isEmpty()){
									currentInventory = null;
									itemPickup = false;
								}
							}
						}
						if(e.getKeyChar() < 'a' || e.getKeyChar() >'z'){
							currentInventory = null;
							itemPickup = false;
						}

					}else if(inventoryScreen){
						txt.clear();
						refreshText();
						boolean selected = false;
						char c = e.getKeyChar();
						if (player.inv.contains(c)) {
							selected = true;
							// open item menu
							/* TODO: temporary */
							// TODO: replace inventory with split-by-class system
							Item i = player.inv.getItem(c);

							appendText(c+" - "+i.getDisplayName()+" selected.");
							appendText(i.getDescription());

							String[] prompts = i.listPrompts();
							for(String s: prompts) {
								appendText(s);
							}
							selectedItem = c;
						}
						if(selected) itemScreen = true;
					}
					inventoryScreen = false;
				}else if(itemScreen){
					char c = selectedItem;
					Item i = player.inv.getItem(c);

					// TODO: fix all of this.
					if(e.getKeyChar() == 'd'){
						i.drop(player);
						
						// player.inv.dropAll(c, player);
					}

					if(i instanceof Equippable){
						Equippable eq = (Equippable) i;
						// TODO: implement
					}

					if(i instanceof Consumable){
						Consumable cnsm = (Consumable) i;
						// TODO: implement
					}

					itemScreen = false;
					refreshStats();
				}else if(pickItem){
					boolean flag = false;
					for (char c = 'a'; c <= 'z'; c++) {
						if (e.getKeyChar() == c && player.inv.contains(c)) {
							Item i = player.inv.getItem(c);
							if(identify && !player.isItemIdentified(i)){
								flag = true;
								player.identify(i);
								identify = false;
							}else if(enchant){
								flag = true;
								enchant = false;
							}
						}
					}
					if(!flag){
						appendText(e.getKeyChar()+" is not a valid option.");
					}else{
						pickItem = false;
					}


					/** DIRECTIONALS **/
				}else if (directionValue(e.getKeyCode(),e.isControlDown(), e.isShiftDown()) != null) {
					Direction dir = directionValue(e.getKeyCode(),e.isControlDown(), e.isShiftDown());
					if(aimScreen){
						Point p = dir.p;
						targetPos.x += p.x;
						targetPos.y += p.y;
						// TODO: aim visuals && los check

					}else{								
						player.act_adj(dir);
					}


				}else if (e.getKeyCode() == KeyEvent.VK_ENTER){
					if(aimScreen){
						ArrayList<Point> line = FOV.bresenhamLine(player.getPos(), targetPos);

						boolean f = true;
						for(Point p: line){
							if(p.equals(player.getPos()) || p.equals(line.get(line.size()-1))){
								continue;
							}

							if(!floors.get(cF).isFullOpen(p.x, p.y)){
								f = false;
								break;
							}
						}
						if(f){
							// valid target
							appendTextRF("valid target");
						}else{
							appendTextRF("invalid target");
						}

						aimScreen = false;
						return;
					}
					if(floors.get(cF).valueAt(player.getPos()) == 3){
						if(floors.size()<=cF+1){
							newFloor();
						}else{
							changeFloor(cF+1,true,false);
						}
					}else if(floors.get(cF).valueAt(player.getPos()) == 2 && cF > 0){
						// floors.set(currentFloor, (new Map(floors.get(currentFloor))));
						changeFloor(cF-1,false,false);
					}
					appendText("Current Floor: "+cF);

				} else if(e.getKeyCode() == KeyEvent.VK_P){
					floors.get(cF).printMap();

				} else if(e.getKeyCode() == KeyEvent.VK_T){
					aimScreen = !aimScreen;
					targetPos = player.getPos();

				} else if(e.getKeyCode() == KeyEvent.VK_SPACE){
					// open attack selections
					// area.append("space.\n");
					// if already open attack
				} else if(e.getKeyCode() == KeyEvent.VK_G){
					// get
					Inventory tileInv = floors.get(cF).tileMap[player.getY()][player.getX()].inventory;
					Inventory selectedInv = null;


					// TODO: fix CHESTS
					if(tileInv.isEmpty()){
						for(Entity n: floors.get(cF).entities){
							if(n.SE != null && n.getX() == player.getX() && n.getY() == player.getY()){
								if(!n.SE.isLocked){
									selectedInv = n.inv;
									appendText("Pick up what?");
								}else{											
									appendText("It's locked.");
									return;
								}
								// n.SE.interact(player,'g');
								break;
							}
						}
					}else{
						selectedInv = tileInv;
					}

					if(selectedInv == null || selectedInv.isEmpty()){
						appendText("There is nothing here.");
						return;
					}

					if(selectedInv.isOneItem()){
						player.pickUp(selectedInv.getFirstItem(),selectedInv);
					}else if(!selectedInv.isEmpty()){
						itemPickup = true;
						currentInventory = selectedInv;
						appendText("Pick up what?");
					}

				}else if(e.getKeyCode() == KeyEvent.VK_I){
					player.inv.printContents(false);

					if(!player.inv.isEmpty()) inventoryScreen = true;

				}else if(e.getKeyCode() == KeyEvent.VK_O){
					for(Entity n: floors.get(cF).entities){
						if(n.SE != null && n.getX() == player.getX() && n.getY() == player.getY()){
							n.SE.interact(player,'o');
						}
					}
				}else if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C){
					txt.clear();
					refreshText();
				}else if(e.getKeyCode() == KeyEvent.VK_5){
					player.startRest();

				}else if(e.getKeyCode() == KeyEvent.VK_X){
					// TODO: overview

				}else{
					for(Player.Ability a: Player.Ability.values()){
						if(e.getKeyCode() == a.k){
							if(player.getSP() >= a.s){
								player.select(a);
								appendText(a.name);
							}else{
								appendText("Not enough stamina (stamina!).");
								player.deselect();
							}
							break;
						}
					}
				}
			}
		});
		// System.out.println(ropePoint.toString());
		refreshFrame(render(ropePoint.x,ropePoint.y));
	}

	public static void startGame(){
		floors.clear();
		cF = 0;

		long time = System.currentTimeMillis();

		floors.put(cF, new Map(MAP_H,MAP_W,MAP_FILL));
		ropePoint = floors.get(cF).getPosition(2);

		System.out.println("@@@ gen = "+(System.currentTimeMillis()-time)+"ms @@@");

		player = new Player(ropePoint.x,ropePoint.y,floors.get(cF));

		// (temporary), player inventory population
		Weapon dagger = new Weapon("dagger");
		player.inv.addItem(dagger);
		player.equip(dagger);

		Missile darts = new Missile("darts");
		player.inv.addItem(darts);
		player.equip(darts);

		// TODO: init Item.Scrolls & Item.Potions
		for(Scroll i: Item.getScrolls()){
			randomNames.put(i.getTypeName(), "scroll(s) labeled "+((randomName()+" "+randomName()).toUpperCase()));
		}
		for(Potion i: Item.getPotions()){
			randomNames.put(i.getTypeName(), randomPotionName(i));
		}

		// (temporary) populate level with mobs
		int mobs = rng.nextInt(8)+16;
		for (int i = 0; i < mobs; i++) {
			Point t;
			do{
				t = floors.get(cF).randomOpenSpace();				
			}while(Math.abs(t.x-player.getX()) <=3 && Math.abs(t.y-player.getY()) <=3);

			System.out.println("point picked");
			new Creature(RANDOMTYPE, t.x, t.y);
			System.out.println("Entity Added.");
		}

		int items = rng.nextInt(6)+14; // 6+8, 6+28
		for(int i = 0; i < items; i++){
			Point t = floors.get(cF).randomOpenSpace();
			Item.ItemType ty = Item.randomItemType(cF);
			System.out.println("adding "+ty);
			floors.get(cF).tileMap[t.y][t.x].inventory.addItem(new Weapon("Dagger"));
		}

		int chests = rng.nextInt(2)+1;
		for(int i = 0; i < chests; i++){
			Point t = floors.get(cF).randomEmptySpace();
			System.out.println("adding Chest/Key pair");
			floors.get(cF).tileMap[t.y][t.x].inventory.addItem(new Special("Silver Key"));
			t = floors.get(cF).randomEmptySpace();

			// TODO: add chests
			new Entity(StaticEntity.SEType.SILVER_CHEST,t.x,t.y,floors.get(cF));
		}

		try{
			File output = new File("renders/"+String.valueOf(rng.nextInt(seed))+"_"+String.valueOf(cF)+"-t"+System.currentTimeMillis()+".png");
			ImageIO.write(floors.get(cF).renderMap(), "png", output);
		}catch(Exception e){
			e.printStackTrace();
		}

		System.out.println("@@@ Ready = "+(System.currentTimeMillis()-time)+"ms @@@");
		if(frame==null){
			frame = buildFrame(render(ropePoint.x,ropePoint.y));
		}else{
			refreshFrame(render(ropePoint.x,ropePoint.y));
		}

		System.out.println("@@@ frameUp = "+(System.currentTimeMillis()-time)+"ms @@@");
		running = true;
	}

	public static void newFloor(){
		// floors.set(currentFloor, new Map(floors.get(currentFloor)));
		blackOverlay();
		// floors.replace(currentFloor, new Map(map_h,map_w,map_fill,rng));
		System.out.println(floors.size()+" total");
		floors.put(cF+1, new Map(MAP_H, MAP_W, MAP_FILL));
		changeFloor(cF+1,true,true);
	}

	public static void changeFloor(int floor, boolean down, boolean isNew){
		cF = floor;
		Point startPoint= floors.get(cF).getPosition(2);
		// TODO: update for multiple stairs
		if(!down) startPoint= floors.get(cF).getPosition(3);
		player.map = floors.get(cF);
		floors.get(cF).player = player;
		player.x = startPoint.x;
		player.y = startPoint.y;
		player.map = floors.get(cF);
		// floors.get(currentFloor).player = player;

		if(isNew){

			/** TEMPORARY **/
			Point t;
			int mobs = (rng.nextInt(6)+10)*(cF+1);
			for (int i = 0; i < mobs; i++) {
				do{
					t = floors.get(cF).randomOpenSpace();				
				}while(Math.abs(t.x-player.getX()) <=3 && Math.abs(t.y-player.getY()) <=3);
				
				new Creature();
			}

			int items = rng.nextInt(6)+8;
			for(int i = 0; i < items; i++){
				t = floors.get(cF).randomOpenSpace();
				System.out.println("adding item #"+i);

				// TODO: refactor & reimplement
				floors.get(cF).tileMap[t.y][t.x].inventory.addItem(Item.randomItem(1));
			}

			int chests = rng.nextInt(2)+1;
			for(int i = 0; i < chests; i++){
				t = floors.get(cF).randomEmptySpace();
				System.out.println("adding Chest/Key pair");
				floors.get(cF).tileMap[t.y][t.x].inventory.addItem(new Special("Silver Key"));
				t = floors.get(cF).randomEmptySpace();
				floors.get(cF).addEntity(new Entity(StaticEntity.SEType.SILVER_CHEST,t.x,t.y,floors.get(cF)));
			}
		}
		try{
			File output = new File("renders/"+String.valueOf(rng.nextInt(seed))+"_"+String.valueOf(cF)+"-t"+System.currentTimeMillis()+".png");
			ImageIO.write(floors.get(cF).renderMap(), "png", output);
		}catch(Exception e){};
		refreshFrame(render(startPoint.x,startPoint.y));
	}

	private static JFrame buildFrame(BufferedImage img) {
		// GUI gui = new GUI();
		// gui.run();

		JFrame frame = new JFrame();
		JFrame_HEIGHT = img.getHeight()+42;
		JFrame_WIDTH = Math.min(img.getWidth()*7/3,1600);
		frame.setIconImage(Creature.PLAYER.SPRITE);

		panel.setLayout(new BorderLayout());
		panel.add(consolePanel,BorderLayout.EAST);

		area.setBackground(new Color(0,0,0));
		area.setFont(f);
		area.setEditable(false);
		area.setFocusable(false);
		area.setForeground(Color.white);

		stats.setFocusable(false);
		stats.setEditable(false);

		consolePanel.setLayout(new BorderLayout());
		consolePanel.add(stats,BorderLayout.NORTH);
		consolePanel.add(area,BorderLayout.CENTER);
		consolePanel.setBackground(new Color(0,0,0));

		frame.setTitle("Roguelike");
		panel.setBackground(new Color(0,0,0));
		panel.add(consolePanel,BorderLayout.EAST);


		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(JFrame_WIDTH, JFrame_HEIGHT);
		frame.setVisible(true);
		return frame;
	}

	public static Font f = new Font("Serif",Font.BOLD,20);
	public static JTextArea area = new JTextArea();
	public static JTextArea stats = new JTextArea();
	public static ArrayList<String> txt = new ArrayList<String>();

	public static void refreshFrame(BufferedImage render) {
		panel.removeAll();
		JLabel picLabel = new JLabel(new ImageIcon(render));
		panel.add(picLabel,BorderLayout.WEST);
		panel.setSize(frame.getWidth(), frame.getHeight());
		// panel.setLocation(new Point(panel.getX(),panel.getY()+25));
		consolePanel.setSize((int) (frame.getWidth()-picLabel.getWidth()*1.1), frame.getHeight()-10);
		area.setSize(consolePanel.getWidth(), consolePanel.getHeight());
		stats.setSize(consolePanel.getWidth(), consolePanel.getHeight());
		refreshStats();

		panel.add(consolePanel,BorderLayout.EAST);
		frame.add(panel);
		frame.setFocusable(true);
		frame.requestFocusInWindow();
		frame.revalidate();
		frame.repaint();
	}

	public static void refreshStats(){
		stats.setBackground(new Color(0,0,0));
		stats.setForeground(Color.LIGHT_GRAY);
		stats.setFont(f);
		stats.setText("");

		stats.append("  <Player Name>\t\t                     .\n\n");
		stats.append("  HP: "+player.getHP()+"\n");
		stats.append("  SP: "+player.getSP()+"\n");
		stats.append("  STR: "+player.getStrength()+"\n");
		stats.append("  SAT: "+ActionLibrary.round(player.getSatiation(),1)+"\n");

		if(player.weapon != null){			
			stats.append("  Weapon: "+player.weapon.getDisplayName());
		}else{
			stats.append("  Weapon: none");
		}
		stats.append("\n");

		if(player.quivered != null){			
			stats.append("  Quivr'd: "+player.quivered.getDisplayName());
		}else{
			stats.append("  Quivr'd: none");
		}
		stats.append("\n");


		String line = "  ";
		for(Status s: player.getStatuses().keySet()){
			line += s.name();
		}
		stats.append(line+"\n");
		stats.append("      _________________________________________________________\n\n");
	}

	public static void appendTextRF(String text){
		appendText(text);
		refreshText();
	}

	public static void appendText(String text){
		String[] strArray = text.split("\\r?\\n");
		for(String str: strArray){
			txt.add(str);
		}
		refreshText();
	}

	public static final int rows = 15;
	public static void refreshText(){
		area.setText("");
		while (txt.size()>rows){
			txt.remove(0);
		}
		for(int i = 0; i < txt.size(); i++){
			area.append(txt.get(i)+"\n"+"\n");
		}

		//		if(txt.size()>=1) area.append(txt.get(txt.size()-1));
	}

	public static void blackOverlay(){
		// TODO: load file into memory
		try{
			refreshFrame(ImageIO.read(new File("imgs/descendingOverlay.png")));
		}catch(Exception e){
			System.out.println("overlay not found.");
		}
	}

	public static BufferedImage resize(BufferedImage img, int w, int h){
		Image tmp = img.getScaledInstance(w, h, Image.SCALE_REPLICATE);
		BufferedImage dimg = new BufferedImage(w,h,BufferedImage.TYPE_4BYTE_ABGR);

		Graphics2D g = dimg.createGraphics();
		g.drawImage(tmp,0,0,null);
		g.dispose();

		return dimg;
	}

	public static void advanceTicks(int _ticks){
		ticks++;
		Point pos = player.getPos();
		// System.out.println(pos.toString());
		refreshFrame(render(pos.x,pos.y));
	}

	public static Point lastPos;
	// TODO: switch Entity to Creature
	public static void takeTurn(){
		Inventory floorInv = floors.get(cF).tileMap[player.getY()][player.getX()].inventory;
		if(!floorInv.isEmpty() && (lastPos!=null && !player.getPos().equals(lastPos))){
			floorInv.printContents(true);
		}

		// double playerHP = player.HP;
		// TODO: move max to e
		player.upkeep();
		
		ArrayList<Creature> dead = new ArrayList<>();

		// int[] order = floors.get(currentFloor).getEntityPriority();
		// for(int i: order){
		Queue<Creature> creatureQueue = new LinkedList<>();

		// TODO: creature list.
		for(Creature c: floors.get(cF).creatures){
			c.awakeCheck();
			if(c.awake) creatureQueue.add(c);
		}

		
		// TODO: overhaul.
		while(!creatureQueue.isEmpty()){
			Creature c = creatureQueue.remove();
			// TODO: add mob sleep/detection stuff
			// Entity e = floors.get(currentFloor).entities.get(i);
			Entity.turnEnding ending = c.takeTurn();
			System.out.println(c.getName()+" takes a turn.");
			if(ending.equals(Entity.turnEnding.DEAD)){
				c.x = -1;
				c.y = -1;
				dead.add(c);
			}else if(ending.equals(Entity.turnEnding.WAITING)){
				System.out.println(c.getName()+" is waiting.");
				if(!allWaiting(creatureQueue)){
					creatureQueue.add(c);
				}else{
					
				}
			}
		}

		for(Creature c: dead){
			c.die();
		}

		// TODO: move death logic to Player.
		Point pos = player.getPos();
		lastPos = pos;
		// if(player.HP != playerHP) appendText("Player HP = " + ActionLibrary.round(player.HP,2));
		if(player.getHP() < 0.05){
			appendText("You die...");
			running = false;
		}
		refreshFrame(render(pos.x,pos.y));
	}

	@Deprecated
	public static boolean allWaiting(Queue<Creature> q){
		while(!q.isEmpty()){
			if(!q.remove().waiting) return false;
		}
		return true;
	}

	public static BufferedImage render(int x, int y){
		BufferedImage img = floors.get(cF).render_vig(x, y, player.ViewDistance, player.Luminosity);
		img = resize(img, img.getWidth()*3, img.getHeight()*3);
		return img;
	}

	// TODO: move to Potion Class
	public static String randomPotionName(Potion p){
		String[] colours = {"red","orange","green","blue","violet","pink","mahogany",
				"aquamarine","golden","silver","charcoal","brown"};
		String[] descriptors = {"bubbly","foggy","smoking","flat","swirling","percipitated","thick","glowing","shimmering","frosted"};

		String name = "";
		if(rng.nextBoolean()){
			name+=descriptors[rng.nextInt(descriptors.length)]+" ";
		}

		int r;
		do{
			r = rng.nextInt(colours.length);
		}
		while(potionColours.containsValue(Potion.PotionColours.values()[r]));

		name+=colours[r];
		potionColours.put(p.getTypeName(), Potion.PotionColours.values()[r]);
		name+=" potion";

		return name;
	}

	public static String randomName(){

		int length = rng.nextInt(5)+4;
		char[] vowels = {'a','e','i','o','u'};

		char[] ctv = {'r','h','w','l','y','n'};
		char[] consonants = {'b','c','d','f','g','j','k','l','m','p','r','v','z','s','t','n','w'};

		// String[] pairs = {"ld","st","pr","qu","sh"};

		String name = "";
		int prev = -1;

		while(name.length() < length){
			if((name.length() == length-1 && prev == 2) || prev == 3 || (prev == 2 && rng.nextBoolean())
					|| (prev == 0 && rng.nextBoolean() && rng.nextBoolean()) || (prev==-1 && rng.nextBoolean())){

				name+=vowels[rng.nextInt(vowels.length)];
				prev = 0;
			}else{
				if(prev==2 || (prev==-1 && rng.nextBoolean())){
					name+=ctv[rng.nextInt(ctv.length)];

					prev = 3;
				}else{
					name+=consonants[rng.nextInt(consonants.length)];

					prev = 2;
				}
			}
		}
		return name;
	}

	public static Direction directionValue(int keyCode, boolean controlDown, boolean shiftDown){
		if (keyCode == KeyEvent.VK_K || keyCode == KeyEvent.VK_UP) {
			return Direction.UP;
		} else if (keyCode == KeyEvent.VK_H || keyCode == KeyEvent.VK_LEFT) {
			if(controlDown){
				return Direction.DOWN_LEFT;
			}else if(shiftDown){
				return Direction.UP_LEFT;
			}else{
				return Direction.LEFT;
			}
		} else if (keyCode == KeyEvent.VK_J || keyCode == KeyEvent.VK_DOWN) {
			return Direction.DOWN;
		} else if (keyCode == KeyEvent.VK_L || keyCode == KeyEvent.VK_RIGHT) {
			if(controlDown){
				return Direction.DOWN_RIGHT;
			}else if(shiftDown){
				return Direction.UP_RIGHT;
			}else{
				return Direction.DOWN_RIGHT;
			}
		}else if(keyCode == KeyEvent.VK_Y){
			return Direction.UP_LEFT;
		}else if(keyCode == KeyEvent.VK_U){
			return Direction.UP_RIGHT;
		}else if(keyCode == KeyEvent.VK_B){
			return Direction.DOWN_LEFT;
		}else if(keyCode == KeyEvent.VK_N){
			return Direction.DOWN_RIGHT;
		}
		return null;
	}
}