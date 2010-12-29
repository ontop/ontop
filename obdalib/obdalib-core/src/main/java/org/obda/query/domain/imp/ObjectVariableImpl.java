package org.obda.query.domain.imp;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.obda.query.domain.FunctionSymbol;
import org.obda.query.domain.Term;
import org.obda.query.domain.Variable;

public class ObjectVariableImpl implements Variable {

	private FunctionSymbol	functor	= null;
	private List<Term>		terms	= null;

	protected ObjectVariableImpl(FunctionSymbol fs, List<Term> vars) {
		this.functor = fs;
		this.terms = vars;
	}

	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Variable)) {
			return false;
		}

		return this.hash() == ((ObjectVariableImpl) obj).hash();
	}

	public long hash() {
		return functor.hash();
	}

	public String getName() {
		return functor.getName();
	}

	public Variable copy() {
		Vector<Term> vex = new Vector<Term>();
		Iterator<Term> it = terms.iterator();
		while (it.hasNext()) {
			vex.add(it.next().copy());
		}
		return new ObjectVariableImpl(functor.copy(), vex);
	}

	public void setTerms(List<Term> terms) {
		this.terms = terms;
	}

	public List<Term> getTerms() {
		return terms;
	}

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
	
	
	public boolean containsTerm(Term t) {
		for (int i = 0; i < terms.size(); i++) {
			Term t2 = terms.get(i);
			if (t2.toString().equals(t.toString()))
				return true;
		}
		return false;
	}
}
