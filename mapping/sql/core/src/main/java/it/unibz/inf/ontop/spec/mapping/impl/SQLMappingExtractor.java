package it.unibz.inf.ontop.spec.mapping.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.CachingMetadataLookup;
import it.unibz.inf.ontop.dbschema.impl.DatabaseMetadataProviderFactory;
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.injection.OntopMappingSQLSettings;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.OBDASpecInput;
import it.unibz.inf.ontop.spec.dbschema.ImplicitDBConstraintsProviderFactory;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQueryFactory;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.MetaMappingExpander;
import it.unibz.inf.ontop.spec.mapping.pp.impl.PreMetaMappingExpander;
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
import it.unibz.inf.ontop.utils.LocalJDBCConnectionUtils;
import net.sf.jsqlparser.expression.JdbcParameter;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.RDF;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Reader;
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
    private final PreMetaMappingExpander expander;

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

    @Inject
    private SQLMappingExtractor(SQLMappingParser mappingParser, MappingOntologyComplianceValidator ontologyComplianceValidator,
                                SQLPPMappingConverter ppMappingConverter, MappingDatatypeFiller mappingDatatypeFiller,
                                OntopMappingSQLSettings settings,
                                MappingCanonicalTransformer canonicalTransformer, TermFactory termFactory,
                                SubstitutionFactory substitutionFactory, RDF rdfFactory,
                                MappingCaster mappingCaster, MappingEqualityTransformer mappingEqualityTransformer,
                                SQLPPSourceQueryFactory sourceQueryFactory,
                                MetaMappingExpander metamappingExpander, ImplicitDBConstraintsProviderFactory implicitDBConstraintExtractor,
                                TypeFactory typeFactory) {

        this.ontologyComplianceValidator = ontologyComplianceValidator;
        this.mappingParser = mappingParser;
        this.ppMappingConverter = ppMappingConverter;
        this.mappingDatatypeFiller = mappingDatatypeFiller;
        this.settings = settings;
        this.canonicalTransformer = canonicalTransformer;
        this.mappingCaster = mappingCaster;
        this.mappingEqualityTransformer = mappingEqualityTransformer;
        this.metamappingExpander = metamappingExpander;
        this.expander = new PreMetaMappingExpander(termFactory, substitutionFactory, rdfFactory, sourceQueryFactory);
        this.implicitDBConstraintExtractor = implicitDBConstraintExtractor;
        this.typeFactory = typeFactory;
    }

    @Override
    public MappingAndDBParameters extract(@Nonnull OBDASpecInput specInput,
                                          @Nonnull Optional<Ontology> ontology)
            throws MappingException, MetadataExtractionException {

        return convertPPMapping(extractPPMapping(specInput), specInput, ontology);
    }

    @Override
    public MappingAndDBParameters extract(@Nonnull PreProcessedMapping ppMapping,
                                          @Nonnull OBDASpecInput specInput,
                                          @Nonnull Optional<Ontology> ontology)
            throws MappingException, MetadataExtractionException {

        return convertPPMapping((SQLPPMapping) ppMapping, specInput, ontology);
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
    protected MappingAndDBParameters convertPPMapping(SQLPPMapping ppMapping,
                                                      OBDASpecInput specInput,
                                                      Optional<Ontology> optionalOntology)
            throws MetaMappingExpansionException, MetadataExtractionException, MappingOntologyMismatchException,
            InvalidMappingSourceQueriesException, UnknownDatatypeException {

        MappingAndDBParameters mm = convert(ppMapping.getTripleMaps(), specInput.getConstraintFile());

        ImmutableList<MappingAssertion> eqMapping = mappingEqualityTransformer.transform(mm.getMapping());
        ImmutableList<MappingAssertion> expMapping = metamappingExpander.transform(eqMapping, settings, mm.getDBParameters());
        ImmutableList<MappingAssertion> filledProvMapping = mappingDatatypeFiller.transform(expMapping);
        ImmutableList<MappingAssertion> castMapping = mappingCaster.transform(filledProvMapping);
        ImmutableList<MappingAssertion> canonizedMapping = canonicalTransformer.transform(castMapping);

        // Validation: Mismatch between the ontology and the mapping
        if (optionalOntology.isPresent()) {
            ontologyComplianceValidator.validate(canonizedMapping, optionalOntology.get());
        }

        return new MappingAndDBParametersImpl(canonizedMapping, mm.getDBParameters());
    }


    private MappingAndDBParameters convert(ImmutableList<SQLPPTriplesMap> mapping,
                                           Optional<File> constraintFile) throws MetadataExtractionException, InvalidMappingSourceQueriesException, MetaMappingExpansionException {

        try (Connection connection = LocalJDBCConnectionUtils.createConnection(settings)) {

            MetadataProvider metadataLoader = DatabaseMetadataProviderFactory.getMetadataProvider(connection, typeFactory.getDBTypeFactory());
            MetadataProvider implicitConstraints = implicitDBConstraintExtractor.extract(
                    constraintFile, metadataLoader);

            CachingMetadataLookup metadataLookup = new CachingMetadataLookup(implicitConstraints);
            //ImmutableList<SQLPPTriplesMap> expandedPPMapping = expander.getExpandedMappings(mapping, connection, metadataLookup);
            ImmutableList<MappingAssertion> provMapping = ppMappingConverter.convert(mapping, metadataLookup);

            metadataLookup.extractImmutableMetadata();

            return new MappingAndDBParametersImpl(provMapping, implicitConstraints.getDBParameters());
        }
        catch (SQLException e) {
            throw new MetadataExtractionException(e.getMessage());
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
