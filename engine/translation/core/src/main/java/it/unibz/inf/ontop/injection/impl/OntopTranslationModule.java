package it.unibz.inf.ontop.injection.impl;


import com.google.inject.util.Providers;
import it.unibz.inf.ontop.answering.input.InputQueryFactory;
import it.unibz.inf.ontop.answering.input.RDF4JInputQueryFactory;
import it.unibz.inf.ontop.injection.OntopTranslationConfiguration;
import it.unibz.inf.ontop.injection.OntopTranslationSettings;
import it.unibz.inf.ontop.answering.reformulation.IRIDictionary;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.MappingSameAsPredicateExtractor;
import it.unibz.inf.ontop.owlrefplatform.core.translator.SameAsRewriter;

import java.util.Optional;

/**
 * NB: please also consider OntopQueryAnsweringPostModule
 */
public class OntopTranslationModule extends OntopAbstractModule {
    // Temporary
    private OntopTranslationConfiguration configuration;

    protected OntopTranslationModule(OntopTranslationConfiguration configuration) {
        super(configuration.getSettings());
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        bind(OntopTranslationSettings.class).toInstance(configuration.getSettings());
        bindFromPreferences(RDF4JInputQueryFactory.class);
        bindFromPreferences(InputQueryFactory.class);
        bindFromPreferences(MappingSameAsPredicateExtractor.class);

        Optional<IRIDictionary> iriDictionary = configuration.getIRIDictionary();
        if (iriDictionary.isPresent()) {
            bind(IRIDictionary.class).toInstance(iriDictionary.get());
        }
        else {
            bind(IRIDictionary.class).toProvider(Providers.of(null));
        }

        configuration = null;
    }
}
