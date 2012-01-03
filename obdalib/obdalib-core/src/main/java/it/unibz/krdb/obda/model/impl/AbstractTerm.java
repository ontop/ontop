package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.Term;

public abstract class AbstractTerm implements Term, Cloneable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 626920825158789773L;

	@Override
	public abstract Term clone();

}
