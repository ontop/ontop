package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.spec.ontology.MappingVocabularyExtractor;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.mapping.transformer.*;

public class DefaultMappingTransformer implements MappingTransformer {

    private final MappingCanonicalRewriter mappingCanonicalRewriter;
    private final MappingNormalizer mappingNormalizer;
    private final MappingSaturator mappingSaturator;
    private final ABoxFactIntoMappingConverter factConverter;
    private final MappingMerger mappingMerger;
    private final OntopMappingSettings settings;
    private final MappingSameAsInverseRewriter sameAsInverseRewriter;
    private final SpecificationFactory specificationFactory;
    private final MappingVocabularyExtractor vocabularyExtractor;

    @Inject
    private DefaultMappingTransformer(MappingCanonicalRewriter mappingCanonicalRewriter,
                                     MappingNormalizer mappingNormalizer,
                                     MappingSaturator mappingSaturator,
                                     ABoxFactIntoMappingConverter inserter,
                                     MappingMerger mappingMerger,
                                     OntopMappingSettings settings,
                                     MappingSameAsInverseRewriter sameAsInverseRewriter,
                                     SpecificationFactory specificationFactory,
                                     MappingVocabularyExtractor vocabularyExtractor) {
        this.mappingCanonicalRewriter = mappingCanonicalRewriter;
        this.mappingNormalizer = mappingNormalizer;
        this.mappingSaturator = mappingSaturator;
        this.factConverter = inserter;
        this.mappingMerger = mappingMerger;
        this.settings = settings;
        this.sameAsInverseRewriter = sameAsInverseRewriter;
        this.specificationFactory = specificationFactory;
        this.vocabularyExtractor = vocabularyExtractor;
    }

    @Override
    public OBDASpecification transform(Mapping mapping, DBMetadata dbMetadata, Ontology ontology) {

        Mapping factsAsMapping = factConverter.convert(ontology.abox(), mapping.getExecutorRegistry(),
                settings.isOntologyAnnotationQueryingEnabled(), mapping.getMetadata().getUriTemplateMatcher());
        Mapping mappingWithFacts = mappingMerger.merge(mapping, factsAsMapping);

        Mapping sameAsOptimizedMapping = sameAsInverseRewriter.rewrite(mappingWithFacts, dbMetadata);
        Mapping canonicalMapping = mappingCanonicalRewriter.rewrite(sameAsOptimizedMapping, dbMetadata);
        Mapping saturatedMapping = mappingSaturator.saturate(canonicalMapping, dbMetadata, ontology.tbox());
        Mapping normalizedMapping = mappingNormalizer.normalize(saturatedMapping);

        return specificationFactory.createSpecification(normalizedMapping, dbMetadata, ontology.tbox());
    }

    @Override
    public OBDASpecification transform(Mapping mapping, DBMetadata dbMetadata) {

        Mapping sameAsOptimizedMapping = sameAsInverseRewriter.rewrite(mapping, dbMetadata);
        Mapping canonicalMapping = mappingCanonicalRewriter.rewrite(sameAsOptimizedMapping, dbMetadata);
        Mapping saturatedMapping = mappingSaturator.saturate(canonicalMapping, dbMetadata);
        Mapping normalizedMapping = mappingNormalizer.normalize(saturatedMapping);

        // extract empty TBox (just the list of classes / properties)
        ClassifiedTBox tBox = vocabularyExtractor.extractOntology(mapping);

        return specificationFactory.createSpecification(normalizedMapping, dbMetadata, tBox);
    }
}
