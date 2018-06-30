import java.util.Map;

public class AI {
	public Pathfinder p = new Pathfinder();
	public Entity e;
	public int t = 0;
	public ActionLibrary lib;
	public Entity player;
	
	AI(Entity e){
		this.e = e;
		this.lib = new ActionLibrary(this.e);
		player = e.map.player;
	}
	
	public boolean takeTurn(){
		if(e.HP <= 0){
			return false;
		}
		
		if(e.creature.equals(Creature.RAT)){
			rat();
		}
		
		// end of ai selection
		e.SP = Math.min(e.SP + e.creature.SP_REGEN, e.creature.SP_MAX);
		
		return true;
	}
	
	// what one turn of the given creature looks like
	
	public void rat(){
		if(e.isAdjacentTo(player.getPos())){
			lib.melee();
		}else if(lib.distance == 2 && e.SP >= 3){
			lib.lunge();
		}else{
			lib.move();
		}
	}
}
