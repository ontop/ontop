package it.unibz.inf.ontop.spec.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.mapping.extraction.PreProcessedMapping;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.spec.*;
import it.unibz.inf.ontop.spec.MappingExtractor.MappingAndDBMetadata;
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
    public OBDASpecification extract(@Nonnull OBDASpecInput specInput, @Nonnull Optional<DBMetadata> dbMetadata,
                                     @Nonnull Optional<Ontology> ontology, ExecutorRegistry executorRegistry)
            throws OBDASpecificationException {
        MappingAndDBMetadata mappingAndDBMetadata = mappingExtractor.extract(specInput, dbMetadata, ontology,
                executorRegistry);
        return mappingTransformer.transform(specInput, mappingAndDBMetadata.getMapping(),
                mappingAndDBMetadata.getDBMetadata(), ontology);
    }

    @Override
    public OBDASpecification extract(@Nonnull OBDASpecInput specInput, @Nonnull PreProcessedMapping ppMapping,
                                     @Nonnull Optional<DBMetadata> dbMetadata, @Nonnull Optional<Ontology> ontology,
                                     ExecutorRegistry executorRegistry) throws OBDASpecificationException {
        MappingAndDBMetadata mappingAndDBMetadata = mappingExtractor.extract(ppMapping, dbMetadata, ontology,
                specInput, executorRegistry);
        return mappingTransformer.transform(specInput, mappingAndDBMetadata.getMapping(),
                mappingAndDBMetadata.getDBMetadata(), ontology);
    }
}
