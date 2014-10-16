package org.semanticweb.ontop.injection;

import com.google.inject.AbstractModule;

import java.util.Properties;

/**
 * TODO: add generic code about property analysis
 */
public abstract class OntopAbstractModule extends AbstractModule {

    private final Properties configuration;

    protected OntopAbstractModule(Properties configuration) {
        this.configuration = configuration;
    }

}
