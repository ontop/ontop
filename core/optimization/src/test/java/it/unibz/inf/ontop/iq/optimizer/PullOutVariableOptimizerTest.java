package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transformer.ExplicitEqualityTransformer;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import org.junit.Test;

import static it.unibz.inf.ontop.NoDependencyTestDBMetadata.*;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static junit.framework.TestCase.assertEquals;

public class PullOutVariableOptimizerTest {
    private final static Variable X = TERM_FACTORY.getVariable("X");
    private final static Variable X0 = TERM_FACTORY.getVariable("Xf0");
    private final static Variable X2 = TERM_FACTORY.getVariable("Xf2");
    private final static Variable X4 = TERM_FACTORY.getVariable("Xf0f3");
    private final static Variable X5 = TERM_FACTORY.getVariable("Xf0f1");
    private final static Variable Y = TERM_FACTORY.getVariable("Y");
    private final static Variable Y1 = TERM_FACTORY.getVariable("Yf1");
    private final static Variable Z = TERM_FACTORY.getVariable("Z");
    private final static Variable Z0 = TERM_FACTORY.getVariable("Zf0");
    private final static Variable Z2 = TERM_FACTORY.getVariable("Zf2");
    private final static Variable W = TERM_FACTORY.getVariable("W");
    private final static Variable R = TERM_FACTORY.getVariable("R");
    private final static Variable S = TERM_FACTORY.getVariable("S");
    private final static Variable T = TERM_FACTORY.getVariable("T");

    private final static ImmutableExpression EXPRESSION1 = TERM_FACTORY.getStrictEquality(X, X0);
    private final static ImmutableExpression EXPRESSION2 = TERM_FACTORY.getStrictEquality(Y, Y1);
    private final static ImmutableExpression EXPRESSION4 = TERM_FACTORY.getStrictEquality(X, X2);
    private final static ImmutableExpression EXPRESSION7 = TERM_FACTORY.getStrictEquality(X0, X4);
    private final static ImmutableExpression EXPRESSION8 = TERM_FACTORY.getStrictEquality(X0, X5);
    private final static ImmutableExpression EXPRESSION_Z_Z0 = TERM_FACTORY.getStrictEquality(Z, Z0);
    private final static ImmutableExpression EXPRESSION_Z_Z2 = TERM_FACTORY.getStrictEquality(Z, Z2);

    @Test
    public void testDataNode() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(3), X, Y, Z);

        ExtensionalDataNode dataNode =  createExtensionalDataNode(TABLE7_AR4, ImmutableList.of(Z, X, Z, Y));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, dataNode);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        FilterNode filterNode = IQ_FACTORY.createFilterNode(EXPRESSION_Z_Z0);
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE7_AR4, ImmutableList.of(Z, X, Z0, Y));

        IQTree newTree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createUnaryIQTree(
                        filterNode,
                        dataNode2));
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCheck(initialIQ, expectedIQ);
    }

    @Test
    public void testJoiningConditionTest1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(3), X, Y, Z);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X, Z));

        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createNaryIQTree(
                        joinNode1,
                        ImmutableList.of(dataNode1, dataNode2)));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode(EXPRESSION1);
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X0, Z));

        IQTree newTree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createNaryIQTree(
                        joinNode2,
                        ImmutableList.of(dataNode1, dataNode3)));
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCheck(initialIQ, expectedIQ);
    }

    @Test
    public void testJoiningConditionTest2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(3), X, Y, Z);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        LeftJoinNode leftJoinNode1 = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE4_AR3, ImmutableList.of(X, Y, Z));

        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createBinaryNonCommutativeIQTree(
                        leftJoinNode1,
                        dataNode1, dataNode2));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        LeftJoinNode leftJoinNode2 = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getConjunction(EXPRESSION1, EXPRESSION2));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE4_AR3, ImmutableList.of(X0, Y1, Z));

        IQTree newTree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createBinaryNonCommutativeIQTree(
                        leftJoinNode2,
                        dataNode1, dataNode3));
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCheck(initialIQ, expectedIQ);
    }

    @Test
    public void testJoin3() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(3), X, Y, Z);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(R, S));
        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(W, Y));
        ExtensionalDataNode dataNode5 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(T, Z));

        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createNaryIQTree(
                        joinNode1,
                        ImmutableList.of(dataNode1, dataNode2, dataNode3, dataNode4, dataNode5)));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);


        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getConjunction(
                EXPRESSION1, EXPRESSION2, EXPRESSION_Z_Z2));
        ExtensionalDataNode newDataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X0, Z));
        ExtensionalDataNode newDataNode4 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(W, Y1));
        ExtensionalDataNode newDataNode5 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(T, Z2));

        IQTree newTree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createNaryIQTree(
                        joinNode2,
                        ImmutableList.of(dataNode1, newDataNode2, dataNode3, newDataNode4, newDataNode5)));
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCheck(initialIQ, expectedIQ);
    }

    @Test
    public void testJoin4() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(3), X, Y, Z);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(W, Z));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(T, Z));

        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createNaryIQTree(
                        joinNode1,
                        ImmutableList.of(dataNode1, dataNode2, dataNode3)));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);


        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode(EXPRESSION_Z_Z0);
        ExtensionalDataNode newDataNode3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(T, Z0));

        IQTree newTree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createNaryIQTree(
                        joinNode2,
                        ImmutableList.of(dataNode1, dataNode2, newDataNode3)));
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCheck(initialIQ, expectedIQ);
    }

    @Test
    public void testJoiningConditionTest3() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(2), X, Y);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        LeftJoinNode leftJoinNode1 = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE5_AR3, ImmutableList.of(X, X, Y));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE4_AR3, ImmutableList.of(X, Y, X));

        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode1, dataNode1, dataNode2));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);


        LeftJoinNode leftJoinNode2 = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getConjunction(EXPRESSION1,
                EXPRESSION2, EXPRESSION7));
        FilterNode filterNode1 = IQ_FACTORY.createFilterNode(EXPRESSION4);
        ExtensionalDataNode dataNode3 =  createExtensionalDataNode(TABLE5_AR3, ImmutableList.of(X, X2, Y));
        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE4_AR3, ImmutableList.of(X0, Y1, X4));

        IQTree newTree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createUnaryIQTree(
                        filterNode1,
                                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode2, dataNode3, dataNode4)));
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCheck(initialIQ, expectedIQ);
    }

    @Test
    public void testJoiningConditionTest4() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(4), X, Y, Z, W);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode();
        LeftJoinNode leftJoinNode1 = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(X, W));

        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createNaryIQTree(
                        joinNode1,
                        ImmutableList.of(
                                dataNode1,
                                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode1, dataNode2, dataNode3))));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode(EXPRESSION1);
        LeftJoinNode leftJoinNode2 = IQ_FACTORY.createLeftJoinNode(EXPRESSION8);
        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode5 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X0, Z));
        ExtensionalDataNode dataNode6 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(X5, W));

        IQTree newTree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createNaryIQTree(
                        joinNode2,
                        ImmutableList.of(
                                dataNode4,
                                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode2, dataNode5, dataNode6))));
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCheck(initialIQ, expectedIQ);
    }

    @Test
    public void testJoiningConditionTest5() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(3), X, Y, Z);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE4_AR3, ImmutableList.of(X, Z, Y));

        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createNaryIQTree(
                        joinNode1,
                        ImmutableList.of(dataNode1, dataNode2)));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);


        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getConjunction(EXPRESSION1, EXPRESSION2));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE4_AR3, ImmutableList.of(X0, Z, Y1));

        IQTree newTree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createNaryIQTree(
                        joinNode2,
                        ImmutableList.of(dataNode1, dataNode3)));
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCheck(initialIQ, expectedIQ);
    }

    @Test
    public void testLJUnnecessaryConstructionNode1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(3), X, Y, Z);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        LeftJoinNode ljNode1 = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(X, Y));
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Z));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X, Z));

        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createBinaryNonCommutativeIQTree(
                        ljNode1,
                        dataNode1,
                        IQ_FACTORY.createUnaryIQTree(rightConstructionNode, dataNode2)));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);


        LeftJoinNode ljNode2 = IQ_FACTORY.createLeftJoinNode(EXPRESSION1);
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X0, Z));

        IQTree newTree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createBinaryNonCommutativeIQTree(
                        ljNode2,
                        dataNode1,
                        dataNode3));
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCheck(initialIQ, expectedIQ);
    }

    @Test
    public void testDistinctProjection() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(1), B);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR3, ImmutableMap.of(0, A, 1, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR3, ImmutableMap.of(0, A, 1, C));
        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();


        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createUnaryIQTree(
                        distinctNode,
                        IQ_FACTORY.createNaryIQTree(
                                IQ_FACTORY.createInnerJoinNode(),
                                ImmutableList.of(dataNode1, dataNode2))));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR3, ImmutableMap.of(0, AF0, 1, C));
        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, B, C));
        InnerJoinNode newInnerJoinNode = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getStrictEquality(A, AF0));

        IQTree newTree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createUnaryIQTree(
                        distinctNode,
                        IQ_FACTORY.createUnaryIQTree(
                                newConstructionNode,
                                IQ_FACTORY.createNaryIQTree(
                                        newInnerJoinNode,
                                        ImmutableList.of(dataNode1, dataNode3)))));
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCheck(initialIQ, expectedIQ);
    }

    @Test
    public void testUnionDistinctProjection() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(1), B);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR3, ImmutableMap.of(0, A, 1, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR3, ImmutableMap.of(0, A, 1, C));
        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();


        IQTree subTree1 = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createUnaryIQTree(
                        distinctNode,
                        IQ_FACTORY.createNaryIQTree(
                                IQ_FACTORY.createInnerJoinNode(),
                                ImmutableList.of(dataNode1, dataNode2))));


        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR3, ImmutableMap.of(2, B));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(
                        IQ_FACTORY.createUnionNode(projectionAtom.getVariables()),
                        ImmutableList.of(subTree1, dataNode3)));

        ExtensionalDataNode newDataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR3, ImmutableMap.of(0, AF0, 1, C));
        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, B, C));
        InnerJoinNode newInnerJoinNode = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getStrictEquality(A, AF0));

        IQTree newSubTree1 = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createUnaryIQTree(
                        distinctNode,
                        IQ_FACTORY.createUnaryIQTree(
                                newConstructionNode,
                                IQ_FACTORY.createNaryIQTree(
                                        newInnerJoinNode,
                                        ImmutableList.of(dataNode1, newDataNode2)))));
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(
                        IQ_FACTORY.createUnionNode(projectionAtom.getVariables()),
                        ImmutableList.of(newSubTree1, dataNode3)));

        optimizeAndCheck(initialIQ, expectedIQ);
    }

    private void optimizeAndCheck(IQ initialQuery, IQ expectedQuery) {
        System.out.println("\nBefore optimization: \n" +  initialQuery);

        ExplicitEqualityTransformer eet = OPTIMIZER_FACTORY.createEETransformer(initialQuery.getVariableGenerator());
        IQ optimizedIQ = IQ_FACTORY.createIQ(
                        initialQuery.getProjectionAtom(),
                        eet.transform(initialQuery.getTree()));
        System.out.println("\nAfter optimization: \n" +  optimizedIQ);

        System.out.println("\nExpected: \n" +  expectedQuery);

        assertEquals(expectedQuery, optimizedIQ);
    }
}
