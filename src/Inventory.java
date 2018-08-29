import java.awt.image.BufferedImage;
import java.util.HashMap;

public class Inventory {

	HashMap<Character,Item> inv = new HashMap<Character,Item>();
	HashMap<Integer,Integer> keys = new HashMap<Integer,Integer>();

	char firstOpen = 'a';

	public boolean isEmpty(){
		return inv.isEmpty();
	}

	public boolean isOneItem(){
		return inv.size()==1;
	}
	
	public boolean containsUnidentified(){
		for(Item i: inv.values()){
			if(i.isUnknown() && !Main.player.identifiedItems.containsKey(i.type)){
				return true;
			}
		}
		return false;
	}

	public void pickUp(char c, Entity destination){
		Item i;
		if(isOneItem()){
			i = inv.remove(getFirstItem());
		}else{
			i = inv.remove(c);
		}
		if(i.type.supertype.equals(Item.Items.Item_Supertype.SPECIAL)){
			if(i.isInClass(Item.Items.keys)){
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
		for(char c: inv.keySet()){
			Item i = inv.get(c);
			String quantity;
			if(i.amount==1){
				if(isVowelStart(i.getDisplayName())){
					quantity = "an";
				}else{
					quantity = "a";
				}
			}else{
				quantity = String.valueOf(i.amount);
			}

			String line = "";
			line+= c+" - "+quantity+" "+i.getDisplayName();
			
			if(i.weilded) line+=(" (weilded)");
			if(i.worn) line+=(" (worn)");
			Main.appendText(line);

		}
	}


	public boolean isVowelStart(String str){
		if(str.length()==0) return false;
		char c = str.charAt(0);
		if(c=='a'||c=='i'||c=='e'||c=='o'||c=='u') return true;
		return false;
	}


	public BufferedImage drawPile(){
		for(Item i: inv.values()){
			if(i.type.supertype.equals(Item.Items.Item_Supertype.POTION)){
				return Main.potionColours.get(i.type).image;
			}else{
				return i.sprite;
			}
		}
		return null;
	}
	
	public char getItemTypeChar(Item.Items i){
		for(char c = 'a'; c <= 'z'; c++){
			if(inv.get(c).type.equals(i)){
				return c;
			}
		}
		return '!';
	}

	public char getFirstItem(){
		for(char c = 'a'; c <= 'z'; c++){
			if(inv.containsKey(c)){
				return c;
			}
		}
		return '!';
	}
	public void getFirstOpen(){
		for(char c = 'a'; c <= 'z'; c++){
			if(!inv.containsKey(c)){
				firstOpen = c;
				return;
			}
		}
		firstOpen = '!';
	}

	public void addItem(Item i){
		// stack items
		if (i.isStackable()) {
			for (Item t : inv.values()) {
				if (t.name.equals(i.name)){
					t.amount+=i.amount;
					return;
				}
			}
		}

		// if not added to stack
		getFirstOpen();
		if (firstOpen != '!') {
			inv.put(firstOpen, i);
		} else {
			Main.appendText("Inventory full.");
			return;
		}
	}

	public void removeItem(char c){
		inv.get(c).amount--;
		if(inv.get(c).amount <= 0){
			inv.remove(c);			
		}
	}
	
	public void makeRandomInventory(int tier, int amount){
		double n = (double) amount*2/3 + Main.rng.nextDouble()*(double) amount*2/3;
		n = (int) ActionLibrary.round(n, 0);
		
		for(int x = 0; x < n; x++){
			Item.Items t = Item.Items.randomItemType(tier);
			addItem(new Item (t,getStackSize(t),Main.cF));
		}
	}
	
	public int getStackSize(Item.Items type){
		int c = type.commonStackSize;
		// TODO: variation
		return c;
	}
	
	public void dropAll(char c, Entity e){
		e.map.tileMap[e.y][e.x].inventory.addItem(inv.remove(c));
	}

	public void switchItem(char itemToMove, char destination){
		if(inv.containsKey(itemToMove)){
			if(inv.containsKey(destination)){ // if there's an item in the target spot
				Item temp = inv.get(destination);
				inv.replace(destination,inv.get(itemToMove));
				inv.replace(itemToMove, temp); // switch the items
			}else{
				inv.put(destination, inv.get(itemToMove));
				inv.remove(itemToMove);
			}
		}
	}
}
