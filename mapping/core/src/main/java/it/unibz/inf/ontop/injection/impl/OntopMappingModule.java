package it.unibz.inf.ontop.injection.impl;


import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import it.unibz.inf.ontop.injection.OntopMappingConfiguration;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.injection.ProvenanceMappingFactory;
import it.unibz.inf.ontop.iq.optimizer.MappingIQNormalizer;
import it.unibz.inf.ontop.iq.optimizer.MappingUnionNormalizer;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.mapping.validation.MappingOntologyComplianceValidator;
import it.unibz.inf.ontop.spec.mapping.transformer.*;
import it.unibz.inf.ontop.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.datalog.Mapping2DatalogConverter;
import it.unibz.inf.ontop.spec.mapping.TMappingExclusionConfig;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingTransformer;


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
        bindFromSettings(MappingVariableNameNormalizer.class);
        bindFromSettings(MappingSaturator.class);
        bindFromSettings(MappingCanonicalRewriter.class);
        bindFromSettings(Datalog2QueryMappingConverter.class);
        bindFromSettings(Mapping2DatalogConverter.class);
        bindFromSettings(ABoxFactIntoMappingConverter.class);
        bindFromSettings(MappingDatatypeFiller.class);
        bindFromSettings(MappingMerger.class);
        bindFromSettings(MappingTransformer.class);
        bindFromSettings(MappingOntologyComplianceValidator.class);
        bindFromSettings(MappingSameAsInverseRewriter.class);
        bindFromSettings(MappingUnionNormalizer.class);
        bindFromSettings(MappingIQNormalizer.class);

        Module factoryModule = buildFactory(ImmutableList.of(MappingWithProvenance.class),
                ProvenanceMappingFactory.class);
        install(factoryModule);

    }

    private void bindTMappingExclusionConfig() {
        TMappingExclusionConfig tMappingExclusionConfig = configuration.getTmappingExclusions()
                .orElseGet(TMappingExclusionConfig::empty);

        bind(TMappingExclusionConfig.class).toInstance(tMappingExclusionConfig);
    }
}
