package it.unibz.inf.ontop.utils;

import com.google.inject.Injector;
import it.unibz.inf.ontop.injection.MappingFactory;
import it.unibz.inf.ontop.injection.OntopMappingConfiguration;
import it.unibz.inf.ontop.injection.OntopModelFactory;
import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.impl.OntopModelSingletons;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQueryBuilder;
import it.unibz.inf.ontop.pivotalrepr.MetadataForQueryOptimization;
import it.unibz.inf.ontop.pivotalrepr.impl.EmptyMetadataForQueryOptimization;
import it.unibz.inf.ontop.pivotalrepr.utils.ExecutorRegistry;

public class MappingTestingTools {

    private static final ExecutorRegistry EXECUTOR_REGISTRY;
    private static final OntopModelFactory MODEL_FACTORY;
    public static final MetadataForQueryOptimization EMPTY_METADATA = new EmptyMetadataForQueryOptimization();
    public static final OBDADataFactory DATA_FACTORY = OntopModelSingletons.DATA_FACTORY;
    public static final MappingFactory MAPPING_FACTORY;

    static {

        OntopMappingConfiguration defaultConfiguration = OntopMappingConfiguration.defaultBuilder()
                .enableTestMode()
                .build();

        Injector injector = defaultConfiguration.getInjector();
        EXECUTOR_REGISTRY = defaultConfiguration.getExecutorRegistry();
        MODEL_FACTORY = injector.getInstance(OntopModelFactory.class);
        MAPPING_FACTORY = injector.getInstance(MappingFactory.class);
    }

    public static IntermediateQueryBuilder createQueryBuilder(MetadataForQueryOptimization metadata) {
        return MODEL_FACTORY.create(metadata, EXECUTOR_REGISTRY);
    }
}
