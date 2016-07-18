package eth.nlp.common;

public class Term implements Comparable<Term>{
	
	private String termTxt = null;
	private int numInCorpus = 1;
	
	public Term(){}
	
	public Term(String txt){
		termTxt = txt;
	}
	
	public void setTermTxt(String txt){
		termTxt = txt;
	}
	
	public String getTermTxt(){
		return termTxt;
	}
	
	public void addTermNum(int increment){
		numInCorpus += increment;
	}
	
	public int getTermNum(){
		return numInCorpus;
	}
	
	@Override
	public int compareTo(Term term) {
		// TODO Auto-generated method stub
		Integer integer = new Integer(numInCorpus);
		return integer.compareTo(term.getTermNum());			//ֻ�ж����к���
	}
	
	public boolean equals(Object obj){
		Term term = (Term)obj;
		return termTxt.equals(term.getTermTxt());
	
	}
	
	public int hashCode(){
		return termTxt.hashCode();
	}
	
	public String toString(){
		return termTxt+":"+numInCorpus;
	}
}
