import java.awt.Point;

// TODO: static-ize most of this.
public class ActionLibrary {
	private Creature c;
	private Pathfinder pf = new Pathfinder();
	public static Player player;
	private Map map;
	public Pathfinder.PointBFS pBFS;
	public int distance;
	
	// TODO (M) migrate to Creature
	ActionLibrary(Creature _c){
		this.c = _c;
		map = c.map;
		
		// TODO (F) Refactor
		if(c instanceof Player) {
			player = (Player) c;
		}else {
			player = map.player;
		}
		pf.setEntity(c);
		// System.out.println("updating path...");
		// updatePath();
		// System.out.println("done.");
	}

	//goes reverse, therefore last point will be first
	public void updatePath(){
		// boolean flag = false;
		Pathfinder.PointBFS temp;
		// flag = false;
		if (player == null) System.out.println("NO PLAYERRRRRRR AHHHH");
		pBFS = pf.pathfindBFS(player.getPos(), c.getPos(), map.buildOpenMap(), map.entities, true, true);
		// System.out.println("done pathing");
		Pathfinder.PointBFS direct_path;
		direct_path = pf.pathfindBFS(player.getPos(), c.getPos(), map.buildOpenMap(), map.entities, true, false);
		
		temp = pBFS;
		distance = 0;
		while(temp!=null && temp.getParent()!=null){
			distance++;
			temp = temp.getParent();
		}
		
		int directDistance = 0;
		temp = direct_path;
		while(temp!=null && temp.getParent()!=null){
			directDistance++;
			temp = temp.getParent();
		}
		
		if(pBFS == null || (directDistance*2 < distance)){
			pBFS = direct_path;
			distance = directDistance;
		}
	}
	
	public void newPath(Point p, boolean avoidP){
		// boolean flag = false;
		Pathfinder.PointBFS temp;
		// flag = false;
		
		pBFS = pf.pathfindBFS(p, c.getPos(), map.buildOpenMap(), map.entities, true, true);
		// System.out.println("done pathing");
		if(pBFS==null || pBFS.getParent()==null){
			int[][] m = map.buildOpenMap();
			
			if(avoidP) m[player.getPos().x][player.getPos().y] = 1;
			pBFS = pf.pathfindBFS(p, c.getPos(), map.buildOpenMap(), map.entities, true, false);
		}
		
		temp = pBFS;
		distance = 0;
		while(temp!=null && temp.getParent()!=null){
			distance++;
			temp = temp.getParent();
		}
	}

	public boolean move(boolean keepPath){
		if(!keepPath) updatePath();
		if(pBFS!=null){
			pBFS = pBFS.getParent();
			if(pBFS==null) return false;
			Point p = pBFS.point;
			return c.move(c.getAdjacentDir(p));
		}
		return false;
	}

	public boolean melee(Creature target, double damage_modifier) {
		if(c.isAdjacentTo(target.getPos())){
			if(hitCalculation(target)){
				target.changeHP(-calculateDmg(target,damage_modifier));
			}else{
				return false;
			}
		}
		// TODO (R) review functionality
		return true;
	}
	
	public double getAttackDamage(){
		double baseDamage = c.getStrength();
		if(c.weapon!=null) baseDamage += c.weapon.getDamage();
		return baseDamage/2 + (Main.rng.nextDouble()*baseDamage);
	}

	public double calculateDmg(Creature target, double damage_modifier){
		double atkDmg = getAttackDamage() * damage_modifier;
		// 50% DEF <-> 100% DEF
		double defAmt = target.getDefence()/2 + (Main.rng.nextDouble()*target.getDefence())/2;
		
		// System.out.println("AtkDmg: "+atkDmg+" - DefAmt"+defAmt);
		return round(Math.max(0,atkDmg-defAmt), 1);
	}


	public boolean lunge(){
		c.changeSP(-3);
		// System.out.println("Lunge:");
		return (move(false) && melee(player,0.5));
	}


	// 102.456
	// 102.456 * 10 = 1024.56
	// 1024.56 -> 1025
	// 1025 / 10
	// 102.5

	public static double round(double n, int decimals){
		double m = Math.pow(10, decimals);
		return Math.round(n*m)/m;
	}

	public double calculateAccuracy(){
		if (c.weapon!=null) {
			double excessStrength = c.getStrength() - c.weapon.getWeight();
			// <1 if str < weight.
			// unlimited negative, caps at +6 excess strength.
			// TODO (T) TEMP
			
			return 0.75 + (0.1 * Math.min(excessStrength, 6)) + (0.05 * c.weapon.getAccuracy());
		}else{
			// TODO (A) Implement default accuracy
			return 1.0;
		}
	}

	public boolean hitCalculation(Creature target){
		double accuracy = calculateAccuracy();
		// System.out.println("acc = "+accuracy);
		// standard formula -- hail mary
		
		// 50% ACC <-> 125% ACC
		double calcAcc = round(accuracy/2+(Main.rng.nextDouble()*accuracy*3/4),4);
		// 50% EV <-> 125% EV
		double calcEV = round(target.getEV()/2+(Main.rng.nextDouble()*target.getEV()*3/4),4);
		
		// System.out.println(e.name+"'s ACC: "+calcAcc + " {vs} "+target.name+"'s EV: "+calcEV);
		
		// if hit & win 90/10 or miss & win 10/90
		return ((calcAcc >= calcEV && Main.rng.nextInt(10) >= 1) || Main.rng.nextInt(10)==0);
	}
}
