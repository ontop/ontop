package it.unibz.inf.ontop.model.impl;

import it.unibz.inf.ontop.model.DatatypeFactory;
import it.unibz.inf.ontop.model.OBDADataFactory;

/**
 * Ubiquitously used in the code
 */
public class OntopModelSingletons {
    public static final DatatypeFactory DATATYPE_FACTORY = new DatatypeFactoryImpl();
    public static final OBDADataFactory DATA_FACTORY = new OBDADataFactoryImpl(DATATYPE_FACTORY);
}
