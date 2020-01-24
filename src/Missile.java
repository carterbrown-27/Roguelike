
public class Missile extends Item implements Equippable, Consumable {
	private boolean equipped;
	private int amount;
	
	Missile(){
		super("");
	}
	
	public void equip() {
		
	}
	
	public void unequip() {
		
	}
	
	public boolean isEquipped() {
		return this.equipped;
	}
	
	public void throwThis() {
		// throwing logic
		use();
	}
	
	public void use() {
		
	}
	
	public void getAmount() {
		
	}
	
	public void setAmount(int amt) {
		
	}
	
	public void changeAmount(int amt) {
		
	}
}
