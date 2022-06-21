package cecs429.indexes;

import java.util.List;

/**
 * A PositionalIndexPosting encapulates a document ID and 
 * List of positions the query appeared in the corresponding document ID
 * associated with a search query component.
 * and Wdt
 */
public class PositionalIndexPosting {
	private int mDocumentId;
	private List<Integer> positions;
	private double wdt;
	
	public PositionalIndexPosting(int documentId,List<Integer> positionList) {
		mDocumentId = documentId;
		positions= positionList;
	}
	
	public PositionalIndexPosting(int documentId,List<Integer> positionList,double wdt) {
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

	public double getWdt() {
		return wdt;
	}

	public void setWdt(double wdt) {
		this.wdt = wdt;
	}
}
