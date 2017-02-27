package it.unibz.inf.ontop.utils;

import com.google.inject.Injector;
import it.unibz.inf.ontop.injection.MappingFactory;
import it.unibz.inf.ontop.injection.OntopMappingConfiguration;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.impl.OntopModelSingletons;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQueryBuilder;
import it.unibz.inf.ontop.pivotalrepr.utils.ExecutorRegistry;
import it.unibz.inf.ontop.sql.DBMetadataTestingTools;

public class MappingTestingTools {

    private static final ExecutorRegistry EXECUTOR_REGISTRY;
    private static final IntermediateQueryFactory MODEL_FACTORY;
    public static final DBMetadata EMPTY_METADATA = DBMetadataTestingTools.createDummyMetadata();

    public static final OBDADataFactory DATA_FACTORY = OntopModelSingletons.DATA_FACTORY;
    public static final MappingFactory MAPPING_FACTORY;

    static {
        EMPTY_METADATA.freeze();

        OntopMappingConfiguration defaultConfiguration = OntopMappingConfiguration.defaultBuilder()
                .enableTestMode()
                .build();

        Injector injector = defaultConfiguration.getInjector();
        EXECUTOR_REGISTRY = defaultConfiguration.getExecutorRegistry();
        MODEL_FACTORY = injector.getInstance(IntermediateQueryFactory.class);
        MAPPING_FACTORY = injector.getInstance(MappingFactory.class);
    }

    public static IntermediateQueryBuilder createQueryBuilder(DBMetadata dbMetadata) {
        return MODEL_FACTORY.createIQBuilder(dbMetadata, EXECUTOR_REGISTRY);
    }
}
