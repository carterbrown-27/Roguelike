
public class Key extends Item implements Consumable {
	private int floor;
	
	Key(String type, int floor){
		super(type);
		this.floor = floor;
	}
	
	@Override
	public void use(Creature c) {
		// TODO (A) Implement
		// open()
	}
	
	public int getFloor() {
		return floor;
	}
}
