package inf.unibz.it.obda.model.impl;

import inf.unibz.it.obda.model.impl.OBDADataFactoryImpl;

import inf.unibz.it.obda.api.controller.OBDADataFactory;

import java.net.URI;


public class OBDADataFactoryImpl  implements OBDADataFactory {

	private static OBDADataFactoryImpl instance = null;

	protected OBDADataFactoryImpl(){
		// protected constructor prevents instantiation from other classes.
	}

	public static OBDADataFactory getInstance(){
		if(instance == null){
			instance = new OBDADataFactoryImpl();
		}
		return instance;
	}

	public PredicateImp createPredicate(URI name, int arity) {
		return new PredicateImp(name, arity);
	}

}
