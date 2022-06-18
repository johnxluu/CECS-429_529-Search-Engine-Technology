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
	private float wdt;
	
	public PositionalIndexPosting(int documentId,List<Integer> positionList) {
		mDocumentId = documentId;
		positions= positionList;
	}
	
	public PositionalIndexPosting(int documentId,List<Integer> positionList,float wdt) {
		mDocumentId = documentId;
		positions= positionList;
		this.setWdt(wdt);
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

	public float getWdt() {
		return wdt;
	}

	public void setWdt(float wdt) {
		this.wdt = wdt;
	}
}
