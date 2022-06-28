package cecs429.classification;

public class MutualInformation implements Comparable<MutualInformation> {

	String term;
	double ict;
	
	public MutualInformation(String term, double ict) {
		super();
		this.term = term;
		this.ict = ict;
	}
	
	public String getTerm() {
		return term;
	}
	
	public void setTerm(String term) {
		this.term = term;
	}
	
	public double getIct() {
		return ict;
	}
	
	public void setIct(double ict) {
		this.ict = ict;
	}

//	@Override
//	public int compareTo(Object o) {
//		MutualInformation m=(MutualInformation) o;
//		return (int) (-1*(this.ict-m.ict));
//	}
	
	@Override
	public int compareTo(MutualInformation o) {
		if (this.ict < o.ict) {
			return 1;
		} else if (this.ict > o.ict) {
			return -1;
		} else {
			return 0;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		MutualInformation m=(MutualInformation) obj;
		
		return this.term.equals(m.term);
	}
	
	@Override
	public int hashCode() {
		int result=0;
		for(int i=0;i<term.length();i++) {
			result+=((term.charAt(i)*(i+1)))%100000;
		}
		return result;
	}

}
