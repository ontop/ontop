package it.unibz.inf.ontop.injection.impl;


import it.unibz.inf.ontop.injection.OntopMappingConfiguration;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.spec.trans.*;
import it.unibz.inf.ontop.mapping.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.mapping.datalog.Mapping2DatalogConverter;
import it.unibz.inf.ontop.mapping.pp.validation.PPMappingOntologyComplianceValidator;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.TMappingExclusionConfig;
import it.unibz.inf.ontop.spec.trans.MappingTransformer;


public class OntopMappingModule extends OntopAbstractModule {

    private final OntopMappingConfiguration configuration;

    OntopMappingModule(OntopMappingConfiguration configuration) {
        super(configuration.getSettings());
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        bindTMappingExclusionConfig();
        bind(OntopMappingSettings.class).toInstance(configuration.getSettings());
        bindFromPreferences(MappingNormalizer.class);
        bindFromPreferences(MappingSaturator.class);
        bindFromPreferences(MappingCanonicalRewriter.class);
        bindFromPreferences(Datalog2QueryMappingConverter.class);
        bindFromPreferences(Mapping2DatalogConverter.class);
        bindFromPreferences(PPMappingOntologyComplianceValidator.class);
        bindFromPreferences(ABoxFactIntoMappingConverter.class);
        bindFromPreferences(MappingDatatypeFiller.class);
        bindFromPreferences(MappingMerger.class);
        bindFromPreferences(MappingTransformer.class);
    }

    private void bindTMappingExclusionConfig() {
        TMappingExclusionConfig tMappingExclusionConfig = configuration.getTmappingExclusions()
                .orElseGet(TMappingExclusionConfig::empty);

        bind(TMappingExclusionConfig.class).toInstance(tMappingExclusionConfig);
    }
}
