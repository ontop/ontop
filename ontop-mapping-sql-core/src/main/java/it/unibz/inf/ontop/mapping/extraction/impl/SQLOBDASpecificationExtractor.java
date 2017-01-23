package it.unibz.inf.ontop.mapping.extraction.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.mapping.MappingParser;
import it.unibz.inf.ontop.mapping.conversion.SQLPPMapping2DSModelConverter;
import it.unibz.inf.ontop.pivotalrepr.utils.ExecutorRegistry;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.OBDASpecificationExtractor;
import it.unibz.inf.ontop.mapping.extraction.PreProcessedMapping;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.ontology.Ontology;
import org.eclipse.rdf4j.model.Model;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Optional;

public class SQLOBDASpecificationExtractor implements OBDASpecificationExtractor {

    private final MappingParser mappingParser;
    private final SQLPPMapping2DSModelConverter converter;

    @Inject
    private SQLOBDASpecificationExtractor(MappingParser mappingParser,
                                          SQLPPMapping2DSModelConverter converter) {
        this.mappingParser = mappingParser;
        this.converter = converter;
    }

    @Override
    public OBDASpecification extract(@Nonnull File mappingFile, @Nonnull Optional<DBMetadata> dbMetadata,
                                     @Nonnull Optional<Ontology> ontology, ExecutorRegistry executorRegistry)
            throws OBDASpecificationException, IOException {

        OBDAModel ppMapping =  mappingParser.parse(mappingFile);
        return convertToDataSourceModel(ppMapping, dbMetadata, ontology, executorRegistry);
    }

    @Override
    public OBDASpecification extract(@Nonnull Reader mappingReader, @Nonnull Optional<DBMetadata> dbMetadata,
                                     @Nonnull Optional<Ontology> ontology, ExecutorRegistry executorRegistry)
            throws OBDASpecificationException, IOException {
        OBDAModel ppModel =  mappingParser.parse(mappingReader);
        return convertToDataSourceModel(ppModel, dbMetadata, ontology, executorRegistry);
    }

    @Override
    public OBDASpecification extract(@Nonnull Model mappingGraph, @Nonnull Optional<DBMetadata> dbMetadata,
                                     @Nonnull Optional<Ontology> ontology, ExecutorRegistry executorRegistry)
            throws OBDASpecificationException, IOException {
        OBDAModel ppModel =  mappingParser.parse(mappingGraph);
        return convertToDataSourceModel(ppModel, dbMetadata, ontology, executorRegistry);
    }

    @Override
    public OBDASpecification extract(@Nonnull PreProcessedMapping ppMapping, @Nonnull Optional<DBMetadata> dbMetadata,
                                     @Nonnull Optional<Ontology> ontology, ExecutorRegistry executorRegistry)
            throws OBDASpecificationException, IOException {
        if (ppMapping instanceof OBDAModel) {
            return convertToDataSourceModel((OBDAModel) ppMapping, dbMetadata, ontology, executorRegistry);
        }
        else {
            throw new IllegalArgumentException("SQLDataSourceModelExtractor only accepts OBDAModel as PreProcessedMapping");
        }
    }

    private OBDASpecification convertToDataSourceModel(OBDAModel ppMapping, Optional<DBMetadata> dbMetadata,
                                                       Optional<Ontology> ontology, ExecutorRegistry executorRegistry)
            throws OBDASpecificationException {
        return converter.convert(ppMapping, dbMetadata, ontology, executorRegistry);
    }
}
