package cecs429.indexes;

import java.util.List;

/**
 * A Posting encapulates a document ID associated with a search query component.
 */
public class Posting {
	private int mDocumentId;
	private List<Integer> positions;
	double wdt;
	
	public Posting(int documentId) {
		mDocumentId = documentId;
	}
	
	public int getDocumentId() {
		return mDocumentId;
	}
	
	public List<Integer> getPositions(int documentId){
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
