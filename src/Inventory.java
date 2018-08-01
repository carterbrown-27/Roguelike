import java.awt.image.BufferedImage;
import java.util.HashMap;

public class Inventory {

	HashMap<Character,Item> inv = new HashMap<Character,Item>();

	char firstOpen = 'a';

	public boolean isEmpty(){
		return inv.isEmpty();
	}

	public boolean isOneItem(){
		return inv.size()==1;
	}

	public void pickUp(char c, Inventory destination){
		Item i;
		if(isOneItem()){
			i = inv.remove(getFirstItem());
		}else{
			i = inv.remove(c);
		}
		destination.addItem(i);
	}

	public void printContents(){
		Main.appendText("Things here:");
		for(char c: inv.keySet()){
			Item i = inv.get(c);
			String quantity;
			if(i.amount==1){
				if(isVowelStart(i.name)){
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
			return i.sprite;
		}
		return null;
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
		inv.remove(c);
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
