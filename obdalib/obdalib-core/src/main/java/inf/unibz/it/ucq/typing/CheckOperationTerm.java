package inf.unibz.it.ucq.typing;

import org.obda.query.domain.Term;

public class CheckOperationTerm {

	private Term term1 = null;
	private Term term2 = null;
	private String operator = null;

	public CheckOperationTerm(Term t1,String op, Term t2) {
		term1 = t1;
		term2 = t2;
		operator = op;
	}

	public Term getTerm1() {
		return term1;
	}

	public Term getTerm2() {
		return term2;
	}

	public String getOperator() {
		return operator;
	}

	@Override
	public String toString() {
		return term1.getName() + operator + term2.getName();
	}
}
