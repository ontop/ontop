package it.unibz.inf.ontop;


import com.google.inject.Injector;
import it.unibz.inf.ontop.injection.OntopModelFactory;
import it.unibz.inf.ontop.injection.OntopOptimizationConfiguration;
import it.unibz.inf.ontop.pivotalrepr.MetadataForQueryOptimization;
import it.unibz.inf.ontop.pivotalrepr.impl.EmptyMetadataForQueryOptimization;
import it.unibz.inf.ontop.pivotalrepr.utils.ExecutorRegistry;

public class OptimizationTestingTools {

    public static final ExecutorRegistry EXECUTOR_REGISTRY;
    public static final OntopModelFactory MODEL_FACTORY;
    public static final MetadataForQueryOptimization EMPTY_METADATA = new EmptyMetadataForQueryOptimization();

    static {

        OntopOptimizationConfiguration defaultConfiguration = OntopOptimizationConfiguration.defaultBuilder()
                .enableTestMode()
                .build();

        Injector injector = defaultConfiguration.getInjector();
        EXECUTOR_REGISTRY = defaultConfiguration.getExecutorRegistry();
        MODEL_FACTORY = injector.getInstance(OntopModelFactory.class);
    }

}
