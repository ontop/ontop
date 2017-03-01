package it.unibz.inf.ontop.injection.impl;


import com.google.inject.util.Providers;
import it.unibz.inf.ontop.injection.OntopMappingConfiguration;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.mapping.MappingNormalizer;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.TMappingExclusionConfig;
import it.unibz.inf.ontop.sql.ImplicitDBConstraintsReader;

import java.util.Optional;


public class OntopMappingModule extends OntopAbstractModule {

    private final OntopMappingConfiguration configuration;

    OntopMappingModule(OntopMappingConfiguration configuration) {
        super(configuration.getSettings());
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        bindImplicitDBConstraints();
        bindTMappingExclusionConfig();
        bind(OntopMappingSettings.class).toInstance(configuration.getSettings());
        bindFromPreferences(MappingNormalizer.class);
    }

    private void bindImplicitDBConstraints() {
        Optional<ImplicitDBConstraintsReader> optionalDBConstraints = configuration.getImplicitDBConstraintsReader();
        if (optionalDBConstraints.isPresent()) {
            bind(ImplicitDBConstraintsReader.class).toInstance(optionalDBConstraints.get());
        } else {
            bind(ImplicitDBConstraintsReader.class).toProvider(Providers.of(null));
        }
    }

    private void bindTMappingExclusionConfig() {
        TMappingExclusionConfig tMappingExclusionConfig = configuration.getTmappingExclusions()
                .orElseGet(TMappingExclusionConfig::empty);

        bind(TMappingExclusionConfig.class).toInstance(tMappingExclusionConfig);
    }
}
