package inf.unibz.it.obda.model.impl;

import inf.unibz.it.obda.model.Atom;
import inf.unibz.it.obda.model.Predicate;
import inf.unibz.it.obda.model.Term;
import inf.unibz.it.obda.utils.EventGeneratingLinkedList;
import inf.unibz.it.obda.utils.ListListener;

import java.net.URI;
import java.util.Iterator;
import java.util.List;


/***
 * The implentation of an Atom. This implementation is aware of changes in the
 * list of terms. Any call to a content changing method in the list of terms
 * will force the atom to invalidate the current hash and string values and
 * recompute them in the next calls to hashCode or toString.
 * 
 * The implementation will also listen to changes in the list of terms of any
 * functional term inside the atom.
 * 
 * @author Mariano Rodriguez Muro
 * 
 */
public class AtomImpl implements Atom, ListListener {

	private Predicate	predicate	= null;
	private List<Term>	terms		= null;

	// true when the list of atoms has been modified or when a term inside a
	// functional term has been modified
	boolean				rehash		= true;

	// null when the list of atoms has been modified or when a term inside a
	// functional term has been modified
	String				string		= null;

	private int			hash		= 0;

	protected AtomImpl(Predicate predicate, List<Term> terms) {
		if (predicate.getArity() != terms.size()) {
			throw new IllegalArgumentException("There must be the same number of terms as indicated by predicate");
		}
		this.predicate = predicate;

		EventGeneratingLinkedList<Term> eventlist = new EventGeneratingLinkedList<Term>();
		eventlist.addAll(terms);
		this.terms = eventlist;

		
		eventlist.addListener(this);

		for (Term term : terms) {
			if (term instanceof FunctionalTermImpl) {
				FunctionalTermImpl function = (FunctionalTermImpl) term;
				EventGeneratingLinkedList<Term> innertermlist = (EventGeneratingLinkedList<Term>) function.getTerms();
				innertermlist.addListener(this);
			}
		}
	}
	
	@Override
	public int hashCode() {
		if (rehash) {
			hash = toString().hashCode();
			rehash = false;
		}
		return hash;
	}
	

	public int getArity() {
		return terms.size();
	}

	public Predicate getPredicate() {
		return predicate;
	}

	public List<Term> getTerms() {
		return terms;
	}

	public void updateTerms(List<Term> newterms) {
		
		for (Term term : terms) {
			if (term instanceof FunctionalTermImpl) {
				FunctionalTermImpl function = (FunctionalTermImpl) term;
				EventGeneratingLinkedList<Term> innertermlist = (EventGeneratingLinkedList<Term>) function.getTerms();
				innertermlist.removeListener(this);
			}
		}
		
		terms.clear();
		terms.addAll(newterms);
		
		for (Term term : terms) {
			if (term instanceof FunctionalTermImpl) {
				FunctionalTermImpl function = (FunctionalTermImpl) term;
				EventGeneratingLinkedList<Term> innertermlist = (EventGeneratingLinkedList<Term>) function.getTerms();
				innertermlist.addListener(this);
			}
		}
	}

	public Atom copy() {
		EventGeneratingLinkedList<Term> v = new EventGeneratingLinkedList<Term>();
		Iterator<Term> it = terms.iterator();
		while (it.hasNext()) {
			v.add(it.next().copy());
		}
		return new AtomImpl(predicate.copy(), v);
	}

	@Override
	public String toString() {
		if (string != null)
			return string;

		StringBuffer bf = new StringBuffer();
		URI predicateURI = predicate.getName();
		bf.append(predicateURI.toString());
		bf.append("(");
		for (int i = 0; i < terms.size(); i++) {
			bf.append(terms.get(i));
			if (i + 1 < terms.size()) {
				bf.append(", ");
			}
		}
		bf.append(")");

		string = bf.toString();

		return string;
	}

	/***
	 * Compares two atoms by their string representation.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AtomImpl) {
			AtomImpl a2 = (AtomImpl) obj;
			return this.hashCode() == a2.hashCode();
		}
		return false;
	}

	@Override
	public void listChanged() {
		rehash = true;
		string = null;
	}

	@Override
	public int getFirstOcurrance(Term t, int i) {
		int size = terms.size();
		for (int j = 0; j < size; j++) {
			Term t2 = terms.get(j);
			if (t2 instanceof FunctionalTermImpl) {
				FunctionalTermImpl f = (FunctionalTermImpl)t2;
				int newindex = f.getFirstOcurrance(t, 0);
				if (newindex != -1)
					return j;
			} else {
				if (t2.equals(t))
					return j;
			}
		}
		return -1;
	}

}
