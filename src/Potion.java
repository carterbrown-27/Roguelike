
public class Potion extends Item implements Consumable {
	private int amount;
	
	Potion(String id, int _amount){
		super(id);
		this.amount = _amount;
	}
	
	Potion(String id){
		this(id,1);
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
