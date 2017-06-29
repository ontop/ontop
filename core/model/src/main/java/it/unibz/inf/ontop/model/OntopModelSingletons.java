package it.unibz.inf.ontop.model;

import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.impl.AtomFactoryImpl;
import it.unibz.inf.ontop.model.impl.DatatypeFactoryImpl;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.type.DatatypeFactory;

/**
 * Ubiquitously used in the code
 */
public class OntopModelSingletons {
    public static final DatatypeFactory DATATYPE_FACTORY = DatatypeFactoryImpl.getInstance();
    public static final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
    public static final AtomFactory ATOM_FACTORY = AtomFactoryImpl.getInstance();
}
