package it.unibz.inf.ontop.spec;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.DBMetadataExtractionException;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.exception.MetaMappingExpansionException;
import it.unibz.inf.ontop.injection.OntopMappingSQLSettings;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.mapping.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.mapping.pp.PreProcessedMapping;
import it.unibz.inf.ontop.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.mapping.pp.validation.PPMappingOntologyComplianceValidator;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.nativeql.RDBMetadataExtractor;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.EQNormalizer;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.pivotalrepr.tools.ExecutorRegistry;
import it.unibz.inf.ontop.sql.RDBMetadata;
import it.unibz.inf.ontop.utils.SQLPPMapping2DatalogConverter;
import it.unibz.inf.ontop.utils.MetaMappingExpander;
import org.eclipse.rdf4j.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;


public class SQLMappingExtractor implements MappingExtractor<RDBMetadata> {


    class SQLMappingAndDBMetadata implements MappingExtractor.MappingAndDBMetadata<RDBMetadata> {
        private final Mapping mapping;
        private final RDBMetadata metadata;

        SQLMappingAndDBMetadata(Mapping mapping, RDBMetadata metadata) {
            this.mapping = mapping;
            this.metadata = metadata;
        }

        @Override
        public Mapping getMapping() {
            return mapping;
        }

        @Override
        public RDBMetadata getDBMetadata() {
            return metadata;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLMappingExtractor.class);

    private final SQLMappingParser mappingParser;
    private final PPMappingOntologyComplianceValidator ontologyComplianceValidator;
    private final Datalog2QueryMappingConverter datalog2QueryMappingConverter;
    // TODO: Move to the OBDASpecificationExtractor (after isolating the DBMetadata expansion in convertMappingAxioms())
    private final RDBMetadataExtractor dbMetadataExtractor;
    // TODO: Move to the OBDASpecificationExtractor (after isolating the DBMetadata expansion in convertMappingAxioms())
    private final OntopMappingSQLSettings settings;

    @Inject
    private SQLMappingExtractor(SQLMappingParser mappingParser, PPMappingOntologyComplianceValidator ontologyComplianceValidator,
                                RDBMetadataExtractor dbMetadataExtractor,
                                OntopMappingSQLSettings settings,
                                Datalog2QueryMappingConverter datalog2QueryMappingConverter) {

        this.mappingParser = mappingParser;
        this.ontologyComplianceValidator = ontologyComplianceValidator;
        this.dbMetadataExtractor = dbMetadataExtractor;
        this.settings = settings;
        this.datalog2QueryMappingConverter = datalog2QueryMappingConverter;
    }

    @Override
    public SQLMappingAndDBMetadata extract(@Nonnull PreProcessedMapping ppMapping, @Nonnull Optional<RDBMetadata> dbMetadata, @Nonnull Optional<TBoxReasoner> tBox, @Nonnull Optional<File> constraintsFile, ExecutorRegistry executorRegistry)
            throws MappingException, DBMetadataExtractionException {
        if (ppMapping instanceof SQLPPMapping) {
            return convertPPMapping((SQLPPMapping) ppMapping, dbMetadata, tBox, constraintsFile, executorRegistry);
        }
        throw new IllegalArgumentException("SQLMappingExtractor only accepts a SQLPPMapping as PreProcessedMapping");
    }

    @Override
    public PreProcessedMapping loadPPMapping(File mappingFile) {
        return mappingParser.parse(mappingFile);
    }

    @Override
    public PreProcessedMapping loadPPMapping(Reader mappingReader) {
        return mappingParser.parse(mappingReader);
    }

    @Override
    public PreProcessedMapping loadPPMapping(Model mappingGraph) {
        return mappingParser.parse(mappingGraph);
    }

    private SQLMappingAndDBMetadata convertPPMapping(SQLPPMapping ppMapping, Optional<RDBMetadata> optionalDBMetadata,
                                                  Optional<TBoxReasoner> ontology, Optional<File> constraintFile,
                                                  ExecutorRegistry executorRegistry) {

        ontology.ifPresent(o -> ontologyComplianceValidator.validate(ppMapping, o));

        RDBMetadata dbMetadata = extractDBMetadata(ppMapping, optionalDBMetadata, constraintFile);
        ImmutableList<SQLPPTriplesMap> expandedMappingAxioms = MetaMappingExpander.expand(
                ppMapping.getTripleMaps(),
                settings,
                dbMetadata
        );

        // NB: may also add views in the DBMetadata (for non-understood SQL queries)
        // TODO: isolate the DBMetadata expansion, move the DBMetada creation and expansion to the OBDASpecificationExtractor, and return a simple Mapping instance (instead of MappingAndDBMetadata )
        ImmutableList<CQIE> initialMappingRules = convertMappingAxioms(expandedMappingAxioms, dbMetadata);
        dbMetadata.freeze();

        Mapping mapping = datalog2QueryMappingConverter.convertMappingRules(
                initialMappingRules,
                dbMetadata,
                executorRegistry,
                ppMapping.getMetadata()
        );
        return new SQLMappingAndDBMetadata(mapping, dbMetadata);
    }


    /**
     * Makes use of the DB connection
     */
    // TODO: Move to the OBDASpecificationExtractor (after isolating the DBMetadata expansion in convertMappingAxioms())
    // simple Mapping instance (instead of MappingAndDBMetadata )
    private RDBMetadata extractDBMetadata(final SQLPPMapping ppMapping, Optional<RDBMetadata> optionalDBMetadata,
                                          Optional<File> constraintFile)
            throws DBMetadataExtractionException, MetaMappingExpansionException {

        try (Connection localConnection = createConnection()) {
            return optionalDBMetadata.isPresent()
                    ? dbMetadataExtractor.extract(ppMapping, localConnection, optionalDBMetadata.get(), constraintFile)
                    : dbMetadataExtractor.extract(ppMapping, localConnection, constraintFile);
        }
        /*
         * Problem while creating the connection
         */
        catch (SQLException e) {
            throw new DBMetadataExtractionException(e.getMessage());
        }
    }

    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(settings.getJdbcUrl(), settings.getJdbcUser(), settings.getJdbcPassword());
    }

    /**
     * May also add views in the DBMetadata!
     */
    private ImmutableList<CQIE> convertMappingAxioms(ImmutableList<SQLPPTriplesMap> mappingAxioms, RDBMetadata dbMetadata) {


        ImmutableList<CQIE> unfoldingProgram = SQLPPMapping2DatalogConverter.constructDatalogProgram(mappingAxioms, dbMetadata);

        LOGGER.debug("Original mapping size: {}", unfoldingProgram.size());

        // TODO: move it to the converter
        // Normalizing language tags and equalities
        normalizeMapping(unfoldingProgram);

        return unfoldingProgram;
    }

    /**
     * Normalize language tags (make them lower-case) and equalities
     * (remove them by replacing all equivalent terms with one representative)
     */

    private void normalizeMapping(List<CQIE> unfoldingProgram) {

        // Normalizing language tags. Making all LOWER CASE

        for (CQIE mapping : unfoldingProgram) {
            Function head = mapping.getHead();
            for (Term term : head.getTerms()) {
                if (!(term instanceof Function))
                    continue;

                Function typedTerm = (Function) term;
                if (typedTerm.getTerms().size() == 2 && typedTerm.getFunctionSymbol().getName().equals(OBDAVocabulary.RDFS_LITERAL_URI)) {
                    // changing the language, its always the second inner term (literal,lang)
                    Term originalLangTag = typedTerm.getTerm(1);
                    if (originalLangTag instanceof ValueConstant) {
                        ValueConstant originalLangConstant = (ValueConstant) originalLangTag;
                        Term normalizedLangTag = DATA_FACTORY.getConstantLiteral(originalLangConstant.getValue().toLowerCase(),
                                originalLangConstant.getType());
                        typedTerm.setTerm(1, normalizedLangTag);
                    }
                }
            }
        }

        // Normalizing equalities
        for (CQIE cq: unfoldingProgram)
            EQNormalizer.enforceEqualities(cq);
    }
}
