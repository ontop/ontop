package it.unibz.inf.ontop.spec.mapping.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.injection.OntopMappingSQLSettings;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.OBDASpecInput;
import it.unibz.inf.ontop.spec.dbschema.ImplicitDBConstraintsProviderFactory;
import it.unibz.inf.ontop.spec.impl.MappingAndDBMetadataImpl;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQueryFactory;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingCaster;
import it.unibz.inf.ontop.spec.mapping.MappingExtractor;
import it.unibz.inf.ontop.spec.mapping.parser.SQLMappingParser;
import it.unibz.inf.ontop.spec.mapping.pp.PreProcessedMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMappingConverter;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingCanonicalTransformer;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingDatatypeFiller;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingEqualityTransformer;
import it.unibz.inf.ontop.spec.mapping.validation.MappingOntologyComplianceValidator;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.LocalJDBCConnectionUtils;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static it.unibz.inf.ontop.spec.dbschema.impl.SQLTableNameExtractor.getRealTables;


@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class SQLMappingExtractor implements MappingExtractor {

    private final SQLPPMappingConverter ppMappingConverter;
    private final OntopMappingSQLSettings settings;
    private final MappingDatatypeFiller mappingDatatypeFiller;
    private final MappingCanonicalTransformer canonicalTransformer;
    private final MappingCaster mappingCaster;
    private final MappingEqualityTransformer mappingEqualityTransformer;
    private final MetaMappingExpander expander;

    private final MappingOntologyComplianceValidator ontologyComplianceValidator;
    private final SQLMappingParser mappingParser;

    /**
     * If we have to parse the full metadata or just the table list in the mappings.
     */
    private final boolean obtainFullMetadata;

    /**
     * This represents user-supplied constraints, i.e. primary
     * and foreign keys not present in the database metadata
     *
     * Can be useful for eliminating self-joins
     */
    private final ImplicitDBConstraintsProviderFactory implicitDBConstraintExtractor;
    private final TypeFactory typeFactory;

    private static final Logger log = LoggerFactory.getLogger(SQLMappingExtractor.class);

    @Inject
    private SQLMappingExtractor(SQLMappingParser mappingParser, MappingOntologyComplianceValidator ontologyComplianceValidator,
                                SQLPPMappingConverter ppMappingConverter, MappingDatatypeFiller mappingDatatypeFiller,
                                OntopMappingSQLSettings settings,
                                MappingCanonicalTransformer canonicalTransformer, TermFactory termFactory,
                                SubstitutionFactory substitutionFactory, RDF rdfFactory,
                                MappingCaster mappingCaster, MappingEqualityTransformer mappingEqualityTransformer,
                                SQLPPSourceQueryFactory sourceQueryFactory,
                                ImplicitDBConstraintsProviderFactory implicitDBConstraintExtractor,
                                TypeFactory typeFactory) {

        this.ontologyComplianceValidator = ontologyComplianceValidator;
        this.mappingParser = mappingParser;
        this.ppMappingConverter = ppMappingConverter;
        this.mappingDatatypeFiller = mappingDatatypeFiller;
        this.settings = settings;
        this.canonicalTransformer = canonicalTransformer;
        this.mappingCaster = mappingCaster;
        this.mappingEqualityTransformer = mappingEqualityTransformer;
        this.expander = new MetaMappingExpander(termFactory, substitutionFactory, rdfFactory, sourceQueryFactory);
        this.obtainFullMetadata = settings.isFullMetadataExtractionEnabled();
        this.implicitDBConstraintExtractor = implicitDBConstraintExtractor;
        this.typeFactory = typeFactory;

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
            throws MappingIOException, InvalidMappingException {

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

        BasicDBMetadata dbMetadata;
        if (optionalDBMetadata.isPresent()) {
            if (!(optionalDBMetadata.get() instanceof BasicDBMetadata))
                throw new IllegalArgumentException("Was expecting a DBMetadata");

            // Metadata extraction can be disabled when DBMetadata is already provided
            if (!settings.isProvidedDBMetadataCompletionEnabled())
                dbMetadata = (BasicDBMetadata)optionalDBMetadata.get();
            else
                dbMetadata = extract(ppMapping, Optional.of(optionalDBMetadata.get().getDBParameters()), specInput.getConstraintFile());
        }
        else
            dbMetadata = extract(ppMapping, Optional.empty(), specInput.getConstraintFile());

        SQLPPMapping expandedPPMapping = expander.getExpandedMappings(ppMapping, settings, dbMetadata);
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


    private BasicDBMetadata extract(SQLPPMapping ppMapping,
                                    Optional<DBParameters> optionalDBParameters,
                                    Optional<File> constraintFile) throws MetadataExtractionException {

        try {
            Connection connection = LocalJDBCConnectionUtils.createConnection(settings);
            DBParameters dbParameters = optionalDBParameters.isPresent()
                    ? optionalDBParameters.get()
                    : RDBMetadataExtractionTools.createDBParameters(connection, typeFactory.getDBTypeFactory());

            MetadataProvider implicitConstraints = implicitDBConstraintExtractor.extract(
                    constraintFile, dbParameters.getQuotedIDFactory());

            RDBMetadataProvider metadataLoader = RDBMetadataExtractionTools.getMetadataProvider(connection, dbParameters);
            BasicDBMetadata metadata = RDBMetadataExtractionTools.createMetadata(dbParameters);

            // if we have to parse the full metadata or just the table list in the mappings
            ImmutableList<RelationID> seedRelationIds;
            if (obtainFullMetadata) {
                seedRelationIds = metadataLoader.getRelationIDs();
            }
            else {
                // This is the NEW way of obtaining part of the metadata
                // (the schema.table names) by parsing the mappings
                // Parse mappings. Just to get the table names in use

                Set<RelationID> realTables = getRealTables(dbParameters.getQuotedIDFactory(), ppMapping.getTripleMaps());
                realTables.addAll(implicitConstraints.getRelationIDs());
                seedRelationIds = realTables.stream()
                        .map(metadataLoader::getRelationCanonicalID)
                        .collect(ImmutableCollectors.toList());
            }
            List<DatabaseRelationDefinition> extractedRelations2 = new ArrayList<>();
            for (RelationID seedId : seedRelationIds) {
                for (RelationDefinition.AttributeListBuilder r : metadataLoader.getRelationAttributes(seedId)) {
                    DatabaseRelationDefinition relation = metadata.createDatabaseRelation(r);
                    extractedRelations2.add(relation);
                }
            }

            for (DatabaseRelationDefinition relation : extractedRelations2)
                metadataLoader.insertIntegrityConstraints(relation, metadata);

            implicitConstraints.insertIntegrityConstraints(metadata);
            metadata.freeze();

            log.debug("DB Metadata: \n{}", metadata);
            return metadata;
        }
        catch (SQLException e) {
            throw new MetadataExtractionException(e.getMessage());
        }
    }
}
