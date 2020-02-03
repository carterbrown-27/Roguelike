import java.util.Random;

public class StringHelper {
	
	private static Random rng;
	StringHelper(Random rng){
		StringHelper.rng = rng;
	}
	
	public static boolean isVowelStart(String str){
		if(str.length()==0) return false;
		char c = str.charAt(0);
		if(c=='a'||c=='i'||c=='e'||c=='o'||c=='u') return true;
		return false;
	}
	
	public String randomName(){

		int length = rng.nextInt(6)+4;
		char[] vowels = {'a','a','e','e','i','i','o','o','u'};
		char[] ctv = {'r','r','n','t','s','l','y','b','j'};
		char[] consonants = {'b','c','d','f','g','h','j','j','k','k','l','m','p','r','v','s','t','n','w','z','z'};

		// String[] pairs = {"ld","st","pr","qu","sh"};

		String name = "";
		int prev = -1;

		while(name.length() < length){
			if((name.length() == length-1 && prev == 2 && rng.nextInt(4)+1 > 1) || prev == 3 || (prev == 2 && rng.nextBoolean())
					|| (prev == 0 && rng.nextInt(5)+1 == 5) || (prev==-1 && rng.nextInt(5)+1 >= 4)){

				name+=vowels[rng.nextInt(vowels.length)];
				prev = 0;
			}else{
				if((prev==2 && rng.nextInt(5)+1 >= 2) || (prev==-1 && rng.nextBoolean())){
					name+=ctv[rng.nextInt(ctv.length)];

					prev = 3;
				}else{
					name+=consonants[rng.nextInt(consonants.length)];

					prev = 2;
				}
			}
		}
		return name;
	}
}
