import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import cecs429.indexes.DiskPositionalIndex;
import cecs429.indexes.Posting;
import cecs429.queries.BooleanQueryParser;

public class BooleanQueryParserTest {

	private BooleanQueryParser booleanQueryParser;
	private DiskPositionalIndex diskPositionalIndex;
	
	@Before
	public void setUp() throws IOException {
		booleanQueryParser = new BooleanQueryParser();
		diskPositionalIndex = new DiskPositionalIndex("junits/res");
	}

	@Test
	public void parseSingleQueryTest() {
		List<Posting> postings = booleanQueryParser.parseQuery("movies").getPostings(diskPositionalIndex);
		assertEquals(1, postings.size());
		assertEquals(3, postings.get(0).getDocumentId());
	}
	
	@Test
	public void parseMultipleTermQueryTest() {
		List<Posting> postings = booleanQueryParser.parseQuery("in the").getPostings(diskPositionalIndex);
		assertEquals(1, postings.size());
		assertEquals(2, postings.get(0).getDocumentId());
	}
	
	@Test
	public void parseEmptyQueryTest() {
		List<Posting> postings = booleanQueryParser.parseQuery("baseball").getPostings(diskPositionalIndex);
		assertEquals(0, postings.size());
	}

}
