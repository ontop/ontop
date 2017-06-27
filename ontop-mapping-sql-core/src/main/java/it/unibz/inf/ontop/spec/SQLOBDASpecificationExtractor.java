package it.unibz.inf.ontop.spec;

import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.OntopMappingSQLSettings;
import it.unibz.inf.ontop.mapping.pp.PreProcessedMapping;
import it.unibz.inf.ontop.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.model.SQLMappingParser;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;
import it.unibz.inf.ontop.pivotalrepr.tools.ExecutorRegistry;
import org.eclipse.rdf4j.model.Model;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Reader;
import java.util.Optional;

public class SQLOBDASpecificationExtractor implements OBDASpecificationExtractor {

    private final SQLMappingExtractor mappingExtractor;
    private final SQLMappingTransformer mappingTransformer;
    private final SQLMappingParser mappingParser;
    private final OntopMappingSQLSettings settings;

    @Inject
    private SQLOBDASpecificationExtractor(SQLMappingParser mappingParser, SQLMappingExtractor mappingExtractor,
                                          SQLMappingTransformer mappingTransformer, OntopMappingSQLSettings settings) {
        this.mappingParser = mappingParser;
        this.mappingExtractor = mappingExtractor;
        this.mappingTransformer = mappingTransformer;
        this.settings = settings;
    }

    @Override
    public OBDASpecification extract(@Nonnull File mappingFile, @Nonnull Optional<DBMetadata> dbMetadata,
                                     @Nonnull Optional<Ontology> ontology, @Nonnull Optional<File> constraintFile,
                                     ExecutorRegistry executorRegistry)
            throws OBDASpecificationException {
        return convertToDataSourceModel(mappingParser.parse(mappingFile), dbMetadata, ontology, constraintFile, executorRegistry);
    }


    @Override
    public OBDASpecification extract(@Nonnull Reader mappingReader, @Nonnull Optional<DBMetadata> dbMetadata,
                                     @Nonnull Optional<Ontology> ontology, @Nonnull Optional<File> constraintFile,
                                     ExecutorRegistry executorRegistry)
            throws OBDASpecificationException {
        return convertToDataSourceModel(mappingParser.parse(mappingReader), dbMetadata, ontology, constraintFile, executorRegistry);
    }

    @Override
    public OBDASpecification extract(@Nonnull Model mappingGraph, @Nonnull Optional<DBMetadata> serializedDBMetadata,
                                     @Nonnull Optional<Ontology> ontology, @Nonnull Optional<File> constraintFile,
                                     ExecutorRegistry executorRegistry)
            throws OBDASpecificationException {
        return convertToDataSourceModel(mappingParser.parse(mappingGraph), serializedDBMetadata, ontology, constraintFile, executorRegistry);
    }

    @Override
    public OBDASpecification extract(@Nonnull PreProcessedMapping ppMapping, @Nonnull Optional<DBMetadata> dbMetadata,
                                     @Nonnull Optional<Ontology> ontology, @Nonnull Optional<File> constraintFile,
                                     ExecutorRegistry executorRegistry)
            throws OBDASpecificationException {
        if (ppMapping instanceof SQLPPMapping) {
            return convertToDataSourceModel((SQLPPMapping) ppMapping, dbMetadata, ontology, constraintFile, executorRegistry);
        } else {
            throw new IllegalArgumentException("SQLDataSourceModelExtractor only accepts a SQLPPMapping as PreProcessedMapping");
        }
    }

    private OBDASpecification convertToDataSourceModel(SQLPPMapping ppMapping, Optional<DBMetadata> optionalDBMetadata,
                                                       Optional<Ontology> ontology, Optional<File> constraintFile,
                                                       ExecutorRegistry executorRegistry) {

        Optional<TBoxReasoner> tBox = getTBox(ontology);
        MappingExtractor.MappingAndDBMetadata mappingAndDBMetadata = mappingExtractor.extract(
                ppMapping,
                optionalDBMetadata,
                tBox,
                constraintFile,
                executorRegistry
        );
        return mappingTransformer.transform(
                mappingAndDBMetadata.getMapping(),
                mappingAndDBMetadata.getDBMetadata(),
                tBox
        );
    }

    private Optional<TBoxReasoner> getTBox(Optional<Ontology> ontology) {
        return ontology.isPresent()?
                Optional.of(
                        TBoxReasonerImpl.create(
                                ontology.get(),
                                settings.isEquivalenceOptimizationEnabled()
                        )):
                Optional.empty();
    }
}
