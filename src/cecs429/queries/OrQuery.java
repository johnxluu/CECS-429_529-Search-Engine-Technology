package cecs429.queries;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.type.UnionType;

import cecs429.indexes.Index;
import cecs429.indexes.Posting;

/**
 * An OrQuery composes other QueryComponents and merges their postings with a union-type operation.
 */
public class OrQuery implements QueryComponent {
	// The components of the Or query.
	private List<QueryComponent> mComponents;
	
	public OrQuery(List<QueryComponent> components) {
		mComponents = components;
	}
	
	@Override
	public List<Posting> getPostings(Index index) {
		List<Posting> result = new ArrayList<>();
		
		// TODO: program the merge for an OrQuery, by gathering the postings of the composed QueryComponents and
		// unioning the resulting postings.
		if(this.mComponents.size()==0) {
			return result;
		}
		if(this.mComponents.size()==1) {
			return this.mComponents.get(1).getPostings(index);
		}
		result=orUnion(mComponents.get(0).getPostings(index),mComponents.get(1).getPostings(index));
		for(int i=2;i<mComponents.size();i++) {
			result=orUnion(result, mComponents.get(i).getPostings(index));
		}
		
		return result;
	}
	
	public List<Posting> orUnion(List<Posting> l1, List<Posting> l2){
		int i=0,j=0;
		List<Posting> union=new ArrayList<>();
		while(i<l1.size() && j<l2.size()) {
			if(l1.get(i).getDocumentId()==l2.get(j).getDocumentId()) {
				union.add(l1.get(i));
				i++;
				j++;
			}
			else if(l1.get(i).getDocumentId()<l2.get(j).getDocumentId()) {
				union.add(l1.get(i++));
			}
			else {
				union.add(l2.get(j++));
			}
		}
		while(i<l1.size())union.add(l1.get(i++));
		while(j<l2.size())union.add(l2.get(j++));

		return union;
	}
	
	@Override
	public String toString() {
		// Returns a string of the form "[SUBQUERY] + [SUBQUERY] + [SUBQUERY]"
		return "(" +
		 String.join(" + ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()))
		 + " )";
	}
}
