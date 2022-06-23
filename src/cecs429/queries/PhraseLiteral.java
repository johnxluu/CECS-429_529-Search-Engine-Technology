package cecs429.queries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import cecs429.indexes.Index;
import cecs429.indexes.Posting;
import cecs429.text.CustomTokenProcessor;

/**
 * Represents a phrase literal consisting of one or more terms that must occur in sequence.
 */
public class PhraseLiteral implements QueryComponent {
	// The list of individual terms in the phrase.
	private List<String> mTerms = new ArrayList<>();
	private List<String> tokenizedList=new ArrayList<>();
	private CustomTokenProcessor customTokenProcessor=new CustomTokenProcessor();
	private Set<Integer> set=new HashSet<>();
	/**
	 * Constructs a PhraseLiteral with the given individual phrase terms.
	 */
	@SuppressWarnings("unchecked")
	public PhraseLiteral(List<String> terms) {
//		terms=(List<String>) terms.stream().map((term)->customTokenProcessor.getStem(term));
		mTerms.addAll(terms);
	}
	
	/**
	 * Constructs a PhraseLiteral given a string with one or more individual terms separated by spaces.
	 */
	@SuppressWarnings("unchecked")
	public PhraseLiteral(String terms) {
		mTerms.addAll(Arrays.asList(terms.split(" ")));
		
	}
	
	@Override
	public List<Posting> getPostings(Index index) {
		List<Posting> finalPostings=new ArrayList<Posting>();
		for(String term:mTerms) {
			tokenizedList.addAll(customTokenProcessor.processToken(term));
		}
		if (mTerms.size()==0) 
			return finalPostings;
		
		if(mTerms.size()==1) {
			return index.getPostings(tokenizedList.get(0));
		}
		finalPostings=merge(index.getPostings(tokenizedList.get(0)), index.getPostings(tokenizedList.get(1)),true);
		
		for(int i=2;i<mTerms.size();i++) {
			finalPostings=merge(finalPostings, index.getPostings(tokenizedList.get(i)),false);

		}
		return finalPostings;
	}
	
	public List<Posting> merge(List<Posting> l1,List<Posting> l2,boolean flag){
//		List<Posting> result=new ArrayList<Posting>();
		Set<Posting> resultSet = new HashSet<>();
		Set<Integer> tempSet=new HashSet<>();
		int i=0,j=0;
		l1.sort(Comparator.comparing(Posting::getDocumentId));
		l2.sort(Comparator.comparing(Posting::getDocumentId));
		while(i<l1.size() && j<l2.size()) {
			Posting p1=l1.get(i);
			Posting p2=l2.get(j);
			if(p1.getDocumentId()==p2.getDocumentId()) {
				int m=0;
				int n=0;
				List<Integer> positions1 = p1.getPositions(p1.getDocumentId());
				List<Integer> positions2 = p2.getPositions(p2.getDocumentId());
				while(m<positions1.size() && n<positions2.size() ) {
					if(positions2.get(n)-positions1.get(m)==1) {
						if(flag) {
							this.set.add(positions2.get(n));
							resultSet.add(p2);
						}
						else {
							if(this.set.contains(positions1.get(m))){
								resultSet.add(p2);
								tempSet.add(positions2.get(n));
							}
						}
						m++;
						n++;
					}
					else if(positions1.get(m)>positions2.get(n)) {
						n++;
					}
					else {
						m++;
					}
				}
				i++;
				j++;
			}
			else if(p1.getDocumentId()<p2.getDocumentId()) {
				i++;
			}
			else {
				j++;
			}
		}
		
		if(!flag)this.set=tempSet;
		return resultSet.stream().collect(Collectors.toList());
	}
	
	@Override
	public String toString() {
		return "\"" + String.join(" ", mTerms) + "\"";
	}
}
