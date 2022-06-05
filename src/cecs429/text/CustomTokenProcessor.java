package cecs429.text;

import java.util.ArrayList;
import java.util.List;

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
				list.add(s);
			}
			list.add(String.join("", hypStrings));
		} else {
			list.add(str.toString());
		}
		
		return list;
	}

}
