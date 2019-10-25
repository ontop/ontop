package it.unibz.inf.ontop.injection.impl;


import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import it.unibz.inf.ontop.injection.*;
import it.unibz.inf.ontop.datalog.QueryUnionSplitter;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingCaster;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.mapping.parser.TargetQueryParser;
import it.unibz.inf.ontop.spec.mapping.validation.MappingOntologyComplianceValidator;
import it.unibz.inf.ontop.spec.mapping.transformer.*;
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
        bindFromSettings(MappingCanonicalTransformer.class);
        bindFromSettings(ABoxFactIntoMappingConverter.class);
        bindFromSettings(MappingDatatypeFiller.class);
        bindFromSettings(MappingMerger.class);
        bindFromSettings(MappingTransformer.class);
        bindFromSettings(MappingOntologyComplianceValidator.class);
        bindFromSettings(MappingSameAsInverseRewriter.class);
        bindFromSettings(MappingCQCOptimizer.class);
        bindFromSettings(QueryUnionSplitter.class);
        bindFromSettings(MappingCaster.class);
        bindFromSettings(MappingDistinctTransformer.class);
        bindFromSettings(MappingEqualityTransformer.class);

        bind(MappingCoreSingletons.class).to(MappingCoreSingletonsImpl.class);

        Module factoryModule = buildFactory(ImmutableList.of(MappingWithProvenance.class),
                ProvenanceMappingFactory.class);
        install(factoryModule);


        Module targetQueryParserModule = buildFactory(ImmutableList.of(TargetQueryParser.class),
                TargetQueryParserFactory.class);
        install(targetQueryParserModule);

    }

    private void bindTMappingExclusionConfig() {
        TMappingExclusionConfig tMappingExclusionConfig = configuration.getTmappingExclusions()
                .orElseGet(TMappingExclusionConfig::empty);

        bind(TMappingExclusionConfig.class).toInstance(tMappingExclusionConfig);
    }
}
