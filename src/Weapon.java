public final class Weapon extends Item implements Equippable {
	private boolean equipped = false;
	double damage;
	double accuracy;
	double weight;
	
	Weapon(String id){
		super(id);
		// TODO: weapon init here:
	}
	
	public double getDamage() {
		return damage;
	}
	// TODO: add a dmg calculation here (maybe)
	
	public double getAccuracy() {
		return accuracy;
	}
	
	public double getWeight() {
		return weight;
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
