package it.unibz.inf.ontop.spec.mapping.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.dbschema.RDBMetadata;
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.injection.OntopMappingSQLSettings;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.spec.OBDASpecInput;
import it.unibz.inf.ontop.spec.dbschema.RDBMetadataExtractor;
import it.unibz.inf.ontop.spec.impl.MappingAndDBMetadataImpl;
import it.unibz.inf.ontop.spec.mapping.MappingExtractor;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.mapping.parser.SQLMappingParser;
import it.unibz.inf.ontop.spec.mapping.pp.PreProcessedMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMappingConverter;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.SQLPPMappingImpl;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingDatatypeFiller;
import it.unibz.inf.ontop.spec.mapping.validation.MappingOntologyComplianceValidator;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.utils.LocalJDBCConnectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;


@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class SQLMappingExtractor extends AbstractMappingExtractor<SQLPPMapping, RDBMetadata, SQLMappingParser, OntopMappingSQLSettings> implements MappingExtractor {

    private final SQLPPMappingConverter ppMappingConverter;
    private final RDBMetadataExtractor dbMetadataExtractor;
    private final OntopMappingSQLSettings settings;
    private final MappingDatatypeFiller mappingDatatypeFiller;
    private static final Logger log = LoggerFactory.getLogger(SQLMappingExtractor.class);
    private final AtomFactory atomFactory;
    private final TermFactory termFactory;

    @Inject
    private SQLMappingExtractor(SQLMappingParser mappingParser, MappingOntologyComplianceValidator ontologyComplianceValidator,
                                SQLPPMappingConverter ppMappingConverter, MappingDatatypeFiller mappingDatatypeFiller,
                                RDBMetadataExtractor dbMetadataExtractor, OntopMappingSQLSettings settings,
                                AtomFactory atomFactory, TermFactory termFactory) {

        super(ontologyComplianceValidator, mappingParser);
        this.ppMappingConverter = ppMappingConverter;
        this.dbMetadataExtractor = dbMetadataExtractor;
        this.mappingDatatypeFiller = mappingDatatypeFiller;
        this.settings = settings;
        this.atomFactory = atomFactory;
        this.termFactory = termFactory;
    }

    /**
     * Converts the PPMapping into a Mapping.
     * <p>
     * During the conversion, data types are inferred and mapping assertions are validated
     * TODO: move this method to AbstractMappingExtractor
     */
    protected MappingAndDBMetadata convertPPMapping(SQLPPMapping ppMapping, Optional<RDBMetadata> optionalDBMetadata,
                                                  OBDASpecInput specInput,
                                                  Optional<Ontology> optionalOntology,
                                                  ExecutorRegistry executorRegistry)
            throws MetaMappingExpansionException, DBMetadataExtractionException, MappingOntologyMismatchException,
            InvalidMappingSourceQueriesException, UnknownDatatypeException {


        RDBMetadata dbMetadata = extractDBMetadata(ppMapping, optionalDBMetadata, specInput);

        log.debug("DB Metadata: \n{}", dbMetadata);

        log.debug(dbMetadata.printKeys());

        SQLPPMapping expandedPPMapping = expandPPMapping(ppMapping, settings, dbMetadata);

        // NB: may also add views in the DBMetadata (for non-understood SQL queries)
        MappingWithProvenance provMapping = ppMappingConverter.convert(expandedPPMapping, dbMetadata, executorRegistry);
        dbMetadata.freeze();

        MappingWithProvenance filledProvMapping = mappingDatatypeFiller.inferMissingDatatypes(provMapping, dbMetadata);

        validateMapping(optionalOntology, filledProvMapping);

        return new MappingAndDBMetadataImpl(filledProvMapping.toRegularMapping(), dbMetadata);
    }

    protected SQLPPMapping expandPPMapping(SQLPPMapping ppMapping, OntopMappingSQLSettings settings, RDBMetadata dbMetadata)
            throws MetaMappingExpansionException {

        MetaMappingExpander expander = new MetaMappingExpander(ppMapping.getTripleMaps(), atomFactory, termFactory);
        final ImmutableList<SQLPPTriplesMap> expandedMappingAxioms;
        if (expander.hasMappingsToBeExpanded()) {
            try (Connection connection = LocalJDBCConnectionUtils.createConnection(settings)) {
                expandedMappingAxioms = expander.getExpandedMappings(connection, dbMetadata);
            }
            // Problem while creating the connection
            catch (SQLException e) {
                throw new MetaMappingExpansionException(e.getMessage());
            }
        }
        else
            expandedMappingAxioms = expander.getNonExpandableMappings();

        try {
            return new SQLPPMappingImpl(expandedMappingAxioms, ppMapping.getMetadata());
        }
        catch (DuplicateMappingException e) {
            // Internal bug
            throw new IllegalStateException(e);
        }
    }

    /**
     * Makes use of the DB connection
     */
    private RDBMetadata extractDBMetadata(final SQLPPMapping ppMapping, Optional<RDBMetadata> optionalDBMetadata,
                                          OBDASpecInput specInput)
            throws DBMetadataExtractionException, MetaMappingExpansionException {

        boolean isDBMetadataProvided = optionalDBMetadata.isPresent();

        /*
         * Metadata extraction can be disabled when DBMetadata is already provided
         */
        if (isDBMetadataProvided && (!settings.isProvidedDBMetadataCompletionEnabled()))
            return optionalDBMetadata.get();

        try (Connection localConnection = LocalJDBCConnectionUtils.createConnection(settings)) {
            return isDBMetadataProvided
                    ? dbMetadataExtractor.extract(ppMapping, localConnection, optionalDBMetadata.get(),
                    specInput.getConstraintFile())
                    : dbMetadataExtractor.extract(ppMapping, localConnection, specInput.getConstraintFile());
        }
        /*
         * Problem while creating the connection
         */
        catch (SQLException e) {
            throw new DBMetadataExtractionException(e.getMessage());
        }
    }

    private Connection createConnection() throws SQLException {

        try {
            // This should work in most cases (e.g. from CLI, Protege, or Jetty)
            return DriverManager.getConnection(settings.getJdbcUrl(), settings.getJdbcUser(), settings.getJdbcPassword());
        } catch (SQLException ex) {
            // HACKY(xiao): This part is still necessary for Tomcat.
            // Otherwise, JDBC drivers are not initialized by default.
            settings.getJdbcDriver().ifPresent(className -> {
                try {
                    Class.forName(className);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            });

            return DriverManager.getConnection(settings.getJdbcUrl(), settings.getJdbcUser(), settings.getJdbcPassword());
        }
    }

    protected SQLPPMapping castPPMapping(PreProcessedMapping ppMapping) {
        if (ppMapping instanceof SQLPPMapping) {
            return (SQLPPMapping) ppMapping;
        }
        throw new IllegalArgumentException(SQLMappingExtractor.class.getSimpleName() + " only supports instances of " +
                SQLPPMapping.class.getSimpleName());
    }

    protected Optional<RDBMetadata> castDBMetadata(@Nonnull Optional<DBMetadata> optionalDBMetadata) {
        if (optionalDBMetadata.isPresent()) {
            DBMetadata md = optionalDBMetadata.get();
            if (md instanceof RDBMetadata) {
                return Optional.of((RDBMetadata) md);
            }
            throw new IllegalArgumentException(SQLMappingExtractor.class.getSimpleName() + " only supports instances of " +
                    RDBMetadata.class.getSimpleName());
        }
        return Optional.empty();
    }
}
