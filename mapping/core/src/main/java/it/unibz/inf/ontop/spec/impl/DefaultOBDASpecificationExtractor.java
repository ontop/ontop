package it.unibz.inf.ontop.spec.impl;

import it.unibz.inf.ontop.exception.DBMetadataExtractionException;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.OntologyException;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.ontology.utils.MappingVocabularyExtractor;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;
import it.unibz.inf.ontop.pp.PreProcessedMapping;
import it.unibz.inf.ontop.spec.MappingExtractor;
import it.unibz.inf.ontop.spec.MappingExtractor.MappingAndDBMetadata;
import it.unibz.inf.ontop.spec.OBDASpecInput;
import it.unibz.inf.ontop.spec.trans.MappingTransformer;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.OBDASpecificationExtractor;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.inject.Inject;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class DefaultOBDASpecificationExtractor implements OBDASpecificationExtractor {

    private final MappingExtractor mappingExtractor;
    private final MappingTransformer mappingTransformer;
    private final OntopMappingSettings settings;
    private final MappingVocabularyExtractor vocabularyExtractor;

    @Inject
    private DefaultOBDASpecificationExtractor(MappingExtractor mappingExtractor, MappingTransformer mappingTransformer,
                                              OntopMappingSettings settings, MappingVocabularyExtractor vocabularyExtractor) {
        this.mappingExtractor = mappingExtractor;
        this.mappingTransformer = mappingTransformer;
        this.settings = settings;
        this.vocabularyExtractor = vocabularyExtractor;
    }

    @Override
    public OBDASpecification extract(@Nonnull OBDASpecInput specInput, @Nonnull Optional<DBMetadata> dbMetadata,
                                     @Nonnull Optional<Ontology> optionalOntology, ExecutorRegistry executorRegistry)
            throws OBDASpecificationException {
        Optional<TBoxReasoner> optionalSaturatedTBox = saturateTBox(optionalOntology);

        MappingAndDBMetadata mappingAndDBMetadata = mappingExtractor.extract(specInput, dbMetadata, optionalOntology,
                optionalSaturatedTBox, executorRegistry);

        return transform(specInput, optionalOntology, optionalSaturatedTBox, mappingAndDBMetadata);
    }

    @Override
    public OBDASpecification extract(@Nonnull OBDASpecInput specInput, @Nonnull PreProcessedMapping ppMapping,
                                     @Nonnull Optional<DBMetadata> dbMetadata, @Nonnull Optional<Ontology> optionalOntology,
                                     ExecutorRegistry executorRegistry) throws OBDASpecificationException {
        Optional<TBoxReasoner> optionalSaturatedTBox = saturateTBox(optionalOntology);

        MappingAndDBMetadata mappingAndDBMetadata = mappingExtractor.extract(ppMapping, specInput, dbMetadata,
                optionalOntology, optionalSaturatedTBox, executorRegistry);

        return transform(specInput, optionalOntology, optionalSaturatedTBox, mappingAndDBMetadata);
    }

    private OBDASpecification transform(@Nonnull OBDASpecInput specInput, @Nonnull Optional<Ontology> optionalOntology,
                                        Optional<TBoxReasoner> optionalInputTBox, MappingAndDBMetadata mappingAndDBMetadata)
            throws MappingException, OntologyException, DBMetadataExtractionException {
        //Bootstrap the ontology from the mapping if it does not already exist
        Ontology ontology = optionalOntology
                .orElseGet(() -> vocabularyExtractor.extractOntology(mappingAndDBMetadata.getMapping()));
        TBoxReasoner tBox = optionalInputTBox
                .orElseGet(() -> TBoxReasonerImpl.create(ontology, settings.isEquivalenceOptimizationEnabled()));

        return mappingTransformer.transform(
                specInput,
                mappingAndDBMetadata.getMapping(),
                mappingAndDBMetadata.getDBMetadata(),
                ontology,
                tBox);
    }

    private Optional<TBoxReasoner> saturateTBox(Optional<Ontology> ontology) {
        return ontology
                .map(o -> TBoxReasonerImpl.create(o, settings.isEquivalenceOptimizationEnabled()));
    }


}
