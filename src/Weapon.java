public class Weapon extends Item implements Equippable {
	private boolean equipped = false;
	double baseDamage;
	double baseAccuracy;
	
	Weapon(String id){
		super(id);
	}
	
	public void equip() {
		
	}
	
	public void unequip() {
		
	}
	
	public boolean isEquipped() {
		return this.equipped;
	}
}
