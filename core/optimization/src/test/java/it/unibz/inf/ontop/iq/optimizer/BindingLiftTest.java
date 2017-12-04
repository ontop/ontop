package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.term.functionsymbol.URITemplatePredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.equivalence.IQSyntacticEquivalenceChecker;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.EQ;
import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.SPARQL_DATATYPE;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.RIGHT;
import static junit.framework.TestCase.assertTrue;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;

/**
 * Test the top down substitution lift optimizer
 */
public class BindingLiftTest {


    private final AtomPredicate TABLE1_PREDICATE = ATOM_FACTORY.getAtomPredicate("table1", 2);
    private final AtomPredicate TABLE2_PREDICATE = ATOM_FACTORY.getAtomPredicate("table2", 2);
    private final AtomPredicate TABLE3_PREDICATE = ATOM_FACTORY.getAtomPredicate("table3", 2);
    private final AtomPredicate TABLE4_PREDICATE = ATOM_FACTORY.getAtomPredicate("table4", 2);
    private final AtomPredicate TABLE5_PREDICATE = ATOM_FACTORY.getAtomPredicate("table5", 2);
    private final AtomPredicate TABLE6_PREDICATE = ATOM_FACTORY.getAtomPredicate("table6", 2);
    private final AtomPredicate TABLE7_ARITY_1_PREDICATE = ATOM_FACTORY.getAtomPredicate("table7", 1);
    private final AtomPredicate TABLE8_ARITY_1_PREDICATE = ATOM_FACTORY.getAtomPredicate("table8", 1);
    private final AtomPredicate TABLE9_ARITY_1_PREDICATE = ATOM_FACTORY.getAtomPredicate("table9", 1);

    private final AtomPredicate ANS1_ARITY_1_PREDICATE = ATOM_FACTORY.getAtomPredicate("ans1", 1);
    private final AtomPredicate ANS1_ARITY_2_PREDICATE = ATOM_FACTORY.getAtomPredicate("ans1", 2);
    private final AtomPredicate ANS1_ARITY_3_PREDICATE = ATOM_FACTORY.getAtomPredicate("ans1", 3);
    private final AtomPredicate ANS1_ARITY_4_PREDICATE = ATOM_FACTORY.getAtomPredicate("ans1", 4);

    private final Variable X = TERM_FACTORY.getVariable("x");
    private final Variable Y = TERM_FACTORY.getVariable("y");
    private final Variable W = TERM_FACTORY.getVariable("w");
    private final Variable Z = TERM_FACTORY.getVariable("z");
    private final Variable A = TERM_FACTORY.getVariable("a");
    private final Variable AF0 = TERM_FACTORY.getVariable("af0");
    private final Variable B = TERM_FACTORY.getVariable("b");
    private final Variable C = TERM_FACTORY.getVariable("c");
    private final Variable D = TERM_FACTORY.getVariable("d");
    private final Variable E = TERM_FACTORY.getVariable("e");
    private final Variable F = TERM_FACTORY.getVariable("f");
    private final Variable G = TERM_FACTORY.getVariable("g");
    private final Variable H = TERM_FACTORY.getVariable("h");
    private final Variable I = TERM_FACTORY.getVariable("i");
    private final Variable L = TERM_FACTORY.getVariable("l");
    private final Variable M = TERM_FACTORY.getVariable("m");
    private final Variable N = TERM_FACTORY.getVariable("n");

    private final Constant ONE = TERM_FACTORY.getConstantLiteral("1", XSD.INTEGER);

    private URITemplatePredicate URI_PREDICATE =  TERM_FACTORY.getURITemplatePredicate(2);
    private URITemplatePredicate URI_2PREDICATE =  TERM_FACTORY.getURITemplatePredicate(3);

    private Constant URI_TEMPLATE_STR_1 =  TERM_FACTORY.getConstantLiteral("http://example.org/ds1/{}");
    private Constant URI_TEMPLATE_STR_2 =  TERM_FACTORY.getConstantLiteral("http://example.org/ds2/{}");
    private Constant URI_TEMPLATE_STR_2_2 =  TERM_FACTORY.getConstantLiteral("http://example.org/ds2/{}/{}");

    private ExtensionalDataNode EXPECTED_DATA_NODE_1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, A, C));
    private ExtensionalDataNode EXPECTED_DATA_NODE_3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, C, D));
    private ExtensionalDataNode EXPECTED_DATA_NODE_4 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B));
    private ExtensionalDataNode EXPECTED_DATA_NODE_5 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, B, D));
    private ExtensionalDataNode EXPECTED_DATA_NODE_6 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, A, TERM_FACTORY.getVariable("ff0")));

    private ExtensionalDataNode DATA_NODE_1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B));
    private ExtensionalDataNode DATA_NODE_2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, A, E));
    private ExtensionalDataNode DATA_NODE_3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, C, D));
    private ExtensionalDataNode DATA_NODE_4 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B));
    private ExtensionalDataNode DATA_NODE_5 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, C, D));
    private ExtensionalDataNode DATA_NODE_6 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, E, F));
    private ExtensionalDataNode DATA_NODE_7 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE4_PREDICATE, G, H));
    private ExtensionalDataNode DATA_NODE_8 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE7_ARITY_1_PREDICATE, A));
    private ExtensionalDataNode DATA_NODE_9 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE8_ARITY_1_PREDICATE, B));

    private final ImmutableExpression EXPRESSIONGT = TERM_FACTORY.getImmutableExpression(
            ExpressionOperation.GT, Z, Y);

    public BindingLiftTest() {
    }

    @Test
    public void testSimpleSubstitution() throws EmptyQueryException {
        testSimpleSubstitution(false);
    }

    @Test
    public void testSimpleSubstitutionWithTopConstructionNode() throws EmptyQueryException {
        testSimpleSubstitution(true);
    }

    private void testSimpleSubstitution(boolean withTopConstructionNode) throws EmptyQueryException {
        //Construct unoptimized query
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, X, Y, Z);

        QueryNode joinNode = IQ_FACTORY.createInnerJoinNode(EXPRESSIONGT);

        if (withTopConstructionNode) {
            ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
            queryBuilder.init(projectionAtom, rootNode);
            queryBuilder.addChild(rootNode, joinNode);
        }
        else {
            queryBuilder.init(projectionAtom, joinNode);
        }

        //construct left side join
        ConstructionNode leftNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Z),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(Z, generateInt(A))));
        queryBuilder.addChild(joinNode, leftNode);

        //construct union
        ImmutableSet<Variable> subQueryProjectedVariables = ImmutableSet.of(X,A);
        QueryNode unionNode = IQ_FACTORY.createUnionNode(subQueryProjectedVariables);

        queryBuilder.addChild(leftNode, unionNode);

        //construct node1 union
        ConstructionNode subQueryConstructionNode1 = IQ_FACTORY.createConstructionNode(subQueryProjectedVariables,
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(B))));

        queryBuilder.addChild(unionNode, subQueryConstructionNode1);
        queryBuilder.addChild(subQueryConstructionNode1, DATA_NODE_1);

        //construct node2 union
        ConstructionNode subQueryConstructionNode2 = IQ_FACTORY.createConstructionNode(subQueryProjectedVariables,
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI2(E))));
        queryBuilder.addChild(unionNode, subQueryConstructionNode2);

        queryBuilder.addChild(subQueryConstructionNode2, DATA_NODE_2);

        //construct right side join
        ConstructionNode rightNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateURI1(C),
                        Y, generateInt(D))));

        queryBuilder.addChild(joinNode, rightNode);

        queryBuilder.addChild(rightNode, DATA_NODE_3);

        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);

        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(unOptimizedQuery);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);



        //----------------------------------------------------------------------
        // Construct expected query
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);


        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(C), Y, generateInt(D),
                        Z, generateInt(A))));

        expectedQueryBuilder.init(projectionAtom, expectedRootNode);

        //construct expected innerjoin
        ImmutableExpression expectedEspressionGT = TERM_FACTORY.getImmutableExpression(ExpressionOperation.GT, generateInt(A), generateInt(D));
        InnerJoinNode expectedJoinNode = IQ_FACTORY.createInnerJoinNode(expectedEspressionGT);
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
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, X, W, Z);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        queryBuilder.init(projectionAtom, rootNode);

        //construct innerjoin
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(rootNode, leftJoinNode);

        //construct left side left join (join)
        InnerJoinNode joinNodeOnLeft = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(leftJoinNode, joinNodeOnLeft, LEFT);

        //construct left side join
        ConstructionNode leftNodeJoin = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, W),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A), W, generateString(B))));
        queryBuilder.addChild(joinNodeOnLeft, leftNodeJoin);

        queryBuilder.addChild(leftNodeJoin, DATA_NODE_4 );

        //construct right side join
        ConstructionNode rightNodeJoin = IQ_FACTORY.createConstructionNode(ImmutableSet.of(W,Z),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        W, generateString(C),
                        Z, generateInt(D))));
        queryBuilder.addChild(joinNodeOnLeft, rightNodeJoin);

        queryBuilder.addChild(rightNodeJoin, DATA_NODE_5 );

        //construct right side left join (union)
        ImmutableSet<Variable> subQueryProjectedVariables = ImmutableSet.of(X,Y);
        UnionNode unionNodeOnRight = IQ_FACTORY.createUnionNode(subQueryProjectedVariables);

        queryBuilder.addChild(leftJoinNode, unionNodeOnRight, RIGHT);

        //construct node1 union
        ConstructionNode subQueryConstructionNode1 = IQ_FACTORY.createConstructionNode(subQueryProjectedVariables,
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(E), Y, generateInt((F)))));

        queryBuilder.addChild(unionNodeOnRight, subQueryConstructionNode1);

        queryBuilder.addChild(subQueryConstructionNode1, DATA_NODE_6);

        //construct node2 union
        ConstructionNode subQueryConstructionNode2 = IQ_FACTORY.createConstructionNode(subQueryProjectedVariables,
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI2(G), Y , generateInt(H))));
        queryBuilder.addChild(unionNodeOnRight, subQueryConstructionNode2);

        queryBuilder.addChild(subQueryConstructionNode2, DATA_NODE_7);



        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);

        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(unOptimizedQuery);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);



        //----------------------------------------------------------------------
        // Construct expected query
        //Construct unoptimized query
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of( W, generateString(B), X, generateURI1(A), Z, generateInt(D))));

        expectedQueryBuilder.init(projectionAtom, expectedRootNode);

        //construct innerjoin
        LeftJoinNode expectedleftJoinNode = IQ_FACTORY.createLeftJoinNode();
        expectedQueryBuilder.addChild(expectedRootNode, expectedleftJoinNode);

        //construct left side left join (join)
        InnerJoinNode expectedJoinNodeOnLeft = IQ_FACTORY.createInnerJoinNode();
        expectedQueryBuilder.addChild(expectedleftJoinNode, expectedJoinNodeOnLeft, LEFT);

        expectedQueryBuilder.addChild(expectedJoinNodeOnLeft, EXPECTED_DATA_NODE_4 );

        expectedQueryBuilder.addChild(expectedJoinNodeOnLeft, EXPECTED_DATA_NODE_5);


        //construct right side left join (union)
        ConstructionNode newRightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, Y),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateInt(F)));
        expectedQueryBuilder.addChild(expectedleftJoinNode, newRightConstructionNode, RIGHT);
        expectedQueryBuilder.addChild(newRightConstructionNode,
                IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, A, F)));


        //build expected query
        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));


    }

    @Test
    public void testUnionSubstitution() throws EmptyQueryException {
        //Construct unoptimized query
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, X, Y);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.init(projectionAtom, joinNode);

        //left side first join
        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(joinNode, joinNode2);

        //left side second join (unionNode 2)

        UnionNode unionNode2 =  IQ_FACTORY.createUnionNode(projectionAtom.getVariables());
        queryBuilder.addChild(joinNode2, unionNode2);

        //first child of unionNode2
        ConstructionNode subQuery1UnionNode2 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A), Y, generateURI1(B))));
        queryBuilder.addChild(unionNode2, subQuery1UnionNode2);

        queryBuilder.addChild(subQuery1UnionNode2, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B)) );

        //second child of unionNode2
        ConstructionNode subQuery2UnionNode2 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI2(C), Y, generateURI1(D))));
        queryBuilder.addChild(unionNode2, subQuery2UnionNode2);

        queryBuilder.addChild(subQuery2UnionNode2, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, C, D)) );

        //right side second join (unionNode 3)
        UnionNode unionNode3 =  IQ_FACTORY.createUnionNode(ImmutableSet.of(Y));
        queryBuilder.addChild(joinNode2, unionNode3);

        //first child of unionNode3
        ConstructionNode subQuery1UnionNode3 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of( Y, generateURI1(F))));
        queryBuilder.addChild(unionNode3, subQuery1UnionNode3);

        queryBuilder.addChild(subQuery1UnionNode3, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, E, F)) );

        //second child of unionNode3
        ConstructionNode subQuery2UnionNode3 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(Y,  generateURI2(H))));
        queryBuilder.addChild(unionNode3, subQuery2UnionNode3);

        queryBuilder.addChild(subQuery2UnionNode3, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE4_PREDICATE, G, H)) );

        //right side first join
        UnionNode unionNode1 =  IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        queryBuilder.addChild(joinNode, unionNode1);

        //first child of unionNode1
        ConstructionNode subQuery1UnionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(I))));
        queryBuilder.addChild(unionNode1, subQuery1UnionNode1);

        queryBuilder.addChild(subQuery1UnionNode1, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE5_PREDICATE, I, L)) );

        //second child of unionNode1
        ConstructionNode subQuery2UnionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI2(M))));
        queryBuilder.addChild(unionNode1, subQuery2UnionNode1);

        queryBuilder.addChild(subQuery2UnionNode1, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE6_PREDICATE, M, N)) );

        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);

        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(unOptimizedQuery);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        //----------------------------------------------------------------------
        //Construct expected query
        Variable BF0 = TERM_FACTORY.getVariable("bf0");
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(Y, generateURI1(BF0))));

        expectedQueryBuilder.init(projectionAtom, expectedRootNode);

        //constract union Node
        UnionNode expectedUnionNode =  IQ_FACTORY.createUnionNode(ImmutableSet.of(BF0, X, E));

        expectedQueryBuilder.addChild(expectedRootNode, expectedUnionNode );

        ConstructionNode expectedSubQuery1UnionNode = IQ_FACTORY.createConstructionNode(expectedUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A))));
        expectedQueryBuilder.addChild(expectedUnionNode, expectedSubQuery1UnionNode);

        InnerJoinNode joinNode11 = IQ_FACTORY.createInnerJoinNode();
        expectedQueryBuilder.addChild(expectedSubQuery1UnionNode, joinNode11);

        InnerJoinNode joinNode12 = IQ_FACTORY.createInnerJoinNode();
        expectedQueryBuilder.addChild(joinNode11, joinNode12);

        expectedQueryBuilder.addChild(joinNode11, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE5_PREDICATE, A, L)) );

        expectedQueryBuilder.addChild(joinNode12, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, A, BF0)) );
        expectedQueryBuilder.addChild(joinNode12, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, E, BF0)) );

        ConstructionNode expectedSubQuery2UnionNode = IQ_FACTORY.createConstructionNode(expectedUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI2(C))));
        expectedQueryBuilder.addChild(expectedUnionNode, expectedSubQuery2UnionNode);

        InnerJoinNode joinNode21 = IQ_FACTORY.createInnerJoinNode();
        expectedQueryBuilder.addChild(expectedSubQuery2UnionNode, joinNode21);

        InnerJoinNode joinNode22 = IQ_FACTORY.createInnerJoinNode();
        expectedQueryBuilder.addChild(joinNode21, joinNode22);

        expectedQueryBuilder.addChild(joinNode21, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE6_PREDICATE, C, N)) );

        expectedQueryBuilder.addChild(joinNode22, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, C, BF0)) );
        expectedQueryBuilder.addChild(joinNode22, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, E, BF0)) );


        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        System.out.println("\nExpected  query: \n" +  expectedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }

    @Test
    public void testEqualityLiftingNonProjected1() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ANS1_ARITY_1_PREDICATE, X);

        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        initialQueryBuilder.init(projectionAtom, unionNode);


        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A)));
        initialQueryBuilder.addChild(unionNode, leftConstructionNode);
        initialQueryBuilder.addChild(leftConstructionNode, DATA_NODE_1);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        initialQueryBuilder.addChild(unionNode, joinNode);

        ConstructionNode centralConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateURI1(C)));
        initialQueryBuilder.addChild(joinNode, centralConstructionNode);
        initialQueryBuilder.addChild(centralConstructionNode, DATA_NODE_3);

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(E),
                        Y, generateURI1(E)));
        initialQueryBuilder.addChild(joinNode, rightConstructionNode);

        ExtensionalDataNode rightDataNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, E, F));
        initialQueryBuilder.addChild(rightConstructionNode, rightDataNode);

        IntermediateQuery initialQuery = initialQueryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  initialQuery);

        IntermediateQueryBuilder expectedQueryBuilder = initialQuery.newBuilder();

        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(AF0)));

        expectedQueryBuilder.init(projectionAtom, newRootNode);

        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(AF0));
        expectedQueryBuilder.addChild(newRootNode, newUnionNode);
        expectedQueryBuilder.addChild(newUnionNode, IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, AF0, B)));
        expectedQueryBuilder.addChild(newUnionNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, AF0, D)));
        expectedQueryBuilder.addChild(joinNode, IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, AF0, F)));

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\nExpected  query: \n" +  expectedQuery);

        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(initialQuery);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }

    @Test
    public void testEqualityLiftingNonProjected2WithTopConstructionNode() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ANS1_ARITY_1_PREDICATE, X);

        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        initialQueryBuilder.init(projectionAtom, initialRootNode);

        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        initialQueryBuilder.addChild(initialRootNode, unionNode);


        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A)));
        initialQueryBuilder.addChild(unionNode, leftConstructionNode);
        initialQueryBuilder.addChild(leftConstructionNode, DATA_NODE_1);

        ConstructionNode intermediateConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(E)));
        initialQueryBuilder.addChild(unionNode, intermediateConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        initialQueryBuilder.addChild(intermediateConstructionNode, joinNode);

        ConstructionNode centralConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateURI1(C)));
        initialQueryBuilder.addChild(joinNode, centralConstructionNode);
        initialQueryBuilder.addChild(centralConstructionNode, DATA_NODE_3);

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y, E),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateURI1(E)));
        initialQueryBuilder.addChild(joinNode, rightConstructionNode);

        ExtensionalDataNode rightDataNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, E, F));
        initialQueryBuilder.addChild(rightConstructionNode, rightDataNode);

        IntermediateQuery initialQuery = initialQueryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  initialQuery);

        IntermediateQueryBuilder expectedQueryBuilder = initialQuery.newBuilder();

        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(AF0)));

        expectedQueryBuilder.init(projectionAtom, newRootNode);

        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(AF0));
        expectedQueryBuilder.addChild(newRootNode, newUnionNode);
        expectedQueryBuilder.addChild(newUnionNode, IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, AF0, B)));
        expectedQueryBuilder.addChild(newUnionNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, AF0, D)));
        expectedQueryBuilder.addChild(joinNode, IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, AF0, F)));

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\nExpected  query: \n" +  expectedQuery);

        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(initialQuery);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }

    @Test
    public void testEqualityLiftingNonProjected2() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ANS1_ARITY_1_PREDICATE, X);

        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        initialQueryBuilder.init(projectionAtom, unionNode);

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A)));
        initialQueryBuilder.addChild(unionNode, leftConstructionNode);
        initialQueryBuilder.addChild(leftConstructionNode, DATA_NODE_1);

        ConstructionNode intermediateConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(E)));
        initialQueryBuilder.addChild(unionNode, intermediateConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        initialQueryBuilder.addChild(intermediateConstructionNode, joinNode);

        ConstructionNode centralConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateURI1(C)));
        initialQueryBuilder.addChild(joinNode, centralConstructionNode);
        initialQueryBuilder.addChild(centralConstructionNode, DATA_NODE_3);

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y, E),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateURI1(E)));
        initialQueryBuilder.addChild(joinNode, rightConstructionNode);

        ExtensionalDataNode rightDataNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, E, F));
        initialQueryBuilder.addChild(rightConstructionNode, rightDataNode);

        IntermediateQuery initialQuery = initialQueryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  initialQuery);

        IntermediateQueryBuilder expectedQueryBuilder = initialQuery.newBuilder();

        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(AF0)));

        expectedQueryBuilder.init(projectionAtom, newRootNode);

        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(AF0));
        expectedQueryBuilder.addChild(newRootNode, newUnionNode);
        expectedQueryBuilder.addChild(newUnionNode, IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, AF0, B)));
        expectedQueryBuilder.addChild(newUnionNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, AF0, D)));
        expectedQueryBuilder.addChild(joinNode, IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, AF0, F)));

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\nExpected  query: \n" +  expectedQuery);

        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(initialQuery);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }



    private ImmutableFunctionalTerm generateURI1(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getImmutableFunctionalTerm(URI_PREDICATE, URI_TEMPLATE_STR_1, argument);
    }

    private ImmutableFunctionalTerm generateURI2(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getImmutableFunctionalTerm(URI_PREDICATE, URI_TEMPLATE_STR_2, argument);
    }

    private ImmutableFunctionalTerm generateCompositeURI2(ImmutableTerm argument1, ImmutableTerm argument2) {
        return TERM_FACTORY.getImmutableFunctionalTerm(URI_2PREDICATE, URI_TEMPLATE_STR_2_2, argument1, argument2);
    }

    private ImmutableFunctionalTerm generateInt(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getImmutableFunctionalTerm(
                TERM_FACTORY.getRequiredTypePredicate(XSD.INTEGER),
                argument);
    }

    private ImmutableFunctionalTerm generateString(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getImmutableFunctionalTerm(
                TERM_FACTORY.getRequiredTypePredicate(XSD.STRING),
                argument);
    }


    @Test
    public void testNewConstructionNode() throws EmptyQueryException {
        //Construct unoptimized query
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, X, Y);

        UnionNode unionNode =  IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y));
        queryBuilder.init(projectionAtom, unionNode);

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of( Y, generateURI1(B))));
        queryBuilder.addChild(unionNode, leftConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(leftConstructionNode, joinNode);

        UnionNode unionNode2 =  IQ_FACTORY.createUnionNode(unionNode.getVariables());
        queryBuilder.addChild(unionNode, unionNode2);

        //first child of unionNode2
        ConstructionNode subQuery1UnionNode2 = IQ_FACTORY.createConstructionNode(unionNode2.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(I),
                        Y, NULL
                        )));
        queryBuilder.addChild(unionNode2, subQuery1UnionNode2);

        queryBuilder.addChild(subQuery1UnionNode2, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE5_PREDICATE, I, L)) );

        //second child of unionNode2
        ConstructionNode subQuery2UnionNode2 = IQ_FACTORY.createConstructionNode(unionNode2.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI2(M),
                        Y, NULL
                        )));
        queryBuilder.addChild(unionNode2, subQuery2UnionNode2);

        queryBuilder.addChild(subQuery2UnionNode2, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE6_PREDICATE, M, N)) );


        //first child of JoinNode
        ConstructionNode subQueryJoinNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(B,X),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of( X, generateURI1(A))));
        queryBuilder.addChild(joinNode, subQueryJoinNode);

        queryBuilder.addChild(subQueryJoinNode, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B)) );

        //second child of JoinNode

        queryBuilder.addChild(joinNode, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, E, B)) );


        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);

        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(unOptimizedQuery);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);


        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        UnionNode expectedUnionNode =  IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y));
        expectedQueryBuilder.init(projectionAtom, expectedUnionNode);

        //first child of UnionNode
        ConstructionNode expSubQueryUnionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateURI1(A),
                        Y, generateURI1(B)
                )));
        expectedQueryBuilder.addChild(expectedUnionNode, expSubQueryUnionNode);

        InnerJoinNode expectedJoinNode = IQ_FACTORY.createInnerJoinNode();
        expectedQueryBuilder.addChild(expSubQueryUnionNode, expectedJoinNode);

        expectedQueryBuilder.addChild(expectedJoinNode, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B)) );

        //second child of JoinNode

        expectedQueryBuilder.addChild(expectedJoinNode, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, E, B)) );

        ConstructionNode expectedRightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        Y, NULL
                )));
        expectedQueryBuilder.addChild(expectedUnionNode, expectedRightConstructionNode);

        UnionNode expectedUnionNode2 =  IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        expectedQueryBuilder.addChild(expectedRightConstructionNode, expectedUnionNode2);


        //first child of unionNode2
        ConstructionNode expSubQuery1UnionNode2 = IQ_FACTORY.createConstructionNode(expectedUnionNode2.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(I))));
        expectedQueryBuilder.addChild(expectedUnionNode2, expSubQuery1UnionNode2);

        expectedQueryBuilder.addChild(expSubQuery1UnionNode2, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE5_PREDICATE, I, L)) );

        //second child of unionNode2
        ConstructionNode expSubQuery2UnionNode2 = IQ_FACTORY.createConstructionNode(expectedUnionNode2.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI2(M))));
        expectedQueryBuilder.addChild(expectedUnionNode2, expSubQuery2UnionNode2);

        expectedQueryBuilder.addChild(expSubQuery2UnionNode2, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE6_PREDICATE, M, N)) );

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
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, X, Y);

        UnionNode unionNode =  IQ_FACTORY.createUnionNode(ImmutableSet.of(X,Y));
        queryBuilder.init(projectionAtom, unionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(unionNode, joinNode);

        UnionNode unionNode2 =  unionNode.clone();
        queryBuilder.addChild(unionNode, unionNode2);

        //first child of unionNode2
        ConstructionNode subQuery1UnionNode2 = IQ_FACTORY.createConstructionNode(unionNode2.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateCompositeURI2(I, L),
                        Y, NULL
                        )));
        queryBuilder.addChild(unionNode2, subQuery1UnionNode2);

        queryBuilder.addChild(subQuery1UnionNode2, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE5_PREDICATE, I, L)) );

        //second child of unionNode2
        ConstructionNode subQuery2UnionNode2 = IQ_FACTORY.createConstructionNode(unionNode2.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateCompositeURI2(M, N),
                        Y, NULL
                        )));
        queryBuilder.addChild(unionNode2, subQuery2UnionNode2);

        queryBuilder.addChild(subQuery2UnionNode2, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE6_PREDICATE, M, N)) );


        //first child of JoinNode
        ConstructionNode subQueryJoinNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of( X, generateCompositeURI2(A, B), Y, generateURI1(B))));
        queryBuilder.addChild(joinNode, subQueryJoinNode);

        queryBuilder.addChild(subQueryJoinNode, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B)) );

        //second child of JoinNode
        ConstructionNode subQueryJoinNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of( Y, generateURI1(F))));
        queryBuilder.addChild(joinNode, subQueryJoinNode2);

        queryBuilder.addChild(subQueryJoinNode2, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, E, F)) );


        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);

        Variable AF4 = TERM_FACTORY.getVariable("af4");
        Variable BF5 = TERM_FACTORY.getVariable("bf5");

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of( X, generateCompositeURI2(AF4,BF5))));


        expectedQueryBuilder.init(projectionAtom, expectedRootNode);

        UnionNode expectedUnionNode =  IQ_FACTORY.createUnionNode(ImmutableSet.of(BF5, AF4, Y));
        expectedQueryBuilder.addChild(expectedRootNode, expectedUnionNode);

        //first child of UnionNode
        ConstructionNode expSubQueryUnionNode = IQ_FACTORY.createConstructionNode(expectedUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of( Y, generateURI1(BF5))));
        expectedQueryBuilder.addChild(expectedUnionNode, expSubQueryUnionNode);

        InnerJoinNode expectedJoinNode = IQ_FACTORY.createInnerJoinNode();
        expectedQueryBuilder.addChild(expSubQueryUnionNode, expectedJoinNode);

        expectedQueryBuilder.addChild(expectedJoinNode, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE,AF4,BF5)) );

        //second child of JoinNode

        expectedQueryBuilder.addChild(expectedJoinNode, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, E, BF5)) );


        ConstructionNode newRightConstructionNode = IQ_FACTORY.createConstructionNode(expectedUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of( Y, NULL)));
        expectedQueryBuilder.addChild(expectedUnionNode, newRightConstructionNode);

        UnionNode expectedUnionNode2 =  IQ_FACTORY.createUnionNode(ImmutableSet.of(AF4, BF5));
        expectedQueryBuilder.addChild(newRightConstructionNode, expectedUnionNode2);

        //first child of unionNode2

        expectedQueryBuilder.addChild(expectedUnionNode2, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE5_PREDICATE, AF4, BF5)) );

        //second child of unionNode2

        expectedQueryBuilder.addChild(expectedUnionNode2, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE6_PREDICATE, AF4, BF5)) );

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        System.out.println("\nExpected  query: \n" +  expectedQuery);

        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(unOptimizedQuery);

        //second optimization to lift the bindings of the first union

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }

    @Test
    public void testCompositeURITemplateDoubleRunYProjectedAway() throws EmptyQueryException {
        //Construct unoptimized query
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_1_PREDICATE, X);

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        queryBuilder.init(projectionAtom, rootNode);

        UnionNode unionNode =  IQ_FACTORY.createUnionNode(ImmutableSet.of(X,Y));
        queryBuilder.addChild(rootNode, unionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(unionNode, joinNode);

        UnionNode unionNode2 =  unionNode.clone();
        queryBuilder.addChild(unionNode, unionNode2);

        //first child of unionNode2
        ConstructionNode subQuery1UnionNode2 = IQ_FACTORY.createConstructionNode(unionNode2.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateCompositeURI2(I, L),
                        Y, NULL
                        )));
        queryBuilder.addChild(unionNode2, subQuery1UnionNode2);

        queryBuilder.addChild(subQuery1UnionNode2, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE5_PREDICATE, I, L)) );

        //second child of unionNode2
        ConstructionNode subQuery2UnionNode2 = IQ_FACTORY.createConstructionNode(unionNode2.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateCompositeURI2(M, N),
                        Y, NULL
                        )));
        queryBuilder.addChild(unionNode2, subQuery2UnionNode2);

        queryBuilder.addChild(subQuery2UnionNode2, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE6_PREDICATE, M, N)) );


        //first child of JoinNode
        ConstructionNode subQueryJoinNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of( X, generateCompositeURI2(A, B), Y, generateURI1(B))));
        queryBuilder.addChild(joinNode, subQueryJoinNode);

        queryBuilder.addChild(subQueryJoinNode, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B)) );

        //second child of JoinNode
        ConstructionNode subQueryJoinNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of( Y, generateURI1(F))));
        queryBuilder.addChild(joinNode, subQueryJoinNode2);

        queryBuilder.addChild(subQueryJoinNode2, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, E, F)) );


        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);

        Variable AF4 = TERM_FACTORY.getVariable("af4");
        Variable BF5 = TERM_FACTORY.getVariable("bf5");

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of( X, generateCompositeURI2(AF4,BF5))));


        expectedQueryBuilder.init(projectionAtom, expectedRootNode);

        UnionNode expectedUnionNode =  IQ_FACTORY.createUnionNode(ImmutableSet.of(BF5, AF4, Y));
        expectedQueryBuilder.addChild(expectedRootNode, expectedUnionNode);

        //first child of UnionNode
        ConstructionNode expSubQueryUnionNode = IQ_FACTORY.createConstructionNode(expectedUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of( Y, generateURI1(BF5))));
        expectedQueryBuilder.addChild(expectedUnionNode, expSubQueryUnionNode);

        InnerJoinNode expectedJoinNode = IQ_FACTORY.createInnerJoinNode();
        expectedQueryBuilder.addChild(expSubQueryUnionNode, expectedJoinNode);

        expectedQueryBuilder.addChild(expectedJoinNode, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE,AF4,BF5)) );

        //second child of JoinNode

        expectedQueryBuilder.addChild(expectedJoinNode, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, E, BF5)) );


        ConstructionNode newRightConstructionNode = IQ_FACTORY.createConstructionNode(expectedUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of( Y, NULL)));
        expectedQueryBuilder.addChild(expectedUnionNode, newRightConstructionNode);

        UnionNode expectedUnionNode2 =  IQ_FACTORY.createUnionNode(ImmutableSet.of(AF4, BF5));
        expectedQueryBuilder.addChild(newRightConstructionNode, expectedUnionNode2);

        //first child of unionNode2

        expectedQueryBuilder.addChild(expectedUnionNode2, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE5_PREDICATE, AF4, BF5)) );

        //second child of unionNode2

        expectedQueryBuilder.addChild(expectedUnionNode2, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE6_PREDICATE, AF4, BF5)) );

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        System.out.println("\nExpected  query: \n" +  expectedQuery);

        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(unOptimizedQuery);

        //second optimization to lift the bindings of the first union

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }

    @Test
    public void testLeftJoinAndUnionLiftSubstitution() throws EmptyQueryException {
        testLeftJoinAndUnionLiftSubstitution(false);
    }

    @Test
    public void testLeftJoinAndUnionLiftSubstitutionWithTopConstructionNode() throws EmptyQueryException {
        testLeftJoinAndUnionLiftSubstitution(true);
    }

    private void testLeftJoinAndUnionLiftSubstitution(boolean withTopConstructionNode) throws EmptyQueryException {
        //Construct unoptimized query
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_4_PREDICATE, X, Y, Z, W);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        if (withTopConstructionNode) {
            ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
            queryBuilder.init(projectionAtom, rootNode);
            queryBuilder.addChild(rootNode, joinNode);
        }
        else {
            queryBuilder.init(projectionAtom, joinNode);
        }

        //construct left side join (left join)
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(joinNode, leftJoinNode);

        //construct right side join
        ConstructionNode rightNodeJoin = IQ_FACTORY.createConstructionNode(ImmutableSet.of(W,Y),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        W, generateInt(H),
                        Y, generateInt(G))));
        queryBuilder.addChild(joinNode, rightNodeJoin);

        queryBuilder.addChild(rightNodeJoin, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE4_PREDICATE, G, H)) );

        //construct left side left join (union)
        UnionNode unionNodeOnLeft = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y));
        queryBuilder.addChild(leftJoinNode, unionNodeOnLeft, LEFT);

        ConstructionNode leftUnionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateURI1(A),
                        Y, generateInt(B))));
        queryBuilder.addChild(unionNodeOnLeft, leftUnionNode);

        queryBuilder.addChild(leftUnionNode, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B)) );

        ConstructionNode rightUnionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateURI2(C),
                        Y, generateInt(D))));
        queryBuilder.addChild(unionNodeOnLeft, rightUnionNode);

        queryBuilder.addChild(rightUnionNode, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, C, D)) );

        //construct right side  left join
        ConstructionNode nodeOnRight = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Z),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(E), Z, generateInt(F))));
        queryBuilder.addChild(leftJoinNode, nodeOnRight, RIGHT);

        queryBuilder.addChild(nodeOnRight, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, E, F)) );



        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);

        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(unOptimizedQuery);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);



        //----------------------------------------------------------------------
        // Construct expected query
        //Construct unoptimized query
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(W, generateInt(H), Y, generateInt(G))));

        expectedQueryBuilder.init(projectionAtom, expectedRootNode);

        //construct innerjoin
        InnerJoinNode expectedJoinNode = IQ_FACTORY.createInnerJoinNode();
        expectedQueryBuilder.addChild(expectedRootNode, expectedJoinNode);



        //construct union
        UnionNode expectedUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(G, X, Z));
        expectedQueryBuilder.addChild(expectedJoinNode, expectedUnionNode);

        //construct right side join

        expectedQueryBuilder.addChild(expectedJoinNode, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE4_PREDICATE, G, H)));

        //construct union left side

        ConstructionNode expectedNodeOnLeft =IQ_FACTORY.createConstructionNode(expectedUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A))));

        expectedQueryBuilder.addChild(expectedUnionNode, expectedNodeOnLeft);

        //construct left join
        LeftJoinNode expectedLeftJoinNode = IQ_FACTORY.createLeftJoinNode();
        expectedQueryBuilder.addChild(expectedNodeOnLeft, expectedLeftJoinNode);

        //construct left side left join
        expectedQueryBuilder.addChild(expectedLeftJoinNode, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, A, G)), LEFT);
        //construct right side left join
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(A, Z),
                SUBSTITUTION_FACTORY.getSubstitution(Z, generateInt(F)));
        expectedQueryBuilder.addChild(expectedLeftJoinNode, rightConstructionNode, RIGHT);
        expectedQueryBuilder.addChild(rightConstructionNode, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, A, F)));

        ConstructionNode expectedNodeOnRight =IQ_FACTORY.createConstructionNode(ImmutableSet.of(G, X, Z),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(Z, NULL, X, generateURI2(C))));

        expectedQueryBuilder.addChild(expectedUnionNode, expectedNodeOnRight);

        expectedQueryBuilder.addChild(expectedNodeOnRight, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, C, G)));


        //build expected query
        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));


    }

    @Test
    public void testConstantNonPropagationAcrossUnions() throws EmptyQueryException {

        //Construct unoptimized query
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, X, Y);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(Y, generateURI1(B))));

        queryBuilder.init(projectionAtom, rootNode);

        UnionNode topUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, B));
        queryBuilder.addChild(rootNode, topUnionNode);

        UnionNode leftUnionNode = topUnionNode.clone();
        queryBuilder.addChild(topUnionNode, leftUnionNode);

        ValueConstant two = TERM_FACTORY.getConstantLiteral("2");

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(leftUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateURI1(A),
                        B, two)));
        queryBuilder.addChild(leftUnionNode, constructionNode1);

        ExtensionalDataNode dataNode7 = buildExtensionalDataNode(TABLE7_ARITY_1_PREDICATE, A);
        queryBuilder.addChild(constructionNode1, dataNode7);

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(leftUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateURI2(C),
                        B, two)));
        queryBuilder.addChild(leftUnionNode, constructionNode2);

        ExtensionalDataNode dataNode8 = buildExtensionalDataNode(TABLE8_ARITY_1_PREDICATE, C);
        queryBuilder.addChild(constructionNode2, dataNode8);


        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topUnionNode, joinNode);


        ValueConstant three = TERM_FACTORY.getConstantLiteral("3");

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(leftUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateURI1(D),
                        B, three)));
        queryBuilder.addChild(joinNode, constructionNode3);

        ExtensionalDataNode dataNode9 = buildExtensionalDataNode(TABLE9_ARITY_1_PREDICATE, D);
        queryBuilder.addChild(constructionNode3, dataNode9);

        ExtensionalDataNode dataNode10 = buildExtensionalDataNode(TABLE7_ARITY_1_PREDICATE, E);
        queryBuilder.addChild(joinNode, dataNode10);

        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        expectedQueryBuilder.init(projectionAtom, rootNode);
        expectedQueryBuilder.addChild(rootNode, topUnionNode);

        ConstructionNode newLeftConstructionNode = IQ_FACTORY.createConstructionNode(leftUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        B, two)));
        expectedQueryBuilder.addChild(topUnionNode, newLeftConstructionNode);
        UnionNode newLeftUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        expectedQueryBuilder.addChild(newLeftConstructionNode, newLeftUnionNode);

        ConstructionNode newConstructionNode1 = IQ_FACTORY.createConstructionNode(newLeftUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateURI1(A))));
        expectedQueryBuilder.addChild(newLeftUnionNode, newConstructionNode1);
        expectedQueryBuilder.addChild(newConstructionNode1, dataNode7);

        ConstructionNode newConstructionNode2 = IQ_FACTORY.createConstructionNode(newLeftUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateURI2(C))));
        expectedQueryBuilder.addChild(newLeftUnionNode, newConstructionNode2);
        expectedQueryBuilder.addChild(newConstructionNode2, dataNode8);


        expectedQueryBuilder.addChild(topUnionNode, constructionNode3);
        expectedQueryBuilder.addChild(constructionNode3, joinNode);
        expectedQueryBuilder.addChild(joinNode, dataNode9);
        expectedQueryBuilder.addChild(joinNode, dataNode10);

        //build expected query
        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\n Expected query: \n" +  expectedQuery);


        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(unOptimizedQuery);
        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));

    }

    @Test
    public void testConstantNonPropagationAcrossUnions2() throws EmptyQueryException {

        //Construct unoptimized query
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, X, Y);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(Y, generateURI1(B))));

        queryBuilder.init(projectionAtom, rootNode);

        UnionNode topUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, B));
        queryBuilder.addChild(rootNode, topUnionNode);

        UnionNode leftUnionNode = topUnionNode.clone();
        queryBuilder.addChild(topUnionNode, leftUnionNode);

        ValueConstant two = TERM_FACTORY.getConstantLiteral("2");

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(leftUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateURI1(A),
                        B, two)));
        queryBuilder.addChild(leftUnionNode, constructionNode1);

        ExtensionalDataNode dataNode7 = buildExtensionalDataNode(TABLE7_ARITY_1_PREDICATE, A);
        queryBuilder.addChild(constructionNode1, dataNode7);

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(leftUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateURI2(C),
                        B, two)));
        queryBuilder.addChild(leftUnionNode, constructionNode2);

        ExtensionalDataNode dataNode8 = buildExtensionalDataNode(TABLE8_ARITY_1_PREDICATE, C);
        queryBuilder.addChild(constructionNode2, dataNode8);


        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topUnionNode, joinNode);

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateURI1(D))));
        queryBuilder.addChild(joinNode, constructionNode3);

        ExtensionalDataNode dataNode9 = buildExtensionalDataNode(TABLE9_ARITY_1_PREDICATE, D);
        queryBuilder.addChild(constructionNode3, dataNode9);

        ExtensionalDataNode dataNode10 = buildExtensionalDataNode(TABLE7_ARITY_1_PREDICATE, B);
        queryBuilder.addChild(joinNode, dataNode10);

        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        expectedQueryBuilder.init(projectionAtom, rootNode);
        expectedQueryBuilder.addChild(rootNode, topUnionNode);

        ConstructionNode newLeftConstructionNode = IQ_FACTORY.createConstructionNode(leftUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        B, two)));
        expectedQueryBuilder.addChild(topUnionNode, newLeftConstructionNode);
        UnionNode newLeftUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        expectedQueryBuilder.addChild(newLeftConstructionNode, newLeftUnionNode);

        ConstructionNode newConstructionNode1 = IQ_FACTORY.createConstructionNode(newLeftUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateURI1(A))));
        expectedQueryBuilder.addChild(newLeftUnionNode, newConstructionNode1);
        expectedQueryBuilder.addChild(newConstructionNode1, dataNode7);

        ConstructionNode newConstructionNode2 = IQ_FACTORY.createConstructionNode(newLeftUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateURI2(C))));
        expectedQueryBuilder.addChild(newLeftUnionNode, newConstructionNode2);
        expectedQueryBuilder.addChild(newConstructionNode2, dataNode8);


        ConstructionNode constructionNode3b = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, B),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateURI1(D))));
        expectedQueryBuilder.addChild(topUnionNode, constructionNode3b);
        expectedQueryBuilder.addChild(constructionNode3b, joinNode);
        expectedQueryBuilder.addChild(joinNode, dataNode9);
        expectedQueryBuilder.addChild(joinNode, dataNode10);

        //build expected query
        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\n Expected query: \n" +  expectedQuery);


        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(unOptimizedQuery);
        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));

    }

    /**
     * Currently runs the optimizer twice
     */
    @Test
    public void testTrueNode() throws EmptyQueryException {
        //Construct unoptimized query
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, X, Y);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getImmutableExpression(EQ,
                buildSparqlDatatype(X), buildSparqlDatatype(Y)));

        queryBuilder.init(projectionAtom, joinNode);

        UnionNode unionNode =  IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        queryBuilder.addChild(joinNode, unionNode);

        ConstructionNode leftChildUnion = IQ_FACTORY.createConstructionNode(unionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateInt(A) )));

        queryBuilder.addChild(unionNode, leftChildUnion);
        queryBuilder.addChild(leftChildUnion, DATA_NODE_1 );

        ConstructionNode rightChildUnion = IQ_FACTORY.createConstructionNode(unionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateString(C) )));

        queryBuilder.addChild(unionNode, rightChildUnion);

        queryBuilder.addChild(rightChildUnion, DATA_NODE_3 );

        ConstructionNode otherNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(Y, generateInt(TERM_FACTORY.getConstantLiteral("2", TYPE_FACTORY.getXsdIntegerDatatype())) )));

        queryBuilder.addChild(joinNode, otherNode);
        queryBuilder.addChild(otherNode, IQ_FACTORY.createTrueNode());

        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateInt(A), Y, generateInt(
                        TERM_FACTORY.getConstantLiteral("2", TYPE_FACTORY.getXsdIntegerDatatype())))));
        expectedQueryBuilder.init(projectionAtom, expectedRootNode);

        expectedQueryBuilder.addChild(expectedRootNode, DATA_NODE_1);
        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\nExpected query: \n" +  expectedQuery);

        /*
         * TODO: remove this double call
         */
        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(
                BINDING_LIFT_OPTIMIZER.optimize(unOptimizedQuery));

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
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, X, Y);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of()));
        queryBuilder.init(projectionAtom, rootNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getImmutableExpression(EQ,
                buildSparqlDatatype(X), buildSparqlDatatype(Y)));
        queryBuilder.addChild(rootNode,joinNode);

        UnionNode unionNode =  IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        queryBuilder.addChild(joinNode, unionNode);

        ConstructionNode leftChildUnion = IQ_FACTORY.createConstructionNode(unionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateInt(A) )));

        queryBuilder.addChild(unionNode, leftChildUnion);
        queryBuilder.addChild(leftChildUnion, DATA_NODE_1 );

        ConstructionNode rightChildUnion = IQ_FACTORY.createConstructionNode(unionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateString(C) )));

        queryBuilder.addChild(unionNode, rightChildUnion);

        queryBuilder.addChild(rightChildUnion, DATA_NODE_3 );

        ConstructionNode otherNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(Y, generateInt(F)) ));

        queryBuilder.addChild(joinNode, otherNode);

        queryBuilder.addChild(otherNode, DATA_NODE_6 );

        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateInt(A), Y, generateInt(F))));
        expectedQueryBuilder.init(projectionAtom, expectedRootNode);

        InnerJoinNode expectedJoinNode = IQ_FACTORY.createInnerJoinNode();
        expectedQueryBuilder.addChild(expectedRootNode, expectedJoinNode);

        expectedQueryBuilder.addChild(expectedJoinNode, DATA_NODE_1);
        expectedQueryBuilder.addChild(expectedJoinNode, DATA_NODE_6);
        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\nExpected query: \n" +  expectedQuery);

        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(unOptimizedQuery);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }


    @Test
    @Ignore
    public void testDatatypeExpressionEvaluator() throws EmptyQueryException {
        //Construct unoptimized query
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, X, Y);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of()));
        queryBuilder.init(projectionAtom, rootNode);

        InnerJoinNode jn = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getImmutableExpression(EQ,
                buildSparqlDatatype(X), buildSparqlDatatype(Y)));
        queryBuilder.addChild(rootNode, jn);
        ConstructionNode leftCn = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateInt(A))));
        queryBuilder.addChild(jn, leftCn);
        ConstructionNode rightCn = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(Y, generateInt(B))));
        queryBuilder.addChild(jn, rightCn);
        queryBuilder.addChild(leftCn, DATA_NODE_8);
        queryBuilder.addChild(rightCn, DATA_NODE_9);

        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" + unOptimizedQuery);

        // Expected query
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ImmutableSubstitution expectedRootNodeSubstitution = SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X,
                generateInt(A), Y, generateInt(B)));
        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y), expectedRootNodeSubstitution);
        expectedQueryBuilder.init(projectionAtom, expectedRootNode);
        InnerJoinNode jn2 = IQ_FACTORY.createInnerJoinNode();
        expectedQueryBuilder.addChild(expectedRootNode, jn2);
        expectedQueryBuilder.addChild(jn2, DATA_NODE_8);
        expectedQueryBuilder.addChild(jn2, DATA_NODE_9);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\nExpected query: \n" + expectedQuery);

        // Optimize and compare
        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(unOptimizedQuery);
        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));

    }

    @Test(expected = EmptyQueryException.class)
    public void testEmptySubstitutionToBeLifted() throws EmptyQueryException {

        //Construct unoptimized query
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, X, Y);


        //construct
        QueryNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.init(projectionAtom, joinNode);

        //construct left side join
        LeftJoinNode leftJoinNode =  IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(joinNode, leftJoinNode);

        ConstructionNode leftNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        Y, generateURI1(B))));

        queryBuilder.addChild(leftJoinNode, leftNode, LEFT);

        queryBuilder.addChild(leftNode, DATA_NODE_9);

        ConstructionNode rightNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI2(C),
                        Y, generateURI1(D))));

        queryBuilder.addChild(leftJoinNode, rightNode, RIGHT);

        queryBuilder.addChild(rightNode, DATA_NODE_3);


        //construct right side join
        ConstructionNode node1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A))));
        queryBuilder.addChild(joinNode, node1);

        queryBuilder.addChild(node1, DATA_NODE_8);

        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);

        BINDING_LIFT_OPTIMIZER.optimize(unOptimizedQuery);
    }

    @Test
    public void testUnionRemoval() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, X, Y);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        queryBuilder.init(projectionAtom, rootNode);

        UnionNode unionNode = IQ_FACTORY.createUnionNode(projectionAtom.getVariables());
        queryBuilder.addChild(rootNode, unionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(unionNode, joinNode);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateURI1(A),
                        Y, generateURI2(B))));
        queryBuilder.addChild(joinNode, constructionNode1);
        queryBuilder.addChild(constructionNode1, DATA_NODE_1);

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateURI2(C),
                        Y, generateURI2(D))));
        queryBuilder.addChild(joinNode, constructionNode2);
        queryBuilder.addChild(constructionNode2, DATA_NODE_3);

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateURI1(E),
                        Y, generateURI2(F))));
        queryBuilder.addChild(unionNode, constructionNode3);
        queryBuilder.addChild(constructionNode3, DATA_NODE_6);

        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);


        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        expectedQueryBuilder.init(projectionAtom, constructionNode3);
        expectedQueryBuilder.addChild(constructionNode3, DATA_NODE_6);
        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        System.out.println("\nExpected query: \n" +  expectedQuery);


        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(unOptimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }

    @Test
    public void testJoinTemplateAndFunctionalGroundTerm1() throws EmptyQueryException {
        testJoinTemplateAndFunctionalGroundTerm(true);
    }

    @Test
    public void testJoinTemplateAndFunctionalGroundTerm2() throws EmptyQueryException {
        testJoinTemplateAndFunctionalGroundTerm(false);
    }
    private void testJoinTemplateAndFunctionalGroundTerm(boolean trueNodeOnTheLeft) throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, X, Y);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.init(projectionAtom, joinNode);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateURI1(A),
                        Y, generateURI2(B))));

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateURI1(ONE))));

        if (trueNodeOnTheLeft) {
            queryBuilder.addChild(joinNode, constructionNode2);
            queryBuilder.addChild(joinNode, constructionNode1);
        }
        else {
            queryBuilder.addChild(joinNode, constructionNode1);
            queryBuilder.addChild(joinNode, constructionNode2);
        }

        queryBuilder.addChild(constructionNode1, DATA_NODE_1);
        TrueNode trueNode = IQ_FACTORY.createTrueNode();
        queryBuilder.addChild(constructionNode2, trueNode);

        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);


        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        ImmutableMap.of(
                                X, generateURI1(ONE),
                                Y, generateURI2(B))));
        expectedQueryBuilder.init(projectionAtom, newConstructionNode);
        ExtensionalDataNode newLeftDataNode = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(DATA_NODE_1.getProjectionAtom().getPredicate(), ONE, B));
        expectedQueryBuilder.addChild(newConstructionNode, newLeftDataNode);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        System.out.println("\nExpected query: \n" +  expectedQuery);


        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(unOptimizedQuery);
        System.out.println("\nOptimized query: \n" +  optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }



    @Test
    public void testUnionWithJoin() throws EmptyQueryException {
        /**
         * Original Query
         */

        AtomPredicate TABLE_1 = ATOM_FACTORY.getAtomPredicate("table1", 2);
        AtomPredicate TABLE_2 = ATOM_FACTORY.getAtomPredicate("table2", 2);
        AtomPredicate TABLE_3 = ATOM_FACTORY.getAtomPredicate("table3", 2);
        AtomPredicate TABLE_4 = ATOM_FACTORY.getAtomPredicate("table4", 2);
        AtomPredicate TABLE_5 = ATOM_FACTORY.getAtomPredicate("table5", 3);

        DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
                ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                        ANS1_ARITY_3_PREDICATE, ImmutableList.of(X,Y, Z));

        IntermediateQueryBuilder originalBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootConstructionNode = IQ_FACTORY.createConstructionNode(ROOT_CONSTRUCTION_NODE_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());

        ConstructionNode emptyConstructionNode = IQ_FACTORY.createConstructionNode(ROOT_CONSTRUCTION_NODE_ATOM.getVariables());
        ConstructionNode emptyConstructionNode2 = emptyConstructionNode.clone();

        UnionNode unionNode1  = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y, Z));
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        UnionNode unionNode21 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y));
        UnionNode unionNode22  = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, C));
        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode();

        ConstructionNode constructionNode21 = IQ_FACTORY.createConstructionNode(unionNode21.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(A),
                        Y, generateURI2(B)));

        ConstructionNode constructionNode22 = IQ_FACTORY.createConstructionNode(unionNode22.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A)));

        ConstructionNode constructionNode22URI2 = IQ_FACTORY.createConstructionNode(unionNode22.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(A)));

        ConstructionNode constructionNode21URI2 = IQ_FACTORY.createConstructionNode(unionNode21.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI2(A),
                        Y, generateURI2(B)));

        ConstructionNode constructionNode21URI1XY = constructionNode21.clone();

        ConstructionNode constructionNode22Z = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Z),
                SUBSTITUTION_FACTORY.getSubstitution(Z, generateURI1(C)));

        ConstructionNode constructionNodeOverJoin2 = constructionNode22.clone();
        ConstructionNode constructionNodeOverJoin1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Z),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(A),
                        Z, generateURI1(C)));

        ExtensionalDataNode table1DataNode = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE_1, A, B));
        ExtensionalDataNode table2DataNode = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE_2, A, B));
        ExtensionalDataNode table3DataNode = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE_3, A, B));
        ExtensionalDataNode table4DataNode = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE_4, A, C));
        ExtensionalDataNode table5DataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE_5, X, Y, Z));
        ExtensionalDataNode table5DataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE_5, A, B, C));

        originalBuilder.init(ROOT_CONSTRUCTION_NODE_ATOM, rootConstructionNode);
        originalBuilder.addChild(rootConstructionNode, unionNode1);
        originalBuilder.addChild(unionNode1, emptyConstructionNode);
        originalBuilder.addChild(emptyConstructionNode, joinNode);
        originalBuilder.addChild(unionNode1, emptyConstructionNode2);
        originalBuilder.addChild(emptyConstructionNode2, table5DataNode1);

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
        originalBuilder.addChild(constructionNode22URI2, table5DataNode2);
        originalBuilder.addChild(unionNode22,constructionNodeOverJoin2 );
        originalBuilder.addChild(constructionNodeOverJoin2, joinNode1);
        originalBuilder.addChild(joinNode1, table4DataNode.clone());
        originalBuilder.addChild(joinNode1, table4DataNode.clone());



        IntermediateQuery originalQuery = originalBuilder.build();

        System.out.println("\n Original query: \n" +  originalQuery);

        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(originalQuery);

        System.out.println("\n Optimized query: \n" +  optimizedQuery);

        /**
         * Expected Query
         */
        IntermediateQueryBuilder expectedBuilder = createQueryBuilder(EMPTY_METADATA);

        UnionNode unionNode3 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y, Z));
        UnionNode unionNode4 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B));
        UnionNode unionNode5 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A,C));

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y, Z),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateURI1(A), Y, generateURI2(B), Z, generateURI1(C))));
        ConstructionNode constructionNode2 = emptyConstructionNode;
        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode();
        InnerJoinNode joinNode3 = IQ_FACTORY.createInnerJoinNode();


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
        expectedBuilder.addChild(constructionNode2, table5DataNode1);

        IntermediateQuery expectedQuery = expectedBuilder.build();

        System.out.println("\n Expected query: \n" +  expectedQuery);

        Assert.assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }

    /**
     * Does nothing since there is no substitution
     */
    @Test
    public void testProjectionAwaySubQuery() throws EmptyQueryException {
        //Construct unoptimized query
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_1_PREDICATE, A);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.init(projectionAtom, joinNode);
        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A));
        queryBuilder.addChild(joinNode, leftConstructionNode);
        queryBuilder.addChild(leftConstructionNode, DATA_NODE_1);
        queryBuilder.addChild(joinNode, DATA_NODE_8);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\n Original query: \n" +  query);

        IntermediateQuery expectedQuery = query.createSnapshot();
        System.out.println("\n Expected query: \n" +  expectedQuery);

        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(query);
        System.out.println("\n Optimized query: \n" +  optimizedQuery);

        Assert.assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));

    }

    private static ExtensionalDataNode buildExtensionalDataNode(AtomPredicate predicate, VariableOrGroundTerm ... arguments) {
        return IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(predicate, arguments));
    }

    private static ImmutableFunctionalTerm buildSparqlDatatype(ImmutableTerm argument){
        return TERM_FACTORY.getImmutableFunctionalTerm(SPARQL_DATATYPE, argument);
    }
}
