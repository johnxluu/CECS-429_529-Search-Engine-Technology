package cecs429.queries;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.type.UnionType;

import cecs429.indexes.Index;
import cecs429.indexes.Posting;

/**
 * An notQuery composes other QueryComponents and merges their postings with a union-type operation.
 */
public class NotQuery implements QueryComponent {
	// The components of the Or query.
	private List<QueryComponent> mComponents;
	
	public NotQuery(List<QueryComponent> components) {
		mComponents = components;
	}
	
	@Override
	public List<Posting> getPostings(Index index) {
		List<Posting> result = new ArrayList<>();
		
		// TODO: program the merge for an NotQuery, by gathering the postings of the composed QueryComponents and
		// unioning the resulting postings.
		if(this.mComponents.size()==0) {
			return result;
		}
		if(this.mComponents.size()==1) {
			return this.mComponents.get(1).getPostings(index);
		}
		result=notDiff(mComponents.get(0).getPostings(index),mComponents.get(1).getPostings(index));
		for(int i=2;i<mComponents.size();i++) {
			result=notDiff(result, mComponents.get(i).getPostings(index));
		}
		
		return result;
	}
	
	public List<Posting> notDiff(List<Posting> l1, List<Posting> l2){
		int i=0,j=0;
		List<Posting> not=new ArrayList<>();
			while(i<l1.size() && j<l2.size()) {
			if(l1.get(i).getDocumentId()==l2.get(j).getDocumentId()) {
			
				i++;
				j++;
			}
			else if(l1.get(i).getDocumentId()<l2.get(j).getDocumentId()) {
				
				not.add(l1.get(i));
				i++;
			}
			else if(l1.get(i).getDocumentId()>l2.get(j).getDocumentId()) {
				j++;
				
			}
		}
                while(i<l1.size())not.add(l1.get(i++));

		return not;
	}
	
	@Override
	public String toString() {
		// Returns a string of the form "[SUBQUERY] + [SUBQUERY] + [SUBQUERY]"
		return "(" +
		 String.join(" + ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()))
		 + " )";
	}
}

