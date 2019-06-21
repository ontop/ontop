package it.unibz.inf.ontop.utils;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import it.unibz.inf.ontop.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.datalog.IQ2DatalogTranslator;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.injection.OntopMappingConfiguration;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.datalog.UnionFlattener;
import it.unibz.inf.ontop.iq.tools.IQConverter;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.atom.TargetAtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.iq.IntermediateQueryBuilder;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.mapping.MappingMetadata;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.transformer.*;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.SubstitutionUtilities;
import it.unibz.inf.ontop.substitution.impl.UnifierUtilities;
import org.apache.commons.rdf.api.RDF;

import java.sql.Types;
import java.util.stream.Stream;

public class MappingTestingTools {

    public static final ExecutorRegistry EXECUTOR_REGISTRY;
    public static final IntermediateQueryFactory IQ_FACTORY;
    public static final DBMetadata EMPTY_METADATA;

    public static final TermFactory TERM_FACTORY;
    public static final AtomFactory ATOM_FACTORY;
    public static final TypeFactory TYPE_FACTORY;
    public static final TargetAtomFactory TARGET_ATOM_FACTORY;
    public static final SubstitutionFactory SUBSTITUTION_FACTORY;
    public static final SpecificationFactory MAPPING_FACTORY;
    public static final DatalogFactory DATALOG_FACTORY;
    public static final RDF RDF_FACTORY;
    public static final MappingVariableNameNormalizer MAPPING_NORMALIZER;
    public static final CoreUtilsFactory CORE_UTILS_FACTORY;
    private static final BasicDBMetadata DEFAULT_DUMMY_DB_METADATA;

    public static final SubstitutionUtilities SUBSTITUTION_UTILITIES;
    public static final UnifierUtilities UNIFIER_UTILITIES;
    public static final ImmutabilityTools IMMUTABILITY_TOOLS;

    public static final Datalog2QueryMappingConverter DATALOG_2_QUERY_MAPPING_CONVERTER;
    public static final ABoxFactIntoMappingConverter A_BOX_FACT_INTO_MAPPING_CONVERTER;
    public static final OntopMappingSettings ONTOP_MAPPING_SETTINGS;
    public static final MappingMerger MAPPING_MERGER;
    public static final MappingSameAsInverseRewriter SAME_AS_INVERSE_REWRITER;
    public static final IQ2DatalogTranslator INTERMEDIATE_QUERY_2_DATALOG_TRANSLATOR;
    public static final MappingSaturator MAPPING_SATURATOR;

    public static final UriTemplateMatcher EMPTY_URI_TEMPLATE_MATCHER;
    public static final PrefixManager EMPTY_PREFIX_MANAGER;
    public static final MappingMetadata EMPTY_MAPPING_METADATA;
    public static final UnionFlattener UNION_FLATTENER;
    public static final SpecificationFactory SPECIFICATION_FACTORY;
    public static final IQConverter IQ_CONVERTER;


    public static final RelationPredicate TABLE1_AR2;
    public static final RelationPredicate TABLE2_AR2;
    public static final RelationPredicate TABLE1_AR3;
    public static final RelationPredicate TABLE2_AR3;
    public static final RelationPredicate TABLE3_AR3;

    static {
        OntopMappingConfiguration defaultConfiguration = OntopMappingConfiguration.defaultBuilder()
                .enableTestMode()
                .build();

        Injector injector = defaultConfiguration.getInjector();
        EXECUTOR_REGISTRY = defaultConfiguration.getExecutorRegistry();
        IQ_FACTORY = injector.getInstance(IntermediateQueryFactory.class);
        MAPPING_FACTORY = injector.getInstance(SpecificationFactory.class);
        MAPPING_NORMALIZER = injector.getInstance(MappingVariableNameNormalizer.class);
        ATOM_FACTORY = injector.getInstance(AtomFactory.class);
        TERM_FACTORY = injector.getInstance(TermFactory.class);
        TYPE_FACTORY = injector.getInstance(TypeFactory.class);
        TARGET_ATOM_FACTORY = injector.getInstance(TargetAtomFactory.class);
        DATALOG_FACTORY = injector.getInstance(DatalogFactory.class);
        SUBSTITUTION_FACTORY = injector.getInstance(SubstitutionFactory.class);
        DEFAULT_DUMMY_DB_METADATA = injector.getInstance(DummyBasicDBMetadata.class);
        DATALOG_2_QUERY_MAPPING_CONVERTER = injector.getInstance(Datalog2QueryMappingConverter.class);
        A_BOX_FACT_INTO_MAPPING_CONVERTER = injector.getInstance(ABoxFactIntoMappingConverter.class);
        ONTOP_MAPPING_SETTINGS = injector.getInstance(OntopMappingSettings.class);
        MAPPING_MERGER = injector.getInstance(MappingMerger.class);
        SAME_AS_INVERSE_REWRITER = injector.getInstance(MappingSameAsInverseRewriter.class);
        INTERMEDIATE_QUERY_2_DATALOG_TRANSLATOR = injector.getInstance(IQ2DatalogTranslator.class);
        MAPPING_SATURATOR = injector.getInstance(MappingSaturator.class);
        UNION_FLATTENER = injector.getInstance(UnionFlattener.class);
        SPECIFICATION_FACTORY = injector.getInstance(SpecificationFactory.class);
        IQ_CONVERTER = injector.getInstance(IQConverter.class);
        RDF_FACTORY = injector.getInstance(RDF.class);
        CORE_UTILS_FACTORY = injector.getInstance(CoreUtilsFactory.class);

        EMPTY_URI_TEMPLATE_MATCHER = UriTemplateMatcher.create(Stream.of(), TERM_FACTORY);
        EMPTY_PREFIX_MANAGER = MAPPING_FACTORY.createPrefixManager(ImmutableMap.of());
        EMPTY_MAPPING_METADATA = MAPPING_FACTORY.createMetadata(EMPTY_PREFIX_MANAGER, EMPTY_URI_TEMPLATE_MATCHER);


        SUBSTITUTION_UTILITIES = injector.getInstance(SubstitutionUtilities.class);
        UNIFIER_UTILITIES = injector.getInstance(UnifierUtilities.class);
        IMMUTABILITY_TOOLS = injector.getInstance(ImmutabilityTools.class);

        EMPTY_METADATA = DEFAULT_DUMMY_DB_METADATA.clone();
        EMPTY_METADATA.freeze();

        BasicDBMetadata dbMetadataWithPredicates = createDummyMetadata();
        QuotedIDFactory idFactory = dbMetadataWithPredicates.getQuotedIDFactory();

        TABLE1_AR2 = createRelationPredicate(dbMetadataWithPredicates, idFactory, 1, 2);
        TABLE2_AR2 = createRelationPredicate(dbMetadataWithPredicates, idFactory, 2, 2);
        TABLE1_AR3 = createRelationPredicate(dbMetadataWithPredicates, idFactory, 4, 3);
        TABLE2_AR3 = createRelationPredicate(dbMetadataWithPredicates, idFactory, 5, 3);
        TABLE3_AR3 = createRelationPredicate(dbMetadataWithPredicates, idFactory, 6, 3);

        dbMetadataWithPredicates.freeze();
    }

    public static IntermediateQueryBuilder createQueryBuilder(DBMetadata dbMetadata) {
        return IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);
    }

    public static BasicDBMetadata createDummyMetadata() {
        return DEFAULT_DUMMY_DB_METADATA.clone();
    }


    private static RelationPredicate createRelationPredicate(BasicDBMetadata dbMetadata, QuotedIDFactory idFactory,
                                                             int tableNumber, int arity) {
        DatabaseRelationDefinition tableDef = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null,
                "TABLE" + tableNumber + "AR" + arity));
        for (int i=1 ; i <= arity; i++) {
            tableDef.addAttribute(idFactory.createAttributeID("col" + i), Types.VARCHAR, null, false);
        }
        return tableDef.getAtomPredicate();
    }
}
