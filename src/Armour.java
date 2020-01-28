public final class Armour extends Item implements Equippable {
	private boolean equipped = false;
	private double defence;
	
	Armour(String id) {
		super(id);
	}
	
	public double getDefence() {
		return defence;
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
		// TODO Auto-generated method stub
		return equipped;
	}
}
