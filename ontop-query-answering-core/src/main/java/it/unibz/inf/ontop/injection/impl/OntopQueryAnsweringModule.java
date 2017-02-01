package it.unibz.inf.ontop.injection.impl;


import com.google.inject.util.Providers;
import it.unibz.inf.ontop.injection.OntopRuntimeConfiguration;
import it.unibz.inf.ontop.injection.OntopRuntimeSettings;
import it.unibz.inf.ontop.answering.reformulation.IRIDictionary;

import java.util.Optional;

/**
 * NB: please also consider OntopQueryAnsweringPostModule
 */
public class OntopQueryAnsweringModule extends OntopAbstractModule {
    // Temporary
    private OntopRuntimeConfiguration configuration;

    protected OntopQueryAnsweringModule(OntopRuntimeConfiguration configuration) {
        super(configuration.getSettings());
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        bind(OntopRuntimeSettings.class).toInstance(configuration.getSettings());

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
