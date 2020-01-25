public class Weapon extends Item implements Equippable {
	private boolean equipped = false;
	double baseDamage;
	double baseAccuracy;
	
	Weapon(String id){
		super(id);
	}
	
	@Override
	public String[] listPrompts() {
		// TODO: implement
		return new String[] {};
	}
	
	@Override
	public void equip(Entity e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unequip(Entity e) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean isEquipped() {
		return this.equipped;
	}
	
	@Override
	public boolean isEquippable() {
		return true;
	}
}