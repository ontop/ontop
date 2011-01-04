package org.obda.query.domain.imp;

import java.net.URI;

import org.obda.query.domain.Predicate;
import org.obda.query.domain.PredicateFactory;

public class BasicPredicateFactoryImpl implements PredicateFactory {

	private static BasicPredicateFactoryImpl instance = null;

	protected BasicPredicateFactoryImpl(){
		// protected constructor prevents instantiation from other classes.
	}

	public static BasicPredicateFactoryImpl getInstance(){
		if(instance == null){
			instance = new BasicPredicateFactoryImpl();
		}
		return instance;
	}

	public Predicate createPredicate(URI name, int arity) {
		return new PredicateImp(name, arity);
	}
}
