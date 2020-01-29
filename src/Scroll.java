
public class Scroll extends Item implements Consumable {	
	Scroll(String id, int _amount){
		super(id);
		this.setAmount(_amount);
	}
	
	Scroll(String id){
		this(id,1);
	}
	
	@Override
	public void use(Entity e) {
		
	}
	
	@Override
	public String[] listPrompts() {
		// TODO (A) Implement
		return new String[] {};
	}
}
