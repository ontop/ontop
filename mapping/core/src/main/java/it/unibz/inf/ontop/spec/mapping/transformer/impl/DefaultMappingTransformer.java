package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.DBMetadataExtractionException;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.ontology.TBoxReasoner;
import it.unibz.inf.ontop.spec.OBDASpecInput;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.mapping.transformer.*;

public class DefaultMappingTransformer implements MappingTransformer{


    private final MappingCanonicalRewriter mappingCanonicalRewriter;
    private final MappingNormalizer mappingNormalizer;
    private final MappingSaturator mappingSaturator;
    private final ABoxFactIntoMappingConverter factConverter;
    private final MappingMerger mappingMerger;
    private final OntopMappingSettings settings;
    private final MappingSameAsInverseRewriter sameAsInverseRewriter;
    private final MappingEquivalenceFreeRewriter eqFreeRewriter;
    private final SpecificationFactory specificationFactory;

    @Inject
    private DefaultMappingTransformer(MappingCanonicalRewriter mappingCanonicalRewriter,
                                     MappingNormalizer mappingNormalizer,
                                     MappingSaturator mappingSaturator,
                                     ABoxFactIntoMappingConverter inserter,
                                     MappingMerger mappingMerger,
                                     OntopMappingSettings settings,
                                     MappingSameAsInverseRewriter sameAsInverseRewriter,
                                     MappingEquivalenceFreeRewriter eqFreeRewriter,
                                     SpecificationFactory specificationFactory) {
        this.mappingCanonicalRewriter = mappingCanonicalRewriter;
        this.mappingNormalizer = mappingNormalizer;
        this.mappingSaturator = mappingSaturator;
        this.factConverter = inserter;
        this.mappingMerger = mappingMerger;
        this.settings = settings;
        this.sameAsInverseRewriter = sameAsInverseRewriter;
        this.eqFreeRewriter = eqFreeRewriter;
        this.specificationFactory = specificationFactory;
    }

    @Override
    public OBDASpecification transform(OBDASpecInput specInput, Mapping mapping, DBMetadata dbMetadata, Ontology ontology,
                                       TBoxReasoner tBox)
            throws MappingException, DBMetadataExtractionException {
        Mapping factsAsMapping = factConverter.convert(ontology, mapping.getExecutorRegistry(),
                settings.isOntologyAnnotationQueryingEnabled(), mapping.getMetadata().getUriTemplateMatcher());
        Mapping mappingWithFacts = mappingMerger.merge(mapping, factsAsMapping);
        Mapping eqFreeMapping = eqFreeRewriter.rewrite(mappingWithFacts, tBox, ontology.getVocabulary(), dbMetadata);
        Mapping sameAsOptimizedMapping = sameAsInverseRewriter.rewrite(eqFreeMapping, dbMetadata);
        Mapping canonicalMapping = mappingCanonicalRewriter.rewrite(sameAsOptimizedMapping, dbMetadata);
        Mapping saturatedMapping = mappingSaturator.saturate(canonicalMapping, dbMetadata, tBox);
        Mapping normalizedMapping = mappingNormalizer.normalize(saturatedMapping);

        return specificationFactory.createSpecification(normalizedMapping, dbMetadata, tBox, ontology.getVocabulary());
    }
}
