package it.unibz.inf.ontop.reformulation.tests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.model.impl.URITemplatePredicateImpl;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.FixedPointBindingLiftOptimizer;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.IntermediateQueryOptimizer;
import it.unibz.inf.ontop.injection.QuestCoreConfiguration;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.equivalence.IQSyntacticEquivalenceChecker;
import it.unibz.inf.ontop.pivotalrepr.impl.*;
import it.unibz.inf.ontop.pivotalrepr.impl.tree.DefaultIntermediateQueryBuilder;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.model.ExpressionOperation.EQ;
import static it.unibz.inf.ontop.model.ExpressionOperation.SPARQL_DATATYPE;
import static it.unibz.inf.ontop.model.Predicate.COL_TYPE.INTEGER;
import static it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition.RIGHT;
import static junit.framework.TestCase.assertTrue;

/**
 * Test the top down substitution lift optimizer
 */
public class BindingLiftTest {


    private final AtomPredicate TABLE1_PREDICATE = new AtomPredicateImpl("table1", 2);
    private final AtomPredicate TABLE2_PREDICATE = new AtomPredicateImpl("table2", 2);
    private final AtomPredicate TABLE3_PREDICATE = new AtomPredicateImpl("table3", 2);
    private final AtomPredicate TABLE4_PREDICATE = new AtomPredicateImpl("table4", 2);
    private final AtomPredicate TABLE5_PREDICATE = new AtomPredicateImpl("table5", 2);
    private final AtomPredicate TABLE6_PREDICATE = new AtomPredicateImpl("table6", 2);
    private final AtomPredicate TABLE7_ARITY_1_PREDICATE = new AtomPredicateImpl("table7", 1);
    private final AtomPredicate TABLE8_ARITY_1_PREDICATE = new AtomPredicateImpl("table8", 1);
    private final AtomPredicate TABLE9_ARITY_1_PREDICATE = new AtomPredicateImpl("table9", 1);


    private final AtomPredicate ANS1_ARITY_2_PREDICATE = new AtomPredicateImpl("ans1", 2);
    private final AtomPredicate ANS1_ARITY_3_PREDICATE = new AtomPredicateImpl("ans1", 3);
    private final AtomPredicate ANS1_ARITY_4_PREDICATE = new AtomPredicateImpl("ans1", 4);

    private static final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
    private final Variable X = DATA_FACTORY.getVariable("x");
    private final Variable Y = DATA_FACTORY.getVariable("y");
    private final Variable W = DATA_FACTORY.getVariable("w");
    private final Variable Z = DATA_FACTORY.getVariable("z");
    private final Variable A = DATA_FACTORY.getVariable("a");
    private final Variable B = DATA_FACTORY.getVariable("b");
    private final Variable C = DATA_FACTORY.getVariable("c");
    private final Variable D = DATA_FACTORY.getVariable("d");
    private final Variable E = DATA_FACTORY.getVariable("e");
    private final Variable F = DATA_FACTORY.getVariable("f");
    private final Variable G = DATA_FACTORY.getVariable("g");
    private final Variable H = DATA_FACTORY.getVariable("h");
    private final Variable I = DATA_FACTORY.getVariable("i");
    private final Variable L = DATA_FACTORY.getVariable("l");
    private final Variable M = DATA_FACTORY.getVariable("m");
    private final Variable N = DATA_FACTORY.getVariable("n");


    private URITemplatePredicate URI_PREDICATE =  new URITemplatePredicateImpl(2);
    private URITemplatePredicate URI_2PREDICATE =  new URITemplatePredicateImpl(3);

    private Constant URI_TEMPLATE_STR_1 =  DATA_FACTORY.getConstantLiteral("http://example.org/ds1/{}");
    private Constant URI_TEMPLATE_STR_2 =  DATA_FACTORY.getConstantLiteral("http://example.org/ds2/{}");
    private Constant URI_TEMPLATE_STR_2_2 =  DATA_FACTORY.getConstantLiteral("http://example.org/ds2/{}/{}");

    private ExtensionalDataNode EXPECTED_DATA_NODE_1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, A, C));
    private ExtensionalDataNode EXPECTED_DATA_NODE_3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, C, D));
    private ExtensionalDataNode EXPECTED_DATA_NODE_4 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B));
    private ExtensionalDataNode EXPECTED_DATA_NODE_5 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, B, D));
    private ExtensionalDataNode EXPECTED_DATA_NODE_6 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, A, DATA_FACTORY.getVariable("ff0")));

    private ExtensionalDataNode DATA_NODE_1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B));
    private ExtensionalDataNode DATA_NODE_2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, A, E));
    private ExtensionalDataNode DATA_NODE_3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, C, D));
    private ExtensionalDataNode DATA_NODE_4 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B));
    private ExtensionalDataNode DATA_NODE_5 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, C, D));
    private ExtensionalDataNode DATA_NODE_6 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, E, F));
    private ExtensionalDataNode DATA_NODE_7 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE4_PREDICATE, G, H));
    private ExtensionalDataNode DATA_NODE_8 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE7_ARITY_1_PREDICATE, A));
    private ExtensionalDataNode DATA_NODE_9 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE8_ARITY_1_PREDICATE, B));

    InnerJoinNode joinNode;
    UnionNode unionNode;

    private final ImmutableExpression EXPRESSIONGT = DATA_FACTORY.getImmutableExpression(
            ExpressionOperation.GT, Z, Y);

    private MetadataForQueryOptimization METADATA = new EmptyMetadataForQueryOptimization();

    private static final Injector INJECTOR = QuestCoreConfiguration.defaultBuilder().build().getInjector();

    public BindingLiftTest() {

    }

    @Test
    public void testSimpleSubstitution() throws EmptyQueryException {

        //Construct unoptimized query
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA, INJECTOR);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, X, Y);
        ConstructionNode rootNode = new ConstructionNodeImpl(projectionAtom.getVariables());

        queryBuilder.init(projectionAtom, rootNode);

        //construct innerjoin
        joinNode = new InnerJoinNodeImpl(Optional.of(EXPRESSIONGT));
        queryBuilder.addChild(rootNode, joinNode);

        //construct left side join
        ConstructionNode leftNode = new ConstructionNodeImpl(ImmutableSet.of(X,Z),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(Z, generateInt(A))), Optional.empty());
        queryBuilder.addChild(joinNode, leftNode);

        //construct union
        ImmutableSet<Variable> subQueryProjectedVariables = ImmutableSet.of(X,A);
        unionNode = new UnionNodeImpl(subQueryProjectedVariables);

        queryBuilder.addChild(leftNode, unionNode);

        //construct node1 union
        ConstructionNode subQueryConstructionNode1 = new ConstructionNodeImpl(subQueryProjectedVariables,
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(B))), Optional.empty());

        queryBuilder.addChild(unionNode, subQueryConstructionNode1);
        queryBuilder.addChild(subQueryConstructionNode1, DATA_NODE_1);

        //construct node2 union
        ConstructionNode subQueryConstructionNode2 = new ConstructionNodeImpl(subQueryProjectedVariables,
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI2(E))), Optional.empty());
        queryBuilder.addChild(unionNode, subQueryConstructionNode2);

        queryBuilder.addChild(subQueryConstructionNode2, DATA_NODE_2);

        //construct right side join
        ConstructionNode rightNode = new ConstructionNodeImpl(ImmutableSet.of(X,Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI1(C),
                        Y, generateInt(D))),
                Optional.empty());

        queryBuilder.addChild(joinNode, rightNode);

        queryBuilder.addChild(rightNode, DATA_NODE_3);

        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);


        IntermediateQueryOptimizer substitutionOptimizer = new FixedPointBindingLiftOptimizer();


        IntermediateQuery optimizedQuery = substitutionOptimizer.optimize(unOptimizedQuery);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);



        //----------------------------------------------------------------------
        // Construct expected query
        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA, INJECTOR);


        DistinctVariableOnlyDataAtom expectedProjectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, X, Y);
        ConstructionNode expectedRootNode = new ConstructionNodeImpl(expectedProjectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(C), Y, generateInt(D))),
                Optional.empty());

        expectedQueryBuilder.init(expectedProjectionAtom, expectedRootNode);

        //construct expected innerjoin
        ImmutableExpression expectedEspressionGT = DATA_FACTORY.getImmutableExpression(ExpressionOperation.GT, generateInt(A), generateInt(D));
        InnerJoinNode expectedJoinNode = new InnerJoinNodeImpl(Optional.of(expectedEspressionGT));
        expectedQueryBuilder.addChild(expectedRootNode, expectedJoinNode);

        expectedQueryBuilder.addChild(expectedJoinNode, EXPECTED_DATA_NODE_1);

        expectedQueryBuilder.addChild(expectedJoinNode, EXPECTED_DATA_NODE_3);

        //build expected query
        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }


    @Test
    public void testLeftJoinSubstitution() throws EmptyQueryException {

        //Construct unoptimized query
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA, INJECTOR);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, X, W, Z);
        ConstructionNode rootNode = new ConstructionNodeImpl(projectionAtom.getVariables());

        queryBuilder.init(projectionAtom, rootNode);

        //construct innerjoin
        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(rootNode, leftJoinNode);

        //construct left side left join (join)
        InnerJoinNode joinNodeOnLeft = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(leftJoinNode, joinNodeOnLeft, LEFT);

        //construct left side join
        ConstructionNode leftNodeJoin = new ConstructionNodeImpl(ImmutableSet.of(X, W),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A), W, generateString(B))), Optional.empty());
        queryBuilder.addChild(joinNodeOnLeft, leftNodeJoin);

        queryBuilder.addChild(leftNodeJoin, DATA_NODE_4 );

        //construct right side join
        ConstructionNode rightNodeJoin = new ConstructionNodeImpl(ImmutableSet.of(W,Z),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        W, generateString(C),
                        Z, generateInt(D))),
                Optional.empty());
        queryBuilder.addChild(joinNodeOnLeft, rightNodeJoin);

        queryBuilder.addChild(rightNodeJoin, DATA_NODE_5 );

        //construct right side left join (union)
        ImmutableSet<Variable> subQueryProjectedVariables = ImmutableSet.of(X,Y);
        UnionNode unionNodeOnRight = new UnionNodeImpl(subQueryProjectedVariables);

        queryBuilder.addChild(leftJoinNode, unionNodeOnRight, RIGHT);

        //construct node1 union
        ConstructionNode subQueryConstructionNode1 = new ConstructionNodeImpl(subQueryProjectedVariables,
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(E), Y, generateInt((F)))), Optional.empty());

        queryBuilder.addChild(unionNodeOnRight, subQueryConstructionNode1);

        queryBuilder.addChild(subQueryConstructionNode1, DATA_NODE_6);

        //construct node2 union
        ConstructionNode subQueryConstructionNode2 = new ConstructionNodeImpl(subQueryProjectedVariables,
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI2(G), Y , generateInt(H))), Optional.empty());
        queryBuilder.addChild(unionNodeOnRight, subQueryConstructionNode2);

        queryBuilder.addChild(subQueryConstructionNode2, DATA_NODE_7);



        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);


        IntermediateQueryOptimizer substitutionOptimizer = new FixedPointBindingLiftOptimizer();


        IntermediateQuery optimizedQuery = substitutionOptimizer.optimize(unOptimizedQuery);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);



        //----------------------------------------------------------------------
        // Construct expected query
        //Construct unoptimized query
        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA, INJECTOR);
        ConstructionNode expectedRootNode = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of( W, generateString(B), X, generateURI1(A), Z, generateInt(D))), Optional.empty());

        expectedQueryBuilder.init(projectionAtom, expectedRootNode);

        //construct innerjoin
        LeftJoinNode expectedleftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        expectedQueryBuilder.addChild(expectedRootNode, expectedleftJoinNode);

        //construct left side left join (join)
        InnerJoinNode expectedJoinNodeOnLeft = new InnerJoinNodeImpl(Optional.empty());
        expectedQueryBuilder.addChild(expectedleftJoinNode, expectedJoinNodeOnLeft, LEFT);

        expectedQueryBuilder.addChild(expectedJoinNodeOnLeft, EXPECTED_DATA_NODE_4 );

        expectedQueryBuilder.addChild(expectedJoinNodeOnLeft, EXPECTED_DATA_NODE_5);


        //construct right side left join (union)

        expectedQueryBuilder.addChild(expectedleftJoinNode, EXPECTED_DATA_NODE_6, RIGHT);


        //build expected query
        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));


    }

    @Test
    public void testUnionSubstitution() throws EmptyQueryException {
        //Construct unoptimized query
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA, INJECTOR);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, X, Y);
        ConstructionNode rootNode = new ConstructionNodeImpl(projectionAtom.getVariables());

        queryBuilder.init(projectionAtom, rootNode);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(rootNode, joinNode);


        //left side first join
        InnerJoinNode joinNode2 = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(joinNode, joinNode2);

        //left side second join (unionNode 2)

        UnionNode unionNode2 =  new UnionNodeImpl(projectionAtom.getVariables());
        queryBuilder.addChild(joinNode2, unionNode2);

        //first child of unionNode2
        ConstructionNode subQuery1UnionNode2 = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A), Y, generateURI1(B))), Optional.empty());
        queryBuilder.addChild(unionNode2, subQuery1UnionNode2);

        queryBuilder.addChild(subQuery1UnionNode2, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B)) );

        //second child of unionNode2
        ConstructionNode subQuery2UnionNode2 = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI2(C), Y, generateURI1(D))), Optional.empty());
        queryBuilder.addChild(unionNode2, subQuery2UnionNode2);

        queryBuilder.addChild(subQuery2UnionNode2, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, C, D)) );

        //right side second join (unionNode 3)
        UnionNode unionNode3 =  new UnionNodeImpl(ImmutableSet.of(Y));
        queryBuilder.addChild(joinNode2, unionNode3);

        //first child of unionNode3
        ConstructionNode subQuery1UnionNode3 = new ConstructionNodeImpl(ImmutableSet.of(Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of( Y, generateURI1(F))), Optional.empty());
        queryBuilder.addChild(unionNode3, subQuery1UnionNode3);

        queryBuilder.addChild(subQuery1UnionNode3, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, E, F)) );

        //second child of unionNode3
        ConstructionNode subQuery2UnionNode3 = new ConstructionNodeImpl(ImmutableSet.of(Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(Y,  generateURI2(H))), Optional.empty());
        queryBuilder.addChild(unionNode3, subQuery2UnionNode3);

        queryBuilder.addChild(subQuery2UnionNode3, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE4_PREDICATE, G, H)) );

        //right side first join
        UnionNode unionNode1 =  new UnionNodeImpl(ImmutableSet.of(X));
        queryBuilder.addChild(joinNode, unionNode1);

        //first child of unionNode1
        ConstructionNode subQuery1UnionNode1 = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(I))), Optional.empty());
        queryBuilder.addChild(unionNode1, subQuery1UnionNode1);

        queryBuilder.addChild(subQuery1UnionNode1, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE5_PREDICATE, I, L)) );

        //second child of unionNode1
        ConstructionNode subQuery2UnionNode1 = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI2(M))), Optional.empty());
        queryBuilder.addChild(unionNode1, subQuery2UnionNode1);

        queryBuilder.addChild(subQuery2UnionNode1, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE6_PREDICATE, M, N)) );

        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);

        IntermediateQueryOptimizer substitutionOptimizer = new FixedPointBindingLiftOptimizer();

        IntermediateQuery optimizedQuery = substitutionOptimizer.optimize(unOptimizedQuery);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        //----------------------------------------------------------------------
        //Construct expected query
        Variable BF0 = DATA_FACTORY.getVariable("bf0");
        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA, INJECTOR);
        ConstructionNode expectedRootNode = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(Y, generateURI1(BF0))), Optional.empty());

        expectedQueryBuilder.init(projectionAtom, expectedRootNode);

        //constract union Node
        UnionNode expectedUnionNode =  new UnionNodeImpl(ImmutableSet.of(BF0, X, E));

        expectedQueryBuilder.addChild(expectedRootNode, expectedUnionNode );

        ConstructionNode expectedSubQuery1UnionNode = new ConstructionNodeImpl(expectedUnionNode.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A))), Optional.empty());
        expectedQueryBuilder.addChild(expectedUnionNode, expectedSubQuery1UnionNode);

        InnerJoinNode joinNode11 = new InnerJoinNodeImpl(Optional.empty());
        expectedQueryBuilder.addChild(expectedSubQuery1UnionNode, joinNode11);

        InnerJoinNode joinNode12 = new InnerJoinNodeImpl(Optional.empty());
        expectedQueryBuilder.addChild(joinNode11, joinNode12);

        expectedQueryBuilder.addChild(joinNode11, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE5_PREDICATE, A, L)) );

        expectedQueryBuilder.addChild(joinNode12, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, A, BF0)) );
        expectedQueryBuilder.addChild(joinNode12, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, E, BF0)) );

        ConstructionNode expectedSubQuery2UnionNode = new ConstructionNodeImpl(expectedUnionNode.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI2(C))), Optional.empty());
        expectedQueryBuilder.addChild(expectedUnionNode, expectedSubQuery2UnionNode);

        InnerJoinNode joinNode21 = new InnerJoinNodeImpl(Optional.empty());
        expectedQueryBuilder.addChild(expectedSubQuery2UnionNode, joinNode21);

        InnerJoinNode joinNode22 = new InnerJoinNodeImpl(Optional.empty());
        expectedQueryBuilder.addChild(joinNode21, joinNode22);

        expectedQueryBuilder.addChild(joinNode21, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE6_PREDICATE, C, N)) );

        expectedQueryBuilder.addChild(joinNode22, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, C, BF0)) );
        expectedQueryBuilder.addChild(joinNode22, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, E, BF0)) );


        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        System.out.println("\nExpected  query: \n" +  expectedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));


    }

    private ImmutableFunctionalTerm generateURI1(VariableOrGroundTerm argument) {
        return DATA_FACTORY.getImmutableFunctionalTerm(URI_PREDICATE, URI_TEMPLATE_STR_1, argument);
    }

    private ImmutableFunctionalTerm generateURI2(VariableOrGroundTerm argument) {
        return DATA_FACTORY.getImmutableFunctionalTerm(URI_PREDICATE, URI_TEMPLATE_STR_2, argument);
    }

    private ImmutableFunctionalTerm generateCompositeURI2(ImmutableTerm argument1, ImmutableTerm argument2) {
        return DATA_FACTORY.getImmutableFunctionalTerm(URI_2PREDICATE, URI_TEMPLATE_STR_2_2, argument1, argument2);
    }

    private ImmutableFunctionalTerm generateInt(VariableOrGroundTerm argument) {
        return DATA_FACTORY.getImmutableFunctionalTerm(
                DATA_FACTORY.getDatatypeFactory().getTypePredicate(INTEGER),
                argument);
    }

    private ImmutableFunctionalTerm generateString(VariableOrGroundTerm argument) {
        return DATA_FACTORY.getImmutableFunctionalTerm(
                DATA_FACTORY.getDatatypeFactory().getTypePredicate(Predicate.COL_TYPE.STRING),
                argument);
    }


    @Test
    public void testNewConstructionNode() throws EmptyQueryException {
        //Construct unoptimized query
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA, INJECTOR);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, X, Y);

        ConstructionNode rootNode = new ConstructionNodeImpl(projectionAtom.getVariables());

        queryBuilder.init(projectionAtom, rootNode);

        UnionNode unionNode =  new UnionNodeImpl(ImmutableSet.of(X, Y));
        queryBuilder.addChild(rootNode, unionNode);

        ConstructionNode leftConstructionNode = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of( Y, generateURI1(B))), Optional.empty());
        queryBuilder.addChild(unionNode, leftConstructionNode);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(leftConstructionNode, joinNode);

        UnionNode unionNode2 =  new UnionNodeImpl(unionNode.getVariables());
        queryBuilder.addChild(unionNode, unionNode2);

        //first child of unionNode2
        ConstructionNode subQuery1UnionNode2 = new ConstructionNodeImpl(unionNode2.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(I),
                        Y, OBDAVocabulary.NULL
                        )), Optional.empty());
        queryBuilder.addChild(unionNode2, subQuery1UnionNode2);

        queryBuilder.addChild(subQuery1UnionNode2, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE5_PREDICATE, I, L)) );

        //second child of unionNode2
        ConstructionNode subQuery2UnionNode2 = new ConstructionNodeImpl(unionNode2.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI2(M),
                        Y, OBDAVocabulary.NULL
                        )), Optional.empty());
        queryBuilder.addChild(unionNode2, subQuery2UnionNode2);

        queryBuilder.addChild(subQuery2UnionNode2, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE6_PREDICATE, M, N)) );


        //first child of JoinNode
        ConstructionNode subQueryJoinNode = new ConstructionNodeImpl(ImmutableSet.of(B,X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of( X, generateURI1(A))), Optional.empty());
        queryBuilder.addChild(joinNode, subQueryJoinNode);

        queryBuilder.addChild(subQueryJoinNode, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B)) );

        //second child of JoinNode

        queryBuilder.addChild(joinNode, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, E, B)) );


        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);

        IntermediateQueryOptimizer substitutionOptimizer = new FixedPointBindingLiftOptimizer();

        IntermediateQuery optimizedQuery = substitutionOptimizer.optimize(unOptimizedQuery);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);


        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA, INJECTOR);
        ConstructionNode expectedRootNode = new ConstructionNodeImpl(projectionAtom.getVariables());

        expectedQueryBuilder.init(projectionAtom, expectedRootNode);

        UnionNode expectedUnionNode =  new UnionNodeImpl(ImmutableSet.of(X, Y));
        expectedQueryBuilder.addChild(expectedRootNode, expectedUnionNode);

        //first child of UnionNode
        ConstructionNode expSubQueryUnionNode = new ConstructionNodeImpl(ImmutableSet.of(X, Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI1(A),
                        Y, generateURI1(B)
                )), Optional.empty());
        expectedQueryBuilder.addChild(expectedUnionNode, expSubQueryUnionNode);

        InnerJoinNode expectedJoinNode = new InnerJoinNodeImpl(Optional.empty());
        expectedQueryBuilder.addChild(expSubQueryUnionNode, expectedJoinNode);

        expectedQueryBuilder.addChild(expectedJoinNode, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B)) );

        //second child of JoinNode

        expectedQueryBuilder.addChild(expectedJoinNode, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, E, B)) );

        ConstructionNode expectedRightConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X, Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        Y, OBDAVocabulary.NULL
                )), Optional.empty());
        expectedQueryBuilder.addChild(expectedUnionNode, expectedRightConstructionNode);

        UnionNode expectedUnionNode2 =  new UnionNodeImpl(ImmutableSet.of(X));
        expectedQueryBuilder.addChild(expectedRightConstructionNode, expectedUnionNode2);


        //first child of unionNode2
        ConstructionNode expSubQuery1UnionNode2 = new ConstructionNodeImpl(expectedUnionNode2.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(I))), Optional.empty());
        expectedQueryBuilder.addChild(expectedUnionNode2, expSubQuery1UnionNode2);

        expectedQueryBuilder.addChild(expSubQuery1UnionNode2, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE5_PREDICATE, I, L)) );

        //second child of unionNode2
        ConstructionNode expSubQuery2UnionNode2 = new ConstructionNodeImpl(expectedUnionNode2.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI2(M))), Optional.empty());
        expectedQueryBuilder.addChild(expectedUnionNode2, expSubQuery2UnionNode2);

        expectedQueryBuilder.addChild(expSubQuery2UnionNode2, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE6_PREDICATE, M, N)) );

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        System.out.println("\nExpected  query: \n" +  expectedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));



    }

    /**
     * Second optimization needed to lift the bindings of the first union (presence of a join with bindings as a child of the union)
     * @throws EmptyQueryException
     */

    @Test
    public void testCompositeURITemplateDoubleRun() throws EmptyQueryException {
        //Construct unoptimized query
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA, INJECTOR);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, X, Y);

        ConstructionNode rootNode = new ConstructionNodeImpl(projectionAtom.getVariables());

        queryBuilder.init(projectionAtom, rootNode);

        UnionNode unionNode =  new UnionNodeImpl(ImmutableSet.of(X,Y));
        queryBuilder.addChild(rootNode, unionNode);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(unionNode, joinNode);

        UnionNode unionNode2 =  unionNode.clone();
        queryBuilder.addChild(unionNode, unionNode2);

        //first child of unionNode2
        ConstructionNode subQuery1UnionNode2 = new ConstructionNodeImpl(unionNode2.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateCompositeURI2(I, L),
                        Y, OBDAVocabulary.NULL
                        )), Optional.empty());
        queryBuilder.addChild(unionNode2, subQuery1UnionNode2);

        queryBuilder.addChild(subQuery1UnionNode2, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE5_PREDICATE, I, L)) );

        //second child of unionNode2
        ConstructionNode subQuery2UnionNode2 = new ConstructionNodeImpl(unionNode2.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateCompositeURI2(M, N),
                        Y, OBDAVocabulary.NULL
                        )), Optional.empty());
        queryBuilder.addChild(unionNode2, subQuery2UnionNode2);

        queryBuilder.addChild(subQuery2UnionNode2, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE6_PREDICATE, M, N)) );


        //first child of JoinNode
        ConstructionNode subQueryJoinNode = new ConstructionNodeImpl(ImmutableSet.of(X, Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of( X, generateCompositeURI2(A, B), Y, generateURI1(B))), Optional.empty());
        queryBuilder.addChild(joinNode, subQueryJoinNode);

        queryBuilder.addChild(subQueryJoinNode, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B)) );

        //second child of JoinNode
        ConstructionNode subQueryJoinNode2 = new ConstructionNodeImpl(ImmutableSet.of(Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of( Y, generateURI1(F))), Optional.empty());
        queryBuilder.addChild(joinNode, subQueryJoinNode2);

        queryBuilder.addChild(subQueryJoinNode2, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, E, F)) );


        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);

        Variable AF4 = DATA_FACTORY.getVariable("af4");
        Variable BF5 = DATA_FACTORY.getVariable("bf5");

        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA, INJECTOR);
        ConstructionNode expectedRootNode = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of( X, generateCompositeURI2(AF4,BF5))), Optional.empty());


        expectedQueryBuilder.init(projectionAtom, expectedRootNode);

        UnionNode expectedUnionNode =  new UnionNodeImpl(ImmutableSet.of(BF5, AF4, Y));
        expectedQueryBuilder.addChild(expectedRootNode, expectedUnionNode);

        //first child of UnionNode
        ConstructionNode expSubQueryUnionNode = new ConstructionNodeImpl(expectedUnionNode.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of( Y, generateURI1(BF5))), Optional.empty());
        expectedQueryBuilder.addChild(expectedUnionNode, expSubQueryUnionNode);

        InnerJoinNode expectedJoinNode = new InnerJoinNodeImpl(Optional.empty());
        expectedQueryBuilder.addChild(expSubQueryUnionNode, expectedJoinNode);

        expectedQueryBuilder.addChild(expectedJoinNode, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE,AF4,BF5)) );

        //second child of JoinNode

        expectedQueryBuilder.addChild(expectedJoinNode, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, E, BF5)) );


        ConstructionNode newRightConstructionNode = new ConstructionNodeImpl(expectedUnionNode.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of( Y, OBDAVocabulary.NULL)), Optional.empty());
        expectedQueryBuilder.addChild(expectedUnionNode, newRightConstructionNode);

        UnionNode expectedUnionNode2 =  new UnionNodeImpl(ImmutableSet.of(AF4, BF5));
        expectedQueryBuilder.addChild(newRightConstructionNode, expectedUnionNode2);

        //first child of unionNode2

        expectedQueryBuilder.addChild(expectedUnionNode2, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE5_PREDICATE, AF4, BF5)) );

        //second child of unionNode2

        expectedQueryBuilder.addChild(expectedUnionNode2, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE6_PREDICATE, AF4, BF5)) );

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        System.out.println("\nExpected  query: \n" +  expectedQuery);

        IntermediateQueryOptimizer substitutionOptimizer = new FixedPointBindingLiftOptimizer();

        IntermediateQuery optimizedQuery = substitutionOptimizer.optimize(unOptimizedQuery);

        //second optimization to lift the bindings of the first union

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));



    }

    @Test
    public void testLeftJoinAndUnionLiftSubstitution() throws EmptyQueryException {

        //Construct unoptimized query
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA, INJECTOR);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_4_PREDICATE, X, Y, Z, W);
        ConstructionNode rootNode = new ConstructionNodeImpl(projectionAtom.getVariables());

        queryBuilder.init(projectionAtom, rootNode);

        //construct join
        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(rootNode, joinNode);

        //construct left side join (left join)
        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(joinNode, leftJoinNode);

        //construct right side join
        ConstructionNode rightNodeJoin = new ConstructionNodeImpl(ImmutableSet.of(W,Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        W, generateInt(H),
                        Y, generateInt(G))),
                Optional.empty());
        queryBuilder.addChild(joinNode, rightNodeJoin);

        queryBuilder.addChild(rightNodeJoin, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE4_PREDICATE, G, H)) );

        //construct left side left join (union)
        UnionNode unionNodeOnLeft = new UnionNodeImpl(ImmutableSet.of(X, Y));
        queryBuilder.addChild(leftJoinNode, unionNodeOnLeft, LEFT);

        ConstructionNode leftUnionNode = new ConstructionNodeImpl(ImmutableSet.of(X,Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI1(A),
                        Y, generateInt(B))),
                Optional.empty());
        queryBuilder.addChild(unionNodeOnLeft, leftUnionNode);

        queryBuilder.addChild(leftUnionNode, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B)) );

        ConstructionNode rightUnionNode = new ConstructionNodeImpl(ImmutableSet.of(X,Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI2(C),
                        Y, generateInt(D))),
                Optional.empty());
        queryBuilder.addChild(unionNodeOnLeft, rightUnionNode);

        queryBuilder.addChild(rightUnionNode, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, C, D)) );

        //construct right side  left join
        ConstructionNode nodeOnRight = new ConstructionNodeImpl(ImmutableSet.of(X, Z),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(E), Z, generateInt(F))), Optional.empty());
        queryBuilder.addChild(leftJoinNode, nodeOnRight, RIGHT);

        queryBuilder.addChild(nodeOnRight, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, E, F)) );



        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);


        IntermediateQueryOptimizer substitutionOptimizer = new FixedPointBindingLiftOptimizer();


        IntermediateQuery optimizedQuery = substitutionOptimizer.optimize(unOptimizedQuery);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);



        //----------------------------------------------------------------------
        // Construct expected query
        //Construct unoptimized query
        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA, INJECTOR);
        ConstructionNode expectedRootNode = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(W, generateInt(H), Y, generateInt(G), Z, generateInt(F))), Optional.empty());

        expectedQueryBuilder.init(projectionAtom, expectedRootNode);

        //construct innerjoin
        InnerJoinNode expectedJoinNode = new InnerJoinNodeImpl(Optional.empty());
        expectedQueryBuilder.addChild(expectedRootNode, expectedJoinNode);



        //construct union
        UnionNode expectedUnionNode = new UnionNodeImpl(ImmutableSet.of(G, X, F));
        expectedQueryBuilder.addChild(expectedJoinNode, expectedUnionNode);

        //construct right side join

        expectedQueryBuilder.addChild(expectedJoinNode, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE4_PREDICATE, G, H)));

        //construct union left side

        ConstructionNode expectedNodeOnLeft =new ConstructionNodeImpl(expectedUnionNode.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A))), Optional.empty());

        expectedQueryBuilder.addChild(expectedUnionNode, expectedNodeOnLeft);

        //construct left join
        LeftJoinNode expectedLeftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        expectedQueryBuilder.addChild(expectedNodeOnLeft, expectedLeftJoinNode);

        //construct left side left join
        expectedQueryBuilder.addChild(expectedLeftJoinNode, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, A, G)), LEFT);
        //construct right side left join
        expectedQueryBuilder.addChild(expectedLeftJoinNode, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, A, F)), RIGHT);

        ConstructionNode expectedNodeOnRight =new ConstructionNodeImpl(ImmutableSet.of(G, X, F),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(F, OBDAVocabulary.NULL, X, generateURI2(C))), Optional.empty());

        expectedQueryBuilder.addChild(expectedUnionNode, expectedNodeOnRight);

        expectedQueryBuilder.addChild(expectedNodeOnRight, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, C, G)));


        //build expected query
        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));


    }

    @Test
    public void testConstantNonPropagationAcrossUnions() throws EmptyQueryException {

        //Construct unoptimized query
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA, INJECTOR);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, X, Y);
        ConstructionNode rootNode = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(Y, generateURI1(B))), Optional.empty());

        queryBuilder.init(projectionAtom, rootNode);

        UnionNode topUnionNode = new UnionNodeImpl(ImmutableSet.of(X, B));
        queryBuilder.addChild(rootNode, topUnionNode);

        UnionNode leftUnionNode = topUnionNode.clone();
        queryBuilder.addChild(topUnionNode, leftUnionNode);

        ValueConstant two = DATA_FACTORY.getConstantLiteral("2");

        ConstructionNode constructionNode1 = new ConstructionNodeImpl(leftUnionNode.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI1(A),
                        B, two)),
                        Optional.empty());
        queryBuilder.addChild(leftUnionNode, constructionNode1);

        ExtensionalDataNode dataNode7 = buildExtensionalDataNode(TABLE7_ARITY_1_PREDICATE, A);
        queryBuilder.addChild(constructionNode1, dataNode7);

        ConstructionNode constructionNode2 = new ConstructionNodeImpl(leftUnionNode.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI2(C),
                        B, two)),
                Optional.empty());
        queryBuilder.addChild(leftUnionNode, constructionNode2);

        ExtensionalDataNode dataNode8 = buildExtensionalDataNode(TABLE8_ARITY_1_PREDICATE, C);
        queryBuilder.addChild(constructionNode2, dataNode8);


        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(topUnionNode, joinNode);


        ValueConstant three = DATA_FACTORY.getConstantLiteral("3");

        ConstructionNode constructionNode3 = new ConstructionNodeImpl(leftUnionNode.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI1(D),
                        B, three)),
                Optional.empty());
        queryBuilder.addChild(joinNode, constructionNode3);

        ExtensionalDataNode dataNode9 = buildExtensionalDataNode(TABLE9_ARITY_1_PREDICATE, D);
        queryBuilder.addChild(constructionNode3, dataNode9);

        ExtensionalDataNode dataNode10 = buildExtensionalDataNode(TABLE7_ARITY_1_PREDICATE, E);
        queryBuilder.addChild(joinNode, dataNode10);

        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);


        IntermediateQueryOptimizer substitutionOptimizer = new FixedPointBindingLiftOptimizer();



        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA, INJECTOR);
        expectedQueryBuilder.init(projectionAtom, rootNode);
        expectedQueryBuilder.addChild(rootNode, topUnionNode);

        ConstructionNode newLeftConstructionNode = new ConstructionNodeImpl(leftUnionNode.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        B, two)),
                Optional.empty());
        expectedQueryBuilder.addChild(topUnionNode, newLeftConstructionNode);
        UnionNode newLeftUnionNode = new UnionNodeImpl(ImmutableSet.of(X));
        expectedQueryBuilder.addChild(newLeftConstructionNode, newLeftUnionNode);

        ConstructionNode newConstructionNode1 = new ConstructionNodeImpl(newLeftUnionNode.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI1(A))),
                Optional.empty());
        expectedQueryBuilder.addChild(newLeftUnionNode, newConstructionNode1);
        expectedQueryBuilder.addChild(newConstructionNode1, dataNode7);

        ConstructionNode newConstructionNode2 = new ConstructionNodeImpl(newLeftUnionNode.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI2(C))),
                Optional.empty());
        expectedQueryBuilder.addChild(newLeftUnionNode, newConstructionNode2);
        expectedQueryBuilder.addChild(newConstructionNode2, dataNode8);


        expectedQueryBuilder.addChild(topUnionNode, constructionNode3);
        expectedQueryBuilder.addChild(constructionNode3, joinNode);
        expectedQueryBuilder.addChild(joinNode, dataNode9);
        expectedQueryBuilder.addChild(joinNode, dataNode10);

        //build expected query
        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\n Expected query: \n" +  expectedQuery);


        IntermediateQuery optimizedQuery = substitutionOptimizer.optimize(unOptimizedQuery);
        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));

    }

    @Test
    public void testConstantNonPropagationAcrossUnions2() throws EmptyQueryException {

        //Construct unoptimized query
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA, INJECTOR);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, X, Y);
        ConstructionNode rootNode = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(Y, generateURI1(B))), Optional.empty());

        queryBuilder.init(projectionAtom, rootNode);

        UnionNode topUnionNode = new UnionNodeImpl(ImmutableSet.of(X, B));
        queryBuilder.addChild(rootNode, topUnionNode);

        UnionNode leftUnionNode = topUnionNode.clone();
        queryBuilder.addChild(topUnionNode, leftUnionNode);

        ValueConstant two = DATA_FACTORY.getConstantLiteral("2");

        ConstructionNode constructionNode1 = new ConstructionNodeImpl(leftUnionNode.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI1(A),
                        B, two)),
                Optional.empty());
        queryBuilder.addChild(leftUnionNode, constructionNode1);

        ExtensionalDataNode dataNode7 = buildExtensionalDataNode(TABLE7_ARITY_1_PREDICATE, A);
        queryBuilder.addChild(constructionNode1, dataNode7);

        ConstructionNode constructionNode2 = new ConstructionNodeImpl(leftUnionNode.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI2(C),
                        B, two)),
                Optional.empty());
        queryBuilder.addChild(leftUnionNode, constructionNode2);

        ExtensionalDataNode dataNode8 = buildExtensionalDataNode(TABLE8_ARITY_1_PREDICATE, C);
        queryBuilder.addChild(constructionNode2, dataNode8);


        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(topUnionNode, joinNode);

        ConstructionNode constructionNode3 = new ConstructionNodeImpl(leftUnionNode.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI1(D))),
                Optional.empty());
        queryBuilder.addChild(joinNode, constructionNode3);

        ExtensionalDataNode dataNode9 = buildExtensionalDataNode(TABLE9_ARITY_1_PREDICATE, D);
        queryBuilder.addChild(constructionNode3, dataNode9);

        ExtensionalDataNode dataNode10 = buildExtensionalDataNode(TABLE7_ARITY_1_PREDICATE, B);
        queryBuilder.addChild(joinNode, dataNode10);

        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);


        IntermediateQueryOptimizer substitutionOptimizer = new FixedPointBindingLiftOptimizer();



        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA, INJECTOR);
        expectedQueryBuilder.init(projectionAtom, rootNode);
        expectedQueryBuilder.addChild(rootNode, topUnionNode);

        ConstructionNode newLeftConstructionNode = new ConstructionNodeImpl(leftUnionNode.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        B, two)),
                Optional.empty());
        expectedQueryBuilder.addChild(topUnionNode, newLeftConstructionNode);
        UnionNode newLeftUnionNode = new UnionNodeImpl(ImmutableSet.of(X));
        expectedQueryBuilder.addChild(newLeftConstructionNode, newLeftUnionNode);

        ConstructionNode newConstructionNode1 = new ConstructionNodeImpl(newLeftUnionNode.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI1(A))),
                Optional.empty());
        expectedQueryBuilder.addChild(newLeftUnionNode, newConstructionNode1);
        expectedQueryBuilder.addChild(newConstructionNode1, dataNode7);

        ConstructionNode newConstructionNode2 = new ConstructionNodeImpl(newLeftUnionNode.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI2(C))),
                Optional.empty());
        expectedQueryBuilder.addChild(newLeftUnionNode, newConstructionNode2);
        expectedQueryBuilder.addChild(newConstructionNode2, dataNode8);


        expectedQueryBuilder.addChild(topUnionNode, constructionNode3);
        expectedQueryBuilder.addChild(constructionNode3, joinNode);
        expectedQueryBuilder.addChild(joinNode, dataNode9);
        expectedQueryBuilder.addChild(joinNode, dataNode10);

        //build expected query
        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\n Expected query: \n" +  expectedQuery);


        IntermediateQuery optimizedQuery = substitutionOptimizer.optimize(unOptimizedQuery);
        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));

    }

    /**
     * Currently runs the optimizer twice
     */
    @Test
    public void testTrueNode() throws EmptyQueryException {
        //Construct unoptimized query
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA, INJECTOR);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, X, Y);
        ConstructionNode rootNode = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());
        queryBuilder.init(projectionAtom, rootNode);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.of(DATA_FACTORY.getImmutableExpression(EQ,
                buildSparqlDatatype(X), buildSparqlDatatype(Y))));
        queryBuilder.addChild(rootNode,joinNode);

        UnionNode unionNode =  new UnionNodeImpl(ImmutableSet.of(X));
        queryBuilder.addChild(joinNode, unionNode);

        ConstructionNode leftChildUnion = new ConstructionNodeImpl(unionNode.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateInt(A) )),Optional.empty());

        queryBuilder.addChild(unionNode, leftChildUnion);
        queryBuilder.addChild(leftChildUnion, DATA_NODE_1 );

        ConstructionNode rightChildUnion = new ConstructionNodeImpl(unionNode.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateString(C) )),Optional.empty());

        queryBuilder.addChild(unionNode, rightChildUnion);

        queryBuilder.addChild(rightChildUnion, DATA_NODE_3 );

        ConstructionNode otherNode = new ConstructionNodeImpl(ImmutableSet.of(Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(Y, generateInt(DATA_FACTORY.getConstantLiteral("2", INTEGER)) )),Optional.empty());

        queryBuilder.addChild(joinNode, otherNode);

        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);

        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA, INJECTOR);
        ConstructionNode expectedRootNode = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateInt(A), Y, generateInt(DATA_FACTORY.getConstantLiteral("2", INTEGER)))), Optional.empty());
        expectedQueryBuilder.init(projectionAtom, expectedRootNode);

        expectedQueryBuilder.addChild(expectedRootNode, DATA_NODE_1);
        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\nExpected query: \n" +  expectedQuery);

        IntermediateQueryOptimizer substitutionOptimizer = new FixedPointBindingLiftOptimizer();

        /**
         * TODO: remove this double call
         */
        IntermediateQuery optimizedQuery = substitutionOptimizer.optimize(
                substitutionOptimizer.optimize(unOptimizedQuery));

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }

    /**
     * Second optimization needed to lift the bindings of second construction node generated by the presence of a union
     * @throws EmptyQueryException
     */
    @Test
    public void testJoinAndNotMatchingDatatypesDoubleRun() throws EmptyQueryException {
        //Construct unoptimized query
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA, INJECTOR);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, X, Y);
        ConstructionNode rootNode = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());
        queryBuilder.init(projectionAtom, rootNode);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.of(DATA_FACTORY.getImmutableExpression(EQ,
                buildSparqlDatatype(X), buildSparqlDatatype(Y))));
        queryBuilder.addChild(rootNode,joinNode);

        UnionNode unionNode =  new UnionNodeImpl(ImmutableSet.of(X));
        queryBuilder.addChild(joinNode, unionNode);

        ConstructionNode leftChildUnion = new ConstructionNodeImpl(unionNode.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateInt(A) )),Optional.empty());

        queryBuilder.addChild(unionNode, leftChildUnion);
        queryBuilder.addChild(leftChildUnion, DATA_NODE_1 );

        ConstructionNode rightChildUnion = new ConstructionNodeImpl(unionNode.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateString(C) )),Optional.empty());

        queryBuilder.addChild(unionNode, rightChildUnion);

        queryBuilder.addChild(rightChildUnion, DATA_NODE_3 );

        ConstructionNode otherNode = new ConstructionNodeImpl(ImmutableSet.of(Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(Y, generateInt(F)) ),Optional.empty());

        queryBuilder.addChild(joinNode, otherNode);

        queryBuilder.addChild(otherNode, DATA_NODE_6 );

        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);

        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA, INJECTOR);
        ConstructionNode expectedRootNode = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateInt(A), Y, generateInt(F))), Optional.empty());
        expectedQueryBuilder.init(projectionAtom, expectedRootNode);

        InnerJoinNode expectedJoinNode = new InnerJoinNodeImpl(Optional.empty());
        expectedQueryBuilder.addChild(expectedRootNode, expectedJoinNode);

        expectedQueryBuilder.addChild(expectedJoinNode, DATA_NODE_1);
        expectedQueryBuilder.addChild(expectedJoinNode, DATA_NODE_6);
        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\nExpected query: \n" +  expectedQuery);

        IntermediateQueryOptimizer substitutionOptimizer = new FixedPointBindingLiftOptimizer();

        IntermediateQuery optimizedQuery = substitutionOptimizer.optimize(unOptimizedQuery);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }


    @Test
    @Ignore
    public void testDatatypeExpressionEvaluator() throws EmptyQueryException {
        //Construct unoptimized query
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA, INJECTOR);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, X, Y);
        ConstructionNode rootNode = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());
        queryBuilder.init(projectionAtom, rootNode);

        InnerJoinNode jn = new InnerJoinNodeImpl(Optional.of(DATA_FACTORY.getImmutableExpression(EQ,
                buildSparqlDatatype(X), buildSparqlDatatype(Y))));
        queryBuilder.addChild(rootNode, jn);
        ConstructionNode leftCn = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateInt(A))), Optional.empty());
        queryBuilder.addChild(jn, leftCn);
        ConstructionNode rightCn = new ConstructionNodeImpl(ImmutableSet.of(Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(Y, generateInt(B))), Optional.empty());
        queryBuilder.addChild(jn, rightCn);
        queryBuilder.addChild(leftCn, DATA_NODE_8);
        queryBuilder.addChild(rightCn, DATA_NODE_9);

        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" + unOptimizedQuery);

        // Expected query
        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA, INJECTOR);
        ImmutableSubstitution expectedRootNodeSubstitution = new ImmutableSubstitutionImpl<>(ImmutableMap.of(X,
                generateInt(A), Y, generateInt(B)));
        ConstructionNode expectedRootNode = new ConstructionNodeImpl(ImmutableSet.of(X, Y), expectedRootNodeSubstitution,
                Optional.empty());
        expectedQueryBuilder.init(projectionAtom, expectedRootNode);
        InnerJoinNode jn2 = new InnerJoinNodeImpl(Optional.empty());
        expectedQueryBuilder.addChild(expectedRootNode, jn2);
        expectedQueryBuilder.addChild(jn2, DATA_NODE_8);
        expectedQueryBuilder.addChild(jn2, DATA_NODE_9);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\nExpected query: \n" + expectedQuery);

        // Optimize and compare
        IntermediateQueryOptimizer substitutionOptimizer = new FixedPointBindingLiftOptimizer();
        IntermediateQuery optimizedQuery = substitutionOptimizer.optimize(unOptimizedQuery);
        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));

    }

    @Test(expected = EmptyQueryException.class)
    public void testEmptySubstitutionToBeLifted() throws EmptyQueryException {

        //Construct unoptimized query
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA, INJECTOR);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, X, Y);
        ConstructionNode rootNode = new ConstructionNodeImpl(projectionAtom.getVariables());

        queryBuilder.init(projectionAtom, rootNode);

        //construct
        joinNode = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(rootNode, joinNode);

        //construct left side join
        LeftJoinNode leftJoinNode =  new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(joinNode, leftJoinNode);

        ConstructionNode leftNode = new ConstructionNodeImpl(ImmutableSet.of(Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        Y, generateURI1(B))),
                Optional.empty());

        queryBuilder.addChild(leftJoinNode, leftNode, LEFT);

        queryBuilder.addChild(leftNode, DATA_NODE_9);

        ConstructionNode rightNode = new ConstructionNodeImpl(ImmutableSet.of(X,Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI2(C),
                        Y, generateURI1(D))),
                Optional.empty());

        queryBuilder.addChild(leftJoinNode, rightNode, RIGHT);

        queryBuilder.addChild(rightNode, DATA_NODE_3);


        //construct right side join
        ConstructionNode node1 = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A))), Optional.empty());
        queryBuilder.addChild(joinNode, node1);

        queryBuilder.addChild(node1, DATA_NODE_8);

        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);


        IntermediateQueryOptimizer substitutionOptimizer = new FixedPointBindingLiftOptimizer();

        substitutionOptimizer.optimize(unOptimizedQuery);



    }

    @Test
    public void testUnionRemoval() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA, INJECTOR);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, X, Y);
        ConstructionNode rootNode = new ConstructionNodeImpl(projectionAtom.getVariables());

        queryBuilder.init(projectionAtom, rootNode);

        UnionNode unionNode = new UnionNodeImpl(projectionAtom.getVariables());
        queryBuilder.addChild(rootNode, unionNode);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(unionNode, joinNode);

        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI1(A),
                        Y, generateURI2(B))), Optional.empty());
        queryBuilder.addChild(joinNode, constructionNode1);
        queryBuilder.addChild(constructionNode1, DATA_NODE_1);

        ConstructionNode constructionNode2 = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI2(C),
                        Y, generateURI2(D))), Optional.empty());
        queryBuilder.addChild(joinNode, constructionNode2);
        queryBuilder.addChild(constructionNode2, DATA_NODE_3);

        ConstructionNode constructionNode3 = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI1(E),
                        Y, generateURI2(F))), Optional.empty());
        queryBuilder.addChild(unionNode, constructionNode3);
        queryBuilder.addChild(constructionNode3, DATA_NODE_6);

        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);


        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA, INJECTOR);
        expectedQueryBuilder.init(projectionAtom, constructionNode3);
        expectedQueryBuilder.addChild(constructionNode3, DATA_NODE_6);
        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        System.out.println("\nExpected query: \n" +  expectedQuery);


        IntermediateQueryOptimizer substitutionOptimizer = new FixedPointBindingLiftOptimizer();
        IntermediateQuery optimizedQuery = substitutionOptimizer.optimize(unOptimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }

    @Test
    public void testUnionWithJoin() throws EmptyQueryException {
        /**
         * Original Query
         */

        AtomPredicate TABLE_1 = new AtomPredicateImpl("table1", 2);
        AtomPredicate TABLE_2 = new AtomPredicateImpl("table2", 2);
        AtomPredicate TABLE_3 = new AtomPredicateImpl("table3", 2);
        AtomPredicate TABLE_4 = new AtomPredicateImpl("table4", 2);
        AtomPredicate TABLE_5 = new AtomPredicateImpl("table5", 3);

        DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
                DATA_FACTORY.getDistinctVariableOnlyDataAtom(
                        ANS1_ARITY_3_PREDICATE, ImmutableList.of(X,Y, Z));

        IntermediateQueryBuilder originalBuilder = new DefaultIntermediateQueryBuilder(METADATA, INJECTOR);

        ConstructionNode rootConstructionNode = new ConstructionNodeImpl(ROOT_CONSTRUCTION_NODE_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());

        ConstructionNode emptyConstructionNode = new ConstructionNodeImpl(ROOT_CONSTRUCTION_NODE_ATOM.getVariables());
        ConstructionNode emptyConstructionNode2 = emptyConstructionNode.clone();

        UnionNode unionNode1  = new UnionNodeImpl(ImmutableSet.of(X, Y, Z));
        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        UnionNode unionNode21 = new UnionNodeImpl(ImmutableSet.of(X, Y));
        UnionNode unionNode22  = new UnionNodeImpl(ImmutableSet.of(X, C));
        InnerJoinNode joinNode1 = new InnerJoinNodeImpl(Optional.empty());

        ConstructionNode constructionNode21 = new ConstructionNodeImpl(unionNode21.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI1(A),
                        Y, generateURI2(B))), Optional.empty());

        ConstructionNode constructionNode22 = new ConstructionNodeImpl(unionNode22.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI1(A))), Optional.empty());

        ConstructionNode constructionNode22URI2 = new ConstructionNodeImpl(unionNode22.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI2(A))), Optional.empty());

        ConstructionNode constructionNode21URI2 = new ConstructionNodeImpl(unionNode21.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI2(A), Y, generateURI2(B))), Optional.empty());

        ConstructionNode constructionNode21URI1XY = constructionNode21.clone();

        ConstructionNode constructionNode22Z = new ConstructionNodeImpl(ImmutableSet.of(X, Z),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        Z, generateURI1(C))), Optional.empty());

        ConstructionNode constructionNodeOverJoin2 = constructionNode22.clone();
        ConstructionNode constructionNodeOverJoin1 = new ConstructionNodeImpl(unionNode21.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI1(A), Z, generateURI1(C))), Optional.empty());

        ExtensionalDataNode table1DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, A, B));
        ExtensionalDataNode table2DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_2, A, B));
        ExtensionalDataNode table3DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_3, A, B));
        ExtensionalDataNode table4DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_4, A, C));
        ExtensionalDataNode table5DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_5, A, B, C));

        originalBuilder.init(ROOT_CONSTRUCTION_NODE_ATOM, rootConstructionNode);
        originalBuilder.addChild(rootConstructionNode, unionNode1);
        originalBuilder.addChild(unionNode1, emptyConstructionNode);
        originalBuilder.addChild(emptyConstructionNode, joinNode);
        originalBuilder.addChild(unionNode1, emptyConstructionNode2);
        originalBuilder.addChild(emptyConstructionNode2, table5DataNode);

        originalBuilder.addChild(joinNode, unionNode21);
        originalBuilder.addChild(joinNode, constructionNode22Z);
        originalBuilder.addChild(constructionNode22Z, unionNode22);
        originalBuilder.addChild(joinNode,constructionNodeOverJoin1 );
        originalBuilder.addChild(constructionNodeOverJoin1, table4DataNode);
        originalBuilder.addChild(unionNode21, constructionNode21);
        originalBuilder.addChild(constructionNode21, table1DataNode);
        originalBuilder.addChild(unionNode21, constructionNode21URI2);
        originalBuilder.addChild(constructionNode21URI2, table2DataNode);
        originalBuilder.addChild(unionNode21, constructionNode21URI1XY);
        originalBuilder.addChild(constructionNode21URI1XY, table3DataNode);
        originalBuilder.addChild(unionNode22, constructionNode22);
        originalBuilder.addChild(constructionNode22, table4DataNode.clone());
        originalBuilder.addChild(unionNode22, constructionNode22URI2);
        originalBuilder.addChild(constructionNode22URI2, table5DataNode.clone());
        originalBuilder.addChild(unionNode22,constructionNodeOverJoin2 );
        originalBuilder.addChild(constructionNodeOverJoin2, joinNode1);
        originalBuilder.addChild(joinNode1, table4DataNode.clone());
        originalBuilder.addChild(joinNode1, table4DataNode.clone());



        IntermediateQuery originalQuery = originalBuilder.build();

        System.out.println("\n Original query: \n" +  originalQuery);

        IntermediateQueryOptimizer substitutionOptimizer = new FixedPointBindingLiftOptimizer();
        IntermediateQuery optimizedQuery = substitutionOptimizer.optimize(originalQuery);

        System.out.println("\n Optimized query: \n" +  optimizedQuery);

        /**
         * Expected Query
         */
        IntermediateQueryBuilder expectedBuilder = new DefaultIntermediateQueryBuilder(METADATA, INJECTOR);

        UnionNode unionNode3 = new UnionNodeImpl(ImmutableSet.of(X, Y, Z));
        UnionNode unionNode4 = new UnionNodeImpl(ImmutableSet.of(A, B));
        UnionNode unionNode5 = new UnionNodeImpl(ImmutableSet.of(A,C));

        ConstructionNode constructionNode1 = new ConstructionNodeImpl(ImmutableSet.of(X, Y, Z),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI1(A), Y, generateURI2(B), Z, generateURI1(C))), Optional.empty());
        ConstructionNode constructionNode2 = emptyConstructionNode;
        InnerJoinNode joinNode2 = new InnerJoinNodeImpl(Optional.empty());
        InnerJoinNode joinNode3 = new InnerJoinNodeImpl(Optional.empty());


        expectedBuilder.init(ROOT_CONSTRUCTION_NODE_ATOM, rootConstructionNode);
        expectedBuilder.addChild(rootConstructionNode, unionNode3);
        expectedBuilder.addChild(unionNode3, constructionNode1);
        expectedBuilder.addChild(unionNode3, constructionNode2);
        expectedBuilder.addChild(constructionNode1, joinNode2);
        expectedBuilder.addChild(joinNode2, unionNode4);
        expectedBuilder.addChild(unionNode4, table1DataNode.clone());
        expectedBuilder.addChild(unionNode4, table3DataNode.clone());
        expectedBuilder.addChild(joinNode2, unionNode5);
        expectedBuilder.addChild(unionNode5, table4DataNode.clone());
        expectedBuilder.addChild(unionNode5, joinNode3);
        expectedBuilder.addChild(joinNode3, table4DataNode.clone());
        expectedBuilder.addChild(joinNode3, table4DataNode.clone());
        expectedBuilder.addChild(joinNode2, table4DataNode.clone());
        expectedBuilder.addChild(constructionNode2, table5DataNode.clone());

        IntermediateQuery expectedQuery = expectedBuilder.build();

        System.out.println("\n Expected query: \n" +  expectedQuery);

        Assert.assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));


    }

    private static ExtensionalDataNode buildExtensionalDataNode(AtomPredicate predicate, VariableOrGroundTerm ... arguments) {
        return new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(predicate, arguments));
    }

    private static ImmutableFunctionalTerm buildSparqlDatatype(ImmutableTerm argument){
        return DATA_FACTORY.getImmutableFunctionalTerm(SPARQL_DATATYPE, argument);
    }
}
