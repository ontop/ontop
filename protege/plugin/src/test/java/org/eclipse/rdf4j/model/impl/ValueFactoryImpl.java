package org.eclipse.rdf4j.model.impl;

/**
 * Hacky! 
 * ValueFactoryImpl is removed in the recent RDF4J used in OWLAPI v5. 
 * Since Protege and the tests in the ontop-protege-plugin modules still relies on OWLAPI v4, 
 * this class is here to make test testing happy.
 */
public class ValueFactoryImpl extends SimpleValueFactory {

    private static final ValueFactoryImpl sharedInstance = new ValueFactoryImpl();

    public static ValueFactoryImpl getInstance() {
        return sharedInstance;
    }    
}
