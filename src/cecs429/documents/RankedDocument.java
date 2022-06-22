package cecs429.documents;

public class RankedDocument implements Comparable<RankedDocument> {	

	private int documentId;
	
	private double accumulator;

	public RankedDocument(int documentId) {
		this.documentId = documentId;
	}

	public RankedDocument(int documentId, double score) {
		this.documentId = documentId;
		this.accumulator = score;
	}

	public int getDocumentId() {
		return documentId;
	}

	public void setDocumentId(int documentId) {
		this.documentId = documentId;
	}

	public double getAccumulator() {
		return accumulator;
	}

	public void setAccumulator(double accumulator) {
		this.accumulator = accumulator;
	}

	@Override
	public int compareTo(RankedDocument o) {
		if (this.accumulator < o.accumulator) {
			return 1;
		} else if (this.accumulator > o.accumulator) {
			return -1;
		} else {
			return 0;
		}
	}
}