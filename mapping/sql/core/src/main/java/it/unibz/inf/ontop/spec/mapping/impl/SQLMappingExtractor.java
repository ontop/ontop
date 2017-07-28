package it.unibz.inf.ontop.spec.mapping.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.dbschema.RDBMetadata;
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.injection.OntopMappingSQLSettings;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.spec.OBDASpecInput;
import it.unibz.inf.ontop.spec.mapping.MappingExtractor;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMappingConverter;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.SQLPPMappingImpl;
import it.unibz.inf.ontop.spec.mapping.parser.SQLMappingParser;
import it.unibz.inf.ontop.spec.dbschema.RDBMetadataExtractor;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.ontology.TBoxReasoner;
import it.unibz.inf.ontop.spec.mapping.pp.PreProcessedMapping;
import it.unibz.inf.ontop.spec.mapping.validation.MappingOntologyComplianceValidator;
import it.unibz.inf.ontop.spec.impl.MappingAndDBMetadataImpl;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingDatatypeFiller;
import org.apache.commons.rdf.api.Graph;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;


@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class SQLMappingExtractor implements MappingExtractor {

    private static final String ONTOLOGY_SATURATED_TBOX_ERROR_MSG = "the Ontology and TBoxReasoner must be both present, or none";

    private final SQLMappingParser mappingParser;
    private final MappingOntologyComplianceValidator ontologyComplianceValidator;
    private final SQLPPMappingConverter ppMappingConverter;
    private final RDBMetadataExtractor dbMetadataExtractor;
    private final OntopMappingSQLSettings settings;
    private final MappingDatatypeFiller mappingDatatypeFiller;

    @Inject
    private SQLMappingExtractor(SQLMappingParser mappingParser, MappingOntologyComplianceValidator ontologyComplianceValidator,
                                SQLPPMappingConverter ppMappingConverter, MappingDatatypeFiller mappingDatatypeFiller,
                                NativeQueryLanguageComponentFactory nativeQLFactory, OntopMappingSQLSettings settings) {

        this.mappingParser = mappingParser;
        this.ontologyComplianceValidator = ontologyComplianceValidator;
        this.ppMappingConverter = ppMappingConverter;
        this.dbMetadataExtractor = nativeQLFactory.create();
        this.mappingDatatypeFiller = mappingDatatypeFiller;
        this.settings = settings;
    }

    @Override
    public MappingAndDBMetadata extract(@Nonnull OBDASpecInput specInput, @Nonnull Optional<DBMetadata> dbMetadata,
                                        @Nonnull Optional<Ontology> ontology,
                                        @Nonnull Optional<TBoxReasoner> saturatedTBox,
                                        @Nonnull ExecutorRegistry executorRegistry)
            throws MappingException, DBMetadataExtractionException {

        SQLPPMapping ppMapping = extractPPMapping(specInput);

        return extract(ppMapping, specInput, dbMetadata, ontology, saturatedTBox, executorRegistry);
    }

    private SQLPPMapping extractPPMapping(OBDASpecInput specInput)
            throws DuplicateMappingException, MappingIOException, InvalidMappingException {

        Optional<File> optionalMappingFile = specInput.getMappingFile();
        if (optionalMappingFile.isPresent())
            return mappingParser.parse(optionalMappingFile.get());

        Optional<Reader> optionalMappingReader = specInput.getMappingReader();
        if (optionalMappingReader.isPresent())
            return mappingParser.parse(optionalMappingReader.get());

        Optional<Graph> optionalMappingGraph = specInput.getMappingGraph();
        if (optionalMappingGraph.isPresent())
            return mappingParser.parse(optionalMappingGraph.get());

        throw new IllegalArgumentException("Bad internal configuration: no mapping input provided in the OBDASpecInput!\n" +
                " Should have been detected earlier (in case of an user mistake)");
    }

    @Override
    public MappingAndDBMetadata extract(@Nonnull PreProcessedMapping ppMapping, @Nonnull OBDASpecInput specInput,
                                        @Nonnull Optional<DBMetadata> dbMetadata,
                                        @Nonnull Optional<Ontology> ontology,
                                        @Nonnull Optional<TBoxReasoner> saturatedTBox,
                                        @Nonnull ExecutorRegistry executorRegistry)
            throws MappingException, DBMetadataExtractionException {

        if(ontology.isPresent() != saturatedTBox.isPresent()){
            throw new IllegalArgumentException(ONTOLOGY_SATURATED_TBOX_ERROR_MSG);
        }
        return convertPPMapping(castPPMapping(ppMapping), castDBMetadata(dbMetadata), specInput, ontology, saturatedTBox,
                executorRegistry);
    }

    /**
     * Converts the PPMapping into a Mapping.
     *
     * During the conversion, data types are inferred and mapping assertions are validated
     *
     */
    private MappingAndDBMetadata convertPPMapping(SQLPPMapping ppMapping, Optional<RDBMetadata> optionalDBMetadata,
                                                  OBDASpecInput specInput, Optional<Ontology> optionalOntology,
                                                  Optional<TBoxReasoner> optionalSaturatedTBox,
                                                  ExecutorRegistry executorRegistry)
            throws MetaMappingExpansionException, DBMetadataExtractionException, MappingOntologyMismatchException, InvalidMappingSourceQueriesException {


        RDBMetadata dbMetadata = extractDBMetadata(ppMapping, optionalDBMetadata, specInput);
        SQLPPMapping expandedPPMapping = expandPPMapping(ppMapping, settings, dbMetadata);

        // NB: may also add views in the DBMetadata (for non-understood SQL queries)
        MappingWithProvenance provMapping = ppMappingConverter.convert(expandedPPMapping, dbMetadata, executorRegistry);
        dbMetadata.freeze();

        MappingWithProvenance filledProvMapping = mappingDatatypeFiller.inferMissingDatatypes(provMapping, dbMetadata);

        validateMapping(optionalOntology, optionalSaturatedTBox, filledProvMapping);

        return new MappingAndDBMetadataImpl(filledProvMapping.toRegularMapping(), dbMetadata);
    }

    private SQLPPMapping expandPPMapping(SQLPPMapping ppMapping, OntopMappingSQLSettings settings, RDBMetadata dbMetadata)
            throws MetaMappingExpansionException {
        ImmutableList<SQLPPTriplesMap> expandedMappingAxioms = MetaMappingExpander.expand(
                ppMapping.getTripleMaps(),
                settings,
                dbMetadata);
        try {
            return new SQLPPMappingImpl(expandedMappingAxioms, ppMapping.getMetadata());
        } catch (DuplicateMappingException e) {
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

        try (Connection localConnection = createConnection()) {
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

    /**
     * Validation:
     *    - Mismatch between the ontology and the mapping
     */
    private void validateMapping(Optional<Ontology> optionalOntology, Optional<TBoxReasoner> optionalSaturatedTBox,
                                 MappingWithProvenance filledProvMapping) throws MappingOntologyMismatchException {
        if (optionalOntology.isPresent()) {
            Ontology ontology = optionalOntology.get();
            TBoxReasoner saturatedTBox = optionalSaturatedTBox
                    .orElseThrow(() -> new IllegalArgumentException(ONTOLOGY_SATURATED_TBOX_ERROR_MSG));

            ontologyComplianceValidator.validate(filledProvMapping, ontology.getVocabulary(), saturatedTBox);
        }
    }

    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(settings.getJdbcUrl(), settings.getJdbcUser(), settings.getJdbcPassword());
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
                    RDBMetadata.class.getSimpleName());
        }
        return Optional.empty();
    }
}
