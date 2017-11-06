package it.unibz.inf.ontop.model;

import it.unibz.inf.ontop.model.type.impl.TypeFactoryImpl;
import it.unibz.inf.ontop.model.type.TypeFactory;

/**
 * Ubiquitously used in the code
 */
public interface OntopModelSingletons {

    TypeFactory TYPE_FACTORY = TypeFactoryImpl.getInstance();
}
