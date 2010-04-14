package inf.unibz.it.ucq.typing;

import inf.unibz.it.ucq.domain.QueryTerm;

public class CheckOperationTerm {

	private QueryTerm term1 = null;
	private QueryTerm term2 = null;
	private String operator = null;
	
	public CheckOperationTerm(QueryTerm t1,String op, QueryTerm t2){
		
		term1 = t1;
		term2 = t2;
		operator = op;
	}

	public QueryTerm getTerm1() {
		return term1;
	}

	public QueryTerm getTerm2() {
		return term2;
	}

	public String getOperator() {
		return operator;
	}
	
	public String toString(){
		return term1.getName() + operator + term2.getName();
	}
}
