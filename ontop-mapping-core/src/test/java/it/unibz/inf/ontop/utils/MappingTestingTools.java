package it.unibz.inf.ontop.utils;

import com.google.inject.Injector;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.injection.OntopMappingConfiguration;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.mapping.transf.MappingNormalizer;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.impl.OntopModelSingletons;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQueryBuilder;
import it.unibz.inf.ontop.pivotalrepr.tools.ExecutorRegistry;
import it.unibz.inf.ontop.sql.DBMetadataTestingTools;

public class MappingTestingTools {

    public static final ExecutorRegistry EXECUTOR_REGISTRY;
    public static final IntermediateQueryFactory IQ_FACTORY;
    public static final DBMetadata EMPTY_METADATA = DBMetadataTestingTools.createDummyMetadata();

    public static final OBDADataFactory DATA_FACTORY = OntopModelSingletons.DATA_FACTORY;
    public static final SpecificationFactory MAPPING_FACTORY;
    public static final MappingNormalizer MAPPING_NORMALIZER;

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
    }

    public static IntermediateQueryBuilder createQueryBuilder(DBMetadata dbMetadata) {
        return IQ_FACTORY.createIQBuilder(dbMetadata, EXECUTOR_REGISTRY);
    }
}
