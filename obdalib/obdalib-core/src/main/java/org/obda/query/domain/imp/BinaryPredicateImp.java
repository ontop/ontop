package org.obda.query.domain.imp;

import java.net.URI;

import org.obda.query.domain.BinaryPredicate;

public class BinaryPredicateImp extends PredicateImp implements BinaryPredicate {

	private int arity = 2;
	
	protected BinaryPredicateImp(URI name, int identifier) {
		super(name, identifier, 2);
	}

	public int getArity(){
		return arity;
	}
}
