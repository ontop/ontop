package org.obda.query.domain.imp;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.obda.query.domain.Atom;
import org.obda.query.domain.Predicate;
import org.obda.query.domain.Term;

public class AtomImpl implements Atom{

	private Predicate predicate = null;
	private List<Term> terms = null;


	public AtomImpl(Predicate predicate,List<Term> terms){
		if(predicate.getArity() != terms.size()){
			throw new IllegalArgumentException("There must be the same number of terms as indicated by predicate");
		}
		this.predicate = predicate;
		this.terms = terms;
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


	public void updateTerms(List<Term> terms) {
		this.terms = terms;
	}


	public Atom copy() {
		Vector< Term> v = new Vector<Term>();
		Iterator<Term> it = terms.iterator();
		while(it.hasNext()){
			v.add(it.next().copy());
		}
		return new AtomImpl(predicate.copy(), v);
	}
	
	@Override
	public String toString() {
		StringBuffer bf = new StringBuffer();
		bf.append(predicate.toString());
		bf.append("(");
		for (int i = 0; i < terms.size(); i++) {
			bf.append(terms.get(i));
			if (i+1 < terms.size()) {
				bf.append(", ");
			}
		}
		bf.append(")");
		return bf.toString();
	}
	
}
