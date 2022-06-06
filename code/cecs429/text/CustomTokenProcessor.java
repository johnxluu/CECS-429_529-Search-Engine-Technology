package cecs429.text;

import java.util.ArrayList;
import java.util.List;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

public class CustomTokenProcessor implements TokenProcessor {

	@Override
	public List<String> processToken(String token) {
		List<String> list = new ArrayList<>();
		token = token.toLowerCase();
		token = token.replaceAll("'", "\\'");

		token = token.replaceAll("'", "");
		if(token.length()==0) {
			list.add("");
			return list;
		}
		StringBuilder str = new StringBuilder(token);
		int i = 0;
		boolean flag = true;
		while (flag) {
			if (str.length()>0 &&!Character.isLetterOrDigit(str.charAt(0))) {
				str.deleteCharAt(i);
			} else {
				flag = false;
			}

		}
		
		flag = true;
		while (flag) {
			if (str.length()>0 && !Character.isLetterOrDigit(str.charAt(str.length() - 1))) {
				str.deleteCharAt(str.length() - 1);
			} else {
				flag = false;
			}

		}
		
		if(str.toString().contains("-")) {
			String[] hypStrings = str.toString().split("-");
			for(String s:hypStrings) {
				
				list.add(getStem(s));
			}
			
			list.add(getStem(String.join("", hypStrings)));
		} else {
			list.add(getStem(str.toString()));
		}
		
		// stemming 
		
		
		return list;
	}
	
	public static String getStem(String str) {
		SnowballStemmer engStemmer=new englishStemmer();
		engStemmer.setCurrent(str);
		engStemmer.stem();
//		if(str.equals("undermining")) {
//			System.out.print("=============="+engStemmer.getCurrent());
//		}
		return engStemmer.getCurrent();

	}
	
	

}
