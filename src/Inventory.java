import java.awt.image.BufferedImage;
import java.util.HashMap;

public class Inventory {

	// all items are in generalInventory, specifics in the subs.
	private HashMap<Character,Item> generalInventory = new HashMap<>();
	private HashMap<Integer,Integer> keys = new HashMap<>();
	
	private ArmourSet armourSet = new ArmourSet();

	private char firstOpen = 'a';
	
	Inventory(){
		
	}
	
	public Item getItem(Character c) {
		return generalInventory.get(c);
	}
	
	
	public boolean isEmpty(){
		return generalInventory.isEmpty();
	}

	public boolean isOneItem(){
		return generalInventory.size()==1;
	}
	
	public boolean containsUnidentified(){
		for(Item i: generalInventory.values()){
			if(i.isUnknown() && !Main.player.isItemIdentified(i)){
				return true;
			}
		}
		return false;
	}

	public void pickUp(char c, Entity destination){
		// TODO: rework key system
		Item i = generalInventory.remove(c);
		if(SPECIAL){
			if(KEY){
				destination.pickupKey(i.floorFoundOn);
			}
		}else{
			destination.inv.addItem(i);			
		}
	}

	public void printContents(boolean floor){
		if(floor){
			Main.appendText("Things here:");
		}else{
			Main.appendText("Your Inventory:");
		}
		
		for(char c: generalInventory.keySet()){
			Item i = generalInventory.get(c);
			Main.appendText(i.toString());
		}
	}


	public static boolean isVowelStart(String str){
		if(str.length()==0) return false;
		char c = str.charAt(0);
		if(c=='a'||c=='i'||c=='e'||c=='o'||c=='u') return true;
		return false;
	}


	public BufferedImage drawPile(){
		// returns the first item, TODO: add visual pile indicator
		for(Item i: generalInventory.values()){
			return i.getSprite();
		}
		return null;
	}
	
	@Deprecated
	public char getItemTypeChar(Item t){
		for(char c = 'a'; c <= 'z'; c++){
			// TODO: fix
			if(generalInventory.containsKey(c) && generalInventory.get(c).equals(t)){
				return c;
			}
		}
		return '!';
	}

	@Deprecated
	public char getFirstItem(){
		for(char c = 'a'; c <= 'z'; c++){
			if(generalInventory.containsKey(c)){
				return c;
			}
		}
		return '!';
	}
	public void getFirstOpen(){
		for(char c = 'a'; c <= 'z'; c++){
			if(!generalInventory.containsKey(c)){
				firstOpen = c;
				return;
			}
		}
		firstOpen = '!';
	}

	public <T extends Item> void addItem(T i){
		// stack items
		if (i.isStackable()) {
			// TODO handle better
			for (Item t : generalInventory.values()) {
				if (t.getDisplayName().equals(i.getDisplayName())){
					t.changeAmount(i.getAmount());
					return;
				}
			}
		}

		// if not added to stack
		getFirstOpen();
		if (firstOpen != '!') {
			generalInventory.put(firstOpen, i);
		} else {
			Main.appendText("Inventory full.");
			return;
		}
	}

	// TODO: refactor, move
	public void removeItem(char c){
		generalInventory.get(c).changeAmount(-1);
		if(generalInventory.get(c).getAmount() <= 0){
			generalInventory.remove(c);			
		}
	}
	
	public void makeRandomInventory(int tier, int amount){
		double n = (double) amount*2/3 + (Main.rng.nextDouble() * (double) amount*2/3);
		n = (int) ActionLibrary.round(n, 0);
		
		for(int x = 0; x < n; x++){
			// TODO: change
			Item.ItemType t = Item.randomItemType(tier);
			addItem();
		}
	}
	
	public boolean contains(char c) {
		return generalInventory.containsKey(c);
	}
	
	// TODO: replace
	// public <T extends Item & Consumable> int getStackSize(T type){
		// TODO: variation
		// return type.commonStackSize();
	// }
	
	public void dropAll(char c, Entity e){
		e.map.tileMap[e.getY()][e.getX()].inventory.addItem(generalInventory.remove(c));
	}

	public void switchItem(char itemToMove, char destination){
		if(generalInventory.containsKey(itemToMove)){
			if(generalInventory.containsKey(destination)){ // if there's an item in the target spot
				Item temp = generalInventory.get(destination);
				generalInventory.replace(destination,generalInventory.get(itemToMove));
				generalInventory.replace(itemToMove, temp); // switch the items
			}else{
				generalInventory.put(destination, generalInventory.get(itemToMove));
				generalInventory.remove(itemToMove);
			}
		}
	}
	
	public boolean hasKey(int floor) {
		return keys.containsKey(floor);
	}
	
	public void pickupKey(int floor){
		if(keys.containsKey(floor)){
			keys.replace(floor,keys.get(floor)+1);
		}else{
			keys.put(floor,1);
		}
	}
	
	public void useKey(int floor){
		if(keys.containsKey(floor)){
			keys.replace(floor,keys.get(floor)-1);
			if(keys.get(floor) <= 0){
				keys.remove(floor);
			}
		}
	}
}
