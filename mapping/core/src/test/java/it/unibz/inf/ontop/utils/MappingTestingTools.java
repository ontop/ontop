package it.unibz.inf.ontop.utils;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import it.unibz.inf.ontop.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.datalog.IntermediateQuery2DatalogTranslator;
import it.unibz.inf.ontop.datalog.impl.IntermediateQuery2DatalogTranslatorImpl;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.injection.OntopMappingConfiguration;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.OntopModelSingletons;
import it.unibz.inf.ontop.iq.IntermediateQueryBuilder;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.dbschema.DBMetadataTestingTools;
import it.unibz.inf.ontop.spec.mapping.MappingMetadata;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.transformer.*;
import it.unibz.inf.ontop.spec.mapping.transformer.impl.DefaultMappingTransformer;

import java.util.stream.Stream;

public class MappingTestingTools {

    public static final ExecutorRegistry EXECUTOR_REGISTRY;
    public static final IntermediateQueryFactory IQ_FACTORY;
    public static final DBMetadata EMPTY_METADATA = DBMetadataTestingTools.createDummyMetadata();

    public static final TermFactory DATA_FACTORY = OntopModelSingletons.TERM_FACTORY;
    public static final SpecificationFactory MAPPING_FACTORY;
    public static final MappingNormalizer MAPPING_NORMALIZER;
    public static final Datalog2QueryMappingConverter DATALOG_2_QUERY_MAPPING_CONVERTER;
    public static final DefaultMappingTransformer DEFAULT_MAPPING_TRANSFORMER;
    public static final MappingCanonicalRewriter MAPPING_CANONICAL_REWRITER;
    public static final MappingSaturator MAPPING_SATURATOR;
    public static final ABoxFactIntoMappingConverter A_BOX_FACT_INTO_MAPPING_CONVERTER;
    public static final MappingMerger MAPPING_MERGER;
    public static final OntopMappingSettings ONTOP_MAPPING_SETTINGS;
    public static final MappingSameAsInverseRewriter SAME_AS_INVERSE_REWRITER;
    public static final MappingEquivalenceFreeRewriter EQUIVALENCE_FREE_REWRITER;
    public static final IntermediateQuery2DatalogTranslator INTERMEDIATE_QUERY_2_DATALOG_TRANSLATOR;

    public static final PrefixManager EMPTY_PREFIX_MANAGER;
    public static final UriTemplateMatcher EMPTY_URI_TEMPLATE_MATCHER;
    public static final MappingMetadata EMPTY_MAPPING_METADATA;

    static {
        EMPTY_METADATA.freeze();

        OntopMappingConfiguration defaultConfiguration = OntopMappingConfiguration.defaultBuilder()
                .enableTestMode()
                .build();

        Injector injector = defaultConfiguration.getInjector();
        EXECUTOR_REGISTRY = defaultConfiguration.getExecutorRegistry();
        IQ_FACTORY = injector.getInstance(IntermediateQueryFactory.class);
        MAPPING_FACTORY = injector.getInstance(SpecificationFactory.class);
        MAPPING_NORMALIZER = injector.getInstance(MappingNormalizer.class);
        DATALOG_2_QUERY_MAPPING_CONVERTER = injector.getInstance(Datalog2QueryMappingConverter.class);

        EMPTY_URI_TEMPLATE_MATCHER = UriTemplateMatcher.create(Stream.of());
        EMPTY_PREFIX_MANAGER = MAPPING_FACTORY.createPrefixManager(ImmutableMap.of());
        EMPTY_MAPPING_METADATA = MAPPING_FACTORY.createMetadata(EMPTY_PREFIX_MANAGER, EMPTY_URI_TEMPLATE_MATCHER);
        DEFAULT_MAPPING_TRANSFORMER = injector.getInstance(DefaultMappingTransformer.class);
        MAPPING_CANONICAL_REWRITER = injector.getInstance(MappingCanonicalRewriter.class);
        MAPPING_SATURATOR= injector.getInstance(MappingSaturator.class);
        A_BOX_FACT_INTO_MAPPING_CONVERTER = injector.getInstance(ABoxFactIntoMappingConverter.class);
        ONTOP_MAPPING_SETTINGS = injector.getInstance(OntopMappingSettings.class);
        MAPPING_MERGER = injector.getInstance(MappingMerger.class);
        SAME_AS_INVERSE_REWRITER = injector.getInstance(MappingSameAsInverseRewriter.class);
        EQUIVALENCE_FREE_REWRITER = injector.getInstance(MappingEquivalenceFreeRewriter.class);
        INTERMEDIATE_QUERY_2_DATALOG_TRANSLATOR = injector.getInstance(IntermediateQuery2DatalogTranslator.class);
    }

    public static IntermediateQueryBuilder createQueryBuilder(DBMetadata dbMetadata) {
        return IQ_FACTORY.createIQBuilder(dbMetadata, EXECUTOR_REGISTRY);
    }
}
