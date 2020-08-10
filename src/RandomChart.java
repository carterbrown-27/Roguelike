// TODO: make more versatile/useful.
public class RandomChart {
	int[][] chart;
	
	RandomChart(int[][] _chart){
		chart = _chart;
		if(chart.length>0){
			int totalPercent = 0;
			for(int i = 0; i < chart.length; i++){
				totalPercent+=chart[i][1];
				chart[i][1] = totalPercent;
			}
			if(totalPercent!=100){
				System.out.println("ERROR: random chart does not equal 100");
			}
		}else{
			System.out.println("ERROR: random chart not correct.");
		}
	}
	
	public int pick(){
		int rnd = Main.getRng().nextInt(100);
		for(int[] i: chart){
			if(rnd < i[1]){
				return i[0];
			}
		}
		return -1; // should never happen
	}
}
