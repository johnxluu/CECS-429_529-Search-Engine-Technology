package cecs429.queries;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import cecs429.documents.RankedDocument;
import cecs429.indexes.DiskPositionalIndex;
import cecs429.indexes.Index;
import cecs429.indexes.PositionalIndexPosting;
import cecs429.indexes.Posting;
import cecs429.text.CustomTokenProcessor;

public class QueryService {

	public static List<Posting> processBooleanQueries(String query, Index index) {
		BooleanQueryParser booleanQueryParser = new BooleanQueryParser();
		return booleanQueryParser.parseQuery(query).getPostings(index);
	}

	public static List<RankedDocument> processRankedQueries(String query, Index index, int corpusSize) throws IOException {
		CustomTokenProcessor customTokenProcessor = new CustomTokenProcessor();

//		DiskPositionalIndex diskPositionalIndex = (DiskPositionalIndex) index;
//				List<TermLiteral> queries = TermLiteral.parseQuery(query);
//				List<Posting> queries = BooleanQueryParser

		Map<Integer, RankedDocument> rankedDocs = new HashMap<>();
		List<String> queriesList = Arrays.asList(query.split(" "));

		// To avoid same terms getting repeated, we use HashSet
		Set<String> termSet = new HashSet<String>();
		for (String q : queriesList) {
			for (String term : customTokenProcessor.processToken(q))
				termSet.add(term);
		}

		return processRanking(rankedDocs, termSet, (DiskPositionalIndex) index, corpusSize);

	}

	private static List<RankedDocument> processRanking(Map<Integer, RankedDocument> rankedDocs, Set<String> terms,
			DiskPositionalIndex diskPositionalIndex, int corpusSize) throws IOException {
		// we use priority queue to store our ranked documents
		PriorityQueue<RankedDocument> pQueue = new PriorityQueue<>();
		
		// List to store top k documents in decreasing order
		List<RankedDocument> rankedDocumentsList = new ArrayList<>();

		// We'll be returning top k (=10) ranked documents
		int k = 10;

		for (String term : terms) {
			List<PositionalIndexPosting> termPostingsList = diskPositionalIndex.getPositionIndexPostings(term);
			
			if (termPostingsList != null) {
				// Calculating Wqt for each term
				int postingsSize = termPostingsList.size();
				double wqt = Math.log((1 + ((double) corpusSize / postingsSize)));
				System.out.println("Wqt for term: "+term+" is : "+wqt);
				System.out.println("Size: "+postingsSize);
				// Calculating the document score in each posting
				for (PositionalIndexPosting pos : termPostingsList) {
					// latest score
					double temp = 0;
					RankedDocument rDoc = null;
					//If ranked doc already contains docId
					if (rankedDocs.containsKey(pos.getDocumentId())) {
						rDoc = rankedDocs.get(pos.getDocumentId());
						temp = rDoc.getAccumulator() + (wqt * pos.getWdt());
//						if(temp>0) {
//							temp = temp/diskPositionalIndex.getDocWeight(pos.getDocumentId());
//						}
						rDoc.setAccumulator(temp);
					} else {
						//if ranked doc doesn't contain docId, then create new
						temp = wqt * pos.getWdt();
//						if(temp>0) {
//							temp = temp/diskPositionalIndex.getDocWeight(pos.getDocumentId());
//						}
						rDoc = new RankedDocument(pos.getDocumentId(), temp);
					}
					rankedDocs.put(pos.getDocumentId(), rDoc);
				}
			}
		}

		for (RankedDocument rd : rankedDocs.values()) {
			// Divide Ad by Ld
			rd.setAccumulator(rd.getAccumulator() / diskPositionalIndex.getDocWeight(rd.getDocumentId()));
			pQueue.add(rd);
		}

		// Returning Top k documents, which is 10 in our case
		k = Math.min(k, pQueue.size());
		for (int i = 0; i < k; i++) {
			rankedDocumentsList.add(pQueue.poll());
		}

		return rankedDocumentsList;
	}

}
