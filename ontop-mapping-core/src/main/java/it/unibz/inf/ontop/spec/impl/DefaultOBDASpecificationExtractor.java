package it.unibz.inf.ontop.spec.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.mapping.extraction.PreProcessedMapping;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.pivotalrepr.tools.ExecutorRegistry;
import it.unibz.inf.ontop.spec.MappingExtractor;
import it.unibz.inf.ontop.spec.MappingExtractor.MappingAndDBMetadata;
import it.unibz.inf.ontop.spec.MappingTransformer;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.OBDASpecificationExtractor;
import org.eclipse.rdf4j.model.Model;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Reader;
import java.util.Optional;


@Singleton
public class DefaultOBDASpecificationExtractor implements OBDASpecificationExtractor {

    private final MappingExtractor mappingExtractor;
    private final MappingTransformer mappingTransformer;

    @Inject
    private DefaultOBDASpecificationExtractor(MappingExtractor mappingExtractor, MappingTransformer mappingTransformer) {
        this.mappingExtractor = mappingExtractor;
        this.mappingTransformer = mappingTransformer;
    }


    @Override
    public OBDASpecification extract(@Nonnull File mappingFile, @Nonnull Optional<DBMetadata> dbMetadata,
                                     @Nonnull Optional<Ontology> ontology, ExecutorRegistry executorRegistry)
            throws OBDASpecificationException {

        MappingAndDBMetadata mappingAndDBMetadata = mappingExtractor.extract(mappingFile, dbMetadata, ontology,
                executorRegistry);
        return mappingTransformer.transform(mappingAndDBMetadata.getMapping(), mappingAndDBMetadata.getDBMetadata(),
                ontology);
    }

    @Override
    public OBDASpecification extract(@Nonnull Reader mappingReader, @Nonnull Optional<DBMetadata> dbMetadata,
                                     @Nonnull Optional<Ontology> ontology, ExecutorRegistry executorRegistry)
            throws OBDASpecificationException {
        MappingAndDBMetadata mappingAndDBMetadata = mappingExtractor.extract(mappingReader, dbMetadata, ontology,
                executorRegistry);
        return mappingTransformer.transform(mappingAndDBMetadata.getMapping(), mappingAndDBMetadata.getDBMetadata(),
                ontology);
    }

    @Override
    public OBDASpecification extract(@Nonnull Model mappingGraph, @Nonnull Optional<DBMetadata> dbMetadata,
                                     @Nonnull Optional<Ontology> ontology, ExecutorRegistry executorRegistry)
            throws OBDASpecificationException {
        MappingAndDBMetadata mappingAndDBMetadata = mappingExtractor.extract(mappingGraph, dbMetadata, ontology,
                executorRegistry);
        return mappingTransformer.transform(mappingAndDBMetadata.getMapping(), mappingAndDBMetadata.getDBMetadata(),
                ontology);
    }

    @Override
    public OBDASpecification extract(@Nonnull PreProcessedMapping ppMapping, @Nonnull Optional<DBMetadata> dbMetadata,
                                     @Nonnull Optional<Ontology> ontology, ExecutorRegistry executorRegistry)
            throws OBDASpecificationException {
        MappingAndDBMetadata mappingAndDBMetadata = mappingExtractor.extract(ppMapping, dbMetadata, ontology,
                executorRegistry);
        return mappingTransformer.transform(mappingAndDBMetadata.getMapping(), mappingAndDBMetadata.getDBMetadata(),
                ontology);
    }
}
