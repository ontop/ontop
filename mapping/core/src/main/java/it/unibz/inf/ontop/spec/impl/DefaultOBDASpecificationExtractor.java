package it.unibz.inf.ontop.spec.impl;

import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.spec.ontology.MappingVocabularyExtractor;
import it.unibz.inf.ontop.spec.ontology.impl.ClassifiedTBoxImpl;
import it.unibz.inf.ontop.spec.mapping.pp.PreProcessedMapping;
import it.unibz.inf.ontop.spec.mapping.MappingExtractor;
import it.unibz.inf.ontop.spec.mapping.MappingExtractor.MappingAndDBMetadata;
import it.unibz.inf.ontop.spec.OBDASpecInput;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingTransformer;
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

        if (optionalOntology.isPresent()) {
            ClassifiedTBox saturatedTBox = ClassifiedTBoxImpl.classify(optionalOntology.get().tbox());

            MappingAndDBMetadata mappingAndDBMetadata = mappingExtractor.extract(specInput, dbMetadata,
                    Optional.of(saturatedTBox), executorRegistry);

            return mappingTransformer.transform(
                    specInput,
                    mappingAndDBMetadata.getMapping(),
                    mappingAndDBMetadata.getDBMetadata(),
                    optionalOntology.get().abox(),
                    saturatedTBox);
        }
        else {
            // no ontology given - extract the vocabulary from mappings and use it as an ontology
            MappingAndDBMetadata mappingAndDBMetadata = mappingExtractor.extract(specInput, dbMetadata,
                    Optional.empty(), executorRegistry);

            Ontology ontology = vocabularyExtractor.extractVocabulary(mappingAndDBMetadata.getMapping());

            return mappingTransformer.transform(
                    specInput,
                    mappingAndDBMetadata.getMapping(),
                    mappingAndDBMetadata.getDBMetadata(),
                    ontology.abox(), // EMPTY ABOX
                    ClassifiedTBoxImpl.classify(ontology.tbox()));
        }
    }

    @Override
    public OBDASpecification extract(@Nonnull OBDASpecInput specInput, @Nonnull PreProcessedMapping ppMapping,
                                     @Nonnull Optional<DBMetadata> dbMetadata, @Nonnull Optional<Ontology> optionalOntology,
                                     ExecutorRegistry executorRegistry) throws OBDASpecificationException {

        if (optionalOntology.isPresent()) {
            ClassifiedTBox optionalSaturatedTBox = ClassifiedTBoxImpl.classify(optionalOntology.get().tbox());

            MappingAndDBMetadata mappingAndDBMetadata = mappingExtractor.extract(ppMapping, specInput, dbMetadata,
                    Optional.of(optionalSaturatedTBox), executorRegistry);

            return mappingTransformer.transform(
                    specInput,
                    mappingAndDBMetadata.getMapping(),
                    mappingAndDBMetadata.getDBMetadata(),
                    optionalOntology.get().abox(),
                    optionalSaturatedTBox);
        }
        else {
            // no ontology given - extract the vocabulary from mappings and use it as an ontology
            MappingAndDBMetadata mappingAndDBMetadata = mappingExtractor.extract(ppMapping, specInput, dbMetadata,
                    Optional.empty(), executorRegistry);

            Ontology ontology = vocabularyExtractor.extractVocabulary(mappingAndDBMetadata.getMapping());

            return mappingTransformer.transform(
                    specInput,
                    mappingAndDBMetadata.getMapping(),
                    mappingAndDBMetadata.getDBMetadata(),
                    ontology.abox(), // EMPTY ABOX
                    ClassifiedTBoxImpl.classify(ontology.tbox()));
        }
    }
}
