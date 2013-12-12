package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.Term;

public abstract class AbstractLiteral implements Term {

	private static final long serialVersionUID = 626920825158789773L;

	private Term parent = null;

	public void setParent(Term parent) {
		this.parent = parent;
	}

	public Term getParent() {
		return parent;
	}

	public abstract Term clone();
}
