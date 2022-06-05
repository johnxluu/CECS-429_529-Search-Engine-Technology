package cecs429.indexes;

import java.util.ArrayList;
import java.util.List;

/**
 * A PositionalIndexPosting encapulates a document ID and 
 * List of positions the query appeared in the corresponding document ID
 * associated with a search query component.
 */
public class PositionalIndexPosting {
	private int mDocumentId;
	private List<Integer> positions;
	
	public PositionalIndexPosting(int documentId,int position) {
		mDocumentId = documentId;
		positions= new ArrayList<Integer>();
		positions.add(position);
	}
	
	public int getDocumentId() {
		return mDocumentId;
	}
	
	public List<Integer> getPositions(){
		return positions;
	}
	
	public void setPositions(List<Integer> positions) {
		this.positions = positions;
	}
}
