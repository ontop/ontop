package it.unibz.inf.ontop;

import com.google.inject.Injector;
import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.OntopModelSingletons;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;

/**
 *
 */
public class OntopModelTestingTools {

    public static final IntermediateQueryFactory IQ_FACTORY;
    public static final ExecutorRegistry EXECUTOR_REGISTRY;
    public static final TermFactory DATA_FACTORY = OntopModelSingletons.TERM_FACTORY;

    static {
        OntopModelConfiguration defaultConfiguration = OntopModelConfiguration.defaultBuilder()
                .enableTestMode()
                .build();
        Injector injector = defaultConfiguration.getInjector();

        IQ_FACTORY = injector.getInstance(IntermediateQueryFactory.class);

        EXECUTOR_REGISTRY = defaultConfiguration.getExecutorRegistry();
    }
}
