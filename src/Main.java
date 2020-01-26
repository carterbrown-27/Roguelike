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

	public static HashMap<Integer,Map> floors = new HashMap<Integer,Map>();
	public static Map gen;

	public static int cF;
	public static Point ropePoint;

	public static final int map_h = 60;
	public static final int map_w = 60;
	public static final int map_fill = 49;

	public static int seed;

	public static HashMap<String,String> randomNames = new HashMap<>();
	public static HashMap<String,Potion.PotionColours> potionColours = new HashMap<>();	

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
				if(running){
					if (System.currentTimeMillis()-lastPress>=interval) {
						lastPress = System.currentTimeMillis();
						if(!itemPickup && !inventoryScreen){
							txt.clear(); /**TODO: TEMP**/
							refreshText();
						}
						if(itemPickup){
							for (char c = 'a'; c <= 'z'; c++) {
								if(currentInventory==null){
									currentInventory = floors.get(cF).tileMap[player.e.y][player.e.x].inventory;
								}

								if (e.getKeyChar() == c && currentInventory.inv.containsKey(c)) {
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
							if (player.e.inv.inv.containsKey(c)) {
								selected = true;
								// open item menu
								/** TODO: temporary **/
								// TODO: replace inventory with split-by-class system
								Item i = player.e.inv.inv.get(c);

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
						Item i = player.e.inv.inv.get(c);
						
						// TODO: fix all of this.
						if(e.getKeyChar() == 'd'){
							if(i.isEquipped()){
								player.equip(i);
							}
							player.e.inv.dropAll(c, player.e);
						}
						
						if(EQUIPPABLE){
						
						}
						
						if(CONSUMABLE){
							
						}
						itemScreen = false;
						refreshStats();
					}else if(pickItem){
						boolean flag = false;
						for (char c = 'a'; c <= 'z'; c++) {
							if (e.getKeyChar() == c && player.e.inv.inv.containsKey(c)) {
								Item i = player.e.inv.inv.get(c);
								if(identify && !player.identifiedItems.containsKey(i.type)){
									flag = true;
									i.identify();
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
					}else if (directionValue(e.getKeyCode(),e.isControlDown(), e.isShiftDown()) >= 0) {
						int d = directionValue(e.getKeyCode(),e.isControlDown(), e.isShiftDown());
						if(aimScreen){
							Point p = Direction.values()[d].p;
							targetPos.x += p.x;
							targetPos.y += p.y;
							// TODO: aim visuals && los check

						}else{								
							player.act_adj(d);
						}


					}else if (e.getKeyCode() == KeyEvent.VK_ENTER){
						if(aimScreen){
							ArrayList<Point> line = FOV.bresenhamLine(player.e.getPos(), targetPos);

							boolean f = true;
							for(Point p: line){
								if(p.equals(player.e.getPos()) || p.equals(line.get(line.size()-1))){
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
						if(floors.get(cF).valueAt(player.e.getPos()) == 3){
							if(floors.size()<=cF+1){
								newFloor();
							}else{
								changeFloor(cF+1,true,false);
							}
						}else if(floors.get(cF).valueAt(player.e.getPos()) == 2 && cF > 0){
							// floors.set(currentFloor, (new Map(floors.get(currentFloor))));
							changeFloor(cF-1,false,false);
						}
						appendText("Current Floor: "+cF);

					} else if(e.getKeyCode() == KeyEvent.VK_P){
						floors.get(cF).printMap();

					} else if(e.getKeyCode() == KeyEvent.VK_T){
						aimScreen = !aimScreen;
						targetPos = player.e.getPos();

					} else if(e.getKeyCode() == KeyEvent.VK_SPACE){
						// open attack selections
						// area.append("space.\n");
						// if already open attack
					} else if(e.getKeyCode() == KeyEvent.VK_G){
						// get
						Inventory tileInv = floors.get(cF).tileMap[player.e.y][player.e.x].inventory;
						Inventory selectedInv = null;
						if(tileInv.isEmpty()){
							for(Entity n: floors.get(cF).entities.values()){
								if(n.SE != null && n.x == player.e.x && n.y == player.e.y){
									if(!n.SE.isLocked){
										selectedInv = n.inv;
										appendText("Pick up what?");
									}else{											
										appendText("It's locked.");
										return;
									}
									// n.SE.interact(player.e,'g');
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
						player.e.inv.printContents(false);

						if(!player.e.inv.isEmpty()) inventoryScreen = true;

					}else if(e.getKeyCode() == KeyEvent.VK_O){
						for(Entity n: floors.get(cF).entities.values()){
							if(n.SE != null && n.x == player.e.x && n.y == player.e.y){
								n.SE.interact(player.e,'o');
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
								if(player.e.SP >= a.s){
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
				}else if(e.getKeyCode() == KeyEvent.VK_SPACE){
					// restart
					startGame();
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

		floors.put(cF, new Map(map_h,map_w,map_fill,rng));
		ropePoint = floors.get(cF).getPosition(2);

		System.out.println("@@@ gen = "+(System.currentTimeMillis()-time)+"ms @@@");

		player = new Player(ropePoint.x,ropePoint.y,floors.get(cF));

		/** TEMP **/
		Weapon dagger = new Weapon("dagger");
		player.e.inv.addItem(dagger);
		// TODO FIX THIS
		player.equip((Weapon) player.e.inv.inv.get(player.e.inv.getFirstItem()));

		Missile darts = new Missile("darts");
		player.e.inv.addItem(darts);
		player.quiver(player.e.inv.inv.get(player.e.inv.getItemTypeChar(Item.Items.DART)));
		
		// TODO: init Item.Scrolls & Item.Potions
		for(Scroll i: Item.scrolls){
			randomNames.put(i.getTypeName(), "scroll(s) labeled "+((randomName()+" "+randomName()).toUpperCase()));
		}
		for(Potion i: Item.potions){
			randomNames.put(i.getTypeName(), randomPotionName(i));
		}

		/** temporary **/
		int mobs = rng.nextInt(8)+16;
		for (int i = 0; i < mobs; i++) {
			Point t;
			do{
				t = floors.get(cF).randomOpenSpace();				
			}while(Math.abs(t.x-player.e.x) <=3 && Math.abs(t.y-player.e.y) <=3);
			
			System.out.println("point picked");
			new Entity(Creature.randomType(), t.x, t.y, floors.get(cF));
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
		floors.put(cF+1, new Map(map_h, map_w, map_fill, rng));
		changeFloor(cF+1,true,true);
	}

	public static void changeFloor(int floor, boolean down, boolean isNew){
		cF = floor;
		Point startPoint= floors.get(cF).getPosition(2);
		// TODO: update for multiple stairs
		if(!down) startPoint= floors.get(cF).getPosition(3);
		player.map = floors.get(cF);
		floors.get(cF).player = player.e;
		player.e.x = startPoint.x;
		player.e.y = startPoint.y;
		player.e.map = floors.get(cF);
		// floors.get(currentFloor).player = player.e;

		if(isNew){

			/** TEMPORARY **/
			Point t;
			int mobs = (rng.nextInt(6)+10)*(cF+1);
			for (int i = 0; i < mobs; i++) {
				do{
					t = floors.get(cF).randomOpenSpace();				
				}while(Math.abs(t.x-player.e.x) <=3 && Math.abs(t.y-player.e.y) <=3);
				new Entity(Creature.randomType(), t.x, t.y, floors.get(cF));
			}

			int items = rng.nextInt(6)+8;
			for(int i = 0; i < items; i++){
				t = floors.get(cF).randomOpenSpace();
				System.out.println("adding item #"+i);
				
				// TODO: CHANGE THIS
				floors.get(cF).tileMap[t.y][t.x].inventory.addItem(Item.randomItemType(level));
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

	public static int JFrame_WIDTH = 1500;
	public static int JFrame_HEIGHT = 1000;
	public static JPanel consolePanel = new JPanel();

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

		player.e.HP = ActionLibrary.round(player.e.HP, 1);

		stats.append("  <Player Name>\t\t                     .\n\n");
		stats.append("  HP: "+player.e.HP+"\n");
		stats.append("  SP: "+player.e.SP+"\n");
		stats.append("  STR: "+player.e.STRENGTH+"\n");
		stats.append("  SAT: "+ActionLibrary.round(player.e.SAT,1)+"\n");

		if(player.e.weapon != null){			
			stats.append("  Weapon: "+player.e.weapon.getDisplayName());
		}else{
			stats.append("  Weapon: none");
		}
		stats.append("\n");

		if(player.e.quivered != null){			
			stats.append("  Quivr'd: "+player.e.quivered.getDisplayName());
		}else{
			stats.append("  Quivr'd: none");
		}
		stats.append("\n");


		String line = "  ";
		for(Status s: player.e.statuses.keySet()){
			line+=s.name();
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
		Point pos = player.e.getPos();
		// System.out.println(pos.toString());
		refreshFrame(render(pos.x,pos.y));
	}

	public static Point lastPos;
	public static void takeTurn(){
		Inventory floorInv = floors.get(cF).tileMap[player.e.y][player.e.x].inventory;
		if(!floorInv.isEmpty() && (lastPos!=null && !player.e.getPos().equals(lastPos))){
			floorInv.printContents(true);
		}
		player.e.HP = ActionLibrary.round(player.e.HP, 1);

		// double playerHP = player.e.HP;
		// TODO: move max to e
		player.e.upkeep();

		player.e.SP =  Math.min(player.e.SP + Creature.PLAYER.SP_REGEN, player.e.creature.SP_MAX);
		ArrayList<Entity> dead = new ArrayList<Entity>();

		// int[] order = floors.get(currentFloor).getEntityPriority();
		// for(int i: order){
		Queue<Entity> q = new LinkedList<Entity>();
		for(Entity e: floors.get(cF).entities.values()){
			e.awakeCheck();
			if(e.awake) q.add(e);
		}

		while(!q.isEmpty()){
			Entity e = q.remove();
			// TODO: add mob sleep/detection stuff
			// Entity e = floors.get(currentFloor).entities.get(i);
			Entity.turnEnding ending = e.takeTurn();
			System.out.println(e.name+" takes a turn.");
			if(ending.equals(Entity.turnEnding.DEAD)){
				e.x = -1;
				e.y = -1;
				dead.add(e);
			}else if(ending.equals(Entity.turnEnding.WAITING)){
				System.out.println(e.name+" is waiting.");
				if(!allWaiting(q)){
					q.add(e);
				}else{
				}
			}
		}

		for(Entity e: dead){
			e.die();
		}

		Point pos = player.e.getPos();
		lastPos = pos;
		// if(player.e.HP != playerHP) appendText("Player HP = " + ActionLibrary.round(player.e.HP,2));
		if(player.e.HP < 0.05){
			appendText("You die...");
			running = false;
		}
		refreshFrame(render(pos.x,pos.y));
	}

	public static boolean allWaiting(Queue<Entity> q){
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

	public static int directionValue(int keyCode, boolean controlDown, boolean shiftDown){
		if (keyCode == KeyEvent.VK_K || keyCode == KeyEvent.VK_UP) {
			return 0;
		} else if (keyCode == KeyEvent.VK_H || keyCode == KeyEvent.VK_LEFT) {
			if(controlDown){
				return 6;
			}else if(shiftDown){
				return 7;
			}else{
				return 3;
			}
		} else if (keyCode == KeyEvent.VK_J || keyCode == KeyEvent.VK_DOWN) {
			return 2;
		} else if (keyCode == KeyEvent.VK_L || keyCode == KeyEvent.VK_RIGHT) {
			if(controlDown){
				return 5;
			}else if(shiftDown){
				return 4;
			}else{
				return 1;
			}
		}else if(keyCode == KeyEvent.VK_Y){
			return 7;
		}else if(keyCode == KeyEvent.VK_U){
			return 4;
		}else if(keyCode == KeyEvent.VK_B){
			return 6;
		}else if(keyCode == KeyEvent.VK_N){
			return 5;
		}
		return -1;
	}

	public static enum Direction  {
		UP 				(0,-1),
		RIGHT 			(+1,0),
		DOWN 			(0,+1),
		LEFT			(-1,0),
		UP_RIGHT		(+1,-1),
		DOWN_RIGHT		(+1,+1),
		DOWN_LEFT		(-1,+1),
		UP_LEFT			(-1,-1);

		Point p;
		
		Direction(int x, int y){
			p = new Point(x,y);
		}
	}
}