package inf.unibz.it.obda.model.impl;

import inf.unibz.it.obda.model.Function;
import inf.unibz.it.obda.model.Predicate;
import inf.unibz.it.obda.model.Term;

import java.util.Iterator;
import java.util.List;

import org.obda.query.tools.util.EventGeneratingLinkedList;
import org.obda.query.tools.util.ListListener;

public class FunctionalTermImpl implements Function, ListListener {

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
	 * @param functor the function symbol name. It is defined the same as a
	 * predicate.
	 * @param terms the list of arguments.
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

	//TODO FunctionalTerm: getName doesn't make sense in this class
	@Override
	public String getName() {
		return functor.getName().toString();
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
	public FunctionalTermImpl copy() {
		EventGeneratingLinkedList<Term> copyTerms = new EventGeneratingLinkedList<Term>();
		Iterator<Term> it = terms.iterator();
		while (it.hasNext()) {
			copyTerms.add(it.next().copy());
		}
		return new FunctionalTermImpl(functor.copy(), copyTerms);
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
			sb_t.append(terms.get(i).getName());
		}
		StringBuffer sb_name = new StringBuffer();
		sb_name.append(this.functor.getName());
		sb_name.append("(");
		sb_name.append(sb_t);
		sb_name.append(")");

		string = sb_name.toString();
		return string;
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

	@Override
	public void listChanged() {
		rehash = true;
		string = null;
	}
}
