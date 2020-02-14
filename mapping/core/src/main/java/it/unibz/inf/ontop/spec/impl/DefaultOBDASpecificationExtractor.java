package it.unibz.inf.ontop.spec.impl;

import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
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

    @Inject
    private DefaultOBDASpecificationExtractor(MappingExtractor mappingExtractor, MappingTransformer mappingTransformer,
                                              OntopMappingSettings settings) {
        this.mappingExtractor = mappingExtractor;
        this.mappingTransformer = mappingTransformer;
    }

    @Override
    public OBDASpecification extract(@Nonnull OBDASpecInput specInput, @Nonnull Optional<DBMetadata> dbMetadata,
                                     @Nonnull Optional<Ontology> optionalOntology, ExecutorRegistry executorRegistry)
            throws OBDASpecificationException {

        MappingAndDBMetadata mappingAndDBMetadata = mappingExtractor.extract(
                specInput, dbMetadata, optionalOntology, executorRegistry);

        return mappingTransformer.transform(
                mappingAndDBMetadata.getMapping(), mappingAndDBMetadata.getDBParameters(), optionalOntology);
    }

    @Override
    public OBDASpecification extract(@Nonnull OBDASpecInput specInput, @Nonnull PreProcessedMapping ppMapping,
                                     @Nonnull Optional<DBMetadata> dbMetadata, @Nonnull Optional<Ontology> optionalOntology,
                                     ExecutorRegistry executorRegistry) throws OBDASpecificationException {

        MappingAndDBMetadata mappingAndDBMetadata = mappingExtractor.extract(
                ppMapping, specInput, dbMetadata, optionalOntology, executorRegistry);

        return mappingTransformer.transform(
                mappingAndDBMetadata.getMapping(), mappingAndDBMetadata.getDBParameters(), optionalOntology);
    }
}
