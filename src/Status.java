// TODO: JSON-ize
public enum Status {
		RESTING		(0),
		MIGHTY		(1),
		POISONED	(2),
		FLIGHT		(3);

		public String name;
		public String[] names = {"Resting","Mighty","Poisoned","Flying"};

		public boolean upkeep = false;
		public boolean[] upkeeps = {true,false,true,false};

		public int baseDuration;
		public int[] baseDurations = {25,45,12,45};
		
		private int t;
		Status(int _t){
			t = _t;
			name = names[t];
			upkeep = upkeeps[t];
			baseDuration = baseDurations[t];
		}
	}
