import java.awt.Point;

// TODO: static-ize most of this.
public class ActionLibrary {
	private Creature c;
	private Pathfinder pf = new Pathfinder();
	public Player player;
	private Map map;
	public Pathfinder.PointBFS pBFS;
	public int distance;

	public boolean isAI = true;

	// TODO: migrate to Creature
	ActionLibrary(Creature _c){
		this.c = _c;
		map = c.map;
		player = map.player;
		if(PLAYER) isAI = false;
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
			return c.move(c.getDir(p));
		}
		return false;
	}

	public boolean melee(Creature target, double damage_modifier){
		int dir = c.getDir(player.getPos());
		if(!isAI || dir!=-1){
			if(hitCalculation(target)){
				target.changeHP(-calculateDmg(target,damage_modifier));
			}else{
				return false;
			}
		}
		return true;
	}
	
	public double getAttackDamage(){
		double baseDamage = c.getStrength();
		if(c.weapon!=null) baseDamage += c.weapon.getDamage();
		return baseDamage/2 + (Main.rng.nextDouble()*baseDamage);
	}

	public double calculateDmg(Creature target, double damage_modifier){
		return round(getAttackDamage() * damage_modifier
				- ( (target.getDefence()/2 + (Main.rng.nextDouble()*target.getDefence()) ) / 2), 1);
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
		/* accuracy is "quadratic"
		 * deltaS <=~ -6; acc = 0
		 * deltaS ~ acc 
		 * if acc<=0.1, acc = 0.1
		 * 0.025x^2+1 with +/-
		 * MAX = 1.6
		 * MIN = 0.1
		 */

		
		// TODO: migrate
		if (c.weapon!=null) {
			double deltaS = c.getStrength() + c.weapon.getAccuracy() /* x skill */ - (c.weapon.getAccuracy() * 2);
			
			// calculations, move these?
			if (deltaS < 0){
				return Math.min(Math.max(Math.pow(deltaS, 2) * (-1) * 0.025 + 1.0 , 0.1), 1.51);	
			}else{		
				return Math.min(Math.pow(deltaS, 2) * 0.0125 + 1.0 , 1.6);	
			}
		}else{
			// TODO: terrible, get rid of this
			if(!c.hasAI){
				return 1.1;
			}else{
				return 1.0;
			}
		}
	}

	public boolean hitCalculation(Creature target){
		double accuracy = calculateAccuracy();
		// System.out.println("acc = "+accuracy);
		// standard formula -- hail mary
		double calcAcc = round(accuracy/2+(Main.rng.nextDouble()*accuracy*3/4),4);
		double calcEV = round(target.getEV()/2+(Main.rng.nextDouble()*target.getEV()),4);
		
		// System.out.println(e.name+"'s ACC: "+calcAcc + " {vs} "+target.name+"'s EV: "+calcEV);
		
		if((calcAcc >  calcEV && Main.rng.nextInt(9) >= 1) || Main.rng.nextInt(9)==9){
			return true;
		}
		return false;
	}
}
