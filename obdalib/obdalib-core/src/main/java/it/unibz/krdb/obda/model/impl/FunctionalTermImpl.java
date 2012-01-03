package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.utils.EventGeneratingLinkedList;
import it.unibz.krdb.obda.utils.ListListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FunctionalTermImpl implements Function, ListListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2832481815465364535L;
	private Predicate functor = null;
	private List<Term> terms = null;
	private int identifier = -1;

	// true when the list of terms has been modified
	boolean rehash = true;

	// null when the list of terms has been modified
	String string = null;

	/**
	 * The default constructor.
	 * 
	 * @param functor
	 *            the function symbol name. It is defined the same as a
	 *            predicate.
	 * @param terms
	 *            the list of arguments.
	 */
	protected FunctionalTermImpl(Predicate functor, List<Term> terms) {
		this.functor = functor;

		EventGeneratingLinkedList<Term> eventlist = new EventGeneratingLinkedList<Term>();
		eventlist.addAll(terms);

		this.terms = eventlist;

		eventlist.addListener(this);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof FunctionalTermImpl))
			return false;

		FunctionalTermImpl functor2 = (FunctionalTermImpl) obj;
		return this.hashCode() == functor2.hashCode();
	}

	@Override
	public int hashCode() {
		if (rehash) {
			identifier = toString().hashCode();
			rehash = false;
		}
		return identifier;
	}

	@Override
	public Predicate getFunctionSymbol() {
		return functor;
	}

	@Override
	public List<Term> getTerms() {
		return terms;
	}

	@Override
	public int getArity() {
		return getTerms().size();
	}

	@Override
	public FunctionalTermImpl clone() {
		List<Term> copyTerms = new ArrayList<Term>(terms.size() + 10);
		Iterator<Term> it = terms.iterator();
		while (it.hasNext()) {
			copyTerms.add(it.next().clone());
		}
		FunctionalTermImpl clone = new FunctionalTermImpl(functor, copyTerms);
		clone.identifier = identifier;
		clone.string = string;
		clone.rehash = rehash;
		return clone;
	}

	@Override
	public String toString() {
		if (string != null)
			return string;

		StringBuffer sb_t = new StringBuffer();

		for (int i = 0; i < terms.size(); i++) {
			if (sb_t.length() > 0) {
				sb_t.append(",");
			}
			sb_t.append(terms.get(i).toString());
		}
		StringBuffer sb_name = new StringBuffer();

		if (functor == OBDAVocabulary.EQ) {
			sb_name.append("EQ");
		} else if (functor == OBDAVocabulary.NEQ) {
			sb_name.append("NEQ");
		} else if (functor == OBDAVocabulary.GT) {
			sb_name.append("GT");
		} else if (functor == OBDAVocabulary.GTE) {
			sb_name.append("GTE");
		} else if (functor == OBDAVocabulary.LT) {
			sb_name.append("LT");
		} else if (functor == OBDAVocabulary.LTE) {
			sb_name.append("LTE");
		} else if (functor == OBDAVocabulary.NOT) {
			sb_name.append("NOT");
		} else if (functor == OBDAVocabulary.AND) {
			sb_name.append("AND");
		} else if (functor == OBDAVocabulary.OR) {
			sb_name.append("OR");
		} else {
			sb_name.append(this.functor.getName());
		}

		sb_name.append("(");
		sb_name.append(sb_t);
		sb_name.append(")");

		string = sb_name.toString();
		return string;
	}

	/**
	 * Check whether the function contains a particular term argument or not.
	 * 
	 * @param t
	 *            the term in question.
	 * @return true if the function contains the term, or false otherwise.
	 */
	public boolean containsTerm(Term t) {
		for (int i = 0; i < terms.size(); i++) {
			Term t2 = terms.get(i);
			if (t2.equals(t))
				return true;
		}
		return false;
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
	public void listChanged() {
		rehash = true;
		string = null;
	}
}
