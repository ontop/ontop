package org.obda.query.domain.imp;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.obda.query.domain.Function;
import org.obda.query.domain.Predicate;
import org.obda.query.domain.Term;

public class FunctionalTermImpl implements Function {

	private Predicate functor = null;
	private List<Term> terms = null;
	private int identifier = -1;

	/**
	 * The default constructor.
	 *
	 * @param functor the function symbol name. It is defined the same as a
	 * predicate.
	 * @param terms the list of arguments.
	 */
	protected FunctionalTermImpl(Predicate functor, List<Term> terms) {
		this.functor = functor;
		this.terms = terms;
		this.identifier = functor.hashCode();
	}

	/**
	 * Replace the existing arguments with the new term arguments.
	 *
	 * @param terms the new terms.
	 */
	public void setTerms(List<Term> terms) {
		this.terms = terms;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof FunctionalTermImpl))
			return false;

		FunctionalTermImpl functor2 = (FunctionalTermImpl) obj;
		return this.identifier == functor2.identifier;
	}

	@Override
	public int hashCode() {
		return functor.hashCode();
	}

	@Override
	public String getName() {
		return functor.getName().toString();
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
	public FunctionalTermImpl copy() {
		Vector<Term> terms = new Vector<Term>();
		Iterator<Term> it = terms.iterator();
		while (it.hasNext()) {
			terms.add(it.next().copy());
		}
		return new FunctionalTermImpl(functor.copy(), terms);
	}

	@Override
	public String toString() {
		StringBuffer sb_t = new StringBuffer();
		for (int i = 0; i < terms.size(); i++) {
			if (sb_t.length() > 0) {
				sb_t.append(",");
			}
			sb_t.append(terms.get(i).getName());
		}
		StringBuffer sb_name = new StringBuffer();
		sb_name.append(this.functor.getName());
		sb_name.append("(");
		sb_name.append(sb_t);
		sb_name.append(")");

		return sb_name.toString();
	}

	/**
	 * Check whether the function contains a particular term argument or not.
	 *
	 * @param t the term in question.
	 * @return true if the function contains the term, or false otherwise.
	 */
	public boolean containsTerm(Term t) {
		for (int i = 0; i < terms.size(); i++) {
			Term t2 = terms.get(i);
			if (t2.toString().equals(t.toString()))
				return true;
		}
		return false;
	}
}
