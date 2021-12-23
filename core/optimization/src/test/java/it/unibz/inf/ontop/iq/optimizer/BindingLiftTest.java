package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.equivalence.IQSyntacticEquivalenceChecker;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static it.unibz.inf.ontop.NoDependencyTestDBMetadata.*;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.RIGHT;
import static it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel.GT;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;

/**
 * Test the top down substitution lift optimizer
 */
public class BindingLiftTest {

    private final AtomPredicate ANS1_ARITY_1_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate( 1);
    private final AtomPredicate ANS1_ARITY_2_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate( 2);
    private final AtomPredicate ANS1_ARITY_3_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate( 3);
    private final AtomPredicate ANS1_ARITY_4_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate( 4);

    // TEMPORARY HACK!
    private ImmutableList<Template.Component> URI_TEMPLATE_STR_1 = Template.of("http://example.org/ds1/", 0);
    private ImmutableList<Template.Component> URI_TEMPLATE_STR_2 =  Template.of("http://example.org/ds2/", 0);
    private ImmutableList<Template.Component> URI_TEMPLATE_STR_2_2 = Template.of("http://example.org/ds2/", 0, "/", 1);

    private final ExtensionalDataNode DATA_NODE_1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
    private final ExtensionalDataNode DATA_NODE_2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, E));
    private final ExtensionalDataNode DATA_NODE_3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(C, D));
    private final ExtensionalDataNode DATA_NODE_4 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
    private final ExtensionalDataNode DATA_NODE_5 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, D));
    private final ExtensionalDataNode DATA_NODE_6 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(E, F));
    private final ExtensionalDataNode DATA_NODE_7 = createExtensionalDataNode(TABLE4_AR2, ImmutableList.of(G, H));
    private final ExtensionalDataNode DATA_NODE_8 = createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(A));
    private final ExtensionalDataNode DATA_NODE_9 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(B));

    private final ImmutableExpression EXPRESSIONGT = TERM_FACTORY.getDBDefaultInequality(
            GT, Z, Y);

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
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR3_PREDICATE, X, Y, Z);

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
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateIRIWithTemplate1(B))));

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
                        X, generateIRIWithTemplate1(C),
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
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();


        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateIRIWithTemplate1(C), Y, generateInt(D),
                        Z, generateInt(A))));

        expectedQueryBuilder.init(projectionAtom, expectedRootNode);

        //construct expected innerjoin
        ImmutableExpression expectedEspressionGT = TERM_FACTORY.getDBDefaultInequality(GT, generateInt(A), generateInt(D));
        InnerJoinNode expectedJoinNode = IQ_FACTORY.createInnerJoinNode(expectedEspressionGT);
        expectedQueryBuilder.addChild(expectedRootNode, expectedJoinNode);

        expectedQueryBuilder.addChild(expectedJoinNode, createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, C)));
        expectedQueryBuilder.addChild(expectedJoinNode, createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(C, D)));

        //build expected query
        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }


    @Test
    public void testLeftJoinSubstitution() throws EmptyQueryException {

        //Construct unoptimized query
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR3_PREDICATE, X, W, Z);
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
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateIRIWithTemplate1(A), W, generateString(B))));
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
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateIRIWithTemplate1(E), Y, generateInt((F)))));

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
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of( W, generateString(B), X, generateIRIWithTemplate1(A),
                        Z, generateInt(D))));

        expectedQueryBuilder.init(projectionAtom, expectedRootNode);

        //construct innerjoin
        LeftJoinNode expectedleftJoinNode = IQ_FACTORY.createLeftJoinNode();
        expectedQueryBuilder.addChild(expectedRootNode, expectedleftJoinNode);

        //construct left side left join (join)
        InnerJoinNode expectedJoinNodeOnLeft = IQ_FACTORY.createInnerJoinNode();
        expectedQueryBuilder.addChild(expectedleftJoinNode, expectedJoinNodeOnLeft, LEFT);

        expectedQueryBuilder.addChild(expectedJoinNodeOnLeft,
                createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B)));

        expectedQueryBuilder.addChild(expectedJoinNodeOnLeft,
                createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(B, D)));


        //construct right side left join (union)
        expectedQueryBuilder.addChild(expectedleftJoinNode,
                IQ_FACTORY.createExtensionalDataNode(TABLE3_AR2, ImmutableMap.of(0, A)), RIGHT);


        //build expected query
        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));


    }

    @Test
    public void testUnionSubstitution() throws EmptyQueryException {
        //Construct unoptimized query
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);

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
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateIRIWithTemplate1(A), Y, generateIRIWithTemplate1(B))));
        queryBuilder.addChild(unionNode2, subQuery1UnionNode2);

        queryBuilder.addChild(subQuery1UnionNode2, createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B)) );

        //second child of unionNode2
        ConstructionNode subQuery2UnionNode2 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI2(C), Y, generateIRIWithTemplate1(D))));
        queryBuilder.addChild(unionNode2, subQuery2UnionNode2);

        queryBuilder.addChild(subQuery2UnionNode2, createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, D)) );

        //right side second join (unionNode 3)
        UnionNode unionNode3 =  IQ_FACTORY.createUnionNode(ImmutableSet.of(Y));
        queryBuilder.addChild(joinNode2, unionNode3);

        //first child of unionNode3
        ConstructionNode subQuery1UnionNode3 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of( Y, generateIRIWithTemplate1(F))));
        queryBuilder.addChild(unionNode3, subQuery1UnionNode3);

        queryBuilder.addChild(subQuery1UnionNode3, createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(E, F)) );

        //second child of unionNode3
        ConstructionNode subQuery2UnionNode3 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(Y,  generateURI2(H))));
        queryBuilder.addChild(unionNode3, subQuery2UnionNode3);

        queryBuilder.addChild(subQuery2UnionNode3, createExtensionalDataNode(TABLE4_AR2, ImmutableList.of(G, H)) );

        //right side first join
        UnionNode unionNode1 =  IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        queryBuilder.addChild(joinNode, unionNode1);

        //first child of unionNode1
        ConstructionNode subQuery1UnionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateIRIWithTemplate1(I))));
        queryBuilder.addChild(unionNode1, subQuery1UnionNode1);

        queryBuilder.addChild(subQuery1UnionNode1, createExtensionalDataNode(TABLE5_AR2, ImmutableList.of(I, L)) );

        //second child of unionNode1
        ConstructionNode subQuery2UnionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI2(M))));
        queryBuilder.addChild(unionNode1, subQuery2UnionNode1);

        queryBuilder.addChild(subQuery2UnionNode1, createExtensionalDataNode(TABLE6_AR2, ImmutableList.of(M, N)) );

        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);

        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(unOptimizedQuery);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        //----------------------------------------------------------------------
        //Construct expected query

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, TERM_FACTORY.getIRIFunctionalTerm(F0),
                            //generateIRIWithTemplate1(F0, AF1),
                        Y, generateIRIWithTemplate1(BF1))));

        expectedQueryBuilder.init(projectionAtom, expectedRootNode);

        //constract union Node
        UnionNode expectedUnionNode =  IQ_FACTORY.createUnionNode(ImmutableSet.of(BF1, F0));

        expectedQueryBuilder.addChild(expectedRootNode, expectedUnionNode );

        ConstructionNode expectedSubQuery1UnionNode = IQ_FACTORY.createConstructionNode(expectedUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(F0, generateIRIString(URI_TEMPLATE_STR_1, A))));
        expectedQueryBuilder.addChild(expectedUnionNode, expectedSubQuery1UnionNode);

        InnerJoinNode joinNode11 = IQ_FACTORY.createInnerJoinNode();
        expectedQueryBuilder.addChild(expectedSubQuery1UnionNode, joinNode11);

        expectedQueryBuilder.addChild(joinNode11, createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, BF1)) );
        expectedQueryBuilder.addChild(joinNode11, IQ_FACTORY.createExtensionalDataNode(TABLE3_AR2, ImmutableMap.of(1, BF1)) );
        expectedQueryBuilder.addChild(joinNode11, IQ_FACTORY.createExtensionalDataNode(TABLE5_AR2, ImmutableMap.of(0, A)) );

        ConstructionNode expectedSubQuery2UnionNode = IQ_FACTORY.createConstructionNode(expectedUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(F0, generateIRIString(URI_TEMPLATE_STR_2, C))));
        expectedQueryBuilder.addChild(expectedUnionNode, expectedSubQuery2UnionNode);

        InnerJoinNode joinNode21 = IQ_FACTORY.createInnerJoinNode();
        expectedQueryBuilder.addChild(expectedSubQuery2UnionNode, joinNode21);


        expectedQueryBuilder.addChild(joinNode21, createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, BF1)) );
        expectedQueryBuilder.addChild(joinNode21, IQ_FACTORY.createExtensionalDataNode(TABLE3_AR2, ImmutableMap.of(1, BF1)) );
        expectedQueryBuilder.addChild(joinNode21, IQ_FACTORY.createExtensionalDataNode(TABLE6_AR2, ImmutableMap.of(0, C)) );


        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        System.out.println("\nExpected  query: \n" +  expectedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }

    @Test
    public void testEqualityLiftingNonProjected1() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ANS1_AR1_PREDICATE, X);

        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        initialQueryBuilder.init(projectionAtom, unionNode);


        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(A)));
        initialQueryBuilder.addChild(unionNode, leftConstructionNode);
        initialQueryBuilder.addChild(leftConstructionNode, DATA_NODE_1);

        ConstructionNode subConstructionNode1 = IQ_FACTORY.createConstructionNode(unionNode.getVariables());
        initialQueryBuilder.addChild(unionNode, subConstructionNode1);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        initialQueryBuilder.addChild(subConstructionNode1, joinNode);

        ConstructionNode centralConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateIRIWithTemplate1(C)));
        initialQueryBuilder.addChild(joinNode, centralConstructionNode);
        initialQueryBuilder.addChild(centralConstructionNode, DATA_NODE_3);

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateIRIWithTemplate1(E),
                        Y, generateIRIWithTemplate1(E)));
        initialQueryBuilder.addChild(joinNode, rightConstructionNode);

        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(E, F));
        initialQueryBuilder.addChild(rightConstructionNode, rightDataNode);

        IntermediateQuery initialQuery = initialQueryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  initialQuery);

        IntermediateQueryBuilder expectedQueryBuilder = IQ_FACTORY.createIQBuilder();

        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(AF1)));

        expectedQueryBuilder.init(projectionAtom, newRootNode);

        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(AF1));
        expectedQueryBuilder.addChild(newRootNode, newUnionNode);
        expectedQueryBuilder.addChild(newUnionNode, IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR2, ImmutableMap.of(0, AF1)));
        expectedQueryBuilder.addChild(newUnionNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, IQ_FACTORY.createExtensionalDataNode(
                TABLE3_AR2, ImmutableMap.of(0, AF1)));
        expectedQueryBuilder.addChild(joinNode, IQ_FACTORY.createExtensionalDataNode(
                TABLE2_AR2, ImmutableMap.of(0, AF1)));

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\nExpected  query: \n" +  expectedQuery);

        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(initialQuery);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }

    @Test
    public void testEqualityLiftingNonProjected2WithTopConstructionNode() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ANS1_AR1_PREDICATE, X);

        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        initialQueryBuilder.init(projectionAtom, initialRootNode);

        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        initialQueryBuilder.addChild(initialRootNode, unionNode);


        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(A)));
        initialQueryBuilder.addChild(unionNode, leftConstructionNode);
        initialQueryBuilder.addChild(leftConstructionNode, DATA_NODE_1);

        ConstructionNode intermediateConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(E)));
        initialQueryBuilder.addChild(unionNode, intermediateConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        initialQueryBuilder.addChild(intermediateConstructionNode, joinNode);

        ConstructionNode centralConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateIRIWithTemplate1(C)));
        initialQueryBuilder.addChild(joinNode, centralConstructionNode);
        initialQueryBuilder.addChild(centralConstructionNode, DATA_NODE_3);

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y, E),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateIRIWithTemplate1(E)));
        initialQueryBuilder.addChild(joinNode, rightConstructionNode);

        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(E, F));
        initialQueryBuilder.addChild(rightConstructionNode, rightDataNode);

        IntermediateQuery initialQuery = initialQueryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  initialQuery);

        IntermediateQueryBuilder expectedQueryBuilder = IQ_FACTORY.createIQBuilder();

        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(AF1)));

        expectedQueryBuilder.init(projectionAtom, newRootNode);

        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(AF1));
        expectedQueryBuilder.addChild(newRootNode, newUnionNode);
        expectedQueryBuilder.addChild(newUnionNode, IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR2, ImmutableMap.of(0, AF1)));
        expectedQueryBuilder.addChild(newUnionNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, IQ_FACTORY.createExtensionalDataNode(
                TABLE3_AR2, ImmutableMap.of(0, AF1)));
        expectedQueryBuilder.addChild(joinNode, IQ_FACTORY.createExtensionalDataNode(
                TABLE2_AR2, ImmutableMap.of(0, AF1)));

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\nExpected  query: \n" +  expectedQuery);

        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(initialQuery);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }

    @Test
    public void testEqualityLiftingNonProjected2() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ANS1_AR1_PREDICATE, X);

        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        initialQueryBuilder.init(projectionAtom, unionNode);

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(A)));
        initialQueryBuilder.addChild(unionNode, leftConstructionNode);
        initialQueryBuilder.addChild(leftConstructionNode, DATA_NODE_1);

        ConstructionNode intermediateConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(E)));
        initialQueryBuilder.addChild(unionNode, intermediateConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        initialQueryBuilder.addChild(intermediateConstructionNode, joinNode);

        ConstructionNode centralConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateIRIWithTemplate1(C)));
        initialQueryBuilder.addChild(joinNode, centralConstructionNode);
        initialQueryBuilder.addChild(centralConstructionNode, DATA_NODE_3);

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y, E),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateIRIWithTemplate1(E)));
        initialQueryBuilder.addChild(joinNode, rightConstructionNode);

        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(E, F));
        initialQueryBuilder.addChild(rightConstructionNode, rightDataNode);

        IntermediateQuery initialQuery = initialQueryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  initialQuery);

        IntermediateQueryBuilder expectedQueryBuilder = IQ_FACTORY.createIQBuilder();

        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(AF1)));

        expectedQueryBuilder.init(projectionAtom, newRootNode);

        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(AF1));
        expectedQueryBuilder.addChild(newRootNode, newUnionNode);
        expectedQueryBuilder.addChild(newUnionNode, IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR2, ImmutableMap.of(0, AF1)));
        expectedQueryBuilder.addChild(newUnionNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, IQ_FACTORY.createExtensionalDataNode(
                TABLE3_AR2, ImmutableMap.of(0, AF1)));
        expectedQueryBuilder.addChild(joinNode, IQ_FACTORY.createExtensionalDataNode(
                TABLE2_AR2, ImmutableMap.of(0, AF1)));

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\nExpected  query: \n" +  expectedQuery);

        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(initialQuery);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }



    private ImmutableFunctionalTerm generateIRIWithTemplate1(VariableOrGroundTerm argument) {
        return generateURI1(URI_TEMPLATE_STR_1, argument);
    }

    private ImmutableFunctionalTerm generateIRIString(ImmutableList<Template.Component> template, Variable... variables) {
        return TERM_FACTORY.getImmutableFunctionalTerm(
                FUNCTION_SYMBOL_FACTORY.getDBFunctionSymbolFactory().getIRIStringTemplateFunctionSymbol(template),
                variables);
    }

    private ImmutableFunctionalTerm generateURI1(ImmutableList<Template.Component> prefix, VariableOrGroundTerm argument) {
        return TERM_FACTORY.getIRIFunctionalTerm(prefix, ImmutableList.of(argument));
    }

    private ImmutableFunctionalTerm generateURI2(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STR_2, ImmutableList.of(argument));
    }

    private ImmutableFunctionalTerm generateCompositeURI2(ImmutableTerm argument1, ImmutableTerm argument2) {
        return TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STR_2_2, ImmutableList.of(argument1, argument2));
    }

    private ImmutableFunctionalTerm generateInt(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getRDFLiteralFunctionalTerm(argument, XSD.INTEGER);
    }

    private ImmutableFunctionalTerm generateString(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getRDFLiteralFunctionalTerm(argument, XSD.STRING);

    }


    @Test
    public void testNewConstructionNode() throws EmptyQueryException {
        //Construct unoptimized query
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);

        UnionNode unionNode =  IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y));
        queryBuilder.init(projectionAtom, unionNode);

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of( Y, generateIRIWithTemplate1(B))));
        queryBuilder.addChild(unionNode, leftConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(leftConstructionNode, joinNode);

        UnionNode unionNode2 =  IQ_FACTORY.createUnionNode(unionNode.getVariables());
        queryBuilder.addChild(unionNode, unionNode2);

        //first child of unionNode2
        ConstructionNode subQuery1UnionNode2 = IQ_FACTORY.createConstructionNode(unionNode2.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateIRIWithTemplate1(I),
                        Y, NULL
                        )));
        queryBuilder.addChild(unionNode2, subQuery1UnionNode2);

        queryBuilder.addChild(subQuery1UnionNode2, createExtensionalDataNode(TABLE5_AR2, ImmutableList.of(I, L)) );

        //second child of unionNode2
        ConstructionNode subQuery2UnionNode2 = IQ_FACTORY.createConstructionNode(unionNode2.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI2(M),
                        Y, NULL
                        )));
        queryBuilder.addChild(unionNode2, subQuery2UnionNode2);

        queryBuilder.addChild(subQuery2UnionNode2, createExtensionalDataNode(TABLE6_AR2, ImmutableList.of(M, N)) );


        //first child of JoinNode
        ConstructionNode subQueryJoinNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(B,X),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of( X, generateIRIWithTemplate1(A))));
        queryBuilder.addChild(joinNode, subQueryJoinNode);

        queryBuilder.addChild(subQueryJoinNode, createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B)) );

        //second child of JoinNode

        queryBuilder.addChild(joinNode, createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(E, B)) );


        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);

        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(unOptimizedQuery);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        Variable f0f1 = TERM_FACTORY.getVariable("f0f1");
        Variable f2 = TERM_FACTORY.getVariable("f2");
        Variable f3 = TERM_FACTORY.getVariable("f3");

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, TERM_FACTORY.getIRIFunctionalTerm(f0f1),
                        Y, TERM_FACTORY.getRDFFunctionalTerm(f2, f3))));
        expectedQueryBuilder.init(projectionAtom, topConstructionNode);

        UnionNode expectedUnionNode =  IQ_FACTORY.createUnionNode(ImmutableSet.of(f0f1, f2, f3));
        expectedQueryBuilder.addChild(topConstructionNode, expectedUnionNode);

        //first child of UnionNode
        ConstructionNode expSubQueryUnionNode = IQ_FACTORY.createConstructionNode(expectedUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        f2, generateIRIWithTemplate1(B).getTerm(0),
                        f3, TERM_FACTORY.getRDFTermTypeConstant(TYPE_FACTORY.getIRITermType()),
                        f0f1, generateIRIString(URI_TEMPLATE_STR_1, A)
                )));
        expectedQueryBuilder.addChild(expectedUnionNode, expSubQueryUnionNode);

        InnerJoinNode expectedJoinNode = IQ_FACTORY.createInnerJoinNode();
        expectedQueryBuilder.addChild(expSubQueryUnionNode, expectedJoinNode);

        expectedQueryBuilder.addChild(expectedJoinNode, createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B)) );
        expectedQueryBuilder.addChild(expectedJoinNode, IQ_FACTORY.createExtensionalDataNode(
                TABLE3_AR2, ImmutableMap.of(1, B)) );

        ConstructionNode expectedRightConstructionNode = IQ_FACTORY.createConstructionNode(expectedUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        f2, NULL,
                        f3, NULL
                )));
        expectedQueryBuilder.addChild(expectedUnionNode, expectedRightConstructionNode);

        UnionNode expectedUnionNode2 =  IQ_FACTORY.createUnionNode(ImmutableSet.of(f0f1));
        expectedQueryBuilder.addChild(expectedRightConstructionNode, expectedUnionNode2);


        //first child of unionNode2
        ConstructionNode expSubQuery1UnionNode2 = IQ_FACTORY.createConstructionNode(expectedUnionNode2.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(f0f1, generateIRIString(URI_TEMPLATE_STR_1, I))));
        expectedQueryBuilder.addChild(expectedUnionNode2, expSubQuery1UnionNode2);

        expectedQueryBuilder.addChild(expSubQuery1UnionNode2, IQ_FACTORY.createExtensionalDataNode(
                TABLE5_AR2, ImmutableMap.of(0, I)) );

        //second child of unionNode2
        ConstructionNode expSubQuery2UnionNode2 = IQ_FACTORY.createConstructionNode(expectedUnionNode2.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(f0f1, generateIRIString(URI_TEMPLATE_STR_2, M))));
        expectedQueryBuilder.addChild(expectedUnionNode2, expSubQuery2UnionNode2);

        expectedQueryBuilder.addChild(expSubQuery2UnionNode2, IQ_FACTORY.createExtensionalDataNode(
                TABLE6_AR2, ImmutableMap.of(0, M)) );

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
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);

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

        queryBuilder.addChild(subQuery1UnionNode2, createExtensionalDataNode(TABLE5_AR2, ImmutableList.of(I, L)) );

        //second child of unionNode2
        ConstructionNode subQuery2UnionNode2 = IQ_FACTORY.createConstructionNode(unionNode2.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateCompositeURI2(M, N),
                        Y, NULL
                        )));
        queryBuilder.addChild(unionNode2, subQuery2UnionNode2);

        queryBuilder.addChild(subQuery2UnionNode2, createExtensionalDataNode(TABLE6_AR2, ImmutableList.of(M, N)) );


        //first child of JoinNode
        ConstructionNode subQueryJoinNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of( X, generateCompositeURI2(A, B), Y, generateIRIWithTemplate1(B))));
        queryBuilder.addChild(joinNode, subQueryJoinNode);

        queryBuilder.addChild(subQueryJoinNode, createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B)) );

        //second child of JoinNode
        ConstructionNode subQueryJoinNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of( Y, generateIRIWithTemplate1(F))));
        queryBuilder.addChild(joinNode, subQueryJoinNode2);

        queryBuilder.addChild(subQueryJoinNode2, createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(E, F)) );


        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);

        Variable af3 = TERM_FACTORY.getVariable("af3");
        Variable bf4 = TERM_FACTORY.getVariable("bf4");
        Variable f5 = TERM_FACTORY.getVariable("f5");
        Variable f6 = TERM_FACTORY.getVariable("f6");

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateCompositeURI2(af3,bf4),
                        Y, TERM_FACTORY.getRDFFunctionalTerm(f5, f6)
                        )));


        expectedQueryBuilder.init(projectionAtom, expectedRootNode);

        UnionNode expectedUnionNode =  IQ_FACTORY.createUnionNode(ImmutableSet.of(bf4, af3, f5, f6));
        expectedQueryBuilder.addChild(expectedRootNode, expectedUnionNode);

        //first child of UnionNode
        ConstructionNode expSubQueryUnionNode = IQ_FACTORY.createConstructionNode(expectedUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        f5, generateIRIWithTemplate1(bf4).getTerm(0),
                        f6, TERM_FACTORY.getRDFTermTypeConstant(TYPE_FACTORY.getIRITermType()))));
        expectedQueryBuilder.addChild(expectedUnionNode, expSubQueryUnionNode);

        InnerJoinNode expectedJoinNode = IQ_FACTORY.createInnerJoinNode();
        expectedQueryBuilder.addChild(expSubQueryUnionNode, expectedJoinNode);

        expectedQueryBuilder.addChild(expectedJoinNode, createExtensionalDataNode(TABLE1_AR2,ImmutableList.of(af3,bf4)) );

        //second child of JoinNode

        expectedQueryBuilder.addChild(expectedJoinNode, IQ_FACTORY.createExtensionalDataNode(
                TABLE3_AR2, ImmutableMap.of(1, bf4)) );


        ConstructionNode newRightConstructionNode = IQ_FACTORY.createConstructionNode(expectedUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        f5, NULL,
                        f6, NULL)));
        expectedQueryBuilder.addChild(expectedUnionNode, newRightConstructionNode);

        UnionNode expectedUnionNode2 =  IQ_FACTORY.createUnionNode(ImmutableSet.of(af3, bf4));
        expectedQueryBuilder.addChild(newRightConstructionNode, expectedUnionNode2);

        //first child of unionNode2

        expectedQueryBuilder.addChild(expectedUnionNode2, createExtensionalDataNode(TABLE5_AR2, ImmutableList.of(af3, bf4)) );

        //second child of unionNode2

        expectedQueryBuilder.addChild(expectedUnionNode2, createExtensionalDataNode(TABLE6_AR2, ImmutableList.of(af3, bf4)) );

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
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, X);

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

        queryBuilder.addChild(subQuery1UnionNode2, createExtensionalDataNode(TABLE5_AR2, ImmutableList.of(I, L)) );

        //second child of unionNode2
        ConstructionNode subQuery2UnionNode2 = IQ_FACTORY.createConstructionNode(unionNode2.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateCompositeURI2(M, N),
                        Y, NULL
                        )));
        queryBuilder.addChild(unionNode2, subQuery2UnionNode2);

        queryBuilder.addChild(subQuery2UnionNode2, createExtensionalDataNode(TABLE6_AR2, ImmutableList.of(M, N)) );


        //first child of JoinNode
        ConstructionNode subQueryJoinNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of( X, generateCompositeURI2(A, B), Y, generateIRIWithTemplate1(B))));
        queryBuilder.addChild(joinNode, subQueryJoinNode);

        queryBuilder.addChild(subQueryJoinNode, createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B)) );

        //second child of JoinNode
        ConstructionNode subQueryJoinNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of( Y, generateIRIWithTemplate1(F))));
        queryBuilder.addChild(joinNode, subQueryJoinNode2);

        queryBuilder.addChild(subQueryJoinNode2, createExtensionalDataNode(TABLE3_AR2,ImmutableList.of( E, F)) );


        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);

        Variable af3 = TERM_FACTORY.getVariable("af3");
        Variable bf4 = TERM_FACTORY.getVariable("bf4");
        Variable f5 = TERM_FACTORY.getVariable("f5");
        Variable f6 = TERM_FACTORY.getVariable("f6");

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of( X, generateCompositeURI2(af3,bf4))));


        expectedQueryBuilder.init(projectionAtom, expectedRootNode);

        UnionNode expectedUnionNode =  IQ_FACTORY.createUnionNode(ImmutableSet.of(bf4, af3));
        expectedQueryBuilder.addChild(expectedRootNode, expectedUnionNode);

        //first child of UnionNode

        InnerJoinNode expectedJoinNode = IQ_FACTORY.createInnerJoinNode();
        expectedQueryBuilder.addChild(expectedUnionNode, expectedJoinNode);

        expectedQueryBuilder.addChild(expectedJoinNode, createExtensionalDataNode(TABLE1_AR2,ImmutableList.of(af3,bf4)) );

        //second child of JoinNode

        expectedQueryBuilder.addChild(expectedJoinNode, IQ_FACTORY.createExtensionalDataNode(
                TABLE3_AR2, ImmutableMap.of(1, bf4)) );

        //first child of unionNode2

        expectedQueryBuilder.addChild(expectedUnionNode, createExtensionalDataNode(TABLE5_AR2, ImmutableList.of(af3, bf4)) );

        //second child of unionNode2

        expectedQueryBuilder.addChild(expectedUnionNode, createExtensionalDataNode(TABLE6_AR2, ImmutableList.of(af3, bf4)) );

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
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR4_PREDICATE, X, Y, Z, W);

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

        queryBuilder.addChild(rightNodeJoin, createExtensionalDataNode(TABLE4_AR2, ImmutableList.of(G, H)) );

        //construct left side left join (union)
        UnionNode unionNodeOnLeft = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y));
        queryBuilder.addChild(leftJoinNode, unionNodeOnLeft, LEFT);

        ConstructionNode leftUnionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateIRIWithTemplate1(A),
                        Y, generateInt(B))));
        queryBuilder.addChild(unionNodeOnLeft, leftUnionNode);

        queryBuilder.addChild(leftUnionNode, createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B)) );

        ConstructionNode rightUnionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateURI2(C),
                        Y, generateInt(D))));
        queryBuilder.addChild(unionNodeOnLeft, rightUnionNode);

        queryBuilder.addChild(rightUnionNode, createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, D)) );

        //construct right side  left join
        ConstructionNode nodeOnRight = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Z),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateIRIWithTemplate1(E), Z, generateInt(F))));
        queryBuilder.addChild(leftJoinNode, nodeOnRight, RIGHT);

        queryBuilder.addChild(nodeOnRight, createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(E, F)) );



        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);

        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(unOptimizedQuery);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);



        //----------------------------------------------------------------------
        // Construct expected query
        //Construct unoptimized query
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        W, generateInt(H),
                        X, TERM_FACTORY.getIRIFunctionalTerm(F0),
                        Y, generateInt(BF1),
                        Z, TERM_FACTORY.getRDFFunctionalTerm(
                                F,
                                TERM_FACTORY.getIfElseNull(
                                        TERM_FACTORY.getDBIsNotNull(F),
                                        TERM_FACTORY.getRDFTermTypeConstant(TYPE_FACTORY.getDatatype(XSD.INTEGER)))))));

        expectedQueryBuilder.init(projectionAtom, expectedRootNode);


        //construct union
        UnionNode expectedUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(F0, BF1, F, H));
        expectedQueryBuilder.addChild(expectedRootNode, expectedUnionNode);


        //construct union left side

        ConstructionNode expectedNodeOnLeft =IQ_FACTORY.createConstructionNode(expectedUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        F0, generateIRIString(URI_TEMPLATE_STR_1, A))));

        expectedQueryBuilder.addChild(expectedUnionNode, expectedNodeOnLeft);

        //construct left join
        LeftJoinNode expectedLeftJoinNode = IQ_FACTORY.createLeftJoinNode();
        expectedQueryBuilder.addChild(expectedNodeOnLeft, expectedLeftJoinNode);

        InnerJoinNode leftInnerJoinNode = IQ_FACTORY.createInnerJoinNode();

        //construct left side left join
        expectedQueryBuilder.addChild(expectedLeftJoinNode, leftInnerJoinNode, LEFT);

        ExtensionalDataNode newDataNode2 = createExtensionalDataNode(TABLE4_AR2, ImmutableList.of(BF1, H));

        expectedQueryBuilder.addChild(leftInnerJoinNode, createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, BF1)));
        expectedQueryBuilder.addChild(leftInnerJoinNode, newDataNode2);


        //construct right side left join
        expectedQueryBuilder.addChild(expectedLeftJoinNode,
                createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(A, F)), RIGHT);

        ConstructionNode expectedNodeOnRight =IQ_FACTORY.createConstructionNode(expectedUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(F, NULL,
                        F0, generateIRIString(URI_TEMPLATE_STR_2, C))));

        expectedQueryBuilder.addChild(expectedUnionNode, expectedNodeOnRight);

        InnerJoinNode rightInnerJoinNode = IQ_FACTORY.createInnerJoinNode();
        expectedQueryBuilder.addChild(expectedNodeOnRight, rightInnerJoinNode);

        expectedQueryBuilder.addChild(rightInnerJoinNode, createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, BF1)));
        expectedQueryBuilder.addChild(rightInnerJoinNode, newDataNode2.clone());

        //build expected query
        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));


    }

    @Test
    public void testConstantNonPropagationAcrossUnions() throws EmptyQueryException {

        //Construct unoptimized query
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(Y, generateIRIWithTemplate1(B))));

        queryBuilder.init(projectionAtom, rootNode);

        UnionNode topUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, B));
        queryBuilder.addChild(rootNode, topUnionNode);

        UnionNode leftUnionNode = topUnionNode.clone();
        queryBuilder.addChild(topUnionNode, leftUnionNode);

        DBConstant two = TERM_FACTORY.getDBConstant("2", TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType());

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(leftUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateIRIWithTemplate1(A),
                        B, two)));
        queryBuilder.addChild(leftUnionNode, constructionNode1);

        ExtensionalDataNode dataNode7 = createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(A));
        queryBuilder.addChild(constructionNode1, dataNode7);

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(leftUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateURI2(C),
                        B, two)));
        queryBuilder.addChild(leftUnionNode, constructionNode2);

        ExtensionalDataNode dataNode8 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(C));
        queryBuilder.addChild(constructionNode2, dataNode8);


        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topUnionNode, joinNode);


        DBConstant three = TERM_FACTORY.getDBConstant("3", TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType());

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(leftUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateIRIWithTemplate1(D),
                        B, three)));
        queryBuilder.addChild(joinNode, constructionNode3);

        ExtensionalDataNode dataNode9 = createExtensionalDataNode(TABLE3_AR1, ImmutableList.of(D));
        queryBuilder.addChild(constructionNode3, dataNode9);

        ConstructionNode lastConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateIRIWithTemplate1(E))));
        queryBuilder.addChild(joinNode, lastConstructionNode);
        ExtensionalDataNode dataNode10 = createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(E));
        queryBuilder.addChild(lastConstructionNode, dataNode10);

        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);

        Variable f0f2 = TERM_FACTORY.getVariable("f0f2");

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, TERM_FACTORY.getIRIFunctionalTerm(f0f2),
                        Y, generateIRIWithTemplate1(B))));
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        UnionNode newTopUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(f0f2, B));
        expectedQueryBuilder.addChild(newRootNode, newTopUnionNode);

        ConstructionNode newLeftConstructionNode = IQ_FACTORY.createConstructionNode(newTopUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        B, two)));
        expectedQueryBuilder.addChild(newTopUnionNode, newLeftConstructionNode);
        UnionNode newLeftUnionNode = IQ_FACTORY.createUnionNode(newLeftConstructionNode.getChildVariables());
        expectedQueryBuilder.addChild(newLeftConstructionNode, newLeftUnionNode);

        ConstructionNode newConstructionNode1 = IQ_FACTORY.createConstructionNode(newLeftUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        f0f2, generateIRIString(URI_TEMPLATE_STR_1, A))));
        expectedQueryBuilder.addChild(newLeftUnionNode, newConstructionNode1);
        expectedQueryBuilder.addChild(newConstructionNode1, createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(A)));

        ConstructionNode newConstructionNode2 = IQ_FACTORY.createConstructionNode(newLeftUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        f0f2, generateIRIString(URI_TEMPLATE_STR_2, C))));
        expectedQueryBuilder.addChild(newLeftUnionNode, newConstructionNode2);
        expectedQueryBuilder.addChild(newConstructionNode2, createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(C)));


        ConstructionNode newConstructionNode3 = IQ_FACTORY.createConstructionNode(newTopUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        f0f2, generateIRIString(URI_TEMPLATE_STR_1, D),
                        B, three)));
        expectedQueryBuilder.addChild(newTopUnionNode, newConstructionNode3);
        expectedQueryBuilder.addChild(newConstructionNode3, joinNode);
        expectedQueryBuilder.addChild(joinNode, createExtensionalDataNode(TABLE3_AR1, ImmutableList.of(D)));
        expectedQueryBuilder.addChild(joinNode, createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(D)));

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
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(Y, generateIRIWithTemplate1(B))));

        queryBuilder.init(projectionAtom, rootNode);

        UnionNode topUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, B));
        queryBuilder.addChild(rootNode, topUnionNode);

        UnionNode leftUnionNode = topUnionNode.clone();
        queryBuilder.addChild(topUnionNode, leftUnionNode);

        DBConstant two = TERM_FACTORY.getDBConstant("2", TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType());

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(leftUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateIRIWithTemplate1(A),
                        B, two)));
        queryBuilder.addChild(leftUnionNode, constructionNode1);

        ExtensionalDataNode dataNode7 = createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(A));
        queryBuilder.addChild(constructionNode1, dataNode7);

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(leftUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateURI2(C),
                        B, two)));
        queryBuilder.addChild(leftUnionNode, constructionNode2);

        ExtensionalDataNode dataNode8 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(C));
        queryBuilder.addChild(constructionNode2, dataNode8);


        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topUnionNode, joinNode);

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateIRIWithTemplate1(D))));
        queryBuilder.addChild(joinNode, constructionNode3);

        ExtensionalDataNode dataNode9 = createExtensionalDataNode(TABLE3_AR1, ImmutableList.of(D));
        queryBuilder.addChild(constructionNode3, dataNode9);

        ExtensionalDataNode dataNode10 = createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(B));
        queryBuilder.addChild(joinNode, dataNode10);

        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);

        Variable f0f1 = TERM_FACTORY.getVariable("f0f1");
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, TERM_FACTORY.getIRIFunctionalTerm(f0f1),
                        Y, generateIRIWithTemplate1(B))));
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        UnionNode newTopUnionNode = IQ_FACTORY.createUnionNode(newRootNode.getChildVariables());
        expectedQueryBuilder.addChild(newRootNode, newTopUnionNode);

        ConstructionNode newLeftConstructionNode = IQ_FACTORY.createConstructionNode(newTopUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        B, two)));
        expectedQueryBuilder.addChild(newTopUnionNode, newLeftConstructionNode);
        UnionNode newLeftUnionNode = IQ_FACTORY.createUnionNode(newLeftConstructionNode.getChildVariables());
        expectedQueryBuilder.addChild(newLeftConstructionNode, newLeftUnionNode);

        ConstructionNode newConstructionNode1 = IQ_FACTORY.createConstructionNode(newLeftUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        f0f1, generateIRIString(URI_TEMPLATE_STR_1, A))));
        expectedQueryBuilder.addChild(newLeftUnionNode, newConstructionNode1);
        expectedQueryBuilder.addChild(newConstructionNode1, createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(A)));

        ConstructionNode newConstructionNode2 = IQ_FACTORY.createConstructionNode(newLeftUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        f0f1, generateIRIString(URI_TEMPLATE_STR_2, C))));
        expectedQueryBuilder.addChild(newLeftUnionNode, newConstructionNode2);
        expectedQueryBuilder.addChild(newConstructionNode2, createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(C)));


        ConstructionNode constructionNode3b = IQ_FACTORY.createConstructionNode(newTopUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        f0f1, generateIRIString(URI_TEMPLATE_STR_1, D))));
        expectedQueryBuilder.addChild(newTopUnionNode, constructionNode3b);
        expectedQueryBuilder.addChild(constructionNode3b, joinNode);
        expectedQueryBuilder.addChild(joinNode, createExtensionalDataNode(TABLE3_AR1, ImmutableList.of(D)));
        expectedQueryBuilder.addChild(joinNode, dataNode10);

        //build expected query
        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\n Expected query: \n" +  expectedQuery);


        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(unOptimizedQuery);
        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));

    }

    @Test
    public void testTrueNode() throws EmptyQueryException {
        testTrueNode(TERM_FACTORY.getStrictEquality(
                buildSparqlDatatype(X), buildSparqlDatatype(Y)));
    }

    private void testTrueNode(ImmutableExpression joiningCondition) throws EmptyQueryException {
        //Construct unoptimized query
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(joiningCondition);

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
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(Y, generateInt(
                        TERM_FACTORY.getDBStringConstant("2")))));

        queryBuilder.addChild(joinNode, otherNode);
        queryBuilder.addChild(otherNode, IQ_FACTORY.createTrueNode());

        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);

        Variable af0 = TERM_FACTORY.getVariable("af0");

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateInt(af0), Y,
                        TERM_FACTORY.getRDFLiteralConstant("2", XSD.INTEGER))));
        expectedQueryBuilder.init(projectionAtom, expectedRootNode);

        expectedQueryBuilder.addChild(expectedRootNode, IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR2, ImmutableMap.of(0, af0)));
        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\nExpected query: \n" +  expectedQuery);

        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(unOptimizedQuery);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }

    @Test
    public void testJoinAndNotMatchingDatatypesDoubleRun() throws EmptyQueryException {
        testJoinAndNotMatchingDatatypesDoubleRun(TERM_FACTORY.getStrictEquality(
                buildSparqlDatatype(X), buildSparqlDatatype(Y)));
    }


    private void testJoinAndNotMatchingDatatypesDoubleRun(ImmutableExpression joiningCondition) throws EmptyQueryException {
        //Construct unoptimized query
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of()));
        queryBuilder.init(projectionAtom, rootNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(joiningCondition);
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

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateInt(AF0), Y, generateInt(F))));
        expectedQueryBuilder.init(projectionAtom, expectedRootNode);

        InnerJoinNode expectedJoinNode = IQ_FACTORY.createInnerJoinNode();
        expectedQueryBuilder.addChild(expectedRootNode, expectedJoinNode);

        expectedQueryBuilder.addChild(expectedJoinNode, IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR2, ImmutableMap.of(0, AF0)));
        expectedQueryBuilder.addChild(expectedJoinNode, IQ_FACTORY.createExtensionalDataNode(
                TABLE3_AR2, ImmutableMap.of(1, F)));
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
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of()));
        queryBuilder.init(projectionAtom, rootNode);

        InnerJoinNode jn = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getStrictEquality(
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
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
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
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);


        //construct
        QueryNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.init(projectionAtom, joinNode);

        //construct left side join
        LeftJoinNode leftJoinNode =  IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(joinNode, leftJoinNode);

        ConstructionNode leftNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        Y, generateIRIWithTemplate1(B))));

        queryBuilder.addChild(leftJoinNode, leftNode, LEFT);

        queryBuilder.addChild(leftNode, DATA_NODE_9);

        ConstructionNode rightNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI2(C),
                        Y, generateIRIWithTemplate1(D))));

        queryBuilder.addChild(leftJoinNode, rightNode, RIGHT);

        queryBuilder.addChild(rightNode, DATA_NODE_3);


        //construct right side join
        ConstructionNode node1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateIRIWithTemplate1(A))));
        queryBuilder.addChild(joinNode, node1);

        queryBuilder.addChild(node1, DATA_NODE_8);

        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);

        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(unOptimizedQuery);
        System.out.println("\nUnexpected optimized query: \n" +  optimizedQuery);
    }

    @Test
    public void testUnionRemoval() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        queryBuilder.init(projectionAtom, rootNode);

        UnionNode unionNode = IQ_FACTORY.createUnionNode(projectionAtom.getVariables());
        queryBuilder.addChild(rootNode, unionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(unionNode, joinNode);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateIRIWithTemplate1(A),
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
                        X, generateIRIWithTemplate1(E),
                        Y, generateURI2(F))));
        queryBuilder.addChild(unionNode, constructionNode3);
        queryBuilder.addChild(constructionNode3, DATA_NODE_6);

        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);


        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        expectedQueryBuilder.init(projectionAtom, constructionNode3);
        expectedQueryBuilder.addChild(constructionNode3, DATA_NODE_6);
        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        System.out.println("\nExpected query: \n" +  expectedQuery);


        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(unOptimizedQuery);
        System.out.println("\nOptimized query: \n" +  optimizedQuery);

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
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.init(projectionAtom, joinNode);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateIRIWithTemplate1(A),
                        Y, generateURI2(B))));

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateIRIWithTemplate1(ONE_STR))));

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


        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        ImmutableMap.of(
                                X, generateIRIWithTemplate1(ONE_STR).simplify(),
                                Y, generateURI2(B))));
        expectedQueryBuilder.init(projectionAtom, newConstructionNode);
        ExtensionalDataNode newLeftDataNode = IQ_FACTORY.createExtensionalDataNode(
                DATA_NODE_1.getRelationDefinition(), ImmutableMap.of(
                        0, ONE_STR,
                        1, B));
        expectedQueryBuilder.addChild(newConstructionNode, newLeftDataNode);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        System.out.println("\nExpected query: \n" +  expectedQuery);


        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(unOptimizedQuery);
        System.out.println("\nOptimized query: \n" +  optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }



    @Test
    public void testUnionWithJoin() throws EmptyQueryException {
        /*
         * Original Query
         */

        DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
                ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                        ANS1_AR3_PREDICATE, ImmutableList.of(X,Y, Z));

        IntermediateQueryBuilder originalBuilder = createQueryBuilder();

        ConstructionNode emptyConstructionNode = IQ_FACTORY.createConstructionNode(ROOT_CONSTRUCTION_NODE_ATOM.getVariables());
        ConstructionNode emptyConstructionNode2 = emptyConstructionNode.clone();

        UnionNode unionNode1  = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y, Z));
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        UnionNode unionNode21 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y));
        UnionNode unionNode22  = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, C));
        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode();

        ConstructionNode constructionNode21 = IQ_FACTORY.createConstructionNode(unionNode21.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateIRIWithTemplate1(A),
                        Y, generateURI2(B)));

        ConstructionNode constructionNode22 = IQ_FACTORY.createConstructionNode(unionNode22.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(D)));

        ConstructionNode constructionNode22URI2 = IQ_FACTORY.createConstructionNode(unionNode22.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(D)));

        ConstructionNode constructionNode21URI2 = IQ_FACTORY.createConstructionNode(unionNode21.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI2(A),
                        Y, generateURI2(B)));

        ConstructionNode constructionNode21URI1XY = constructionNode21.clone();

        ConstructionNode constructionNode22Z = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Z),
                SUBSTITUTION_FACTORY.getSubstitution(Z, generateIRIWithTemplate1(C)));

        ConstructionNode constructionNodeOverJoin2 = constructionNode22.clone();
        ConstructionNode constructionNodeOverJoin1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Z),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateIRIWithTemplate1(F),
                        Z, generateIRIWithTemplate1(G)));

        ExtensionalDataNode table1DataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode table2DataNode = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode table3DataNode = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode table4DataNode1 = createExtensionalDataNode(TABLE4_AR2, ImmutableList.of(D, C));
        ExtensionalDataNode table4DataNode2 = createExtensionalDataNode(TABLE4_AR2, ImmutableList.of(D, C));
        ExtensionalDataNode table4DataNode3 = createExtensionalDataNode(TABLE4_AR2, ImmutableList.of(D, C));
        ExtensionalDataNode table5DataNode1 = createExtensionalDataNode(TABLE5_AR3, ImmutableList.of(X, Y, Z));
        ExtensionalDataNode table5DataNode2 = createExtensionalDataNode(TABLE5_AR3, ImmutableList.of(D, E, C));
        ExtensionalDataNode table6DataNode = createExtensionalDataNode(TABLE6_AR2, ImmutableList.of(F, G));

        originalBuilder.init(ROOT_CONSTRUCTION_NODE_ATOM, unionNode1);
        originalBuilder.addChild(unionNode1, emptyConstructionNode);
        originalBuilder.addChild(emptyConstructionNode, joinNode);
        originalBuilder.addChild(unionNode1, emptyConstructionNode2);
        originalBuilder.addChild(emptyConstructionNode2, table5DataNode1);

        originalBuilder.addChild(joinNode, unionNode21);
        originalBuilder.addChild(joinNode, constructionNode22Z);
        originalBuilder.addChild(constructionNode22Z, unionNode22);
        originalBuilder.addChild(joinNode,constructionNodeOverJoin1 );
        originalBuilder.addChild(constructionNodeOverJoin1, table6DataNode);
        originalBuilder.addChild(unionNode21, constructionNode21);
        originalBuilder.addChild(constructionNode21, table1DataNode);
        originalBuilder.addChild(unionNode21, constructionNode21URI2);
        originalBuilder.addChild(constructionNode21URI2, table2DataNode);
        originalBuilder.addChild(unionNode21, constructionNode21URI1XY);
        originalBuilder.addChild(constructionNode21URI1XY, table3DataNode);
        originalBuilder.addChild(unionNode22, constructionNode22);
        originalBuilder.addChild(constructionNode22, table4DataNode1);
        originalBuilder.addChild(unionNode22, constructionNode22URI2);
        originalBuilder.addChild(constructionNode22URI2, table5DataNode2);
        originalBuilder.addChild(unionNode22,constructionNodeOverJoin2 );
        originalBuilder.addChild(constructionNodeOverJoin2, joinNode1);
        originalBuilder.addChild(joinNode1, table4DataNode2);
        originalBuilder.addChild(joinNode1, table4DataNode3);



        IntermediateQuery originalQuery = originalBuilder.build();

        System.out.println("\n Original query: \n" +  originalQuery);

        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(originalQuery);

        System.out.println("\n Optimized query: \n" +  optimizedQuery);

        /*
         * Expected Query
         */
        Variable bf2f3 = TERM_FACTORY.getVariable("bf2f3");
        IntermediateQueryBuilder expectedBuilder = createQueryBuilder();

        UnionNode unionNode3 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y, Z));
        UnionNode unionNode4 = IQ_FACTORY.createUnionNode(ImmutableSet.of(F, bf2f3));
        UnionNode unionNode5 = IQ_FACTORY.createUnionNode(ImmutableSet.of(F,C));

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y, Z),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        X, generateIRIWithTemplate1(F), Y, generateURI2(bf2f3), Z, generateIRIWithTemplate1(C))));
        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode();
        InnerJoinNode joinNode3 = IQ_FACTORY.createInnerJoinNode();

        ExtensionalDataNode newTable1DataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(F, bf2f3));
        ExtensionalDataNode newTable3DataNode = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(F, bf2f3));

        ExtensionalDataNode newTable4DataNode = createExtensionalDataNode(TABLE4_AR2, ImmutableList.of(F, C));
        ExtensionalDataNode newTable6DataNode = createExtensionalDataNode(TABLE6_AR2, ImmutableList.of(F, C));


        expectedBuilder.init(ROOT_CONSTRUCTION_NODE_ATOM, unionNode3);
        expectedBuilder.addChild(unionNode3, constructionNode1);
        expectedBuilder.addChild(unionNode3, table5DataNode1);
        expectedBuilder.addChild(constructionNode1, joinNode2);
        expectedBuilder.addChild(joinNode2, unionNode4);
        expectedBuilder.addChild(unionNode4, newTable1DataNode);
        expectedBuilder.addChild(unionNode4, newTable3DataNode);
        expectedBuilder.addChild(joinNode2, unionNode5);
        expectedBuilder.addChild(unionNode5, newTable4DataNode);
        expectedBuilder.addChild(unionNode5, joinNode3);
        expectedBuilder.addChild(joinNode2, newTable6DataNode);
        expectedBuilder.addChild(joinNode3, newTable4DataNode.clone());
        expectedBuilder.addChild(joinNode3, newTable4DataNode.clone());

        IntermediateQuery expectedQuery = expectedBuilder.build();

        System.out.println("\n Expected query: \n" +  expectedQuery);

        Assert.assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }

    @Test
    public void testProjectionAwaySubQuery() throws EmptyQueryException {
        //Construct unoptimized query
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, A);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.init(projectionAtom, joinNode);
        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A));
        queryBuilder.addChild(joinNode, leftConstructionNode);
        queryBuilder.addChild(leftConstructionNode, DATA_NODE_1);
        queryBuilder.addChild(joinNode, DATA_NODE_8);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\n Original query: \n" +  query);

        IntermediateQueryBuilder expectedQueryBuilder = IQ_FACTORY.createIQBuilder();
        expectedQueryBuilder.init(projectionAtom, joinNode);
        expectedQueryBuilder.addChild(joinNode, IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR2, ImmutableMap.of(0,A)));
        expectedQueryBuilder.addChild(joinNode, DATA_NODE_8);
        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        System.out.println("\n Expected query: \n" +  expectedQuery);

        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(query);
        System.out.println("\n Optimized query: \n" +  optimizedQuery);

        Assert.assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));

    }

    @Test
    public void testLeftJoin2Join1() {
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(A));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(B));

        IQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(), dataNode1, dataNode2);

        IQTree filterNodeTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createFilterNode(TERM_FACTORY.getStrictEquality(A, B)),
                leftJoinTree);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, A);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQTree constructionTree = IQ_FACTORY.createUnaryIQTree(constructionNode, filterNodeTree);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, constructionTree);

        // Expected
        ExtensionalDataNode newDataNode2 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(A));
        IQTree joinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(dataNode1, newDataNode2));

        IQ expectedIQ = IQ_FACTORY.createIQ(initialIQ.getProjectionAtom(), joinTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLeftJoin2Join2() {
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(A));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(B, C));

        IQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(), dataNode1, dataNode2);

        IQTree filterNodeTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createFilterNode(TERM_FACTORY.getStrictEquality(B, C)),
                leftJoinTree);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQTree constructionTree = IQ_FACTORY.createUnaryIQTree(constructionNode, filterNodeTree);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, constructionTree);

        // Expected
        ExtensionalDataNode newDataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(B, B));
        IQTree newJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(dataNode1, newDataNode2));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newJoinTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLeftJoin2Join3() {
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(A));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(B));

        IQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(), dataNode1, dataNode2);

        IQTree filterNodeTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createFilterNode(TERM_FACTORY.getStrictEquality(B, ONE)),
                leftJoinTree);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQTree constructionTree = IQ_FACTORY.createUnaryIQTree(constructionNode, filterNodeTree);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, constructionTree);

        // Expected
        ExtensionalDataNode newDataNode2 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(ONE));
        IQTree newJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(dataNode1, newDataNode2));

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(B, ONE));

        IQTree newConstructionTree = IQ_FACTORY.createUnaryIQTree(newConstructionNode, newJoinTree);
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newConstructionTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Ignore("TODO: support this")
    @Test
    public void testLeftJoin2Join4() {
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(A));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(B));

        // TODO: use another function (a UDF for instance)
        GroundFunctionalTerm groundTerm = (GroundFunctionalTerm) TERM_FACTORY.getConstantIRI(
                RDF_FACTORY.createIRI("http://my-uri/constant"));

        IQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(), dataNode1, dataNode2);

        ImmutableExpression filterCondition = TERM_FACTORY.getStrictEquality(groundTerm, B);
        FilterNode filterNode = IQ_FACTORY.createFilterNode(filterCondition);

        IQTree filterNodeTree = IQ_FACTORY.createUnaryIQTree(filterNode, leftJoinTree);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQTree constructionTree = IQ_FACTORY.createUnaryIQTree(constructionNode, filterNodeTree);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, constructionTree);

        // Expected
        ExtensionalDataNode newDataNode2 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(B));
        IQTree newJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(dataNode1, newDataNode2));

        IQTree newFilterNodeTree = IQ_FACTORY.createUnaryIQTree(filterNode, newJoinTree);

        IQTree newConstructionTree = IQ_FACTORY.createUnaryIQTree(constructionNode, newFilterNodeTree);
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newConstructionTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Ignore("TODO: support this")
    @Test
    public void testLeftJoin2Join5() {
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(A));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(B));

        // TODO: use another function (a UDF for instance)
        ImmutableFunctionalTerm functionalTerm = TERM_FACTORY.getIRIFunctionalTerm(A);

        IQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(), dataNode1, dataNode2);

        ImmutableExpression filterCondition = TERM_FACTORY.getStrictEquality(functionalTerm, B);
        FilterNode filterNode = IQ_FACTORY.createFilterNode(filterCondition);

        IQTree filterNodeTree = IQ_FACTORY.createUnaryIQTree(filterNode, leftJoinTree);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQTree constructionTree = IQ_FACTORY.createUnaryIQTree(constructionNode, filterNodeTree);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, constructionTree);

        // Expected
        ExtensionalDataNode newDataNode2 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(B));
        IQTree newJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(dataNode1, newDataNode2));

        IQTree newFilterNodeTree = IQ_FACTORY.createUnaryIQTree(filterNode, newJoinTree);

        IQTree newConstructionTree = IQ_FACTORY.createUnaryIQTree(constructionNode, newFilterNodeTree);
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newConstructionTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    /**
     * Implicit joining condition
     */
    @Ignore("TODO: support it")
    @Test
    public void testLeftJoin2Join6() {
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(A));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(B));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE3_AR1, ImmutableList.of(B));

        IQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(), dataNode1, dataNode2);

        InnerJoinNode topJoinNode = IQ_FACTORY.createInnerJoinNode();

        IQTree topJoinNodeTree = IQ_FACTORY.createNaryIQTree(topJoinNode,
                ImmutableList.of(leftJoinTree, dataNode3));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQTree constructionTree = IQ_FACTORY.createUnaryIQTree(constructionNode, topJoinNodeTree);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, constructionTree);

        // Expected
        ExtensionalDataNode newDataNode2 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(B));
        IQTree newJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(dataNode1, newDataNode2));

        IQTree newTopJoinNodeTree = IQ_FACTORY.createNaryIQTree(topJoinNode, ImmutableList.of(newJoinTree, dataNode3));

        IQTree newConstructionTree = IQ_FACTORY.createUnaryIQTree(constructionNode, newTopJoinNodeTree);
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newConstructionTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testDescendingEqualityLeftJoinPreserved() {
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(B, C));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(), dataNode1, dataNode2);

        UnaryIQTree filterNodeTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createFilterNode(TERM_FACTORY.getStrictEquality(A, B)),
                leftJoinTree);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, C);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQTree constructionTree = IQ_FACTORY.createUnaryIQTree(constructionNode, filterNodeTree);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, constructionTree);

        // Expected
        ExtensionalDataNode newDataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, A));
        ExtensionalDataNode newDataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, C));
        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(), newDataNode1, newDataNode2);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newLeftJoinTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLJConstant1() {
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(B, C));

        ConstructionNode rightConstruction = IQ_FACTORY.createConstructionNode(ImmutableSet.of(B, D),
                SUBSTITUTION_FACTORY.getSubstitution(D, ONE));

        UnaryIQTree rightTree = IQ_FACTORY.createUnaryIQTree(rightConstruction, dataNode2);

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(), dataNode1, rightTree);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, D);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQTree constructionTree = IQ_FACTORY.createUnaryIQTree(constructionNode, leftJoinTree);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, constructionTree);

        Variable prov = TERM_FACTORY.getVariable("prov");
        ExtensionalDataNode newDataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2,
                ImmutableMap.of(0, B, 1, prov));

        // Expected
        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(), dataNode1, newDataNode2);

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(D, generateIfIsNotNullElseNull(prov, ONE)));

        IQTree newConstructionTree = IQ_FACTORY.createUnaryIQTree(newConstructionNode, newLeftJoinTree);
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newConstructionTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLJConstant2() {
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, B));

        ConstructionNode rightConstruction = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, B, D),
                SUBSTITUTION_FACTORY.getSubstitution(D, ONE));

        UnaryIQTree rightTree = IQ_FACTORY.createUnaryIQTree(rightConstruction, dataNode2);

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(), dataNode1, rightTree);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, D);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQTree constructionTree = IQ_FACTORY.createUnaryIQTree(constructionNode, leftJoinTree);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, constructionTree);

        // Expected
        ConstructionNode newRightConstruction = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, B, F0),
                SUBSTITUTION_FACTORY.getSubstitution(F0, TERM_FACTORY.getProvenanceSpecialConstant()));

        UnaryIQTree newRightTree = IQ_FACTORY.createUnaryIQTree(newRightConstruction, dataNode2);

        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(), dataNode1, newRightTree);

        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(D, generateIfIsNotNullElseNull(F0, ONE)));

        IQTree newConstructionTree = IQ_FACTORY.createUnaryIQTree(newRootNode, newLeftJoinTree);
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newConstructionTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLJNull() {
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(B, C));

        ConstructionNode rightConstruction = IQ_FACTORY.createConstructionNode(ImmutableSet.of(B, D),
                SUBSTITUTION_FACTORY.getSubstitution(D, NULL));

        UnaryIQTree rightTree = IQ_FACTORY.createUnaryIQTree(rightConstruction, dataNode2);

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(), dataNode1, rightTree);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, D);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQTree constructionTree = IQ_FACTORY.createUnaryIQTree(constructionNode, leftJoinTree);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, constructionTree);

        // Expected
        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(), dataNode1,
                IQ_FACTORY.createExtensionalDataNode(
                        TABLE2_AR2, ImmutableMap.of(0, B)));

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(D, NULL));

        IQTree newConstructionTree = IQ_FACTORY.createUnaryIQTree(newConstructionNode, newLeftJoinTree);
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newConstructionTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testAscendingSubstitutionNormalization1() {
        ExtensionalDataNode table1 = createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(A));
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y, A),
                SUBSTITUTION_FACTORY.getSubstitution(X, A, Y, A));
        UnaryIQTree subTree1 = IQ_FACTORY.createUnaryIQTree(constructionNode1, table1);
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y));
        UnaryIQTree initialIQTree = IQ_FACTORY.createUnaryIQTree(constructionNode2, subTree1);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);
        IQ initialQuery = IQ_FACTORY.createIQ(projectionAtom, initialIQTree);

        // Expected
        ExtensionalDataNode newTable = createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(X));
        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(Y, X));
        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createUnaryIQTree(constructionNode3, newTable));

        optimizeAndCompare(initialQuery, expectedQuery);
    }

    @Test
    public void testRightFunctionalTerm1() {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, A));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, D));

        UnaryIQTree leftChild = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X),
                        SUBSTITUTION_FACTORY.getSubstitution(
                                X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STR_1, ImmutableList.of(A)))),
                dataNode1);

        ImmutableFunctionalTerm concatFunctionalTerm = TERM_FACTORY.getNullRejectingDBConcatFunctionalTerm(
                ImmutableList.of(C, D));

        UnaryIQTree rightChild = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X, Y),
                        SUBSTITUTION_FACTORY.getSubstitution(
                                X, TERM_FACTORY.getIRIFunctionalTerm(
                                        URI_TEMPLATE_STR_1, ImmutableList.of(concatFunctionalTerm)),
                                Y, TERM_FACTORY.getRDFLiteralFunctionalTerm(D, XSD.STRING))),
                dataNode2);

        BinaryNonCommutativeIQTree initialLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                leftChild, rightChild);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ANS1_ARITY_2_PREDICATE, ImmutableList.of(X, Y));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialLeftJoinTree);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(
                initialLeftJoinTree.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, TERM_FACTORY.getIRIFunctionalTerm(
                                URI_TEMPLATE_STR_1, ImmutableList.of(A)),
                        Y, TERM_FACTORY.getRDFFunctionalTerm(D,
                                TERM_FACTORY.getIfElseNull(
                                        TERM_FACTORY.getDBIsNotNull(D),
                                        TERM_FACTORY.getRDFTermTypeConstant(TYPE_FACTORY.getXsdStringDatatype())))));

        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getStrictEquality(A, concatFunctionalTerm)),
                dataNode1, dataNode2);

        IQ expectedIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(topConstructionNode, newLeftJoinTree));

        assertEquals(expectedIQ, initialIQ.normalizeForOptimization());
    }

    @Test
    public void testRightFunctionalTerm2() {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2, ImmutableMap.of(0, E));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(C, D));

        ImmutableFunctionalTerm iriFunctionalTerm1 = TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STR_1, ImmutableList.of(A));
        ImmutableFunctionalTerm iriFunctionalTerm2 = TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STR_2, ImmutableList.of(E));

        UnaryIQTree leftChild1 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X),
                        SUBSTITUTION_FACTORY.getSubstitution(X, iriFunctionalTerm1)),
                dataNode1);

        UnaryIQTree leftChild2 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X),
                        SUBSTITUTION_FACTORY.getSubstitution(X, iriFunctionalTerm2)),
                dataNode2);

        NaryIQTree leftUnionTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                ImmutableList.of(leftChild1, leftChild2));

        ImmutableFunctionalTerm concatFunctionalTerm = TERM_FACTORY.getNullRejectingDBConcatFunctionalTerm(
                ImmutableList.of(C, D));

        ImmutableFunctionalTerm iriFunctionalTerm3 = TERM_FACTORY.getIRIFunctionalTerm(
                URI_TEMPLATE_STR_1, ImmutableList.of(concatFunctionalTerm));

        UnaryIQTree rightChild = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X, Y),
                        SUBSTITUTION_FACTORY.getSubstitution(
                                X, iriFunctionalTerm3,
                                Y, TERM_FACTORY.getRDFFunctionalTerm(D,
                                        TERM_FACTORY.getIfElseNull(
                                                TERM_FACTORY.getDBIsNotNull(D),
                                                TERM_FACTORY.getRDFTermTypeConstant(TYPE_FACTORY.getXsdStringDatatype()))))),
                dataNode3);

        BinaryNonCommutativeIQTree initialLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                leftUnionTree, rightChild);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ANS1_ARITY_2_PREDICATE, ImmutableList.of(X, Y));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialLeftJoinTree);


        UnaryIQTree newLeftChild1 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(F0),
                        SUBSTITUTION_FACTORY.getSubstitution(F0, iriFunctionalTerm1.getTerm(0))),
                dataNode1);

        UnaryIQTree newLeftChild2 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(F0),
                        SUBSTITUTION_FACTORY.getSubstitution(F0, iriFunctionalTerm2.getTerm(0))),
                dataNode2);

        NaryIQTree newLeftUnionTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(F0)),
                ImmutableList.of(newLeftChild1, newLeftChild2));


        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(
                        TERM_FACTORY.getStrictEquality(F0, iriFunctionalTerm3.getTerm(0))),
                newLeftUnionTree, dataNode3);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(
                initialLeftJoinTree.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, TERM_FACTORY.getIRIFunctionalTerm(F0),
                        Y, TERM_FACTORY.getRDFFunctionalTerm(D,
                                TERM_FACTORY.getIfElseNull(
                                        TERM_FACTORY.getDBIsNotNull(D),
                                        TERM_FACTORY.getRDFTermTypeConstant(TYPE_FACTORY.getXsdStringDatatype()))))
                        );

        IQ expectedIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(topConstructionNode, newLeftJoinTree));

        assertEquals(expectedIQ, initialIQ.normalizeForOptimization());
    }

    @Test
    public void testProvenanceVariableAndProjection1() {
        OfflineMetadataProviderBuilder3 builder = createMetadataProviderBuilder();
        RelationDefinition table1Ar2 = builder.createRelationWithStringAttributes(1, 2, true);
        RelationDefinition table2Ar2 = builder.createRelationWithStringAttributes(2, 2, true);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(table1Ar2,
                ImmutableMap.of(0, A));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(table2Ar2, ImmutableList.of(C, D));

        Variable f1 = TERM_FACTORY.getVariable("f1");

        UnaryIQTree leftChild = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X),
                        SUBSTITUTION_FACTORY.getSubstitution(
                                X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STR_1, ImmutableList.of(A)))),
                dataNode1);

        UnaryIQTree rightChild = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X, Y),
                        SUBSTITUTION_FACTORY.getSubstitution(
                                X, TERM_FACTORY.getIRIFunctionalTerm(
                                        URI_TEMPLATE_STR_1, ImmutableList.of(C)),
                                Y, TERM_FACTORY.getRDFLiteralConstant("Hi", XSD.STRING))),
                dataNode2);

        BinaryNonCommutativeIQTree initialLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                leftChild, rightChild);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ANS1_ARITY_2_PREDICATE, ImmutableList.of(X, Y));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialLeftJoinTree);

        ExtensionalDataNode newDataNode2 = IQ_FACTORY.createExtensionalDataNode(
                table2Ar2, ImmutableMap.of(0, A));

        UnaryIQTree newRightChild = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(A, f1),
                        SUBSTITUTION_FACTORY.getSubstitution(f1, TERM_FACTORY.getProvenanceSpecialConstant())),
                newDataNode2);

        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1, newRightChild);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(
                initialLeftJoinTree.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, TERM_FACTORY.getIRIFunctionalTerm(
                                URI_TEMPLATE_STR_1, ImmutableList.of(A)),
                        Y, TERM_FACTORY.getRDFFunctionalTerm(
                                TERM_FACTORY.getIfElseNull(
                                        TERM_FACTORY.getDBIsNotNull(f1),
                                        TERM_FACTORY.getDBStringConstant("Hi")),
                                TERM_FACTORY.getIfElseNull(
                                        TERM_FACTORY.getDBIsNotNull(f1),
                                        TERM_FACTORY.getRDFTermTypeConstant(TYPE_FACTORY.getXsdStringDatatype())))));

        IQ expectedIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(topConstructionNode, newLeftJoinTree));

        assertEquals(expectedIQ, initialIQ.normalizeForOptimization());
    }

    private static ImmutableFunctionalTerm generateIfIsNotNullElseNull(Variable rightSpecificVariable,
                                                                       ImmutableTerm conditionalValue) {
        return TERM_FACTORY.getIfElseNull(
                TERM_FACTORY.getDBIsNotNull(rightSpecificVariable),
                conditionalValue);
    }

    private static void optimizeAndCompare(IQ initialIQ, IQ expectedIQ) {
        System.out.println("\n Original query: \n" +  initialIQ);
        System.out.println("\n Expected query: \n" +  expectedIQ);

        IQ optimizedIQ = UNION_AND_BINDING_LIFT_OPTIMIZER.optimize(initialIQ);
        System.out.println("\n Optimized query: \n" +  optimizedIQ);

        assertEquals(expectedIQ, optimizedIQ);
    }


    private static ImmutableFunctionalTerm buildSparqlDatatype(ImmutableTerm argument){
        return TERM_FACTORY.getImmutableFunctionalTerm(
                FUNCTION_SYMBOL_FACTORY.getRequiredSPARQLFunctionSymbol(SPARQL.DATATYPE, 1),
                argument);
    }
}
