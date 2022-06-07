package cecs429.documents;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonFileDocument implements JsonDocument{
	private int jsonDocumentId;
	private Path jsonFilePath;
	private String jsonTitle;
	private String jsonUrl;
	private Reader jsonBody;
	
	/**
	 * Constructs a JsonFileDocument with the given document ID representing the file at the given
	 * absolute file path.
	 */
	public JsonFileDocument(int id, Path absoluteFilePath) {
		jsonDocumentId = id;
		jsonFilePath = absoluteFilePath;
	}
	
	@Override
	public int getId() {
		return jsonDocumentId;
	}

	@Override
	public Reader getContent() {
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();
	    try {
			Map<?, ?> map = gson.fromJson(Files.newBufferedReader(Paths.get(jsonFilePath.toString())), Map.class);
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				if(((String)entry.getKey()).equalsIgnoreCase("Title")) {
					jsonTitle=(String)entry.getValue();
				} else if(((String)entry.getKey()).equalsIgnoreCase("Body")) {
					 jsonBody = new StringReader((String)entry.getValue());
				} else if(((String)entry.getKey()).equalsIgnoreCase("URL")) {
					jsonUrl =(String)entry.getValue();
				}
		        
		    }
		} catch (Exception e) {
			e.printStackTrace();
		}

		return jsonBody;
		
	}

	@Override
	public String getTitle() {
		return jsonTitle;
	}

	@Override
	public Path getFilePath() {
		return jsonFilePath;
	}

	@Override
	public String getUrl() {
		return jsonUrl;
	}
	
	public static JsonDocument loadJsonFileDocument(Path absolutePath, int documentId) {
		return new JsonFileDocument(documentId, absolutePath);
	}


}
