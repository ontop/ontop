package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.injection.TemporalSpecificationFactory;
import it.unibz.inf.ontop.spec.OBDASpecInput;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.TemporalMapping;
import it.unibz.inf.ontop.spec.mapping.transformer.*;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.ontology.OntologyABox;
import it.unibz.inf.ontop.spec.ontology.impl.OntologyBuilderImpl;
import it.unibz.inf.ontop.temporal.model.DatalogMTLProgram;

import java.util.Optional;

public class TemporalMappingTransformerImpl implements TemporalMappingTransformer {

    private final MappingCanonicalRewriter mappingCanonicalRewriter;
    private final MappingVariableNameNormalizer mappingNormalizer;
    private final MappingSaturator mappingSaturator;
    private final ABoxFactIntoMappingConverter factConverter;
    private final MappingMerger mappingMerger;
    private final OntopMappingSettings settings;
    private final MappingSameAsInverseRewriter sameAsInverseRewriter;
    private final SpecificationFactory specificationFactory;
    private final TemporalSpecificationFactory temporalSpecificationFactory;
    private final TemporalMappingSaturator temporalMappingSaturator;
    private final StaticRuleMappingSaturator staticRuleMappingSaturator;

    @Inject
    private TemporalMappingTransformerImpl(MappingCanonicalRewriter mappingCanonicalRewriter,
                                           MappingVariableNameNormalizer mappingNormalizer,
                                           MappingSaturator mappingSaturator,
                                           ABoxFactIntoMappingConverter inserter,
                                           MappingMerger mappingMerger,
                                           OntopMappingSettings settings,
                                           MappingSameAsInverseRewriter sameAsInverseRewriter,
                                           SpecificationFactory specificationFactory1,
                                           TemporalSpecificationFactory specificationFactory,
                                           TemporalMappingSaturator temporalMappingSaturator,
                                           StaticRuleMappingSaturator staticRuleMappingSaturator) {
        this.mappingCanonicalRewriter = mappingCanonicalRewriter;
        this.mappingNormalizer = mappingNormalizer;
        this.mappingSaturator = mappingSaturator;
        this.factConverter = inserter;
        this.mappingMerger = mappingMerger;
        this.settings = settings;
        this.sameAsInverseRewriter = sameAsInverseRewriter;
        this.specificationFactory = specificationFactory1;
        this.temporalSpecificationFactory = specificationFactory;
        this.temporalMappingSaturator = temporalMappingSaturator;
        this.staticRuleMappingSaturator = staticRuleMappingSaturator;
    }

    @Override
    public OBDASpecification transform(Mapping mapping, DBMetadata dbMetadata, Optional<Ontology> ontology, TemporalMapping temporalMapping,
                                       DBMetadata temporalDBMetadata, DatalogMTLProgram datalogMTLProgram) {

        if (ontology.isPresent()) {
            Mapping factsAsMapping = factConverter.convert(ontology.get().abox(), mapping.getExecutorRegistry(),
                    settings.isOntologyAnnotationQueryingEnabled(), mapping.getMetadata().getUriTemplateMatcher());
            Mapping mappingWithFacts = mappingMerger.merge(mapping, factsAsMapping);

            //copied from createSpecification method
            Mapping sameAsOptimizedMapping = sameAsInverseRewriter.rewrite(mappingWithFacts, dbMetadata);
            Mapping canonicalMapping = mappingCanonicalRewriter.rewrite(sameAsOptimizedMapping, dbMetadata);
            Mapping saturatedMapping = mappingSaturator.saturate(canonicalMapping, dbMetadata, ontology.get().tbox());
            Mapping normalizedMapping = mappingNormalizer.normalize(saturatedMapping);

            Mapping saturatedRuleMapping = staticRuleMappingSaturator.saturate(normalizedMapping, dbMetadata, datalogMTLProgram);

            TemporalMapping temporalSaturatedMapping = temporalMappingSaturator.saturate(saturatedRuleMapping, temporalMapping, temporalDBMetadata, datalogMTLProgram);
            return temporalSpecificationFactory.createTemporalSpecification(saturatedRuleMapping, dbMetadata, temporalSaturatedMapping, temporalDBMetadata, ontology.get().tbox());
        }
        else {
            ClassifiedTBox emptyTBox = OntologyBuilderImpl.builder().build().tbox();

            //copied from createSpecification method
            Mapping sameAsOptimizedMapping = sameAsInverseRewriter.rewrite(mapping, dbMetadata);
            Mapping canonicalMapping = mappingCanonicalRewriter.rewrite(sameAsOptimizedMapping, dbMetadata);
            Mapping saturatedMapping = mappingSaturator.saturate(canonicalMapping, dbMetadata, ontology.get().tbox());
            Mapping normalizedMapping = mappingNormalizer.normalize(saturatedMapping);

            Mapping saturatedRuleMapping = staticRuleMappingSaturator.saturate(normalizedMapping, dbMetadata, datalogMTLProgram);

            TemporalMapping temporalSaturatedMapping = temporalMappingSaturator.saturate(saturatedRuleMapping, temporalMapping, temporalDBMetadata, datalogMTLProgram);
            return temporalSpecificationFactory.createTemporalSpecification(saturatedRuleMapping, dbMetadata, temporalSaturatedMapping, temporalDBMetadata, emptyTBox);
        }
    }

    @Override
    public OBDASpecification transform(Mapping mapping, DBMetadata dbMetadata, Optional<Ontology> ontology) {
        if (ontology.isPresent()) {
            Mapping factsAsMapping = factConverter.convert(ontology.get().abox(), mapping.getExecutorRegistry(),
                    settings.isOntologyAnnotationQueryingEnabled(), mapping.getMetadata().getUriTemplateMatcher());
            Mapping mappingWithFacts = mappingMerger.merge(mapping, factsAsMapping);
            return createSpecification(mappingWithFacts, dbMetadata, ontology.get().tbox());
        }
        else {
            ClassifiedTBox emptyTBox = OntologyBuilderImpl.builder().build().tbox();
            return createSpecification(mapping, dbMetadata, emptyTBox);
        }
    }

    OBDASpecification createSpecification(Mapping mapping, DBMetadata dbMetadata, ClassifiedTBox tbox) {
        Mapping sameAsOptimizedMapping = sameAsInverseRewriter.rewrite(mapping, dbMetadata);
        Mapping canonicalMapping = mappingCanonicalRewriter.rewrite(sameAsOptimizedMapping, dbMetadata);
        Mapping saturatedMapping = mappingSaturator.saturate(canonicalMapping, dbMetadata, tbox);
        Mapping normalizedMapping = mappingNormalizer.normalize(saturatedMapping);

        return specificationFactory.createSpecification(normalizedMapping, dbMetadata, tbox);
    }
}
