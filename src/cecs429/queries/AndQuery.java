
package cecs429.queries;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import cecs429.indexes.Index;
import cecs429.indexes.Posting;

/**
 * An AndQuery composes other QueryComponents and merges their postings in an intersection-like operation.
 */
public class AndQuery implements QueryComponent {
	private List<QueryComponent> mComponents;
		
	public AndQuery(List<QueryComponent> components) {
		mComponents = components;
	}
	
	@Override
	public List<Posting> getPostings(Index index) {
		List<Posting> result = new ArrayList<Posting>();
		// TODO: program the merge for an AndQuery, by gathering the postings of the composed QueryComponents and
		// intersecting the resulting postings.
		if(mComponents.size()==0) {
			return result;
		}
		if(mComponents.size()==1) {
			return mComponents.get(0).getPostings(index);
		}
		
		result=andPostings(mComponents.get(0).getPostings(index),mComponents.get(1).getPostings(index));
		
		for(int i=2;i<mComponents.size();i++) {
			result=andPostings(result,mComponents.get(i).getPostings(index));
		}
		

		
		return result;
	}
	
	public List<Posting> andPostings(List<Posting> l1, List<Posting> l2){
		List<Posting> intersect = new ArrayList<>();
		int i=0,j=0;
		while(i<l1.size() && j<l2.size()) {
			if(l1.get(i).getDocumentId()==l2.get(j).getDocumentId()) {
				intersect.add(l1.get(i));
				i++;
				j++;
			}
			else if(l1.get(i).getDocumentId()>l2.get(j).getDocumentId()) {
				j++;
			}
			else {
				i++;
			}
		}

		return intersect;
	}
	
	@Override
	public String toString() {
		return
		 String.join(" ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()));
	}
}
