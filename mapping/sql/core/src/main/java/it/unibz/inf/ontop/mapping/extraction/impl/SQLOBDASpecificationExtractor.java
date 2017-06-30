package it.unibz.inf.ontop.mapping.extraction.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.mapping.SQLMappingParser;
import it.unibz.inf.ontop.mapping.conversion.SQLPPMapping2OBDASpecificationConverter;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.spec.OBDASpecInput;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.OBDASpecificationExtractor;
import it.unibz.inf.ontop.mapping.extraction.PreProcessedMapping;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.model.SQLPPMapping;
import it.unibz.inf.ontop.ontology.Ontology;
import org.eclipse.rdf4j.model.Model;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Reader;
import java.util.Optional;

public class SQLOBDASpecificationExtractor implements OBDASpecificationExtractor {

    private final SQLMappingParser mappingParser;
    private final SQLPPMapping2OBDASpecificationConverter converter;

    @Inject
    private SQLOBDASpecificationExtractor(SQLMappingParser mappingParser,
                                          SQLPPMapping2OBDASpecificationConverter converter) {
        this.mappingParser = mappingParser;
        this.converter = converter;
    }

    private OBDASpecification convertToDataSourceModel(SQLPPMapping ppMapping, Optional<DBMetadata> dbMetadata,
                                                       Optional<Ontology> ontology, @Nonnull OBDASpecInput specInput,
                                                       ExecutorRegistry executorRegistry)
            throws OBDASpecificationException {
        return converter.convert(specInput, ppMapping, dbMetadata, ontology, executorRegistry);
    }

    @Override
    public OBDASpecification extract(@Nonnull OBDASpecInput specInput,
                                     @Nonnull Optional<DBMetadata> dbMetadata,
                                     @Nonnull Optional<Ontology> ontology, ExecutorRegistry executorRegistry)
            throws OBDASpecificationException {
        return converter.convert(specInput, extractPPMapping(specInput), dbMetadata, ontology, executorRegistry);
    }

    private SQLPPMapping extractPPMapping(OBDASpecInput specInput)
            throws DuplicateMappingException, MappingIOException, InvalidMappingException {

        Optional<File> optionalMappingFile = specInput.getMappingFile();
        if (optionalMappingFile.isPresent())
            return mappingParser.parse(optionalMappingFile.get());

        Optional<Reader> optionalMappingReader = specInput.getMappingReader();
        if (optionalMappingReader.isPresent())
            return mappingParser.parse(optionalMappingReader.get());

        Optional<Model> optionalMappingGraph = specInput.getMappingGraph();
        if (optionalMappingGraph.isPresent())
            return mappingParser.parse(optionalMappingGraph.get());

        throw new IllegalArgumentException("Bad internal configuration: no mapping input provided in the OBDASpecInput!\n" +
                " Should have been detected earlier (in case of an user mistake)");

    }

    @Override
    public OBDASpecification extract(@Nonnull OBDASpecInput specInput, @Nonnull PreProcessedMapping ppMapping,
                                     @Nonnull Optional<DBMetadata> dbMetadata, @Nonnull Optional<Ontology> ontology,
                                     ExecutorRegistry executorRegistry) throws OBDASpecificationException {
        if (ppMapping instanceof SQLPPMapping) {
            return convertToDataSourceModel((SQLPPMapping) ppMapping, dbMetadata, ontology, specInput, executorRegistry);
        }
        else {
            throw new IllegalArgumentException("SQLDataSourceModelExtractor only accepts SQLPPMapping as PreProcessedMapping");
        }
    }
}
