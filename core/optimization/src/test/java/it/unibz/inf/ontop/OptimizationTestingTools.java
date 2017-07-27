package it.unibz.inf.ontop;


import com.google.inject.Injector;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopOptimizationConfiguration;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.OntopModelSingletons;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.BindingLiftOptimizer;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.InnerJoinOptimizer;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.JoinLikeOptimizer;
import it.unibz.inf.ontop.iq.IntermediateQueryBuilder;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.dbschema.DBMetadataTestingTools;

public class OptimizationTestingTools {

    private static final ExecutorRegistry EXECUTOR_REGISTRY;
    public static final IntermediateQueryFactory IQ_FACTORY;
    public static final DBMetadata EMPTY_METADATA = DBMetadataTestingTools.createDummyMetadata();
    public static final TermFactory DATA_FACTORY = OntopModelSingletons.TERM_FACTORY;
    public static final JoinLikeOptimizer JOIN_LIKE_OPTIMIZER;
    public static final InnerJoinOptimizer INNER_JOIN_OPTIMIZER;
    public static final BindingLiftOptimizer BINDING_LIFT_OPTIMIZER;

    static {

        OntopOptimizationConfiguration defaultConfiguration = OntopOptimizationConfiguration.defaultBuilder()
                .enableTestMode()
                .build();

        Injector injector = defaultConfiguration.getInjector();
        EXECUTOR_REGISTRY = defaultConfiguration.getExecutorRegistry();
        IQ_FACTORY = injector.getInstance(IntermediateQueryFactory.class);
        JOIN_LIKE_OPTIMIZER = injector.getInstance(JoinLikeOptimizer.class);
        INNER_JOIN_OPTIMIZER = injector.getInstance(InnerJoinOptimizer.class);
        BINDING_LIFT_OPTIMIZER = injector.getInstance(BindingLiftOptimizer.class);
    }

    public static IntermediateQueryBuilder createQueryBuilder(DBMetadata metadata) {
        return IQ_FACTORY.createIQBuilder(metadata, EXECUTOR_REGISTRY);
    }

}
