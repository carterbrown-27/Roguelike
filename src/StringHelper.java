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

		int length = rng.nextInt(5)+4;
		char[] vowels = {'a','e','i','o','u'};

		char[] ctv = {'r','h','w','l','y','n'};
		char[] consonants = {'b','c','d','f','g','j','k','l','m','p','r','v','z','s','t','n','w'};

		// String[] pairs = {"ld","st","pr","qu","sh"};

		String name = "";
		int prev = -1;

		while(name.length() < length){
			if((name.length() == length-1 && prev == 2) || prev == 3 || (prev == 2 && rng.nextBoolean())
					|| (prev == 0 && rng.nextBoolean() && rng.nextBoolean()) || (prev==-1 && rng.nextBoolean())){

				name+=vowels[rng.nextInt(vowels.length)];
				prev = 0;
			}else{
				if(prev==2 || (prev==-1 && rng.nextBoolean())){
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
	
	public String randomScrollName() {
		return (randomName()+" "+randomName()).toUpperCase();
	}
}
