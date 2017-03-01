package it.unibz.inf.ontop.spec.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.exception.OntologyException;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.mapping.MappingNormalizer;
import it.unibz.inf.ontop.mapping.MappingSaturator;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.ontology.OntologyFactory;
import it.unibz.inf.ontop.ontology.impl.OntologyFactoryImpl;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;
import it.unibz.inf.ontop.spec.MappingTransformer;
import it.unibz.inf.ontop.spec.OBDASpecification;

import java.util.Optional;

@Singleton
public class DefaultMappingTransformer implements MappingTransformer {

    private static final OntologyFactory ONTOLOGY_FACTORY = OntologyFactoryImpl.getInstance();

    private final SpecificationFactory specificationFactory;
    private final OntopMappingSettings settings;
    private final MappingSaturator mappingSaturator;
    private final MappingNormalizer mappingNormalizer;

    @Inject
    private DefaultMappingTransformer(SpecificationFactory specificationFactory, OntopMappingSettings settings,
                                      MappingSaturator mappingSaturator, MappingNormalizer mappingNormalizer) {
        this.specificationFactory = specificationFactory;
        this.settings = settings;
        this.mappingSaturator = mappingSaturator;
        this.mappingNormalizer = mappingNormalizer;
    }

    @Override
    public OBDASpecification transform(Mapping mapping, DBMetadata dbMetadata,
                                       Optional<Ontology> optionalOntology) throws MappingException, OntologyException {

        Ontology ontology = optionalOntology
                // TODO: should we extract it from the mapping instead?
                .orElseGet(() -> ONTOLOGY_FACTORY.createOntology(ONTOLOGY_FACTORY.createVocabulary()));

        TBoxReasoner saturatedTBox = TBoxReasonerImpl.create(ontology, settings.isEquivalenceOptimizationEnabled());

        // TODO: support canonical IRI rewriting

        Mapping saturatedMapping = mappingSaturator.saturate(mapping, dbMetadata, saturatedTBox);
        Mapping normalizedMapping = mappingNormalizer.normalize(saturatedMapping);

        return specificationFactory.createSpecification(normalizedMapping, dbMetadata, saturatedTBox,
                ontology.getVocabulary());
    }
}
