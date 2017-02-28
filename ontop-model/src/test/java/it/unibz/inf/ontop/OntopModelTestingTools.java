package it.unibz.inf.ontop;

import com.google.inject.Injector;
import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.impl.OntopModelSingletons;
import it.unibz.inf.ontop.pivotalrepr.utils.ExecutorRegistry;

/**
 *
 */
public class OntopModelTestingTools {

    public static final IntermediateQueryFactory IQ_FACTORY;
    public static final ExecutorRegistry EXECUTOR_REGISTRY;
    public static final OBDADataFactory DATA_FACTORY = OntopModelSingletons.DATA_FACTORY;

    static {
        OntopModelConfiguration defaultConfiguration = OntopModelConfiguration.defaultBuilder()
                .enableTestMode()
                .build();
        Injector injector = defaultConfiguration.getInjector();

        IQ_FACTORY = injector.getInstance(IntermediateQueryFactory.class);

        EXECUTOR_REGISTRY = defaultConfiguration.getExecutorRegistry();
    }
}
