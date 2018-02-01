package it.unibz.inf.ontop.utils;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import it.unibz.inf.ontop.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.datalog.IntermediateQuery2DatalogTranslator;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.injection.OntopMappingConfiguration;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.iq.IntermediateQueryBuilder;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.mapping.MappingMetadata;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.transformer.*;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.SubstitutionUtilities;
import it.unibz.inf.ontop.substitution.impl.UnifierUtilities;

import java.util.stream.Stream;

public class MappingTestingTools {

    public static final ExecutorRegistry EXECUTOR_REGISTRY;
    public static final IntermediateQueryFactory IQ_FACTORY;
    public static final DBMetadata EMPTY_METADATA;

    public static final TermFactory TERM_FACTORY;
    public static final AtomFactory ATOM_FACTORY;
    public static final TypeFactory TYPE_FACTORY;
    public static final SubstitutionFactory SUBSTITUTION_FACTORY;
    public static final Relation2Predicate RELATION_2_PREDICATE;
    public static final SpecificationFactory MAPPING_FACTORY;
    public static final DatalogFactory DATALOG_FACTORY;
    public static final MappingVariableNameNormalizer MAPPING_NORMALIZER;
    private static final BasicDBMetadata DEFAULT_DUMMY_DB_METADATA;

    public static final SubstitutionUtilities SUBSTITUTION_UTILITIES;
    public static final UnifierUtilities UNIFIER_UTILITIES;

    public static final Datalog2QueryMappingConverter DATALOG_2_QUERY_MAPPING_CONVERTER;
    public static final ABoxFactIntoMappingConverter A_BOX_FACT_INTO_MAPPING_CONVERTER;
    public static final OntopMappingSettings ONTOP_MAPPING_SETTINGS;
    public static final MappingMerger MAPPING_MERGER;
    public static final MappingSameAsInverseRewriter SAME_AS_INVERSE_REWRITER;
    public static final IntermediateQuery2DatalogTranslator INTERMEDIATE_QUERY_2_DATALOG_TRANSLATOR;
    public static final MappingSaturator MAPPING_SATURATOR;

    public static final UriTemplateMatcher EMPTY_URI_TEMPLATE_MATCHER;
    public static final PrefixManager EMPTY_PREFIX_MANAGER;
    public static final MappingMetadata EMPTY_MAPPING_METADATA;

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
        DATALOG_FACTORY = injector.getInstance(DatalogFactory.class);
        SUBSTITUTION_FACTORY = injector.getInstance(SubstitutionFactory.class);
        RELATION_2_PREDICATE = injector.getInstance(Relation2Predicate.class);
        DEFAULT_DUMMY_DB_METADATA = injector.getInstance(DummyBasicDBMetadata.class);
        DATALOG_2_QUERY_MAPPING_CONVERTER = injector.getInstance(Datalog2QueryMappingConverter.class);
        A_BOX_FACT_INTO_MAPPING_CONVERTER = injector.getInstance(ABoxFactIntoMappingConverter.class);
        ONTOP_MAPPING_SETTINGS = injector.getInstance(OntopMappingSettings.class);
        MAPPING_MERGER = injector.getInstance(MappingMerger.class);
        SAME_AS_INVERSE_REWRITER = injector.getInstance(MappingSameAsInverseRewriter.class);
        INTERMEDIATE_QUERY_2_DATALOG_TRANSLATOR = injector.getInstance(IntermediateQuery2DatalogTranslator.class);
        MAPPING_SATURATOR = injector.getInstance(MappingSaturator.class);

        EMPTY_URI_TEMPLATE_MATCHER = UriTemplateMatcher.create(Stream.of(), TERM_FACTORY);
        EMPTY_PREFIX_MANAGER = MAPPING_FACTORY.createPrefixManager(ImmutableMap.of());
        EMPTY_MAPPING_METADATA = MAPPING_FACTORY.createMetadata(EMPTY_PREFIX_MANAGER, EMPTY_URI_TEMPLATE_MATCHER);


        SUBSTITUTION_UTILITIES = injector.getInstance(SubstitutionUtilities.class);
        UNIFIER_UTILITIES = injector.getInstance(UnifierUtilities.class);

        EMPTY_METADATA = DEFAULT_DUMMY_DB_METADATA.clone();
        EMPTY_METADATA.freeze();
    }

    public static IntermediateQueryBuilder createQueryBuilder(DBMetadata dbMetadata) {
        return IQ_FACTORY.createIQBuilder(dbMetadata, EXECUTOR_REGISTRY);
    }

    public static BasicDBMetadata createDummyMetadata() {
        return DEFAULT_DUMMY_DB_METADATA.clone();
    }
}
