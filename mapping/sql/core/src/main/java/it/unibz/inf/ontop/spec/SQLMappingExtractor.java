package it.unibz.inf.ontop.spec;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.dbschema.RDBMetadata;
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.injection.OntopMappingSQLSettings;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.mapping.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.model.SQLMappingParser;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.ValueConstant;
import it.unibz.inf.ontop.nativeql.RDBMetadataExtractor;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.EQNormalizer;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.pp.PreProcessedMapping;
import it.unibz.inf.ontop.pp.validation.PPMappingOntologyComplianceValidator;
import it.unibz.inf.ontop.spec.impl.LegacyIsNotNullDatalogMappingFiller;
import it.unibz.inf.ontop.spec.impl.MappingAndDBMetadataImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.MetaMappingExpander;
import it.unibz.inf.ontop.utils.SQLPPMapping2DatalogConverter;
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

import static it.unibz.inf.ontop.model.OntopModelSingletons.DATA_FACTORY;


public class SQLMappingExtractor implements MappingExtractor {


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
                                NativeQueryLanguageComponentFactory nativeQLFactory, OntopMappingSQLSettings settings,
                                Datalog2QueryMappingConverter datalog2QueryMappingConverter) {

        this.mappingParser = mappingParser;
        this.ontologyComplianceValidator = ontologyComplianceValidator;
        this.dbMetadataExtractor = nativeQLFactory.create();
        this.settings = settings;
        this.datalog2QueryMappingConverter = datalog2QueryMappingConverter;
    }

    @Override
    public MappingAndDBMetadata extract(@Nonnull PreProcessedMapping ppMapping, @Nonnull Optional<DBMetadata> dbMetadata,
                                        @Nonnull Optional<Ontology> ontology, @Nonnull Optional<TBoxReasoner> tBox,
                                        @Nonnull Optional<File> constraintsFile, ExecutorRegistry executorRegistry)
            throws MappingException, DBMetadataExtractionException {

        if(ontology.isPresent() != tBox.isPresent()){
            throw new IllegalArgumentException("the Ontology and TBoxReasoner must be both present, or none");
        }

        SQLPPMapping castPPMapping = castPPMapping(ppMapping);
        if(ontology.isPresent()){
            ontologyComplianceValidator.validateMapping(castPPMapping, ontology.get().getVocabulary(), tBox.get());
        }
        return convertPPMapping(castPPMapping, castDBMetadata(dbMetadata), constraintsFile, executorRegistry);
    }

    private SQLPPMapping castPPMapping(PreProcessedMapping ppMapping) {
        if(ppMapping instanceof SQLPPMapping){
            return (SQLPPMapping) ppMapping;
        }
        throw new IllegalArgumentException(SQLMappingExtractor.class.getSimpleName()+" only supports instances of " +
                SQLPPMapping.class.getSimpleName());
    }

    private Optional<RDBMetadata> castDBMetadata(@Nonnull Optional<DBMetadata> optionalDBMetadata) {
        if(optionalDBMetadata.isPresent()){
            DBMetadata md = optionalDBMetadata.get();
            if(optionalDBMetadata.get() instanceof RDBMetadata){
                return Optional.of((RDBMetadata) md);
            }
            throw new IllegalArgumentException(SQLMappingExtractor.class.getSimpleName()+" only supports instances of " +
                    SQLPPMapping.class.getSimpleName());
        }
        return Optional.empty();
    }

    @Override
    public PreProcessedMapping loadPPMapping(File mappingFile) throws DuplicateMappingException, MappingIOException, InvalidMappingException {
        return mappingParser.parse(mappingFile);
    }

    @Override
    public PreProcessedMapping loadPPMapping(Reader mappingReader) throws DuplicateMappingException, MappingIOException, InvalidMappingException {
        return mappingParser.parse(mappingReader);
    }

    @Override
    public PreProcessedMapping loadPPMapping(Model mappingGraph) throws DuplicateMappingException, InvalidMappingException {
        return mappingParser.parse(mappingGraph);
    }

    private MappingAndDBMetadata convertPPMapping(SQLPPMapping ppMapping, Optional<RDBMetadata> optionalDBMetadata,
                                                  Optional<File> constraintFile, ExecutorRegistry executorRegistry) throws MetaMappingExpansionException, DBMetadataExtractionException {


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
        ImmutableList<CQIE> rulesWithNotNull = initialMappingRules.stream()
                .map(r -> LegacyIsNotNullDatalogMappingFiller.addNotNull(r, dbMetadata))
                .collect(ImmutableCollectors.toList());

        Mapping mapping = datalog2QueryMappingConverter.convertMappingRules(
                rulesWithNotNull,
                dbMetadata,
                executorRegistry,
                ppMapping.getMetadata()
        );
        return new MappingAndDBMetadataImpl(mapping, dbMetadata);
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
