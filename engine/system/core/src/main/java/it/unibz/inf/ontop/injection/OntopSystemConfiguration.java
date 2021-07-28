package it.unibz.inf.ontop.injection;


import com.google.inject.Injector;
import it.unibz.inf.ontop.answering.OntopQueryEngine;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.spec.OBDASpecification;

public interface OntopSystemConfiguration extends OntopReformulationConfiguration {

    @Override
    OntopSystemSettings getSettings();

    /**
     * Loads the query engine (which gives access to connection and so on)
     */
    default OntopQueryEngine loadQueryEngine() throws OBDASpecificationException {
        Injector injector = getInjector();
        OntopSystemFactory systemFactory = injector.getInstance(OntopSystemFactory.class);
        OBDASpecification obdaSpecification = loadSpecification();
        return systemFactory.create(obdaSpecification);
    }

    interface OntopSystemBuilderFragment<B extends Builder<B>> {

        B keepPermanentDBConnection(boolean keep);
    }

    interface Builder<B extends Builder<B>>
            extends OntopSystemBuilderFragment<B>,
            OntopReformulationConfiguration.Builder<B> {

        @Override
        OntopSystemConfiguration build();
    }
}
