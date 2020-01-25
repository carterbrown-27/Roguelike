
public class Missile extends Item implements Equippable, Consumable {
	private boolean equipped;
	private int amount;
	
	Missile(String id, int _amount){
		super(id);
		this.amount = _amount;
	}
	
	Missile(String id){
		// TODO: replace 1 with stack size.
		this(id,1);
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
	
	@Override
	public String[] listPrompts() {
		// TODO: implement
		return new String[] {};
	}
}
