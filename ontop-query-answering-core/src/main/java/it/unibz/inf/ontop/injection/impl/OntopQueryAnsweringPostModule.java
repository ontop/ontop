package it.unibz.inf.ontop.injection.impl;


import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import it.unibz.inf.ontop.answering.OntopQueryEngine;
import it.unibz.inf.ontop.answering.reformulation.OntopQueryReformulator;
import it.unibz.inf.ontop.answering.reformulation.unfolding.QueryUnfolder;
import it.unibz.inf.ontop.injection.*;
import it.unibz.inf.ontop.owlrefplatform.core.DBConnector;
import it.unibz.inf.ontop.owlrefplatform.core.srcquerygeneration.NativeQueryGenerator;

/**
 * POST-module: to be loaded after that all the dependencies of concrete implementations have been defined
 *
 */
public class OntopQueryAnsweringPostModule extends OntopAbstractModule {

    protected OntopQueryAnsweringPostModule(OntopQueryAnsweringSettings settings) {
        super(settings);
    }

    @Override
    protected void configure() {
        Module reformulationFactoryModule = buildFactory(
                ImmutableList.of(
                        QueryUnfolder.class,
                        NativeQueryGenerator.class),
                ReformulationFactory.class);
        install(reformulationFactoryModule);

        Module componentFactoryModule = buildFactory(ImmutableList.of(
                OntopQueryReformulator.class, DBConnector.class),
                OntopComponentFactory.class);
        install(componentFactoryModule);

        Module engineFactoryModule = buildFactory(ImmutableList.of(OntopQueryEngine.class),
                OntopEngineFactory.class);
        install(engineFactoryModule);
    }
}
