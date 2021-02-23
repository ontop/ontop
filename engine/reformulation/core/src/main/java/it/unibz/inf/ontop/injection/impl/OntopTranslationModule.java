package it.unibz.inf.ontop.injection.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import it.unibz.inf.ontop.answering.logging.QueryLogger;
import it.unibz.inf.ontop.answering.reformulation.generation.PostProcessingProjectionSplitter;
import it.unibz.inf.ontop.answering.reformulation.input.InputQueryFactory;
import it.unibz.inf.ontop.answering.reformulation.input.RDF4JInputQueryFactory;
import it.unibz.inf.ontop.iq.view.OntopViewUnfolder;
import it.unibz.inf.ontop.injection.OntopReformulationConfiguration;
import it.unibz.inf.ontop.injection.OntopReformulationSettings;

/**
 * NB: please also consider OntopQueryAnsweringPostModule
 */
public class OntopTranslationModule extends OntopAbstractModule {
    // Temporary
    private OntopReformulationConfiguration configuration;

    protected OntopTranslationModule(OntopReformulationConfiguration configuration) {
        super(configuration.getSettings());
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        bind(OntopReformulationSettings.class).toInstance(configuration.getSettings());
        bindFromSettings(RDF4JInputQueryFactory.class);
        bindFromSettings(InputQueryFactory.class);
        bindFromSettings(PostProcessingProjectionSplitter.class);
        bindFromSettings(OntopViewUnfolder.class);

        Module queryLoggingModule = buildFactory(ImmutableList.of(QueryLogger.class), QueryLogger.Factory.class);
        install(queryLoggingModule);

        configuration = null;
    }
}
