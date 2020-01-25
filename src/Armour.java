
public class Armour extends Item implements Equippable {
	private boolean equipped = false;
	Armour(String id) {
		super(id);
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

	@Override
	public boolean isEquippable() {
		// TODO Auto-generated method stub
		return equippable;
	}
}
