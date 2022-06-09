package cecs429.queries;

import java.util.ArrayList;
import java.util.List;

import cecs429.indexes.Index;
import cecs429.indexes.Posting;
import cecs429.text.CustomTokenProcessor;

/**
 * A TermLiteral represents a single term in a subquery.
 */
public class TermLiteral implements QueryComponent {
	private String mTerm;
	private CustomTokenProcessor customTokenProcessor;
	public TermLiteral(String term) {
		mTerm = term;
		customTokenProcessor=new CustomTokenProcessor();
	}
	
	public String getTerm() {
		return mTerm;
	}
	
	@Override
	public List<Posting> getPostings(Index index) {
		List<Posting> postings=new ArrayList<>();
		List<String> processedTokenStrings=customTokenProcessor.processToken(mTerm);
		for(String token:processedTokenStrings) {
			postings.addAll(index.getPostings(token));
		}
		
		return postings;
	}
	
	@Override
	public String toString() {
		return mTerm;
	}
}
