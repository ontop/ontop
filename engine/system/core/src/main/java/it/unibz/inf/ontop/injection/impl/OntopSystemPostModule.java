package it.unibz.inf.ontop.injection.impl;


import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import it.unibz.inf.ontop.answering.OntopQueryEngine;
import it.unibz.inf.ontop.answering.cache.HTTPCacheHeaders;
import it.unibz.inf.ontop.injection.OntopSystemFactory;
import it.unibz.inf.ontop.injection.OntopReformulationSettings;
import it.unibz.inf.ontop.answering.connection.DBConnector;

public class OntopSystemPostModule  extends OntopAbstractModule {

    protected OntopSystemPostModule(OntopReformulationSettings settings) {
        super(settings);
    }

    @Override
    protected void configure() {
        bindFromSettings(HTTPCacheHeaders.class);

        Module engineFactoryModule = buildFactory(ImmutableList.of(
                OntopQueryEngine.class,
                DBConnector.class),
                OntopSystemFactory.class);
        install(engineFactoryModule);
    }
}
