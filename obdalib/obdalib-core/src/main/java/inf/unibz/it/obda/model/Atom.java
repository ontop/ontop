package inf.unibz.it.obda.model;

import java.util.List;

public interface Atom {

	public int getArity();
	
	public Predicate getPredicate();
	
	public List<Term> getTerms();
	
	public Atom copy();
	
	public void updateTerms(List<Term> terms);
	
	public int getFirstOcurrance(Term t, int i);
	
}
