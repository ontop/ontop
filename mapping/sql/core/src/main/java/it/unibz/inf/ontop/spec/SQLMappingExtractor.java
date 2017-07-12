package it.unibz.inf.ontop.spec;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.dbschema.RDBMetadata;
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.injection.OntopMappingSQLSettings;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.mapping.SQLPPMappingConverter;
import it.unibz.inf.ontop.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.mapping.pp.impl.SQLPPMappingImpl;
import it.unibz.inf.ontop.model.SQLMappingParser;
import it.unibz.inf.ontop.nativeql.RDBMetadataExtractor;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.pp.PreProcessedMapping;
import it.unibz.inf.ontop.pp.validation.PPMappingOntologyComplianceValidator;
import it.unibz.inf.ontop.spec.impl.MappingAndDBMetadataImpl;
import it.unibz.inf.ontop.spec.trans.MappingDatatypeFiller;
import it.unibz.inf.ontop.utils.MetaMappingExpander;
import org.eclipse.rdf4j.model.Model;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;


public class SQLMappingExtractor implements MappingExtractor {

    private final SQLMappingParser mappingParser;
    private final PPMappingOntologyComplianceValidator ontologyComplianceValidator;
    private final SQLPPMappingConverter ppMappingConverter;
    private final RDBMetadataExtractor dbMetadataExtractor;
    private final OntopMappingSQLSettings settings;
    private final MappingDatatypeFiller mappingDatatypeFiller;

    @Inject
    private SQLMappingExtractor(SQLMappingParser mappingParser, PPMappingOntologyComplianceValidator ontologyComplianceValidator,
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
                                        @Nonnull Optional<Ontology> ontology, Optional<TBoxReasoner> saturatedTBox,
                                        ExecutorRegistry executorRegistry)
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

        Optional<Model> optionalMappingGraph = specInput.getMappingGraph();
        if (optionalMappingGraph.isPresent())
            return mappingParser.parse(optionalMappingGraph.get());

        throw new IllegalArgumentException("Bad internal configuration: no mapping input provided in the OBDASpecInput!\n" +
                " Should have been detected earlier (in case of an user mistake)");
    }

    @Override
    public MappingAndDBMetadata extract(@Nonnull PreProcessedMapping ppMapping, @Nonnull OBDASpecInput specInput,
                                        @Nonnull Optional<DBMetadata> dbMetadata,
                                        @Nonnull Optional<Ontology> ontology, Optional<TBoxReasoner> saturatedTBox,
                                        ExecutorRegistry executorRegistry)
            throws MappingException, DBMetadataExtractionException {

        if(ontology.isPresent() != saturatedTBox.isPresent()){
            throw new IllegalArgumentException("the Ontology and TBoxReasoner must be both present, or none");
        }

        SQLPPMapping castPPMapping = castPPMapping(ppMapping);
        if(ontology.isPresent()){
            ontologyComplianceValidator.validateMapping(castPPMapping, ontology.get().getVocabulary(), saturatedTBox.get());
        }
        return convertPPMapping(castPPMapping, castDBMetadata(dbMetadata), specInput, ontology, saturatedTBox,
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
            throws MetaMappingExpansionException, DBMetadataExtractionException {


        RDBMetadata dbMetadata = extractDBMetadata(ppMapping, optionalDBMetadata, specInput);
        SQLPPMapping expandedPPMapping = expandPPMapping(ppMapping, settings, dbMetadata);

        // NB: may also add views in the DBMetadata (for non-understood SQL queries)
        MappingWithProvenance provMapping = ppMappingConverter.convert(expandedPPMapping, dbMetadata, executorRegistry);
        dbMetadata.freeze();

        MappingWithProvenance filledProvMapping = mappingDatatypeFiller.inferMissingDatatypes(provMapping, dbMetadata);

        // TODO: fill data types and validate

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

        try (Connection localConnection = createConnection()) {
            return optionalDBMetadata.isPresent()
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
                    SQLPPMapping.class.getSimpleName());
        }
        return Optional.empty();
    }
}
