package cecs429.queries;

import java.util.List;

import cecs429.indexes.Index;
import cecs429.indexes.Posting;

public class QueryService {

	public static List<Posting> processBooleanQueries(String query, Index index) {
		BooleanQueryParser booleanQueryParser = new BooleanQueryParser();
		return booleanQueryParser.parseQuery(query).getPostings(index);
	}
}
