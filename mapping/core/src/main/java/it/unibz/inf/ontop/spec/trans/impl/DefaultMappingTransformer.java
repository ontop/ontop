package it.unibz.inf.ontop.spec.trans.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.exception.OntologyException;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.mapping.*;
import it.unibz.inf.ontop.spec.trans.*;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.spec.trans.MappingTransformer;
import it.unibz.inf.ontop.spec.OBDASpecification;

public class DefaultMappingTransformer implements MappingTransformer{


    private final MappingCanonicalRewriter mappingCanonicalRewriter;
    private final MappingNormalizer mappingNormalizer;
    private final MappingSaturator mappingSaturator;
    private final MappingDatatypeFiller mappingDatatypeFiller;
    private final ABoxFactIntoMappingConverter factConverter;
    private final MappingMerger mappingMerger;
    private final OntopMappingSettings settings;
    private final SpecificationFactory specificationFactory;

    @Inject
    public DefaultMappingTransformer(MappingCanonicalRewriter mappingCanonicalRewriter, MappingNormalizer mappingNormalizer, MappingSaturator mappingSaturator, MappingDatatypeFiller mappingDatatypeFiller, ABoxFactIntoMappingConverter inserter, MappingMerger mappingMerger, OntopMappingSettings settings, SpecificationFactory specificationFactory) {
        this.mappingCanonicalRewriter = mappingCanonicalRewriter;
        this.mappingNormalizer = mappingNormalizer;
        this.mappingSaturator = mappingSaturator;
        this.mappingDatatypeFiller = mappingDatatypeFiller;
        this.factConverter = inserter;
        this.mappingMerger = mappingMerger;
        this.settings = settings;
        this.specificationFactory = specificationFactory;
    }

    @Override
    public OBDASpecification transform(Mapping mapping, DBMetadata dbMetadata, Ontology ontology, TBoxReasoner tBox,
                                       ExecutorRegistry executorRegistry) throws MappingException, OntologyException {
        Mapping datatypedMapping = mappingDatatypeFiller.inferMissingDatatypes(mapping, tBox, ontology
                .getVocabulary(), dbMetadata, executorRegistry);
        Mapping factsAsMapping = factConverter.convert(ontology, executorRegistry,
                settings.isOntologyAnnotationQueryingEnabled());
        Mapping mappingWithFacts = mappingMerger.merge(datatypedMapping, factsAsMapping);
        Mapping canonicalMapping = mappingCanonicalRewriter.rewrite(mappingWithFacts, dbMetadata);
        Mapping saturatedMapping = mappingSaturator.saturate(canonicalMapping, dbMetadata, tBox);
        Mapping normalizedMapping = mappingNormalizer.normalize(saturatedMapping);

        return specificationFactory.createSpecification(normalizedMapping, dbMetadata, tBox, ontology.getVocabulary());
    }
}
