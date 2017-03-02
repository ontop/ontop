package it.unibz.inf.ontop.injection.impl;


import com.google.inject.util.Providers;
import it.unibz.inf.ontop.injection.OntopMappingConfiguration;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.mapping.MappingCanonicalRewriter;
import it.unibz.inf.ontop.mapping.MappingNormalizer;
import it.unibz.inf.ontop.mapping.MappingSaturator;
import it.unibz.inf.ontop.mapping.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.mapping.datalog.Mapping2DatalogConverter;
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
        bindFromPreferences(MappingSaturator.class);
        bindFromPreferences(MappingCanonicalRewriter.class);
        bindFromPreferences(Datalog2QueryMappingConverter.class);
        bindFromPreferences(Mapping2DatalogConverter.class);
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
