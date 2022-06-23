import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import cecs429.indexes.DiskPositionalIndex;
import cecs429.indexes.PositionalIndexPosting;
import cecs429.text.CustomTokenProcessor;

public class DiskPositionalIndexTest {
	private DiskPositionalIndex diskPositionalIndex;
	private String path = "junits/res";
	
	@Before
	public void setUp() throws IOException {
		diskPositionalIndex = new DiskPositionalIndex(path);
	}

	@Test
	public void readPostingsFromFile() throws IOException {
		List<PositionalIndexPosting> list = diskPositionalIndex.getPositionIndexPostings(CustomTokenProcessor.getStem("access"));
		assertEquals(1, list.size());
		assertEquals(0, list.get(0).getDocumentId());
		
		List<PositionalIndexPosting> list2 = diskPositionalIndex.getPositionIndexPostings(CustomTokenProcessor.getStem("in"));
		assertEquals(2, list2.size());
		assertEquals(0, list2.get(0).getDocumentId());
		assertEquals(2, list2.get(1).getDocumentId());
	}
	
	@Test
	public void getWdtTest() throws IOException {
		List<PositionalIndexPosting> list = diskPositionalIndex.getPositionIndexPostings(CustomTokenProcessor.getStem("television"));
		assertEquals(3, list.size());
		for(PositionalIndexPosting pos:list) {
			assertEquals(1.0, pos.getWdt(),1.0);
		}
	}
	
	@Test
	public void readEmptyPostingsTest() throws IOException {
		List<PositionalIndexPosting> list = diskPositionalIndex.getPositionIndexPostings(CustomTokenProcessor.getStem("baseball"));
		assertEquals(0, list.size());
	}
}
