package it.unibz.inf.ontop;


import com.google.inject.Injector;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.datalog.DatalogProgram2QueryConverter;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopOptimizationConfiguration;
import it.unibz.inf.ontop.injection.QueryTransformerFactory;
import it.unibz.inf.ontop.injection.OptimizerFactory;
import it.unibz.inf.ontop.iq.optimizer.*;
import it.unibz.inf.ontop.iq.tools.IQConverter;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.iq.IntermediateQueryBuilder;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;
import it.unibz.inf.ontop.utils.impl.LegacyVariableGenerator;
import org.apache.commons.rdf.api.RDF;

import java.util.Properties;

public class OptimizationTestingTools {

    private static final ExecutorRegistry EXECUTOR_REGISTRY;
    public static final IntermediateQueryFactory IQ_FACTORY;
    public static final DBMetadata EMPTY_METADATA;
    public static final JoinLikeOptimizer JOIN_LIKE_OPTIMIZER;
    public static final BindingLiftOptimizer BINDING_LIFT_OPTIMIZER;
    public static final AtomFactory ATOM_FACTORY;
    public static final TypeFactory TYPE_FACTORY;
    public static final TermFactory TERM_FACTORY;
    public static final FunctionSymbolFactory FUNCTION_SYMBOL_FACTORY;
    public static final DatalogFactory DATALOG_FACTORY;
    public static final SubstitutionFactory SUBSTITUTION_FACTORY;
    public static final QueryTransformerFactory TRANSFORMER_FACTORY;
    public static final OptimizerFactory OPTIMIZER_FACTORY;
    public static final CoreUtilsFactory CORE_UTILS_FACTORY;
    public static final PushDownBooleanExpressionOptimizer PUSH_DOWN_BOOLEAN_EXPRESSION_OPTIMIZER;
    public static final ImmutabilityTools IMMUTABILITY_TOOLS;
    public static final IQConverter IQ_CONVERTER;
    public static final DBConstant TRUE, FALSE;
    public static final Constant NULL;
    public static final UnionAndBindingLiftOptimizer UNION_AND_BINDING_LIFT_OPTIMIZER;
    public static final UnionBasedQueryMerger UNION_BASED_QUERY_MERGER;
    public static final DatalogProgram2QueryConverter DATALOG_PROGRAM_2_QUERY_CONVERTER;
    public static final RDF RDF_FACTORY;
    private static final DummyBasicDBMetadata DEFAULT_DUMMY_DB_METADATA;

    public static final Variable X;
    public static final Variable Y;
    public static final Variable W;
    public static final Variable Z;
    public static final Variable A;
    public static final Variable AF0;
    public static final Variable AF1;
    public static final Variable AF1F3;
    public static final Variable AF1F4;
    public static final Variable AF2;
    public static final Variable AF3;
    public static final Variable B;
    public static final Variable BF1;
    public static final Variable BF2;
    public static final Variable BF4F5;
    public static final Variable C;
    public static final Variable D;
    public static final Variable E;
    public static final Variable F;
    public static final Variable F6;
    public static final Variable F0;
    public static final Variable F0F2;
    public static final Variable F0F3;
    public static final Variable FF4;
    public static final Variable G;
    public static final Variable H;
    public static final Variable I;
    public static final Variable IF7;
    public static final Variable L;
    public static final Variable M;
    public static final Variable N;
    public static final DBConstant ONE, TWO, ONE_STR, TWO_STR;

    public static final AtomPredicate ANS1_AR0_PREDICATE, ANS1_AR1_PREDICATE, ANS1_AR2_PREDICATE, ANS1_AR3_PREDICATE,
            ANS1_AR4_PREDICATE, ANS1_AR5_PREDICATE;

    static {

        // TEMPORARY! TODO: remove it!
        Properties tmpProperties = new Properties();
        tmpProperties.put(VariableGenerator.class.getCanonicalName(), LegacyVariableGenerator.class.getCanonicalName());

        OntopOptimizationConfiguration defaultConfiguration = OntopOptimizationConfiguration.defaultBuilder()
                .properties(tmpProperties)
                .enableTestMode()
                .build();

        Injector injector = defaultConfiguration.getInjector();
        EXECUTOR_REGISTRY = defaultConfiguration.getExecutorRegistry();
        IQ_FACTORY = injector.getInstance(IntermediateQueryFactory.class);
        JOIN_LIKE_OPTIMIZER = injector.getInstance(JoinLikeOptimizer.class);
        BINDING_LIFT_OPTIMIZER = injector.getInstance(BindingLiftOptimizer.class);
        ATOM_FACTORY = injector.getInstance(AtomFactory.class);
        TYPE_FACTORY = injector.getInstance(TypeFactory.class);
        TERM_FACTORY = injector.getInstance(TermFactory.class);
        FUNCTION_SYMBOL_FACTORY = injector.getInstance(FunctionSymbolFactory.class);
        DATALOG_FACTORY = injector.getInstance(DatalogFactory.class);
        SUBSTITUTION_FACTORY = injector.getInstance(SubstitutionFactory.class);
        CORE_UTILS_FACTORY = injector.getInstance(CoreUtilsFactory.class);
        IQ_CONVERTER = injector.getInstance(IQConverter.class);
        UNION_AND_BINDING_LIFT_OPTIMIZER = injector.getInstance(UnionAndBindingLiftOptimizer.class);
        PUSH_DOWN_BOOLEAN_EXPRESSION_OPTIMIZER = injector.getInstance(PushDownBooleanExpressionOptimizer.class);
        TRANSFORMER_FACTORY = injector.getInstance(QueryTransformerFactory.class);
        OPTIMIZER_FACTORY = injector.getInstance(OptimizerFactory.class);
        DATALOG_PROGRAM_2_QUERY_CONVERTER = injector.getInstance(DatalogProgram2QueryConverter.class);

        DEFAULT_DUMMY_DB_METADATA = injector.getInstance(DummyBasicDBMetadata.class);
        EMPTY_METADATA = DEFAULT_DUMMY_DB_METADATA.clone();
        EMPTY_METADATA.freeze();

        IMMUTABILITY_TOOLS = injector.getInstance(ImmutabilityTools.class);
        UNION_BASED_QUERY_MERGER = injector.getInstance(UnionBasedQueryMerger.class);

        NULL = TERM_FACTORY.getNullConstant();
        TRUE = TERM_FACTORY.getDBBooleanConstant(true);
        FALSE = TERM_FACTORY.getDBBooleanConstant(false);
        RDF_FACTORY = injector.getInstance(RDF.class);

        X = TERM_FACTORY.getVariable("x");
        Y = TERM_FACTORY.getVariable("y");
        W = TERM_FACTORY.getVariable("w");
        Z = TERM_FACTORY.getVariable("z");
        A = TERM_FACTORY.getVariable("a");
        AF0 = TERM_FACTORY.getVariable("af0");
        AF1 = TERM_FACTORY.getVariable("af1");
        AF1F3 = TERM_FACTORY.getVariable("af1f3");
        AF1F4 = TERM_FACTORY.getVariable("af1f4");
        AF2 = TERM_FACTORY.getVariable("af2");
        AF3 = TERM_FACTORY.getVariable("af3");
        B = TERM_FACTORY.getVariable("b");
        BF1 = TERM_FACTORY.getVariable("bf1");
        BF2 = TERM_FACTORY.getVariable("bf2");
        BF4F5 = TERM_FACTORY.getVariable("bf4f5");
        C = TERM_FACTORY.getVariable("c");
        D = TERM_FACTORY.getVariable("d");
        E = TERM_FACTORY.getVariable("e");
        F = TERM_FACTORY.getVariable("f");
        F6 = TERM_FACTORY.getVariable("f6");
        F0 = TERM_FACTORY.getVariable("f0");
        F0F2 = TERM_FACTORY.getVariable("f0f2");
        F0F3 = TERM_FACTORY.getVariable("f0f3");
        FF4 = TERM_FACTORY.getVariable("ff4");
        G = TERM_FACTORY.getVariable("g");
        H = TERM_FACTORY.getVariable("h");
        I = TERM_FACTORY.getVariable("i");
        IF7 = TERM_FACTORY.getVariable("if7");
        L = TERM_FACTORY.getVariable("l");
        M = TERM_FACTORY.getVariable("m");
        N = TERM_FACTORY.getVariable("n");
        ONE = TERM_FACTORY.getDBIntegerConstant(1);
        TWO = TERM_FACTORY.getDBIntegerConstant(2);
        ONE_STR = TERM_FACTORY.getDBStringConstant("1");
        TWO_STR = TERM_FACTORY.getDBStringConstant("2");

        ANS1_AR0_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(0);
        ANS1_AR1_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(1);
        ANS1_AR2_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(2);
        ANS1_AR3_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(3);
        ANS1_AR4_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(4);
        ANS1_AR5_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(5);
    }

    public static IntermediateQueryBuilder createQueryBuilder(DBMetadata metadata) {
        return IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);
    }

    public static BasicDBMetadata createDummyMetadata() {
        return DEFAULT_DUMMY_DB_METADATA.clone();
    }
}
