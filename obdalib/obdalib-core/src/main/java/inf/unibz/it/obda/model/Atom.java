package inf.unibz.it.obda.model;

import inf.unibz.it.obda.model.Atom;
import inf.unibz.it.obda.model.Predicate;
import inf.unibz.it.obda.model.Term;

import java.util.List;

public interface Atom {

	public int getArity();
	
	public Predicate getPredicate();
	
	public List<Term> getTerms();
	
	public Atom copy();
	
	public void updateTerms(List<Term> terms);
	
	public int getFirstOcurrance(Term t, int i);
	
}
