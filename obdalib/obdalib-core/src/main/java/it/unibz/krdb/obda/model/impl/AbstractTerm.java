package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.Term;

public abstract class AbstractTerm implements Term, Cloneable {
	
	@Override
	public abstract Term clone();

}
