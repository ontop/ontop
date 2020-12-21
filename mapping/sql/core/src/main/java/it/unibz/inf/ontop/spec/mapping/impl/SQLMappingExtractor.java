package it.unibz.inf.ontop.spec.mapping.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.CachingMetadataLookup;
import it.unibz.inf.ontop.dbschema.impl.JDBCMetadataProviderFactory;
import it.unibz.inf.ontop.dbschema.impl.SerializedMetadataProvider;
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopMappingSQLSettings;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.iq.transform.NoNullValueEnforcer;
import it.unibz.inf.ontop.spec.OBDASpecInput;
import it.unibz.inf.ontop.spec.dbschema.ImplicitDBConstraintsProviderFactory;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.pp.*;
import it.unibz.inf.ontop.spec.mapping.pp.impl.MetaMappingExpander;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingCaster;
import it.unibz.inf.ontop.spec.mapping.MappingExtractor;
import it.unibz.inf.ontop.spec.mapping.parser.SQLMappingParser;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingCanonicalTransformer;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingDatatypeFiller;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingEqualityTransformer;
import it.unibz.inf.ontop.spec.mapping.validation.MappingOntologyComplianceValidator;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.utils.LocalJDBCConnectionUtils;
import org.apache.commons.rdf.api.Graph;

import javax.annotation.Nonnull;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;



@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class SQLMappingExtractor implements MappingExtractor {

    private final SQLPPMappingConverter ppMappingConverter;
    private final OntopMappingSQLSettings settings;
    private final MappingDatatypeFiller mappingDatatypeFiller;
    private final MappingCanonicalTransformer canonicalTransformer;
    private final MappingCaster mappingCaster;
    private final MappingEqualityTransformer mappingEqualityTransformer;
    private final NoNullValueEnforcer noNullValueEnforcer;
    private final IntermediateQueryFactory iqFactory;
    private final JDBCMetadataProviderFactory metadataProviderFactory;

    private final MappingOntologyComplianceValidator ontologyComplianceValidator;
    private final SQLMappingParser mappingParser;

    private final MetaMappingExpander metamappingExpander;

    /**
     * This represents user-supplied constraints, i.e. primary
     * and foreign keys not present in the database metadata
     *
     * Can be useful for eliminating self-joins
     */
    private final ImplicitDBConstraintsProviderFactory implicitDBConstraintExtractor;
    private final TypeFactory typeFactory;
    private final SerializedMetadataProvider.Factory serializedMetadataProviderFactory;

    @Inject
    private SQLMappingExtractor(SQLMappingParser mappingParser,
                                MappingOntologyComplianceValidator ontologyComplianceValidator,
                                SQLPPMappingConverter ppMappingConverter,
                                MappingDatatypeFiller mappingDatatypeFiller,
                                OntopMappingSQLSettings settings,
                                MappingCanonicalTransformer canonicalTransformer,
                                MappingCaster mappingCaster,
                                MappingEqualityTransformer mappingEqualityTransformer,
                                NoNullValueEnforcer noNullValueEnforcer,
                                IntermediateQueryFactory iqFactory,
                                MetaMappingExpander metamappingExpander,
                                ImplicitDBConstraintsProviderFactory implicitDBConstraintExtractor,
                                JDBCMetadataProviderFactory metadataProviderFactory,
                                TypeFactory typeFactory,
                                SerializedMetadataProvider.Factory serializedMetadataProviderFactory) {

        this.ontologyComplianceValidator = ontologyComplianceValidator;
        this.mappingParser = mappingParser;
        this.ppMappingConverter = ppMappingConverter;
        this.mappingDatatypeFiller = mappingDatatypeFiller;
        this.settings = settings;
        this.canonicalTransformer = canonicalTransformer;
        this.mappingCaster = mappingCaster;
        this.mappingEqualityTransformer = mappingEqualityTransformer;
        this.noNullValueEnforcer = noNullValueEnforcer;
        this.iqFactory = iqFactory;
        this.metamappingExpander = metamappingExpander;
        this.metadataProviderFactory = metadataProviderFactory;
        this.implicitDBConstraintExtractor = implicitDBConstraintExtractor;
        this.typeFactory = typeFactory;
        this.serializedMetadataProviderFactory = serializedMetadataProviderFactory;
    }

    @Override
    public MappingAndDBParameters extract(@Nonnull OBDASpecInput specInput,
                                          @Nonnull Optional<Ontology> ontology)
            throws MappingException, MetadataExtractionException {

        return convertPPMapping(extractPPMapping(specInput), specInput, ontology);
    }

    @Override
    public MappingAndDBParameters extract(@Nonnull PreProcessedMapping<? extends PreProcessedTriplesMap> ppMapping,
                                          @Nonnull OBDASpecInput specInput,
                                          @Nonnull Optional<Ontology> ontology)
            throws MappingException, MetadataExtractionException {

        return convertPPMapping((SQLPPMapping) ppMapping, specInput, ontology);
    }


    protected SQLPPMapping extractPPMapping(OBDASpecInput specInput)
            throws MappingIOException, InvalidMappingException {

        try {
            Optional<Reader> optionalMappingReader = specInput.getMappingReader();
            if (optionalMappingReader.isPresent())
                // The parser is in charge of closing the reader
                return mappingParser.parse(optionalMappingReader.get());
        } catch (FileNotFoundException e) {
            throw new MappingIOException(e);
        }

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
    protected MappingAndDBParameters convertPPMapping(SQLPPMapping ppMapping,
                                                      OBDASpecInput specInput,
                                                      Optional<Ontology> optionalOntology)
            throws MetaMappingExpansionException, MetadataExtractionException, MappingOntologyMismatchException,
            InvalidMappingSourceQueriesException, UnknownDatatypeException {

        MappingAndDBParameters mm = convert(ppMapping, specInput);

        ImmutableList<MappingAssertion> expMapping = metamappingExpander.transform(mm.getMapping(), mm.getDBParameters());

        ImmutableList.Builder<MappingAssertion> builder = ImmutableList.builder();
        // no streams because of exception handling
        for (MappingAssertion assertion : expMapping) {
            IQTree tree = assertion.getQuery().getTree();
            IQTree equalityTransformedTree = mappingEqualityTransformer.transform(tree);
            IQTree normalizedTree = equalityTransformedTree.normalizeForOptimization(assertion.getQuery().getVariableGenerator());
            IQTree noNullTree = noNullValueEnforcer.transform(normalizedTree);
            MappingAssertion noNullAssertion = assertion.copyOf(noNullTree, iqFactory);

            MappingAssertion filledProvAssertion = mappingDatatypeFiller.transform(noNullAssertion);
            MappingAssertion castAssertion = mappingCaster.transform(filledProvAssertion);
            builder.add(castAssertion);
        }

        ImmutableList<MappingAssertion> castMapping = builder.build();
        ImmutableList<MappingAssertion> canonizedMapping = canonicalTransformer.transform(castMapping);

        // Validation: Mismatch between the ontology and the mapping
        if (optionalOntology.isPresent()) {
            ontologyComplianceValidator.validate(canonizedMapping, optionalOntology.get());
        }

        return new MappingAndDBParametersImpl(canonizedMapping, mm.getDBParameters());
    }

    private MappingAndDBParameters convert(SQLPPMapping ppMapping, OBDASpecInput specInput)
            throws MetaMappingExpansionException, MetadataExtractionException, InvalidMappingSourceQueriesException {
        try {
            return convert(ppMapping.getTripleMaps(), specInput.getConstraintFile(), specInput.getDBMetadataReader());
        } catch (FileNotFoundException e) {
            throw new MetadataExtractionException(e);
        }
    }

    private MappingAndDBParameters convert(ImmutableList<SQLPPTriplesMap> mapping,
                                           Optional<File> constraintFile,
                                           Optional<Reader> optionalDbMetadataReader) throws MetadataExtractionException, InvalidMappingSourceQueriesException, MetaMappingExpansionException {

        if (optionalDbMetadataReader.isPresent()) {

            try (Reader dbMetadataReader = optionalDbMetadataReader.get();
                 Connection connection = LocalJDBCConnectionUtils.createConnection(settings)) {

                MetadataProvider dbMetadataProvider = metadataProviderFactory.getMetadataProvider(connection);
                DBMetadataProvider serializedDBMetadataProvider = serializedMetadataProviderFactory
                        .getMetadataProvider(dbMetadataReader, dbMetadataProvider.getQuotedIDFactory());
                MetadataProvider withImplicitConstraintsMetadataProvider =
                        implicitDBConstraintExtractor.extract(constraintFile, serializedDBMetadataProvider);


                //serializedDBMetadataProvider.loadAndDeserialize();
                ImmutableList<MappingAssertion> provMapping = ppMappingConverter.convert(mapping, serializedDBMetadataProvider);

                return new MappingAndDBParametersImpl(provMapping, withImplicitConstraintsMetadataProvider.getDBParameters());
            } catch (IOException | SQLException e) {
                throw new MetadataExtractionException(e);
            }

        } else {
            try (Connection connection = LocalJDBCConnectionUtils.createConnection(settings)) {

                MetadataProvider dbMetadataProvider = metadataProviderFactory.getMetadataProvider(connection);
                MetadataProvider withImplicitConstraintsMetadataProvider =
                        implicitDBConstraintExtractor.extract(constraintFile, dbMetadataProvider);

                CachingMetadataLookup metadataLookup = new CachingMetadataLookup(withImplicitConstraintsMetadataProvider);
                ImmutableList<MappingAssertion> provMapping = ppMappingConverter.convert(mapping, metadataLookup);

                metadataLookup.extractImmutableMetadata();

                return new MappingAndDBParametersImpl(provMapping, withImplicitConstraintsMetadataProvider.getDBParameters());
            } catch (SQLException e) {
                throw new MetadataExtractionException(e.getMessage());
            }
        }
    }

    private static class MappingAndDBParametersImpl implements MappingAndDBParameters {
        private final ImmutableList<MappingAssertion> mapping;
        private final DBParameters dbParameters;

        public MappingAndDBParametersImpl(ImmutableList<MappingAssertion> mapping, DBParameters dbParameters) {
            this.mapping = mapping;
            this.dbParameters = dbParameters;
        }

        @Override
        public ImmutableList<MappingAssertion> getMapping() {
            return mapping;
        }

        @Override
        public DBParameters getDBParameters() {
            return dbParameters;
        }
    }
}
