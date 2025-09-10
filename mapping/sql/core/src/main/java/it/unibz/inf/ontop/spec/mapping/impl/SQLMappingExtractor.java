package it.unibz.inf.ontop.spec.mapping.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.CachingMetadataLookup;
import it.unibz.inf.ontop.dbschema.impl.JDBCMetadataProviderFactory;
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopMappingSQLSettings;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.transform.NoNullValueEnforcer;
import it.unibz.inf.ontop.iq.type.NotYetTypedBinaryMathOperationTransformer;
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
import it.unibz.inf.ontop.iq.type.NotYetTypedEqualityTransformer;
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
    private final NotYetTypedEqualityTransformer mappingEqualityTransformer;
    private final NotYetTypedBinaryMathOperationTransformer mappingBinaryMathOperationTransformer;
    private final NoNullValueEnforcer noNullValueEnforcer;
    private final IntermediateQueryFactory iqFactory;
    private final JDBCMetadataProviderFactory metadataProviderFactory;
    private final LensMetadataProvider.Factory lensMetadataProviderFactory;

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
    private final SerializedMetadataProvider.Factory serializedMetadataProviderFactory;

    @Inject
    private SQLMappingExtractor(SQLMappingParser mappingParser,
                                MappingOntologyComplianceValidator ontologyComplianceValidator,
                                SQLPPMappingConverter ppMappingConverter,
                                MappingDatatypeFiller mappingDatatypeFiller,
                                OntopMappingSQLSettings settings,
                                MappingCanonicalTransformer canonicalTransformer,
                                MappingCaster mappingCaster,
                                NotYetTypedEqualityTransformer mappingEqualityTransformer,
                                NotYetTypedBinaryMathOperationTransformer mappingBinaryMathOperatioNTransformer,
                                NoNullValueEnforcer noNullValueEnforcer,
                                IntermediateQueryFactory iqFactory,
                                MetaMappingExpander metamappingExpander,
                                ImplicitDBConstraintsProviderFactory implicitDBConstraintExtractor,
                                JDBCMetadataProviderFactory metadataProviderFactory,
                                SerializedMetadataProvider.Factory serializedMetadataProviderFactory,
                                LensMetadataProvider.Factory lensMetadataProviderFactory) {

        this.ontologyComplianceValidator = ontologyComplianceValidator;
        this.mappingParser = mappingParser;
        this.ppMappingConverter = ppMappingConverter;
        this.mappingDatatypeFiller = mappingDatatypeFiller;
        this.settings = settings;
        this.canonicalTransformer = canonicalTransformer;
        this.mappingCaster = mappingCaster;
        this.mappingEqualityTransformer = mappingEqualityTransformer;
        this.mappingBinaryMathOperationTransformer = mappingBinaryMathOperatioNTransformer;
        this.noNullValueEnforcer = noNullValueEnforcer;
        this.iqFactory = iqFactory;
        this.lensMetadataProviderFactory = lensMetadataProviderFactory;
        this.metamappingExpander = metamappingExpander;
        this.metadataProviderFactory = metadataProviderFactory;
        this.implicitDBConstraintExtractor = implicitDBConstraintExtractor;
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
            IQTree binaryMathOperationsTransformedTree = mappingBinaryMathOperationTransformer.transform(equalityTransformedTree);
            IQTree normalizedTree = binaryMathOperationsTransformedTree.normalizeForOptimization(assertion.getQuery().getVariableGenerator());
            IQTree noNullTree = noNullValueEnforcer.transform(normalizedTree);
            if (noNullTree.isDeclaredAsEmpty())
                continue;

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
            return convert(ppMapping.getTripleMaps(), specInput.getConstraintFile(), specInput.getDBMetadataReader(),
                    specInput.getLensesReader());
        } catch (FileNotFoundException e) {
            throw new MetadataExtractionException(e);
        }
    }

    private MappingAndDBParameters convert(ImmutableList<SQLPPTriplesMap> mapping,
                                           Optional<File> constraintFile,
                                           Optional<Reader> optionalDbMetadataReader, 
                                           Optional<Reader> lensesReader) throws MetadataExtractionException, InvalidMappingSourceQueriesException {

        try {
            if (optionalDbMetadataReader.isPresent()) {
                try (Reader dbMetadataReader = optionalDbMetadataReader.get()) {

                    if (settings.allowRetrievingBlackBoxViewMetadataFromDB()) {
                        try (Connection connection = LocalJDBCConnectionUtils.createLazyConnection(settings)) {

                            return convert(mapping, constraintFile, lensesReader,
                                    serializedMetadataProviderFactory.getMetadataProvider(
                                            dbMetadataReader, () -> metadataProviderFactory.getMetadataProvider(connection)));
                        }
                    }
                    else
                        return convert(mapping, constraintFile, lensesReader,
                                serializedMetadataProviderFactory.getMetadataProvider(dbMetadataReader));
                }
            }
            else {
                try (Connection connection = LocalJDBCConnectionUtils.createConnection(settings)) {
                    return convert(mapping, constraintFile, lensesReader,
                            metadataProviderFactory.getMetadataProvider(connection));
                }
            }
        }
        catch (IOException | SQLException e) {
            throw new MetadataExtractionException(e);
        }
    }

    private MappingAndDBParameters convert(ImmutableList<SQLPPTriplesMap> mapping, Optional<File> constraintFile, 
                                           Optional<Reader> lensesReader, MetadataProvider dbMetadataProvider) throws MetadataExtractionException, InvalidMappingSourceQueriesException {
        
        MetadataProvider metadataProvider;
        if (lensesReader.isPresent()) {
            try(Reader lensReader = lensesReader.get()) {
                metadataProvider = lensMetadataProviderFactory.getMetadataProvider(dbMetadataProvider, lensReader);
            }
            catch (IOException e) {
                throw new MetadataExtractionException(e);
            }
        }
        else
            metadataProvider = dbMetadataProvider;
        
        MetadataProvider withImplicitConstraintsMetadataProvider =
                implicitDBConstraintExtractor.extract(constraintFile, metadataProvider);

        CachingMetadataLookup metadataLookup = new CachingMetadataLookup(withImplicitConstraintsMetadataProvider);
        ImmutableList<MappingAssertion> provMapping = ppMappingConverter.convert(mapping, metadataLookup);

        metadataLookup.extractImmutableMetadata(); // inserts integrity constraints

        return new MappingAndDBParametersImpl(provMapping, metadataProvider.getDBParameters());
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
