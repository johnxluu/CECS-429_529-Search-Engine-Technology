import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.DocumentCorpus;
import cecs429.indexes.Index;
import cecs429.indexes.PositionalInvertedIndex;
import cecs429.text.CustomTokenProcessor;
import edu.csulb.TermDocumentIndexer;

public class PositionalInvertedIndexTest {
	private PositionalInvertedIndex preBuiltIndex;
	private CustomTokenProcessor customTokenProcessor;
	private Index index;
	private DocumentCorpus corpus;

	@SuppressWarnings("static-access")
	@Before
	public void setUp() {
		preBuiltIndex = new PositionalInvertedIndex();
		customTokenProcessor = new CustomTokenProcessor();
		
		corpus = DirectoryCorpus.loadTextDirectory(Paths.get("junits/res").toAbsolutePath(), ".txt");
		index = TermDocumentIndexer.indexCorpus(corpus, ".txt");
		
		preBuiltIndex.addTerm(customTokenProcessor.getStem("sign"),0, 0);
		preBuiltIndex.addTerm(customTokenProcessor.getStem("in"),0, 1);
		preBuiltIndex.addTerm(customTokenProcessor.getStem("to"),0, 2);
		preBuiltIndex.addTerm(customTokenProcessor.getStem("access"),0, 3);
		preBuiltIndex.addTerm(customTokenProcessor.getStem("your"),0, 4);
		preBuiltIndex.addTerm(customTokenProcessor.getStem("photos"),0, 5);

		preBuiltIndex.addTerm(customTokenProcessor.getStem("turn"),1,0);
		preBuiltIndex.addTerm(customTokenProcessor.getStem("off"), 1,1);
		preBuiltIndex.addTerm(customTokenProcessor.getStem("the"), 1,2);
		preBuiltIndex.addTerm(customTokenProcessor.getStem("television"), 1,3);

		preBuiltIndex.addTerm(customTokenProcessor.getStem("television"), 2,0);
		preBuiltIndex.addTerm(customTokenProcessor.getStem("is"), 2,1);
		preBuiltIndex.addTerm(customTokenProcessor.getStem("in"), 2,2);
		preBuiltIndex.addTerm(customTokenProcessor.getStem("the"),2, 3);
		preBuiltIndex.addTerm(customTokenProcessor.getStem("bedroom"),2, 4);

		preBuiltIndex.addTerm(customTokenProcessor.getStem("watching"),3, 0);
		preBuiltIndex.addTerm(customTokenProcessor.getStem("movies"),3, 1);
		preBuiltIndex.addTerm(customTokenProcessor.getStem("on"),3, 2);
		preBuiltIndex.addTerm(customTokenProcessor.getStem("television"),3, 3);

		preBuiltIndex.addTerm(customTokenProcessor.getStem("many"),4, 0);
		preBuiltIndex.addTerm(customTokenProcessor.getStem("photos"),4, 1);
		preBuiltIndex.addTerm(customTokenProcessor.getStem("and"),4, 2);
		preBuiltIndex.addTerm(customTokenProcessor.getStem("videos"),4, 3);
	}

	@SuppressWarnings("static-access")
	@Test
	public void getMultiplePostingsTest() {
		String termString1 = customTokenProcessor.getStem("television");
		String termString2 = customTokenProcessor.getStem("to");
		
		Assert.assertEquals(preBuiltIndex.getPostings(termString1).get(0).getDocumentId(),
				index.getPostings(termString1).get(0).getDocumentId());
		Assert.assertEquals(preBuiltIndex.getPostings(termString2).get(0).getDocumentId(),
				index.getPostings(termString2).get(0).getDocumentId());
	}
	
}
