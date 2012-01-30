package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.DataTypePredicate;

import java.net.URI;

public class DataTypePredicateImpl extends PredicateImpl implements DataTypePredicate {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6678449661465775977L;

	protected DataTypePredicateImpl(URI name, COL_TYPE type) {
		super(name, 1, new COL_TYPE[] { type });
	}
	
	@Override
	public DataTypePredicateImpl clone() {
		return this;
	}
}
