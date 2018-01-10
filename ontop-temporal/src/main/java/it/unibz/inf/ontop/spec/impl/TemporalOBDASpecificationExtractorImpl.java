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
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.MappingExtractor;
import it.unibz.inf.ontop.spec.mapping.TemporalMapping;
import it.unibz.inf.ontop.spec.mapping.TemporalMappingExtractor;
import it.unibz.inf.ontop.spec.mapping.pp.PreProcessedMapping;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingTransformer;
import it.unibz.inf.ontop.spec.mapping.transformer.TemporalMappingTransformer;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.spec.ontology.MappingVocabularyExtractor;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.ontology.OntologyABox;
import it.unibz.inf.ontop.temporal.model.DatalogMTLProgram;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Optional;

public class TemporalOBDASpecificationExtractorImpl implements OBDASpecificationExtractor {
    private final MappingExtractor mappingExtractor;
    private final TemporalMappingExtractor temporalMappingExtractor;
    private final MappingTransformer mappingTransformer;
    private final TemporalMappingTransformer temporalMappingTransformer;
    private final OntopMappingSettings settings;
    private final MappingVocabularyExtractor vocabularyExtractor;

    @Inject
    private TemporalOBDASpecificationExtractorImpl(
            MappingExtractor mappingExtractor,
            MappingTransformer mappingTransformer,
            TemporalMappingExtractor temporalMappingExtractor,
            TemporalMappingTransformer temporalMappingTransformer,
            OntopMappingSettings settings, MappingVocabularyExtractor vocabularyExtractor) {
        this.mappingExtractor = mappingExtractor;
        this.temporalMappingExtractor = temporalMappingExtractor;
        this.mappingTransformer = mappingTransformer;
        this.temporalMappingTransformer = temporalMappingTransformer;
        this.settings = settings;
        this.vocabularyExtractor = vocabularyExtractor;
    }

    @Override
    public OBDASpecification extract(@Nonnull OBDASpecInput specInput, @Nonnull Optional<DBMetadata> dbMetadata,
                                     @Nonnull Optional<Ontology> optionalOntology, ExecutorRegistry executorRegistry) throws OBDASpecificationException {
        if (optionalOntology.isPresent()) {
            ClassifiedTBox saturatedTBox = optionalOntology.get().tbox();

            MappingExtractor.MappingAndDBMetadata mappingAndDBMetadata = mappingExtractor.extract(specInput, dbMetadata,
                    Optional.of(saturatedTBox), executorRegistry);

            TemporalMappingExtractor.MappingAndDBMetadata temporalMappingAndDBMetadata = temporalMappingExtractor.extract(specInput, dbMetadata,
                    Optional.of(saturatedTBox), executorRegistry);

            return temporalMappingTransformer.transform(specInput,
                    mappingAndDBMetadata.getMapping(),
                    mappingAndDBMetadata.getDBMetadata(),
                    optionalOntology.get().abox(),
                    saturatedTBox,
                    temporalMappingAndDBMetadata.getTemporalMapping(),
                    temporalMappingAndDBMetadata.getDBMetadata(),
                    ExampleSiemensProgram.getSampleProgram());
        }
        else {
            // no ontology given - extract the vocabulary from mappings and use it as an ontology
            MappingExtractor.MappingAndDBMetadata mappingAndDBMetadata = mappingExtractor.extract(specInput, dbMetadata,
                    Optional.empty(), executorRegistry);

            Ontology ontology = vocabularyExtractor.extractOntology(mappingAndDBMetadata.getMapping());

            TemporalMappingExtractor.MappingAndDBMetadata temporalMappingAndDBMetadata = temporalMappingExtractor.extract(specInput, dbMetadata,
                    Optional.empty(), executorRegistry);

            //TODO:replace this code with a more convenient code once the datalogMTL parser has been implemented
            return temporalMappingTransformer.transform(specInput,
                    mappingAndDBMetadata.getMapping(),
                    mappingAndDBMetadata.getDBMetadata(),
                    ontology.abox(), // EMPTY ABOX
                    ontology.tbox(),
                    temporalMappingAndDBMetadata.getTemporalMapping(),
                    temporalMappingAndDBMetadata.getDBMetadata(),
                    ExampleSiemensProgram.getSampleProgram());
        }
    }

    @Override
    public OBDASpecification extract(@Nonnull OBDASpecInput specInput, @Nonnull PreProcessedMapping ppMapping,
                                     @Nonnull Optional<DBMetadata> dbMetadata, @Nonnull Optional<Ontology> optionalOntology,
                                     ExecutorRegistry executorRegistry) throws OBDASpecificationException {
        if (optionalOntology.isPresent()) {
            ClassifiedTBox saturatedTBox = optionalOntology.get().tbox();

            MappingExtractor.MappingAndDBMetadata mappingAndDBMetadata = mappingExtractor.extract(ppMapping, specInput, dbMetadata,
                    Optional.of(saturatedTBox), executorRegistry);

            TemporalMappingExtractor.MappingAndDBMetadata temporalMappingAndDBMetadata = temporalMappingExtractor.extract(ppMapping, specInput, dbMetadata,
                    Optional.of(saturatedTBox), executorRegistry);

            return temporalMappingTransformer.transform(specInput,
                    mappingAndDBMetadata.getMapping(),
                    mappingAndDBMetadata.getDBMetadata(),
                    optionalOntology.get().abox(),
                    saturatedTBox,
                    temporalMappingAndDBMetadata.getTemporalMapping(),
                    temporalMappingAndDBMetadata.getDBMetadata(),
                    ExampleSiemensProgram.getSampleProgram());
        }
        else {
            // no ontology given - extract the vocabulary from mappings and use it as an ontology
            MappingExtractor.MappingAndDBMetadata mappingAndDBMetadata = mappingExtractor.extract(ppMapping, specInput, dbMetadata,
                    Optional.empty(), executorRegistry);

            Ontology ontology = vocabularyExtractor.extractOntology(mappingAndDBMetadata.getMapping());

            TemporalMappingExtractor.MappingAndDBMetadata temporalMappingAndDBMetadata = temporalMappingExtractor.extract(ppMapping, specInput, dbMetadata,
                    Optional.empty(), executorRegistry);

            //TODO:replace this code with a more convenient code once the datalogMTL parser has been implemented
            return temporalMappingTransformer.transform(specInput,
                    mappingAndDBMetadata.getMapping(),
                    mappingAndDBMetadata.getDBMetadata(),
                    ontology.abox(), // EMPTY ABOX
                    ontology.tbox(),
                    temporalMappingAndDBMetadata.getTemporalMapping(),
                    temporalMappingAndDBMetadata.getDBMetadata(),
                    ExampleSiemensProgram.getSampleProgram());
        }
    }



}
