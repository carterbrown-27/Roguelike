import java.util.HashMap;

public class ArmourSet {
	
	// backed by this hash map : <"slotName", Armour>
	private HashMap<String,Armour> armourMap = new HashMap<>();
	
	ArmourSet(){
		armourMap.put("head", null);
		armourMap.put("chest", null);
		armourMap.put("legs", null);
		armourMap.put("feet", null);
		armourMap.put("hands", null);
		armourMap.put("l_ring", null);
		armourMap.put("r_ring", null);
		armourMap.put("neck", null);
	}
	
	ArmourSet(HashMap<String,Armour> _armourMap){
		this();
		_armourMap.forEach((String slot, Armour a) -> putInSlot(slot,a));
	}
	
	public Armour getSlot(String slot) {
		return armourMap.get(slot);
	}
	
	public Armour putInSlot(String slot, Armour a) {
		if(armourMap.containsKey(slot)) { 
			Armour last = armourMap.get(slot);
			armourMap.put(slot, a);
			return last;
		}
		return null;
	}
	
	public void takeOff(String slot) {
		armourMap.replace(slot, null);
	}
	
	// removes all of armour (though this should not matter)
	public void takeOff(Armour a) {
		for(String s: armourMap.keySet()) {
			Armour b = armourMap.get(s);
			if(b != null && b.equals(a)) armourMap.replace(s, null);
		}
	}
	
	public double getDefence() {
		double def = 0;
		for(Armour a: armourMap.values()) {
			if(a!=null) {
				def += a.getDefence();
			}
		}
		return def;
	}
}
