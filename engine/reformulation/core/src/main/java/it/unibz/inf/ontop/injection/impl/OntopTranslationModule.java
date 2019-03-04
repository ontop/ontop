package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.answering.reformulation.generation.PostProcessingProjectionSplitter;
import it.unibz.inf.ontop.answering.reformulation.input.InputQueryFactory;
import it.unibz.inf.ontop.answering.reformulation.input.RDF4JInputQueryFactory;
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

        configuration = null;
    }
}
