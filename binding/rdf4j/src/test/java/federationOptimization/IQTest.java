package federationOptimization;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import it.unibz.inf.ontop.dbschema.NamedRelationDefinition;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.dbschema.UniqueConstraint;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.injection.*;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.QueryNodeRenamer;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.*;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.iq.transform.impl.HomogeneousIQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transformer.BooleanExpressionPushDownTransformer;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLEngine;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.impl.SimpleOntopOWLEngine;
import it.unibz.inf.ontop.owlapi.resultset.BooleanOWLResultSet;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;
import it.unibz.inf.ontop.utils.impl.LegacyVariableGenerator;
import org.apache.commons.rdf.api.RDF;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Logger;

import static it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel.GT;

public class IQTest {

    public static final IntermediateQueryFactory IQ_FACTORY;
    public static final JoinLikeOptimizer JOIN_LIKE_OPTIMIZER;
    public static final AtomFactory ATOM_FACTORY;
    public static final TypeFactory TYPE_FACTORY;
    public static final TermFactory TERM_FACTORY;
    public static final FunctionSymbolFactory FUNCTION_SYMBOL_FACTORY;
    public static final SubstitutionFactory SUBSTITUTION_FACTORY;
    public static final QueryTransformerFactory TRANSFORMER_FACTORY;
    public static final OptimizerFactory OPTIMIZER_FACTORY;
    public static final CoreUtilsFactory CORE_UTILS_FACTORY;
    public static final BooleanExpressionPushDownTransformer PUSH_DOWN_BOOLEAN_EXPRESSION_TRANSFORMER;
    public static final DBConstant TRUE, FALSE;
    public static final Constant NULL;
    public static final UnionAndBindingLiftOptimizer UNION_AND_BINDING_LIFT_OPTIMIZER;
    public static final GeneralStructuralAndSemanticIQOptimizer GENERAL_STRUCTURAL_AND_SEMANTIC_IQ_OPTIMIZER;

    public static final FederationOptimizer FEDERATION_OPTIMIZER;

    public static final UnionBasedQueryMerger UNION_BASED_QUERY_MERGER;
    public static final FlattenLifter FLATTEN_LIFTER;
    public static final RDF RDF_FACTORY;
    public static final CoreSingletons CORE_SINGLETONS;

    public final static DBTermType JSON_TYPE;

    public static final Variable X, XF0, Y, W, Z, A, AF0, AF1, B, BF0, BF1, C, CF0, D, E, F, F0, F0F1, F1, F2, F6, G, GF1, H, HF0, I, L, M, N, NF0, O, PROV;
    public static final DBConstant ONE, TWO, ONE_STR, TWO_STR, THREE_STR, FOUR_STR, FIVE_STR;

    public static final AtomPredicate ANS1_AR0_PREDICATE, ANS1_AR1_PREDICATE, ANS1_AR2_PREDICATE, ANS1_AR3_PREDICATE,
            ANS1_AR4_PREDICATE, ANS1_AR5_PREDICATE;

    private final static AtomPredicate ANS1_ARITY_3_PREDICATE;

    static {

        // TEMPORARY! TODO: remove it!
        Properties tmpProperties = new Properties();
        tmpProperties.put(VariableGenerator.class.getCanonicalName(), LegacyVariableGenerator.class.getCanonicalName());

        OntopOptimizationConfiguration defaultConfiguration = OntopOptimizationConfiguration.defaultBuilder()
                .properties(tmpProperties)
                .enableTestMode()
                .build();

        Injector injector = defaultConfiguration.getInjector();
        IQ_FACTORY = injector.getInstance(IntermediateQueryFactory.class);
        JOIN_LIKE_OPTIMIZER = injector.getInstance(JoinLikeOptimizer.class);
        ATOM_FACTORY = injector.getInstance(AtomFactory.class);
        TYPE_FACTORY = injector.getInstance(TypeFactory.class);
        TERM_FACTORY = injector.getInstance(TermFactory.class);
        FUNCTION_SYMBOL_FACTORY = injector.getInstance(FunctionSymbolFactory.class);
        SUBSTITUTION_FACTORY = injector.getInstance(SubstitutionFactory.class);
        CORE_UTILS_FACTORY = injector.getInstance(CoreUtilsFactory.class);
        UNION_AND_BINDING_LIFT_OPTIMIZER = injector.getInstance(UnionAndBindingLiftOptimizer.class);
        PUSH_DOWN_BOOLEAN_EXPRESSION_TRANSFORMER = injector.getInstance(BooleanExpressionPushDownTransformer.class);
        TRANSFORMER_FACTORY = injector.getInstance(QueryTransformerFactory.class);
        OPTIMIZER_FACTORY = injector.getInstance(OptimizerFactory.class);
        CORE_SINGLETONS = injector.getInstance(CoreSingletons.class);
        GENERAL_STRUCTURAL_AND_SEMANTIC_IQ_OPTIMIZER = injector.getInstance(GeneralStructuralAndSemanticIQOptimizer.class);

        FEDERATION_OPTIMIZER = injector.getInstance(FederationOptimizer.class);

        UNION_BASED_QUERY_MERGER = injector.getInstance(UnionBasedQueryMerger.class);

        FLATTEN_LIFTER = injector.getInstance(FlattenLifter.class);

        JSON_TYPE = TYPE_FACTORY.getDBTypeFactory().getDBTermType("JSON");


        NULL = TERM_FACTORY.getNullConstant();
        TRUE = TERM_FACTORY.getDBBooleanConstant(true);
        FALSE = TERM_FACTORY.getDBBooleanConstant(false);
        RDF_FACTORY = injector.getInstance(RDF.class);

        ANS1_ARITY_3_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate( 3);

        X = TERM_FACTORY.getVariable("x");
        XF0 = TERM_FACTORY.getVariable("xf0");
        Y = TERM_FACTORY.getVariable("y");
        W = TERM_FACTORY.getVariable("w");
        Z = TERM_FACTORY.getVariable("z");
        A = TERM_FACTORY.getVariable("a");
        AF0 = TERM_FACTORY.getVariable("af0");
        AF1 = TERM_FACTORY.getVariable("af1");
        B = TERM_FACTORY.getVariable("b");
        BF0 = TERM_FACTORY.getVariable("bf0");
        BF1 = TERM_FACTORY.getVariable("bf1");
        C = TERM_FACTORY.getVariable("c");
        CF0 = TERM_FACTORY.getVariable("cf0");
        D = TERM_FACTORY.getVariable("d");
        E = TERM_FACTORY.getVariable("e");
        F = TERM_FACTORY.getVariable("f");
        F6 = TERM_FACTORY.getVariable("f6");
        F0 = TERM_FACTORY.getVariable("f0");
        F0F1 = TERM_FACTORY.getVariable("f0f1");
        F1 = TERM_FACTORY.getVariable("f1");
        F2 = TERM_FACTORY.getVariable("f2");
        G = TERM_FACTORY.getVariable("g");
        GF1 = TERM_FACTORY.getVariable("gf1");
        H = TERM_FACTORY.getVariable("h");
        HF0 = TERM_FACTORY.getVariable("hf0");
        I = TERM_FACTORY.getVariable("i");
        L = TERM_FACTORY.getVariable("l");
        M = TERM_FACTORY.getVariable("m");
        N = TERM_FACTORY.getVariable("n");
        O = TERM_FACTORY.getVariable("o");
        NF0 = TERM_FACTORY.getVariable("nf0");
        PROV = TERM_FACTORY.getVariable("prov");
        ONE = TERM_FACTORY.getDBIntegerConstant(1);
        TWO = TERM_FACTORY.getDBIntegerConstant(2);
        ONE_STR = TERM_FACTORY.getDBStringConstant("1");
        TWO_STR = TERM_FACTORY.getDBStringConstant("2");
        THREE_STR = TERM_FACTORY.getDBStringConstant("3");
        FOUR_STR = TERM_FACTORY.getDBStringConstant("4");
        FIVE_STR = TERM_FACTORY.getDBStringConstant("5");

        ANS1_AR0_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(0);
        ANS1_AR1_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(1);
        ANS1_AR2_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(2);
        ANS1_AR3_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(3);
        ANS1_AR4_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(4);
        ANS1_AR5_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(5);


    }

    public IQ createInitialIQ(){
        IQ iq_new = null;
        OfflineMetadataProviderBuilder builder = new OfflineMetadataProviderBuilder(CORE_SINGLETONS);
        DBTermType integerDBType = builder.getDBTypeFactory().getDBLargeIntegerType();

        NamedRelationDefinition TABLE1 =  builder.createDatabaseRelation( "TABLE1",
                "col1", integerDBType, false,
                "col2", integerDBType, false,
                "col3", integerDBType, true);
        UniqueConstraint.primaryKeyOf(TABLE1.getAttribute(1));

        NamedRelationDefinition TABLE2 =  builder.createDatabaseRelation( "TABLE2",
                "col1", integerDBType, false,
                "col2", integerDBType, false,
                "col3", integerDBType, true);
        UniqueConstraint.primaryKeyOf(TABLE2.getAttribute(1));

        NamedRelationDefinition TABLE3 =  builder.createDatabaseRelation( "TABLE3",
                "col1", integerDBType, false,
                "col2", integerDBType, false,
                "col3", integerDBType, true);
        UniqueConstraint.primaryKeyOf(TABLE3.getAttribute(1));

        NamedRelationDefinition TABLE4 = builder.createDatabaseRelation("TABLE4",
                "col1", integerDBType, false,
                "col2", integerDBType, false,
                "col3", integerDBType, false);
        UniqueConstraint.primaryKeyOf(TABLE4.getAttribute(1));

        NamedRelationDefinition TABLE5 = builder.createDatabaseRelation("TABLE5",
                "col1", integerDBType, false,
                "col2", integerDBType, false,
                "col3", integerDBType, false);
        UniqueConstraint.primaryKeyOf(TABLE5.getAttribute(1));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A,2, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(0, A, 2, B));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, A, 1, C));
        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE4, ImmutableMap.of(0, A, 1, C));
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE5, ImmutableMap.of(0, A));

        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B));//(ImmutableSet.of(X, A));

        ImmutableSubstitution<ImmutableTerm> xSubstitution11 = SUBSTITUTION_FACTORY.getSubstitution(A, ONE);
        ConstructionNode constructionNode11 = IQ_FACTORY.createConstructionNode(unionNode1.getVariables(), xSubstitution11);
        UnaryIQTree child11 = IQ_FACTORY.createUnaryIQTree(
                constructionNode11,
                dataNode1);

        ImmutableSubstitution<ImmutableTerm> xSubstitution12 = SUBSTITUTION_FACTORY.getSubstitution(A, TWO);
        ConstructionNode constructionNode12 = IQ_FACTORY.createConstructionNode(unionNode1.getVariables(),
                xSubstitution12);
        UnaryIQTree child12 = IQ_FACTORY.createUnaryIQTree(
                constructionNode12,
                dataNode2);

        NaryIQTree unionTree1 = IQ_FACTORY.createNaryIQTree(
                unionNode1,
                ImmutableList.of(
                        child11,
                        child12));

        NaryIQTree unionTree1_1 = IQ_FACTORY.createNaryIQTree(
                unionNode1,
                ImmutableList.of(
                        dataNode1,
                        dataNode2));

        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, C));

        ImmutableSubstitution<ImmutableTerm> xSubstitution21 = SUBSTITUTION_FACTORY.getSubstitution(A, ONE);

        ConstructionNode constructionNode21 = IQ_FACTORY.createConstructionNode(unionNode2.getVariables(), xSubstitution21);

        UnaryIQTree child21 = IQ_FACTORY.createUnaryIQTree(
                constructionNode21,
                dataNode3);

        ImmutableSubstitution<ImmutableTerm> xSubstitution22 = SUBSTITUTION_FACTORY.getSubstitution(A, TWO);

        ConstructionNode constructionNode22 = IQ_FACTORY.createConstructionNode(unionNode2.getVariables(),
                xSubstitution22);

        UnaryIQTree child22 = IQ_FACTORY.createUnaryIQTree(
                constructionNode22,
                dataNode4);

        NaryIQTree unionTree2 = IQ_FACTORY.createNaryIQTree(   //NaryIQTree needs to have at least two childern
                unionNode2,
                ImmutableList.of(
                        child21,
                        child22));

        //ImmutableExpression EXPRESSIONGT = TERM_FACTORY.getDBDefaultInequality(GT, Z, Y);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        NaryIQTree joinTree = IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(unionTree1_1, unionTree2));

        LeftJoinNode leftJNode = IQ_FACTORY.createLeftJoinNode();
        BinaryNonCommutativeIQTree leftJTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJNode, joinTree, dataNode5);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, A, B, C);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, leftJTree);
        /***the above created the IQ for playing***/
        return initialIQ;
    }

    @Test
    public void testCreateIQ(){
        IQ initialIQ = createInitialIQ();

        System.out.println(initialIQ);
        IQTree iqt = initialIQ.getTree();

        ImmutableList<IQTree> childern = iqt.getChildren();
        for(IQTree t: childern){
            ImmutableSet<Variable> vars = t.getVariables();
            EmptyNode en = IQ_FACTORY.createEmptyNode(vars);
            iqt = iqt.replaceSubTree(t, en);
            break;
        }

        System.out.println(iqt);
    }

    public IQ updateIQ(IQ iq){
        IQ iq_new = iq;

        return iq;
    }


    @Test
    public void traverseIQ(){
        IQ initialIQ = createInitialIQ();
        System.out.println(initialIQ);
        System.out.println("======");

        IQTree iq_t = initialIQ.getTree();

        ImmutableList<IQTree> subIQs = iq_t.getChildren();
        for(IQTree t: subIQs){
            System.out.println(t);
            if(t.isLeaf()){
                System.out.println("It is data node : leaf node");
                //QueryNode n = t.getRootNode();
                ExtensionalDataNode n = (ExtensionalDataNode) t.getRootNode();
                System.out.println(n.getRelationDefinition());
                System.out.println(n.getArgumentMap());
                System.out.println(n);
            }
            System.out.println("------");
        }
        System.out.println("======");

        QueryNode q_n = iq_t.getRootNode();
        LeftJoinNode LJ = IQ_FACTORY.createLeftJoinNode();
        if(q_n.equals(LJ)){
            //可以获取LJ node;
            System.out.println("Yes LJ");
        }
        System.out.println(q_n);
    }

}
