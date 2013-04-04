package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.NewLiteral;

public abstract class AbstractLiteral implements NewLiteral {

	private static final long serialVersionUID = 626920825158789773L;

	private NewLiteral parent = null;

	public void setParent(NewLiteral parent) {
		this.parent = parent;
	}

	public NewLiteral getParent() {
		return parent;
	}

	public abstract NewLiteral clone();
}
