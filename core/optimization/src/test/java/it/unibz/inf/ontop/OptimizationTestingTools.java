package it.unibz.inf.ontop;


import com.google.inject.Injector;
import it.unibz.inf.ontop.datalog.impl.DatalogConversionTools;
import it.unibz.inf.ontop.dbschema.Relation2Predicate;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopOptimizationConfiguration;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.iq.optimizer.PullOutVariableOptimizer;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.OntopModelSingletons;
import it.unibz.inf.ontop.iq.optimizer.BindingLiftOptimizer;
import it.unibz.inf.ontop.iq.optimizer.InnerJoinOptimizer;
import it.unibz.inf.ontop.iq.optimizer.JoinLikeOptimizer;
import it.unibz.inf.ontop.iq.IntermediateQueryBuilder;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.dbschema.DBMetadataTestingTools;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;

public class OptimizationTestingTools {

    private static final ExecutorRegistry EXECUTOR_REGISTRY;
    public static final IntermediateQueryFactory IQ_FACTORY;
    public static final DBMetadata EMPTY_METADATA;
    public static final TermFactory DATA_FACTORY = OntopModelSingletons.TERM_FACTORY;
    public static final JoinLikeOptimizer JOIN_LIKE_OPTIMIZER;
    public static final InnerJoinOptimizer INNER_JOIN_OPTIMIZER;
    public static final BindingLiftOptimizer BINDING_LIFT_OPTIMIZER;
    public static final AtomFactory ATOM_FACTORY;
    public static final TypeFactory TYPE_FACTORY;
    public static final SubstitutionFactory SUBSTITUTION_FACTORY;
    public static final Relation2Predicate RELATION_2_PREDICATE;
    public static final PullOutVariableOptimizer PULL_OUT_VARIABLE_OPTIMIZER;
    public static final DatalogConversionTools DATALOG_CONVERSION_TOOLS;

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
        ATOM_FACTORY = injector.getInstance(AtomFactory.class);
        TYPE_FACTORY = injector.getInstance(TypeFactory.class);
        SUBSTITUTION_FACTORY = injector.getInstance(SubstitutionFactory.class);
        RELATION_2_PREDICATE = injector.getInstance(Relation2Predicate.class);
        EMPTY_METADATA = DBMetadataTestingTools.createDummyMetadata(ATOM_FACTORY,
                RELATION_2_PREDICATE);
        PULL_OUT_VARIABLE_OPTIMIZER = injector.getInstance(PullOutVariableOptimizer.class);
        DATALOG_CONVERSION_TOOLS = injector.getInstance(DatalogConversionTools.class);
    }

    public static IntermediateQueryBuilder createQueryBuilder(DBMetadata metadata) {
        return IQ_FACTORY.createIQBuilder(metadata, EXECUTOR_REGISTRY);
    }

}
