package cecs429.documents;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class Test {

	public static void main(String[] args) {
		String dat = "{\r\n"
				+ "  \"name\": \"John Doe\",\r\n"
				+ "  \"email\": \"john.doe@example.com\",\r\n"
				+ "  \"admin\": true\r\n"
				+ "}";
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();
	    try {
			Map<?, ?> map = gson.fromJson((Reader)Files.newBufferedReader(Paths.get("src/cecs429/documents/user.json")),Map.class);
			// print map entries
		    for (Map.Entry<?, ?> entry : map.entrySet()) {
		        System.out.println(entry.getKey() + "=" + entry.getValue());
		    }
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
}
