/** @author carterbrown-27 **/

import java.io.*;
import javax.imageio.*;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.logging.Logger;

// TODO (*) Decouple GUI from Main, Switch to JavaFX for GUI, create JApplet from JavaFX.
public class Main {

	// TODO (+) saving
	//	public static int seed = 12345678;
	public static Random rng;
	//	public static boolean randSeed = true;

	public static boolean running;
	
	public static View view;

	public static Player player;
	public static String playerName = "Hillstyle";

	public static final int MAP_H = 45;
	public static final int MAP_W = 45;
	public static final int MAP_FILL = 49;

	public static int ticks = 0;

	public static double lastPress;
	public static double interval = 70; // 70ms input interval.

	private static GameState state;
	private static InventorySelectAction invSelAction;

	public static char selectedItem;
	public static Point targetPos;

	private static HashMap<Integer,Map> floors;
	public static int floorNumber;
	public static Map currentMap;
	public static Inventory currentInventory;
	public static Point ropePoint;
	public static Point lastPos;

	public static int seed;

	private static final Logger logger = Logger.getLogger(Main.class.getName());
	public static StringHelper stringHelper;

	public static void main(String[] args){

		running = true;
		
		state = GameState.REGULAR;
		invSelAction = InventorySelectAction.NONE;

		rng = new Random();

		lastPress = System.currentTimeMillis();

		floors = new HashMap<>();

		seed = rng.nextInt(Integer.MAX_VALUE);
		rng = new Random(seed);

		stringHelper = new StringHelper(rng);

		view = new View();
		startGame();
		logger.info("Seed = " + seed);

		// controls
		view.getFrame().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e){
				logger.fine("Key Pressed "+KeyEvent.getKeyText(e.getKeyCode()));

				if(!running){
					logger.warning("Game not running.");
					if(e.getKeyCode() == KeyEvent.VK_SPACE){
						// restart
						startGame();
					}
					return;
				}

				if (System.currentTimeMillis()-lastPress>=interval) {
					logger.fine("Valid Press");
					lastPress = System.currentTimeMillis();
				}else {
					return;
				}

				if(!state.equals(GameState.PICKUP) && !state.equals(GameState.INVENTORY)){
					// System.out.println("flag 1");
					view.clearText();
				}

				// ESCAPE takes precedent.
				if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					state = GameState.REGULAR;
					return;
				}

				if(state == GameState.REGULAR) {
					if (directionValue(e) != null) {
						// Directional Key: corresponding move/action
						logger.fine("Directional Key");
						Direction dir = directionValue(e);
						player.act_adj(dir);

					}else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						// ENTER: Descend/Ascend Staircase
						handleUseStaircase();

					} else if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_P) {
						// Ctrl-P: Prints map to console
						currentMap.printMap();

					} else if(e.getKeyCode() == KeyEvent.VK_T) {
						// T: toggles targeting mode
						state = !(state == GameState.TARGETING) ? GameState.TARGETING : GameState.REGULAR;
						// Crosshairs' origin = player.
						targetPos = player.getPos();

					} else if(e.getKeyCode() == KeyEvent.VK_SPACE) {
						// SPACE: Open action selections
						// TODO (A) Implement

					} else if(e.getKeyCode() == KeyEvent.VK_G) {
						// G: get item
						handleGet();

					} else if(e.getKeyCode() == KeyEvent.VK_I){
						// I: open inventory
						player.inv.printContents(false);

						if(!player.inv.isEmpty()) {
							state = GameState.INVENTORY;
						}

					} else if(e.getKeyCode() == KeyEvent.VK_O){
						// O: Open Chest
						// TODO: interact-able checks handled by "Interactable" interface
						for(Entity n: currentMap.entities) {
							if(n instanceof StaticEntity) {
								StaticEntity se = (StaticEntity) n;
								if(se.getPos().equals(player.getPos())){
									se.interact(player,'o');
								}
							}
						}

					}else if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C){
						// Ctrl-C: Clear
						view.clearText();
					}else if(e.getKeyCode() == KeyEvent.VK_5){
						// 5: Rest
						player.startRest();
					}else if(e.getKeyCode() == KeyEvent.VK_X){
						// X: Map Overview	
						// TODO (+) Add Map Overview
					}else{
						// other keys: abilities
						for(Player.Ability a: Player.Ability.values()){
							if(e.getKeyCode() == a.k){
								if(player.getSP() >= a.s){
									player.select(a);
									view.appendText(a.name);
								}else{
									view.appendText("Not enough stamina!");
									player.deselect();
								}
								break;
							}
						}
					}
				} else {
					char k = e.getKeyChar();
					switch(state) {
					case PICKUP : {
						handleItemPickup(k);
						break;
					}

					case INVENTORY : {
						handleInventory(k);
						break;
					}

					case ITEM_SCREEN : {
						handleItemScreen(k);
						break;
					}

					case INVENTORY_SELECT : {
						handleInventorySelect(k);
						break;
					}
					
					case TARGETING : {
						handleTargeting(e);
						break;
					}
					
					// Doesn't apply to REGULAR here, as in this branch REGULAR = False.
					default : {
						logger.severe("State not recognized or fallthrough occured.");
					}
					
					}
				}
			}
		});

		// System.out.println(ropePoint.toString());
		view.refreshFrame(render(ropePoint.x,ropePoint.y));
	}

	public static enum GameState {
		REGULAR,
		INVENTORY,
		PICKUP,
		ITEM_SCREEN,
		TARGETING,
		INVENTORY_SELECT
	}

	public static void setGameState(GameState s) {
		state = s;
	}

	public static enum InventorySelectAction {
		NONE,
		IDENTIFY,
		ENCHANT,
		ETC
	}
	
	public static void setInvSelAction(InventorySelectAction a) {
		invSelAction = a;
	}

	public static void handleItemPickup(char k) {
		logger.fine("Item Pickup Handler called.");
		if(k < 'a' || k > 'z'){
			currentInventory = null;
			state = GameState.REGULAR;
			return;
		}

		if(currentInventory==null){
			currentInventory = currentMap.tileMap[player.getY()][player.getX()].inventory;
		}

		if (currentInventory.contains(k)) {
			player.pickUp(k,currentInventory);
			if(currentInventory.isEmpty()){
				currentInventory = null;
				state = GameState.REGULAR;
			}
		}
	}
	
	public static void handleTargeting(KeyEvent e) {
		Direction dir = directionValue(e);
		if(dir != null) {
			// TODO (+) Aiming visuals & continual L.O.S. check
			targetPos = Direction.translate(targetPos,dir);
		}else if (e.getKeyCode() == KeyEvent.VK_ENTER){
			handleTargetConfirm();
		}
	}
	
	public static void handleItemScreen(char k) {
		logger.fine("Item Screen Handler called.");
		char c = selectedItem;
		Item i = player.inv.getItem(c);

		if(!Character.isAlphabetic(k) || !i.actionsContains(k)) {
			view.appendText(k + " is not an option.");
			return;
		}

		// NOTE: letters for actions must be unique across all items
		switch(k) {

		case 'd' : {
			i.drop(player);
			break;
		}

		case 'a' : {
			// reassign
			break;
		}

		}

		if(i instanceof Equippable){
			Equippable eq = (Equippable) i;

			// TODO: differentiate
			switch(k) {

			case 'w' : {
				view.appendText("You wield your "+i.getDisplayName());
				eq.equip(player);
				break;
			}

			case 'p' : {
				view.appendText("You put on your "+i.getDisplayName());
				eq.equip(player);
				break;
			}

			case 'u' : {
				view.appendText("You unwield your "+i.getDisplayName());
				eq.unequip(player);
				break;
			}

			case 't' : {
				view.appendText("You take off your "+i.getDisplayName());
				eq.unequip(player);
				break;
			}

			}
			// TODO (A) Implement
		}

		if(i instanceof Consumable){
			Consumable cnsm = (Consumable) i;
			// TODO (A) Implement
			switch(k) {

			case 'r' : {
				// read effect
				view.appendText("You read the"+i.getDisplayName());
				cnsm.use(player);
				break;
			}

			case 'q' : {
				// quaff effect
				view.appendText("You quaff the "+i.getDisplayName());
				cnsm.use(player);
				break;
			}

			case 'e' : {
				view.appendText("You eat the "+i.getDisplayName());
				cnsm.use(player);
				break;
			}
			}
		}

		state = GameState.REGULAR;
		view.refreshStats();
	}

	public static void handleInventory(char k) {
		logger.fine("Inventory Handler called.");
		view.clearText();
		
		boolean selected = false;
		if (player.inv.contains(k)) {
			selected = true;
			// open item menu
			// TODO (R) Review
			Item i = player.inv.getItem(k);

			view.appendText(i+" selected.");
			view.appendText(i.getDescription());

			for(String s: i.listPrompts()) {
				view.appendText(s);
			}

			selectedItem = k;
		}

		if(selected) {
			state = GameState.ITEM_SCREEN;
		} else {
			state = GameState.REGULAR;
		}
	}

	public static void handleInventorySelect(char k) {
		logger.fine("Inventory Selection Handler called.");
		
		boolean flag = false;
		if (player.inv.contains(k)) {
			Item i = player.inv.getItem(k);
			if(invSelAction == InventorySelectAction.IDENTIFY  && !player.isItemIdentified(i)){
				flag = true;
				player.identify(i);
			}else if(invSelAction == InventorySelectAction.ENCHANT){
				flag = true;
			}
		}

		if(!flag){
			view.appendText(k+" is not a valid option.");
		}else{
			state = GameState.REGULAR;
			invSelAction = InventorySelectAction.NONE;
		}
	}

	public static void handleTargetConfirm() {
		ArrayList<Point> line = FOV.bresenhamLine(player.getPos(), targetPos);

		boolean f = true;
		for(Point p: line){
			if(p.equals(player.getPos()) || p.equals(line.get(line.size()-1))){
				continue;
			}

			if(!currentMap.isFullOpen(p.x, p.y)){
				f = false;
				break;
			}
		}
		
		// TODO: (A) Implement
		if(f){
			// valid target
			view.appendText("valid target");
		}else{
			// invalid
			view.appendText("invalid target");
		}
		state = GameState.REGULAR;
	}

	public static void handleGet() {
		Inventory tileInv = currentMap.tileMap[player.getY()][player.getX()].inventory;
		Inventory selectedInv = null;


		// TODO (F) Fix Chests
		if(tileInv.isEmpty()){
			for(Entity n: currentMap.entities){
				if(n instanceof StaticEntity) {
					StaticEntity se = (StaticEntity) n;
					if(se.getPos().equals(player.getPos())){
						if(!se.isLocked){
							selectedInv = se.inv;
							view.appendText("Pick up what?");
						}else{											
							view.appendText("It's locked.");
							return;
						}
						// n.SE.interact(player,'g');
						break;
					}
				}
			}
		}else{
			selectedInv = tileInv;
		}

		if(selectedInv == null || selectedInv.isEmpty()){
			view.appendText("There is nothing here.");
			return;
		}

		if(selectedInv.isOneItem()){
			player.pickUp(selectedInv.getFirstItem(),selectedInv);
		}else if(!selectedInv.isEmpty()){
			state = GameState.PICKUP;
			currentInventory = selectedInv;
			view.appendText("Pick up what?");
		}
	}

	public static void handleUseStaircase() {
		if(currentMap.valueAt(player.getPos()) == 3){
			if(floors.size()<=floorNumber+1){
				newFloor();
			}else{
				changeFloor(floorNumber+1,true,false);
			}
		}else if(currentMap.valueAt(player.getPos()) == 2 && floorNumber > 0){
			// floors.set(currentFloor, (new Map(floors.get(currentFloor))));
			changeFloor(floorNumber-1,false,false);
		}
		view.appendText("Current Floor: "+floorNumber);
	}

	public static void startGame(){
		floors.clear();
		floorNumber = 0;

		floors.put(floorNumber,  new Map(MAP_H, MAP_W, MAP_FILL));
		currentMap = floors.get(floorNumber);
		ropePoint = currentMap.getPosition(2);
		player = new Player(ropePoint.x,ropePoint.y, currentMap);

		changeFloor(floorNumber, true, true);

		// TODO (T) TEMP player default inventory
		Weapon dagger = new Weapon("dagger");
		player.inv.addItem(dagger);
		player.equip(dagger);

		Missile darts = new Missile("throwing dart");
		player.inv.addItem(darts);
		player.equip(darts);

		running = true;
	}

	public static void newFloor(){
		// floors.set(currentFloor, new Map(floors.get(currentFloor)));
		blackOverlay();
		// floors.replace(currentFloor, new Map(map_h,map_w,map_fill,rng));
		logger.fine("Now " + floors.size() + " floors.");
		floors.put(floorNumber+1, new Map(MAP_H, MAP_W, MAP_FILL));
		changeFloor(floorNumber+1,true,true);
	}

	public static void changeFloor(int floor, boolean down, boolean isNew){
		long time = System.currentTimeMillis();
		floorNumber = floor;
		currentMap = floors.get(floorNumber);
		Point startPoint= currentMap.getPosition(2);

		// TODO: update for multiple stairs
		if(!down) startPoint= currentMap.getPosition(3);
		player.map = currentMap;
		player.setPos(startPoint);
		currentMap.player = player;

		if(isNew){

			// TODO (T) TEMP populate level with mobs
			int mobs = rng.nextInt(8)+16;
			for (int i = 0; i < mobs; i++) {
				Point t;
				do{
					t = currentMap.randomOpenSpace();				
					// make sure mobs aren't within 3 tiles of player.
				}while(Math.abs(t.x-player.getX()) <=3 && Math.abs(t.y-player.getY()) <=3);

				// logger.info("point picked");
				// TODO: tierCalculation.
				logger.fine("Adding Entity...");
				new Creature(floorNumber,t,currentMap);
			}

			// TODO (T) TEMP populate level with items
			int items = rng.nextInt(6)+14; // 6+8, 6+28
			for(int i = 0; i < items; i++){
				Point t = currentMap.randomOpenSpace();
				Item item = Item.randomItem(floorNumber);
				logger.fine("Adding item: "+ item.getDisplayName());
				currentMap.tileMap[t.y][t.x].inventory.addItem(item);
			}

			// TODO (T) TEMP populate level with chests/keys
			int chests = rng.nextInt(2)+1;
			for(int i = 0; i < chests; i++){
				Point t = currentMap.randomEmptySpace();
				logger.fine("adding Chest/Key pair");
				currentMap.tileMap[t.y][t.x].inventory.addItem(new Key("Silver Key",floorNumber));
				t = currentMap.randomEmptySpace();

				currentMap.entities.add(new StaticEntity("chest", t, currentMap));
			}
		}

		try{
			File output = new File(String.format("renders/%d_%d-t%d.png",seed, floorNumber, System.currentTimeMillis()));
			ImageIO.write(currentMap.renderMap(), "png", output);
		}catch(Exception e){
			e.printStackTrace();
		}

		System.out.printf("@@@ Saved Render = %dms @@@\n", System.currentTimeMillis()-time);

		if(view.getFrame()==null) view.init();
		view.refreshFrame(render(startPoint.x,startPoint.y));

		System.out.printf("@@@ Frame Initialized = %dms @@@\n",System.currentTimeMillis()-time);
	}

	public static void blackOverlay(){
		// TODO (F) load file into memory
		try{
			view.refreshFrame(ImageIO.read(new File("imgs/descendingOverlay.png")));
		}catch(Exception e){
			logger.warning("Black overlay not found.");
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

	// TODO (X) Overhaul
	public static void takeTurn(){
		Inventory floorInv = currentMap.tileMap[player.getY()][player.getX()].inventory;
		if(!floorInv.isEmpty() && (lastPos!=null && !player.getPos().equals(lastPos))){
			floorInv.printContents(true);
		}

		player.upkeep();

		ArrayList<Creature> dead = new ArrayList<>();

		// int[] order = floors.get(currentFloor).getEntityPriority();
		// for(int i: order){
		Queue<Creature> creatureQueue = new LinkedList<>();

		for(Entity e: currentMap.entities){
			if(e instanceof Creature) {
				Creature c = (Creature) e;
				c.awakeCheck();
				// TODO (T) TEMP
				if(c.getHP() <= 0) {
					dead.add(c);
				}
				else if(c.awake) creatureQueue.add(c);
			}
		}
		
		// TODO (T) TEMP
		for(Creature c: creatureQueue) {
			c.takeTurn();
		}

		// TODO (X) Overhaul
		//		while(!creatureQueue.isEmpty()){
		//			Creature c = creatureQueue.remove();
		//			// TODO (+) add mob sleep/detection stuff
		//			// Entity e = floors.get(currentFloor).entities.get(i);
		//			Entity.turnEnding ending = c.takeTurn();
		//			System.out.println(c.getName()+" takes a turn.");
		//			if(ending.equals(Entity.turnEnding.DEAD)){
		//				// TODO (R) Review
		//				c.setPos(new Point(-1,-1));
		//				dead.add(c);
		//			}else if(ending.equals(Entity.turnEnding.WAITING)){
		//				System.out.println(c.getName()+" is waiting.");
		//				if(!allWaiting(creatureQueue)){
		//					creatureQueue.add(c);
		//				}else{
		//
		//				}
		//			}
		//		}

		for(Creature c: dead){
			c.die();
		}

		// TODO (M) death logic to Player.
		Point pos = player.getPos();
		lastPos = pos;
		// if(player.HP != playerHP) appendText("Player HP = " + ActionLibrary.round(player.HP,2));
		if(player.getHP() < 0.05){
			view.appendText("You die...");
			running = false;
		}
		view.refreshFrame(render(pos.x,pos.y));
	}

	@Deprecated
	public static boolean allWaiting(Queue<Creature> q){
		while(!q.isEmpty()){
			/* if(!q.remove().waiting) */ return false;
		}
		return true;
	}

	public static BufferedImage render(int x, int y){
		BufferedImage img = currentMap.render_vig(x, y, player.viewDis, player.luminosity);
		img = resize(img, img.getWidth()*3, img.getHeight()*3);
		return img;
	}
	
	/**
	 * @param keyCode: the KeyCode from the KeyEvent
	 * @param controlDown: is control pressed
	 * @param shiftDown: is shift pressed
	 * @return the direction corresponding to the keys pressed
	 * 
	 * These are how keys correspond to Directions:
	 * 
	 * YKU
	 * H@L	
	 * BJN
	 * 
	 * or Arrow Keys + CTRL (adds down to L/R) + SHIFT (adds up to L/R)
	 * shift-left	up		shift-right
	 * left			@		right
	 * ctrl-left	down	ctrl-right
	 */
	public static Direction directionValue(int keyCode, boolean controlDown, boolean shiftDown){
		logger.finer("dir check");
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
				return Direction.RIGHT;
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
	
	public static Direction directionValue(KeyEvent e) {
		return directionValue(e.getKeyCode(), e.isControlDown(), e.isShiftDown());
	}
}