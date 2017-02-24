package it.unibz.inf.ontop;


import com.google.inject.Injector;
import it.unibz.inf.ontop.injection.OntopModelFactory;
import it.unibz.inf.ontop.injection.OntopOptimizationConfiguration;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.impl.OntopModelSingletons;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQueryBuilder;
import it.unibz.inf.ontop.pivotalrepr.utils.ExecutorRegistry;
import it.unibz.inf.ontop.sql.DBMetadataTestingTools;

public class OptimizationTestingTools {

    private static final ExecutorRegistry EXECUTOR_REGISTRY;
    private static final OntopModelFactory MODEL_FACTORY;
    public static final DBMetadata EMPTY_METADATA = DBMetadataTestingTools.createDummyMetadata();
    public static final OBDADataFactory DATA_FACTORY = OntopModelSingletons.DATA_FACTORY;

    static {

        OntopOptimizationConfiguration defaultConfiguration = OntopOptimizationConfiguration.defaultBuilder()
                .enableTestMode()
                .build();

        Injector injector = defaultConfiguration.getInjector();
        EXECUTOR_REGISTRY = defaultConfiguration.getExecutorRegistry();
        MODEL_FACTORY = injector.getInstance(OntopModelFactory.class);
    }

    public static IntermediateQueryBuilder createQueryBuilder(DBMetadata metadata) {
        return MODEL_FACTORY.create(metadata, EXECUTOR_REGISTRY);
    }

}
