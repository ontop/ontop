package it.unibz.inf.ontop.spec.impl;

import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.DBMetadataExtractionException;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.exception.OntologyException;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.spec.OBDASpecInput;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.OBDASpecificationExtractor;
import it.unibz.inf.ontop.spec.mapping.MappingExtractor;
import it.unibz.inf.ontop.spec.mapping.TemporalMappingExtractor;
import it.unibz.inf.ontop.spec.mapping.pp.PreProcessedMapping;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingTransformer;
import it.unibz.inf.ontop.spec.ontology.MappingVocabularyExtractor;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.ontology.TBoxReasoner;
import it.unibz.inf.ontop.spec.ontology.impl.TBoxReasonerImpl;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Optional;

public class TemporalOBDASpecificationExtractorImpl implements OBDASpecificationExtractor {
    private final MappingExtractor mappingExtractor;
    private final TemporalMappingExtractor temporalMappingExtractor;
    private final MappingTransformer mappingTransformer;
    private final OntopMappingSettings settings;
    private final MappingVocabularyExtractor vocabularyExtractor;

    @Inject
    private TemporalOBDASpecificationExtractorImpl(
            MappingExtractor mappingExtractor,
            TemporalMappingExtractor temporalMappingExtractor,
            MappingTransformer mappingTransformer,
            OntopMappingSettings settings, MappingVocabularyExtractor vocabularyExtractor) {
        this.mappingExtractor = mappingExtractor;
        this.temporalMappingExtractor = temporalMappingExtractor;
        this.mappingTransformer = mappingTransformer;
        this.settings = settings;
        this.vocabularyExtractor = vocabularyExtractor;
    }

    @Override
    public OBDASpecification extract(@Nonnull OBDASpecInput specInput, @Nonnull Optional<DBMetadata> dbMetadata, @Nonnull Optional<Ontology> ontology, ExecutorRegistry executorRegistry) throws OBDASpecificationException {
        //re-implement
        Optional<TBoxReasoner> optionalSaturatedTBox = saturateTBox(ontology);

        MappingExtractor.MappingAndDBMetadata mappingAndDBMetadata = mappingExtractor.extract(specInput, dbMetadata, ontology,
                optionalSaturatedTBox, executorRegistry);

        TemporalMappingExtractor.MappingAndDBMetadata temporalMappingAndDBMetadata = temporalMappingExtractor.extract(specInput, dbMetadata, ontology,
                optionalSaturatedTBox, executorRegistry);

        OBDASpecification s = transform(specInput, ontology, optionalSaturatedTBox, mappingAndDBMetadata);
        System.out.println();
        return s;
    }

    @Override
    public OBDASpecification extract(@Nonnull OBDASpecInput specInput, @Nonnull PreProcessedMapping ppMapping, @Nonnull Optional<DBMetadata> dbMetadata, @Nonnull Optional<Ontology> ontology, ExecutorRegistry executorRegistry) throws OBDASpecificationException {
        Optional<TBoxReasoner> optionalSaturatedTBox = saturateTBox(ontology);

        MappingExtractor.MappingAndDBMetadata mappingAndDBMetadata = mappingExtractor.extract(ppMapping, specInput, dbMetadata,
                ontology, optionalSaturatedTBox, executorRegistry);

        return transform(specInput, ontology, optionalSaturatedTBox, mappingAndDBMetadata);
    }

    private OBDASpecification transform(@Nonnull OBDASpecInput specInput, @Nonnull Optional<Ontology> optionalOntology,
                                        Optional<TBoxReasoner> optionalInputTBox, MappingExtractor.MappingAndDBMetadata mappingAndDBMetadata)
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
