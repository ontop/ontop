package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.spec.ontology.OntologyABox;
import it.unibz.inf.ontop.spec.OBDASpecInput;
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

    @Inject
    private DefaultMappingTransformer(MappingCanonicalRewriter mappingCanonicalRewriter,
                                     MappingNormalizer mappingNormalizer,
                                     MappingSaturator mappingSaturator,
                                     ABoxFactIntoMappingConverter inserter,
                                     MappingMerger mappingMerger,
                                     OntopMappingSettings settings,
                                     MappingSameAsInverseRewriter sameAsInverseRewriter,
                                     SpecificationFactory specificationFactory) {
        this.mappingCanonicalRewriter = mappingCanonicalRewriter;
        this.mappingNormalizer = mappingNormalizer;
        this.mappingSaturator = mappingSaturator;
        this.factConverter = inserter;
        this.mappingMerger = mappingMerger;
        this.settings = settings;
        this.sameAsInverseRewriter = sameAsInverseRewriter;
        this.specificationFactory = specificationFactory;
    }

    @Override
    public OBDASpecification transform(OBDASpecInput specInput, Mapping mapping, DBMetadata dbMetadata, OntologyABox abox,
                                       ClassifiedTBox tBox) {
        Mapping factsAsMapping = factConverter.convert(abox, mapping.getExecutorRegistry(),
                settings.isOntologyAnnotationQueryingEnabled(), mapping.getMetadata().getUriTemplateMatcher());
        Mapping mappingWithFacts = mappingMerger.merge(mapping, factsAsMapping);

        return transform(specInput, mappingWithFacts, dbMetadata, tBox);
    }

    @Override
    public OBDASpecification transform(OBDASpecInput specInput, Mapping mapping, DBMetadata dbMetadata,
                                       ClassifiedTBox tBox) {
        Mapping sameAsOptimizedMapping = sameAsInverseRewriter.rewrite(mapping, dbMetadata);
        Mapping canonicalMapping = mappingCanonicalRewriter.rewrite(sameAsOptimizedMapping, dbMetadata);
        Mapping saturatedMapping = mappingSaturator.saturate(canonicalMapping, dbMetadata, tBox);
        Mapping normalizedMapping = mappingNormalizer.normalize(saturatedMapping);

        return specificationFactory.createSpecification(normalizedMapping, dbMetadata, tBox);
    }
}
