import java.awt.Point;

public class Missile extends Item implements Consumable, Equippable {
	private boolean equipped;
	private int amount;
	
	Missile(String id, int _amount){
		super(id);
		this.setAmount(_amount);
	}
	
	Missile(String id){
		// TODO: replace 1 with stack size.
		this(id,1);
	}
	
	public void equip(Entity e) {
		
	}
	
	public void unequip(Entity e) {
		
	}
	
	public boolean isEquipped() {
		return this.equipped;
	}
	
	public boolean isEquippable() {
		return true;
	}
	
	public void throwThis(Entity e, Point target) {
		// throwing logic
		use(e);
	}
	
	@Override
	public void use(Entity e) {
		
	}
	
	@Override
	public String[] listPrompts() {
		// TODO: implement
		return new String[] {};
	}

	@Override
	public int getAmount() {
		return amount;
	}

	@Override
	public void setAmount(int amt) {
		this.amount = amt;
	}

	@Override
	public void changeAmount(int amt) {
		this.amount += amt;
		if(this.amount < 0) {
			this.amount = 0;
		}
	}
}