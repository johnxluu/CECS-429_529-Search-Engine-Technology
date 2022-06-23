import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import cecs429.documents.RankedDocument;
import cecs429.indexes.DiskPositionalIndex;
import cecs429.indexes.Index;
import cecs429.queries.QueryService;

public class RankedTest {

	private DiskPositionalIndex diskPositionalIndex;
	
	@Before
	public void setUp() throws IOException {
		diskPositionalIndex = new DiskPositionalIndex("junits/res");
	}
	
	@Test
	public void rankedSingleTest() throws IOException {
		List<RankedDocument> list = QueryService.processRankedQueries("access", diskPositionalIndex, 5);
		assertEquals(1, list.size());
		assertEquals(0.7314, list.get(0).getAccumulator(),1.00);		
	}
	
	@Test
	public void rankedMultiTest() throws IOException {
		List<RankedDocument> list = QueryService.processRankedQueries("television", diskPositionalIndex, 5);
		assertEquals(3, list.size());
		assertEquals(0.49, list.get(0).getAccumulator(),1.00);		
	}
	
	@Test
	public void rankedEmptyTest() throws IOException {
		List<RankedDocument> list = QueryService.processRankedQueries("baseball", diskPositionalIndex, 5);
		assertEquals(0, list.size());
	}
	
}
