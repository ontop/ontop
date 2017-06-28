package it.unibz.inf.ontop.spec.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.exception.OntologyException;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.mapping.*;
import it.unibz.inf.ontop.mapping.transf.*;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.pivotalrepr.tools.ExecutorRegistry;
import it.unibz.inf.ontop.spec.MappingTransformer;
import it.unibz.inf.ontop.spec.OBDASpecification;

public class DefaultMappingTransformer implements MappingTransformer{


    private final MappingCanonicalRewriter mappingCanonicalRewriter;
    private final MappingNormalizer mappingNormalizer;
    private final MappingSaturator mappingSaturator;
    private final MissingDatatypeMappingExpander missingDatatypeMappingExpander;
    private final ABoxFactsMappingExpander aBoxFactsMappingExpander;
    private final OntopMappingSettings settings;
    private final SpecificationFactory specificationFactory;

    @Inject
    public DefaultMappingTransformer(MappingCanonicalRewriter mappingCanonicalRewriter, MappingNormalizer mappingNormalizer, MappingSaturator mappingSaturator, MissingDatatypeMappingExpander missingDatatypeMappingExpander, ABoxFactsMappingExpander aBoxFactsMappingExpander, OntopMappingSettings settings, SpecificationFactory specificationFactory) {
        this.mappingCanonicalRewriter = mappingCanonicalRewriter;
        this.mappingNormalizer = mappingNormalizer;
        this.mappingSaturator = mappingSaturator;
        this.missingDatatypeMappingExpander = missingDatatypeMappingExpander;
        this.aBoxFactsMappingExpander = aBoxFactsMappingExpander;
        this.settings = settings;
        this.specificationFactory = specificationFactory;
    }

    @Override
    public OBDASpecification transform(Mapping mapping, DBMetadata dbMetadata, Ontology ontology, TBoxReasoner tBox,
                                       ExecutorRegistry executorRegistry) throws MappingException, OntologyException {
        Mapping datatypedMapping = missingDatatypeMappingExpander.inferMissingDatatypes(mapping, tBox, ontology
                .getVocabulary(), dbMetadata, executorRegistry);
        Mapping mappingExtendedWithFacts = aBoxFactsMappingExpander.insertFacts(datatypedMapping, ontology, dbMetadata,
                executorRegistry, settings.isOntologyAnnotationQueryingEnabled());
        Mapping canonicalMapping = mappingCanonicalRewriter.rewrite(mappingExtendedWithFacts, dbMetadata);
        Mapping saturatedMapping = mappingSaturator.saturate(canonicalMapping, dbMetadata, tBox);
        Mapping normalizedMapping = mappingNormalizer.normalize(saturatedMapping);

        return specificationFactory.createSpecification(normalizedMapping, dbMetadata, tBox, ontology.getVocabulary());
    }
}
