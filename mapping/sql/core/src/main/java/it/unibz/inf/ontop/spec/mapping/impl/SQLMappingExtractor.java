package it.unibz.inf.ontop.spec.mapping.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.BasicDBMetadata;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.injection.OntopMappingSQLSettings;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.OBDASpecInput;
import it.unibz.inf.ontop.spec.dbschema.RDBMetadataExtractor;
import it.unibz.inf.ontop.spec.impl.MappingAndDBMetadataImpl;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingCaster;
import it.unibz.inf.ontop.spec.mapping.MappingExtractor;
import it.unibz.inf.ontop.spec.mapping.parser.SQLMappingParser;
import it.unibz.inf.ontop.spec.mapping.pp.PreProcessedMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMappingConverter;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.SQLPPMappingImpl;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingCanonicalTransformer;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingDatatypeFiller;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingEqualityTransformer;
import it.unibz.inf.ontop.spec.mapping.validation.MappingOntologyComplianceValidator;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.LocalJDBCConnectionUtils;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;


@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class SQLMappingExtractor implements MappingExtractor {

    private final SQLPPMappingConverter ppMappingConverter;
    private final RDBMetadataExtractor dbMetadataExtractor;
    private final OntopMappingSQLSettings settings;
    private final MappingDatatypeFiller mappingDatatypeFiller;
    private final MappingCanonicalTransformer canonicalTransformer;
    private static final Logger log = LoggerFactory.getLogger(SQLMappingExtractor.class);
    private final TermFactory termFactory;
    private final SubstitutionFactory substitutionFactory;
    private final TypeFactory typeFactory;
    private final RDF rdfFactory;
    private final MappingCaster mappingCaster;
    private final MappingEqualityTransformer mappingEqualityTransformer;

    private final MappingOntologyComplianceValidator ontologyComplianceValidator;
    private final SQLMappingParser mappingParser;

    @Inject
    private SQLMappingExtractor(SQLMappingParser mappingParser, MappingOntologyComplianceValidator ontologyComplianceValidator,
                                SQLPPMappingConverter ppMappingConverter, MappingDatatypeFiller mappingDatatypeFiller,
                                RDBMetadataExtractor dbMetadataExtractor, OntopMappingSQLSettings settings,
                                MappingCanonicalTransformer canonicalTransformer, TermFactory termFactory,
                                SubstitutionFactory substitutionFactory, TypeFactory typeFactory, RDF rdfFactory,
                                MappingCaster mappingCaster, MappingEqualityTransformer mappingEqualityTransformer) {

        this.ontologyComplianceValidator = ontologyComplianceValidator;
        this.mappingParser = mappingParser;
        this.ppMappingConverter = ppMappingConverter;
        this.dbMetadataExtractor = dbMetadataExtractor;
        this.mappingDatatypeFiller = mappingDatatypeFiller;
        this.settings = settings;
        this.canonicalTransformer = canonicalTransformer;
        this.termFactory = termFactory;
        this.substitutionFactory = substitutionFactory;
        this.typeFactory = typeFactory;
        this.rdfFactory = rdfFactory;
        this.mappingCaster = mappingCaster;
        this.mappingEqualityTransformer = mappingEqualityTransformer;
    }

    @Override
    public MappingAndDBMetadata extract(@Nonnull OBDASpecInput specInput,
                                        @Nonnull Optional<DBMetadata> dbMetadata,
                                        @Nonnull Optional<Ontology> ontology,
                                        @Nonnull ExecutorRegistry executorRegistry)
            throws MappingException, MetadataExtractionException {

        return convertPPMapping(extractPPMapping(specInput), dbMetadata, specInput, ontology, executorRegistry);
    }

    @Override
    public MappingAndDBMetadata extract(@Nonnull PreProcessedMapping ppMapping,
                                        @Nonnull OBDASpecInput specInput,
                                        @Nonnull Optional<DBMetadata> dbMetadata,
                                        @Nonnull Optional<Ontology> ontology,
                                        @Nonnull ExecutorRegistry executorRegistry)
            throws MappingException, MetadataExtractionException {

        return convertPPMapping((SQLPPMapping) ppMapping, dbMetadata, specInput, ontology, executorRegistry);
    }


    protected SQLPPMapping extractPPMapping(OBDASpecInput specInput)
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


    /**
     * Converts the PPMapping into a Mapping.
     * <p>
     * During the conversion, data types are inferred and mapping assertions are validated
     */
    protected MappingAndDBMetadata convertPPMapping(SQLPPMapping ppMapping, Optional<DBMetadata> optionalDBMetadata,
                                                  OBDASpecInput specInput,
                                                  Optional<Ontology> optionalOntology,
                                                  ExecutorRegistry executorRegistry)
            throws MetaMappingExpansionException, MetadataExtractionException, MappingOntologyMismatchException,
            InvalidMappingSourceQueriesException, UnknownDatatypeException {


        BasicDBMetadata dbMetadata = extractDBMetadata(ppMapping, optionalDBMetadata, specInput);
        dbMetadata.freeze();

        log.debug("DB Metadata: \n{}", dbMetadata);

        SQLPPMapping expandedPPMapping = expandPPMapping(ppMapping, settings, dbMetadata);

        // NB: may also add views in the DBMetadata (for non-understood SQL queries)
        ImmutableList<MappingAssertion> provMapping = ppMappingConverter.convert(expandedPPMapping, dbMetadata, executorRegistry);

        ImmutableList<MappingAssertion> eqMapping = mappingEqualityTransformer.transform(provMapping);
        ImmutableList<MappingAssertion> filledProvMapping = mappingDatatypeFiller.transform(eqMapping);
        ImmutableList<MappingAssertion> castMapping = mappingCaster.transform(filledProvMapping);
        ImmutableList<MappingAssertion> canonizedMapping = canonicalTransformer.transform(castMapping);

        // Validation: Mismatch between the ontology and the mapping
        if (optionalOntology.isPresent()) {
            ontologyComplianceValidator.validate(canonizedMapping, optionalOntology.get());
        }

        return new MappingAndDBMetadataImpl(canonizedMapping, dbMetadata.getDBParameters());
        // dbMetadata GOES NO FURTHER - no need to freeze it
    }

    protected SQLPPMapping expandPPMapping(SQLPPMapping ppMapping, OntopMappingSQLSettings settings, DBMetadata dbMetadata)
            throws MetaMappingExpansionException {

        MetaMappingExpander expander = new MetaMappingExpander(ppMapping.getTripleMaps(), termFactory,
                substitutionFactory, typeFactory, rdfFactory);
        final ImmutableList<SQLPPTriplesMap> expandedMappingAxioms;
        if (expander.hasMappingsToBeExpanded()) {
            try (Connection connection = LocalJDBCConnectionUtils.createConnection(settings)) {
                expandedMappingAxioms = expander.getExpandedMappings(connection, dbMetadata);
            }
            catch (SQLException e) {
                throw new MetaMappingExpansionException(e.getMessage());
            }
        }
        else
            expandedMappingAxioms = expander.getNonExpandableMappings();

        try {
            return new SQLPPMappingImpl(expandedMappingAxioms, ppMapping.getPrefixManager());
        }
        catch (DuplicateMappingException e) {
            // Internal bug
            throw new IllegalStateException(e);
        }
    }

    /**
     * Makes use of the DB connection
     */
    private BasicDBMetadata extractDBMetadata(SQLPPMapping ppMapping, Optional<DBMetadata> optionalDBMetadata,
                                          OBDASpecInput specInput)
            throws MetadataExtractionException {

        /*
         * Metadata extraction can be disabled when DBMetadata is already provided
         */
        if (optionalDBMetadata.isPresent() && (!settings.isProvidedDBMetadataCompletionEnabled()))
            return (BasicDBMetadata)optionalDBMetadata.get();

        try (Connection localConnection = LocalJDBCConnectionUtils.createConnection(settings)) {
            return dbMetadataExtractor.extract(ppMapping, localConnection, optionalDBMetadata,
                    specInput.getConstraintFile());
        }
        catch (SQLException e) {
            throw new MetadataExtractionException(e.getMessage());
        }
    }
}
