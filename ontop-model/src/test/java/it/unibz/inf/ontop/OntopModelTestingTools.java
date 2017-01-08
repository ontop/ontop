package it.unibz.inf.ontop;

import com.google.inject.Injector;
import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.injection.OntopModelFactory;
import it.unibz.inf.ontop.pivotalrepr.utils.ExecutorRegistry;

/**
 *
 */
public class OntopModelTestingTools {

    public static final OntopModelFactory MODEL_FACTORY;
    public static final ExecutorRegistry EXECUTOR_REGISTRY;

    static {
        OntopModelConfiguration defaultConfiguration = OntopModelConfiguration.defaultBuilder()
                .enableTestMode()
                .build();
        Injector injector = defaultConfiguration.getInjector();

        MODEL_FACTORY = injector.getInstance(OntopModelFactory.class);

        EXECUTOR_REGISTRY = defaultConfiguration.getExecutorRegistry();
    }
}
