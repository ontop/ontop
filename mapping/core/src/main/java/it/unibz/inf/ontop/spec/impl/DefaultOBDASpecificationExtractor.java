package it.unibz.inf.ontop.spec.impl;

import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.exception.DBMetadataExtractionException;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.OntologyException;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.mapping.datalog.Mapping2DatalogConverter;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.ontology.utils.MappingVocabularyExtractor;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;
import it.unibz.inf.ontop.pp.PreProcessedMapping;
import it.unibz.inf.ontop.spec.MappingExtractor;
import it.unibz.inf.ontop.spec.trans.MappingTransformer;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.OBDASpecificationExtractor;
import org.eclipse.rdf4j.model.Model;

import java.io.File;
import java.io.Reader;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.inject.Inject;

public class DefaultOBDASpecificationExtractor implements OBDASpecificationExtractor {

    private final MappingExtractor mappingExtractor;
    private final MappingTransformer mappingTransformer;
    private final OntopMappingSettings settings;
    private final Mapping2DatalogConverter mapping2DatalogConverter;

    @Inject
    private DefaultOBDASpecificationExtractor(MappingExtractor mappingExtractor, MappingTransformer mappingTransformer,
                                         OntopMappingSettings settings, Mapping2DatalogConverter mapping2DatalogConverter) {
        this.mappingExtractor = mappingExtractor;
        this.mappingTransformer = mappingTransformer;
        this.settings = settings;
        this.mapping2DatalogConverter = mapping2DatalogConverter;
    }


    @Override
    public OBDASpecification extract(@Nonnull PreProcessedMapping ppMapping,
                                     @Nonnull Optional<DBMetadata> metadata,
                                     @Nonnull Optional<Ontology> ontology,
                                     @Nonnull Optional<File> constraintFile,
                                     ExecutorRegistry executorRegistry)
            throws OBDASpecificationException {
        return convert(ppMapping, metadata, ontology, constraintFile, executorRegistry);
    }

    @Override
    public OBDASpecification extract(@Nonnull File mappingFile, @Nonnull Optional<DBMetadata> dbMetadata, @Nonnull Optional<Ontology> ontology, @Nonnull Optional<File> constraintFile, ExecutorRegistry executorRegistry) throws OBDASpecificationException {
        return extract(mappingExtractor.loadPPMapping(mappingFile), dbMetadata, ontology, constraintFile, executorRegistry );
    }

    @Override
    public OBDASpecification extract(@Nonnull Reader mappingReader, @Nonnull Optional<DBMetadata> dbMetadata, @Nonnull Optional<Ontology> ontology, @Nonnull Optional<File> constraintFile, ExecutorRegistry executorRegistry) throws OBDASpecificationException {
        return extract(mappingExtractor.loadPPMapping(mappingReader), dbMetadata, ontology, constraintFile, executorRegistry );
    }

    @Override
    public OBDASpecification extract(@Nonnull Model mappingGraph, @Nonnull Optional<DBMetadata> dbMetadata, @Nonnull Optional<Ontology> ontology, @Nonnull Optional<File> constraintFile, ExecutorRegistry executorRegistry) throws OBDASpecificationException {
        return extract(mappingExtractor.loadPPMapping(mappingGraph), dbMetadata, ontology, constraintFile,
                executorRegistry );
    }

    private OBDASpecification convert(PreProcessedMapping ppMapping, Optional<DBMetadata> optionalDBMetadata, Optional<Ontology> optionalOntology, Optional<File> constraintFile, ExecutorRegistry executorRegistry) throws MappingException, DBMetadataExtractionException, OntologyException {
        Optional<TBoxReasoner> optionalInputTBox = loadInputTBox(optionalOntology);

        MappingExtractor.MappingAndDBMetadata mappingAndDBMetadata = mappingExtractor.extract(
                ppMapping,
                optionalDBMetadata,
                optionalOntology,
                optionalInputTBox,
                constraintFile,
                executorRegistry
        );

        //Bootstrap the ontology from the mapping if it does not already exist
        Ontology ontology = optionalOntology
                .orElseGet(() -> bootstrapOntology(mappingAndDBMetadata.getMapping()));
        TBoxReasoner tBox = optionalInputTBox
                .orElseGet(() -> TBoxReasonerImpl.create(ontology, settings.isEquivalenceOptimizationEnabled()));

        return mappingTransformer.transform(
                mappingAndDBMetadata.getMapping(),
                mappingAndDBMetadata.getDBMetadata(),
                ontology,
                tBox
        );
    }

    private Optional<TBoxReasoner> loadInputTBox(Optional<Ontology> ontology) {
        return ontology.isPresent() ?
                Optional.of(
                        TBoxReasonerImpl.create(
                                ontology.get(),
                                settings.isEquivalenceOptimizationEnabled()
                        )) :
                Optional.empty();
    }

    private Ontology bootstrapOntology(Mapping mapping) {

        return MappingVocabularyExtractor.extractOntology(
                mapping2DatalogConverter.convert(mapping)
                        .map(CQIE::getHead)
        );
    }
}
