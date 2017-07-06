package it.unibz.inf.ontop.spec.trans.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.exception.OntologyException;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.trans.*;

public class DefaultMappingTransformer implements MappingTransformer{


    private final MappingCanonicalRewriter mappingCanonicalRewriter;
    private final MappingNormalizer mappingNormalizer;
    private final MappingSaturator mappingSaturator;
    private final MappingDatatypeFiller mappingDatatypeFiller;
    private final ABoxFactIntoMappingConverter factConverter;
    private final MappingMerger mappingMerger;
    private final OntopMappingSettings settings;
    private final MappingSameAsRewriter sameAsRewriter;
    private final MappingEquivalenceFreeRewriter eqFreeRewriter;
    private final SpecificationFactory specificationFactory;

    @Inject
    public DefaultMappingTransformer(MappingCanonicalRewriter mappingCanonicalRewriter, MappingNormalizer mappingNormalizer, MappingSaturator mappingSaturator, MappingDatatypeFiller mappingDatatypeFiller, ABoxFactIntoMappingConverter inserter, MappingMerger mappingMerger, OntopMappingSettings settings, MappingSameAsRewriter sameAsRewriter, MappingEquivalenceFreeRewriter eqFreeRewriter, SpecificationFactory specificationFactory) {
        this.mappingCanonicalRewriter = mappingCanonicalRewriter;
        this.mappingNormalizer = mappingNormalizer;
        this.mappingSaturator = mappingSaturator;
        this.mappingDatatypeFiller = mappingDatatypeFiller;
        this.factConverter = inserter;
        this.mappingMerger = mappingMerger;
        this.settings = settings;
        this.sameAsRewriter = sameAsRewriter;
        this.eqFreeRewriter = eqFreeRewriter;
        this.specificationFactory = specificationFactory;
    }

    @Override
    public OBDASpecification transform(Mapping mapping, DBMetadata dbMetadata, Ontology ontology, TBoxReasoner tBox) throws MappingException, OntologyException {
        Mapping datatypedMapping = mappingDatatypeFiller.inferMissingDatatypes(mapping, tBox, ontology.getVocabulary(),
                dbMetadata);
        Mapping factsAsMapping = factConverter.convert(ontology, datatypedMapping.getExecutorRegistry(),
                settings.isOntologyAnnotationQueryingEnabled(), datatypedMapping.getMetadata().getUriTemplateMatcher());
        Mapping mappingWithFacts = mappingMerger.merge(datatypedMapping, factsAsMapping);
        Mapping eqFreeMapping = eqFreeRewriter.rewrite(mappingWithFacts, tBox, ontology.getVocabulary(), dbMetadata);
        Mapping sameAsOptimizedMapping = sameAsRewriter.rewrite(eqFreeMapping, dbMetadata);
        Mapping canonicalMapping = mappingCanonicalRewriter.rewrite(sameAsOptimizedMapping, dbMetadata);
        Mapping saturatedMapping = mappingSaturator.saturate(canonicalMapping, dbMetadata, tBox);
        Mapping normalizedMapping = mappingNormalizer.normalize(saturatedMapping);

        return specificationFactory.createSpecification(normalizedMapping, dbMetadata, tBox, ontology.getVocabulary());
    }
}
