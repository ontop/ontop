package inf.unibz.it.obda.model.impl;

import inf.unibz.it.obda.model.Atom;
import inf.unibz.it.obda.model.Predicate;
import inf.unibz.it.obda.model.Term;

import java.util.List;

public class OperatorAtom implements Atom {

	@Override
	public Atom copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getArity() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Predicate getPredicate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Term> getTerms() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateTerms(List<Term> terms) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getFirstOcurrance(Term t, int i) {
		// TODO Auto-generated method stub
		return -1;
	}

}
