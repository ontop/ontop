package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.PredicateAtom;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.utils.EventGeneratingLinkedList;
import it.unibz.krdb.obda.utils.ListListener;

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
public class PredicateAtomImpl implements PredicateAtom, ListListener, Cloneable {

	private Predicate	predicate	= null;
	private List<Term>	terms		= null;

	// true when the list of atoms has been modified or when a term inside a
	// functional term has been modified
	boolean				rehash		= true;

	// null when the list of atoms has been modified or when a term inside a
	// functional term has been modified
	String				string		= null;

	private int			hash		= 0;

	protected PredicateAtomImpl(Predicate predicate, List<Term> terms) {
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

	public PredicateAtom clone() {
		EventGeneratingLinkedList<Term> v = new EventGeneratingLinkedList<Term>();
		Iterator<Term> it = terms.iterator();
		while (it.hasNext()) {
			v.add(it.next().clone());
		}
		return new PredicateAtomImpl(predicate.clone(), v);
	}

	@Override
	public String toString() {
		if (string != null)
			return string;

		StringBuffer bf = new StringBuffer();

		if (predicate == OBDAVocabulary.EQ) {
			bf.append("EQ");
		} else if (predicate == OBDAVocabulary.NEQ) {
			bf.append("NEQ");
		} else if (predicate == OBDAVocabulary.GT) {
			bf.append("GT");
		} else if (predicate == OBDAVocabulary.GTE) {
			bf.append("GTE");
		} else if (predicate == OBDAVocabulary.LT) {
			bf.append("LT");
		} else if (predicate == OBDAVocabulary.LTE) {
			bf.append("LTE");
		} else if (predicate == OBDAVocabulary.NOT) {
			bf.append("NOT");
		} else if (predicate == OBDAVocabulary.AND) {
			bf.append(" && ");
		} else if (predicate == OBDAVocabulary.OR) {
			bf.append("OR");
		} else {
			bf.append(this.predicate.getName().toString());
		}

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
		if (obj instanceof PredicateAtomImpl) {
			PredicateAtomImpl a2 = (PredicateAtomImpl) obj;
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
				FunctionalTermImpl f = (FunctionalTermImpl) t2;
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

	@Override
	public Term getTerm(int index) {
		return terms.get(index);
	}

	@Override
	public Term setTerm(int index, Term newTerm) {
		return terms.set(index, newTerm);
	}

}
