import java.awt.image.BufferedImage;
import java.util.HashMap;

public class Inventory {

	// all items are in generalInventory, specifics in the subs.
	private HashMap<Character,Item> inventoryMap = new HashMap<>();
	private HashMap<Integer,Integer> keys = new HashMap<>();

	public Item getItem(Character c) {
		return inventoryMap.get(c);
	}
	
	public boolean isEmpty(){
		return inventoryMap.isEmpty();
	}

	public boolean isOneItem(){
		return inventoryMap.size()==1;
	}
	
	public boolean containsUnidentified(){
		for(Item i: inventoryMap.values()){
			if(i.isUnknown() && !Main.getPlayer().isItemIdentified(i)){
				return true;
			}
		}
		return false;
	}

	public void pickUp(char c, Entity destination){
		// TODO (R) rework key system
		Item i = deleteItem(c);
		if(i instanceof Key){
			Key k = (Key) i;
			destination.getInv().pickupKey(k.getFloor());
		}else{
			destination.getInv().addItem(i);			
		}
	}

	public void printContents(boolean floor){
		if(floor){
			Main.getView().appendText("Things here:");
		}else{
			Main.getView().appendText("Your Inventory:");
		}
		
		for(char c: inventoryMap.keySet()){
			Item i = inventoryMap.get(c);
			Main.getView().appendText(i.toString());
		}
	}

	public BufferedImage drawPile(){
		// returns the first item, TODO (+) add visual pile indicator
		for(Item i: inventoryMap.values()){
			return i.getSprite();
		}
		return null;
	}

	public char getFirstItem(){
		// ordered by letters
		for(char c = 'a'; c <= 'z'; c++){
			if(inventoryMap.containsKey(c)){
				return c;
			}
		}
		return '!';
	}
	public char getFirstOpen(){
		// ordered by letters
		for(char c = 'a'; c <= 'z'; c++){
			if(!inventoryMap.containsKey(c)){
				return c;
			}
		}
		return '!';
	}

	// TODO: (A) Implement stacking
	public <T extends Item> void addItem(T i){
		if (i.isStackable()) {
			// TODO (F) handle better
			for (Item t: inventoryMap.values()) {
				if (t.getTypeName().equals(i.getTypeName())){
					t.changeAmount(i.getAmount());
					return;
				}
			}
		}

		// if not added to stack
		char firstOpen = getFirstOpen();
		if (firstOpen != '!') {
			inventoryMap.put(firstOpen, i);
			i.setInventoryID(firstOpen);
		} else {
			// TODO (A) Implement returning item to floor.
			Main.getView().appendText("Inventory full.");
			return;
		}
	}

	// TODO (F) refactor, move
	public void removeOne(char c){
		inventoryMap.get(c).changeAmount(-1);
		if(inventoryMap.get(c).getAmount() <= 0){
			deleteItem(c);		
		}
	}
	
	public Item deleteItem(char c) {
		if(!inventoryMap.containsKey(c)) return null;
		Item i = inventoryMap.remove(c);
		// TODO (F) Unequip if applicable

		return i;
	}
	
	public Item deleteItem(Item i) {
		for(char key: inventoryMap.keySet()) {
			if(inventoryMap.get(key) == i) {
				return deleteItem(key);
			}
		}
		return null;
	}
	
	public void makeRandomInventory(int tier, int amount){
		// random n from amt*66% to amt*133%.
		double n = (double) amount*2/3 + (Main.getRng().nextDouble() * (double) amount*2/3);
		n = (int) ActionLibrary.round(n, 0);
		
		for(int x = 0; x < n; x++){
			// TODO (R) Review
			addItem(Item.randomItem(tier));
		}
	}
	
	public boolean contains(char c) {
		return inventoryMap.containsKey(c);
	}
	
	// TODO (F) Replace
	// public <T extends Item & Consumable> int getStackSize(T type){
		// TODO: variation
		// return type.commonStackSize();
	// }
	
//	public void dropAll(char c, Entity e){
//		e.map.tileMap[e.getY()][e.getX()].inventory.addItem(inventoryMap.remove(c));
//	}

	public void switchItem(char itemToMove, char destination){
		if(inventoryMap.containsKey(itemToMove)){
			if(inventoryMap.containsKey(destination)){ // if there's an item in the target spot
				Item temp = inventoryMap.get(destination);
				inventoryMap.replace(destination,inventoryMap.get(itemToMove));
				inventoryMap.replace(itemToMove, temp); // switch the items
			}else{
				inventoryMap.put(destination, inventoryMap.get(itemToMove));
				inventoryMap.remove(itemToMove);
			}
		}
	}
	
	
	// TODO (+) print list of keys
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
