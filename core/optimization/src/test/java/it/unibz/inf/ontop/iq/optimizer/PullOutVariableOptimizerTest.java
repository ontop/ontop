package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.equivalence.IQSyntacticEquivalenceChecker;
import org.junit.Test;


import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.*;
import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.EQ;
import static junit.framework.TestCase.assertTrue;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;

public class PullOutVariableOptimizerTest {

    private final static AtomPredicate TABLE1_PREDICATE = ATOM_FACTORY.getAtomPredicate("table1", 2);
    private final static AtomPredicate TABLE2_PREDICATE = ATOM_FACTORY.getAtomPredicate("table2", 2);
    private final static AtomPredicate TABLE3_PREDICATE = ATOM_FACTORY.getAtomPredicate("table3", 2);
    private final static AtomPredicate TABLE4_PREDICATE = ATOM_FACTORY.getAtomPredicate("table2", 3);
    private final static AtomPredicate TABLE5_PREDICATE = ATOM_FACTORY.getAtomPredicate("table1", 3);
    private final static AtomPredicate ANS1_PREDICATE1 = ATOM_FACTORY.getAtomPredicate("ans1", 4);
    private final static AtomPredicate ANS1_PREDICATE2 = ATOM_FACTORY.getAtomPredicate("ans1", 3);
    private final static AtomPredicate ANS1_PREDICATE3 = ATOM_FACTORY.getAtomPredicate("ans1", 2);

    private final static Variable X = TERM_FACTORY.getVariable("X");
    private final static Variable X0 = TERM_FACTORY.getVariable("Xf0");
    private final static Variable X1 = TERM_FACTORY.getVariable("Xf1");
    private final static Variable X2 = TERM_FACTORY.getVariable("Xf2");
    private final static Variable X3 = TERM_FACTORY.getVariable("Xf3");
    private final static Variable X4 = TERM_FACTORY.getVariable("Xf0f3");
    private final static Variable X5 = TERM_FACTORY.getVariable("Xf0f1");
    private final static Variable Y = TERM_FACTORY.getVariable("Y");
    private final static Variable Y1 = TERM_FACTORY.getVariable("Yf1");
    private final static Variable Y2 = TERM_FACTORY.getVariable("Yf2");
    private final static Variable Z = TERM_FACTORY.getVariable("Z");
    private final static Variable Z0 = TERM_FACTORY.getVariable("Zf0");
    private final static Variable Z2 = TERM_FACTORY.getVariable("Zf2");
    private final static Variable W = TERM_FACTORY.getVariable("W");
    private final static Variable R = TERM_FACTORY.getVariable("R");
    private final static Variable S = TERM_FACTORY.getVariable("S");
    private final static Variable T = TERM_FACTORY.getVariable("T");

    private final static ImmutableExpression EXPRESSION1 = TERM_FACTORY.getImmutableExpression(
            EQ, X, X0);
    private final static ImmutableExpression EXPRESSION2 = TERM_FACTORY.getImmutableExpression(
            EQ, Y, Y1);
    private final static ImmutableExpression EXPRESSION3 = TERM_FACTORY.getImmutableExpression(
            EQ, X, X1);
    private final static ImmutableExpression EXPRESSION4 = TERM_FACTORY.getImmutableExpression(
            EQ, X, X2);
    private final static ImmutableExpression EXPRESSION5 = TERM_FACTORY.getImmutableExpression(
            EQ, X, X3);
    private final static ImmutableExpression EXPRESSION6 = TERM_FACTORY.getImmutableExpression(
            EQ, Y, Y2);
    private final static ImmutableExpression EXPRESSION7 = TERM_FACTORY.getImmutableExpression(
            EQ, X0, X4);
    private final static ImmutableExpression EXPRESSION8 = TERM_FACTORY.getImmutableExpression(
            EQ, X0, X5);
    private final static ImmutableExpression EXPRESSION_Z_Z0 = TERM_FACTORY.getImmutableExpression(
            EQ, Z, Z0);
    private final static ImmutableExpression EXPRESSION_Z_Z2 = TERM_FACTORY.getImmutableExpression(
            EQ, Z, Z2);

    @Test
    public void testJoiningConditionTest1() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE2, X, Y, Z);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));

        queryBuilder1.init(projectionAtom, constructionNode);
        queryBuilder1.addChild(constructionNode, joinNode1);
        queryBuilder1.addChild(joinNode1, dataNode1);
        queryBuilder1.addChild(joinNode1, dataNode2);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" +  query1);

        IntermediateQuery optimizedQuery = PULL_OUT_VARIABLE_OPTIMIZER.optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom2 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE2, X, Y, Z);
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode(EXPRESSION1);
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, X0, Z));

        queryBuilder2.init(projectionAtom2, constructionNode2);
        queryBuilder2.addChild(constructionNode2, joinNode2);
        queryBuilder2.addChild(joinNode2, dataNode1);
        queryBuilder2.addChild(joinNode2, dataNode3);

        IntermediateQuery query2 = queryBuilder2.build();

        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testJoiningConditionTest2() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE2, X, Y, Z);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        LeftJoinNode leftJoinNode1 = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE4_PREDICATE, X, Y, Z));

        queryBuilder1.init(projectionAtom, constructionNode);
        queryBuilder1.addChild(constructionNode, leftJoinNode1);
        queryBuilder1.addChild(leftJoinNode1, dataNode1, LEFT);
        queryBuilder1.addChild(leftJoinNode1, dataNode2, RIGHT);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" +  query1);

        IntermediateQuery optimizedQuery = PULL_OUT_VARIABLE_OPTIMIZER.optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom2 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE2, X, Y, Z);
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        LeftJoinNode leftJoinNode2 = IQ_FACTORY.createLeftJoinNode(IMMUTABILITY_TOOLS.foldBooleanExpressions(EXPRESSION1, EXPRESSION2));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE4_PREDICATE, X0, Y1, Z));

        queryBuilder2.init(projectionAtom2, constructionNode2);
        queryBuilder2.addChild(constructionNode2, leftJoinNode2);
        queryBuilder2.addChild(leftJoinNode2, dataNode1, LEFT);
        queryBuilder2.addChild(leftJoinNode2, dataNode3, RIGHT);

        IntermediateQuery query2 = queryBuilder2.build();

        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testJoin3() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE2, X, Y, Z);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        ExtensionalDataNode dataNode3 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, R, S));
        ExtensionalDataNode dataNode4 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, W, Y));
        ExtensionalDataNode dataNode5 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, T, Z));

        queryBuilder1.init(projectionAtom, constructionNode);
        queryBuilder1.addChild(constructionNode, joinNode1);
        queryBuilder1.addChild(joinNode1, dataNode1);
        queryBuilder1.addChild(joinNode1, dataNode2);
        queryBuilder1.addChild(joinNode1, dataNode3);
        queryBuilder1.addChild(joinNode1, dataNode4);
        queryBuilder1.addChild(joinNode1, dataNode5);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" +  query1);

        IntermediateQuery optimizedQuery = PULL_OUT_VARIABLE_OPTIMIZER.optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom2 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE2, X, Y, Z);
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode(IMMUTABILITY_TOOLS.foldBooleanExpressions(
                EXPRESSION1, EXPRESSION2, EXPRESSION_Z_Z2));
        ExtensionalDataNode newDataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, X0, Z));
        ExtensionalDataNode newDataNode4 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, W, Y1));
        ExtensionalDataNode newDataNode5 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, T, Z2));

        queryBuilder2.init(projectionAtom2, constructionNode2);
        queryBuilder2.addChild(constructionNode2, joinNode2);
        queryBuilder2.addChild(joinNode2, dataNode1);
        queryBuilder2.addChild(joinNode2, newDataNode2);
        queryBuilder2.addChild(joinNode2, dataNode3);
        queryBuilder2.addChild(joinNode2, newDataNode4);
        queryBuilder2.addChild(joinNode2, newDataNode5);

        IntermediateQuery query2 = queryBuilder2.build();

        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testJoin4() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE2, X, Y, Z);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, W, Z));
        ExtensionalDataNode dataNode3 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, T, Z));

        queryBuilder1.init(projectionAtom, constructionNode);
        queryBuilder1.addChild(constructionNode, joinNode1);
        queryBuilder1.addChild(joinNode1, dataNode1);
        queryBuilder1.addChild(joinNode1, dataNode2);
        queryBuilder1.addChild(joinNode1, dataNode3);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" +  query1);

        IntermediateQuery optimizedQuery = PULL_OUT_VARIABLE_OPTIMIZER.optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom2 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE2, X, Y, Z);
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode(EXPRESSION_Z_Z0);
        ExtensionalDataNode newDataNode3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, T, Z0));

        queryBuilder2.init(projectionAtom2, constructionNode2);
        queryBuilder2.addChild(constructionNode2, joinNode2);
        queryBuilder2.addChild(joinNode2, dataNode1);
        queryBuilder2.addChild(joinNode2, dataNode2);
        queryBuilder2.addChild(joinNode2, newDataNode3);

        IntermediateQuery query2 = queryBuilder2.build();

        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testJoiningConditionTest3() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE3, X, Y);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());

        LeftJoinNode leftJoinNode1 = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE5_PREDICATE, X, X, Y));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE4_PREDICATE, X, Y, X));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, leftJoinNode1);
        queryBuilder1.addChild(leftJoinNode1, dataNode1, LEFT);
        queryBuilder1.addChild(leftJoinNode1, dataNode2, RIGHT);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" +  query1);

        IntermediateQuery optimizedQuery = PULL_OUT_VARIABLE_OPTIMIZER.optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);


        IntermediateQueryBuilder expectedQuery = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom2 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE3, X, Y);
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());

        LeftJoinNode leftJoinNode2 = IQ_FACTORY.createLeftJoinNode(IMMUTABILITY_TOOLS.foldBooleanExpressions(EXPRESSION1,
                EXPRESSION2, EXPRESSION7));
        FilterNode filterNode1 = IQ_FACTORY.createFilterNode(EXPRESSION4);
        ExtensionalDataNode dataNode3 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE5_PREDICATE, X, X2, Y));
        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE4_PREDICATE, X0, Y1, X4));

        expectedQuery.init(projectionAtom2, constructionNode2);
        expectedQuery.addChild(constructionNode2, leftJoinNode2);
        expectedQuery.addChild(leftJoinNode2, filterNode1, LEFT);
        expectedQuery.addChild(filterNode1, dataNode3);
        expectedQuery.addChild(leftJoinNode2, dataNode4, RIGHT);

        IntermediateQuery query2 = expectedQuery.build();

        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testJoiningConditionTest4() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X, Y, Z, W);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode();
        LeftJoinNode leftJoinNode1 = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, X, W));

        queryBuilder1.init(projectionAtom, constructionNode);
        queryBuilder1.addChild(constructionNode, joinNode1);
        queryBuilder1.addChild(joinNode1, dataNode1);
        queryBuilder1.addChild(joinNode1, leftJoinNode1);
        queryBuilder1.addChild(leftJoinNode1, dataNode2, LEFT);
        queryBuilder1.addChild(leftJoinNode1, dataNode3, RIGHT);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" +  query1);

        IntermediateQuery optimizedQuery = PULL_OUT_VARIABLE_OPTIMIZER.optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);

        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode(EXPRESSION1);
        LeftJoinNode leftJoinNode2 = IQ_FACTORY.createLeftJoinNode(EXPRESSION8);
        ExtensionalDataNode dataNode4 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, X0, Z));
        ExtensionalDataNode dataNode6 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, X5, W));

        expectedQueryBuilder.init(projectionAtom, constructionNode);
        expectedQueryBuilder.addChild(constructionNode, joinNode2);
        expectedQueryBuilder.addChild(joinNode2, dataNode4);
        expectedQueryBuilder.addChild(joinNode2, leftJoinNode2);
        expectedQueryBuilder.addChild(leftJoinNode2, dataNode5, LEFT);
        expectedQueryBuilder.addChild(leftJoinNode2, dataNode6, RIGHT);

        IntermediateQuery query2 = expectedQueryBuilder.build();

        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testJoiningConditionTest5() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE2, X, Y, Z);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE4_PREDICATE, X, Z, Y));

        queryBuilder1.init(projectionAtom, constructionNode);
        queryBuilder1.addChild(constructionNode, joinNode1);
        queryBuilder1.addChild(joinNode1, dataNode1);
        queryBuilder1.addChild(joinNode1, dataNode2);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" +  query1);

        IntermediateQuery optimizedQuery = PULL_OUT_VARIABLE_OPTIMIZER.optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom2 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE2, X, Y, Z);
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode(IMMUTABILITY_TOOLS.foldBooleanExpressions(EXPRESSION1, EXPRESSION2));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE4_PREDICATE, X0, Z, Y1));

        queryBuilder2.init(projectionAtom2, constructionNode2);
        queryBuilder2.addChild(constructionNode2, joinNode2);
        queryBuilder2.addChild(joinNode2, dataNode1);
        queryBuilder2.addChild(joinNode2, dataNode3);

        IntermediateQuery query2 = queryBuilder2.build();

        System.out.println("\nExpected: \n" +  query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void testLJUnnecessaryConstructionNode1() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE2, X, Y, Z);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        LeftJoinNode ljNode1 = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Z));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));

        queryBuilder1.init(projectionAtom, constructionNode);
        queryBuilder1.addChild(constructionNode, ljNode1);
        queryBuilder1.addChild(ljNode1, dataNode1, LEFT);
        queryBuilder1.addChild(ljNode1, rightConstructionNode, RIGHT);
        queryBuilder1.addChild(rightConstructionNode, dataNode2);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" +  query1);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom2 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE2, X, Y, Z);
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        LeftJoinNode ljNode2 = IQ_FACTORY.createLeftJoinNode(EXPRESSION1);
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, X0, Z));

        queryBuilder2.init(projectionAtom2, constructionNode2);
        queryBuilder2.addChild(constructionNode2, ljNode2);
        queryBuilder2.addChild(ljNode2, dataNode1, LEFT);
        queryBuilder2.addChild(ljNode2, dataNode3, RIGHT);

        IntermediateQuery query2 = queryBuilder2.build();

        System.out.println("\nExpected: \n" +  query2);

        IntermediateQuery optimizedQuery = PULL_OUT_VARIABLE_OPTIMIZER.optimize(query1);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

}
