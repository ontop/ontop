package it.unibz.inf.ontop.injection.impl;


import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import it.unibz.inf.ontop.answering.OntopQueryEngine;
import it.unibz.inf.ontop.injection.OntopSystemFactory;
import it.unibz.inf.ontop.injection.OntopTranslationSettings;
import it.unibz.inf.ontop.answering.connection.DBConnector;

public class OntopSystemPostModule  extends OntopAbstractModule {

    protected OntopSystemPostModule(OntopTranslationSettings settings) {
        super(settings);
    }

    @Override
    protected void configure() {
        Module engineFactoryModule = buildFactory(ImmutableList.of(
                OntopQueryEngine.class,
                DBConnector.class),
                OntopSystemFactory.class);
        install(engineFactoryModule);
    }
}
