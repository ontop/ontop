package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.iq.*;
import org.junit.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.NoDependencyTestDBMetadata.*;
import static it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel.LT;
import static junit.framework.TestCase.assertEquals;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;


public class PushDownBooleanExpressionOptimizerTest {
    
    private final static AtomPredicate ANS1_PREDICATE1 = ATOM_FACTORY.getRDFAnswerPredicate( 4);
    private final static AtomPredicate ANS1_PREDICATE2 = ATOM_FACTORY.getRDFAnswerPredicate( 3);
    private final static AtomPredicate ANS1_PREDICATE3 = ATOM_FACTORY.getRDFAnswerPredicate( 5);
    private final static AtomPredicate ANS2_PREDICATE1 = ATOM_FACTORY.getRDFAnswerPredicate(2);
    private final static Variable X = TERM_FACTORY.getVariable("X");
    private final static Variable Y = TERM_FACTORY.getVariable("Y");
    private final static Variable Z = TERM_FACTORY.getVariable("Z");
    private final static Variable W = TERM_FACTORY.getVariable("W");
    private final static Variable A = TERM_FACTORY.getVariable("A");

    private final static ImmutableExpression EXPRESSION1 = TERM_FACTORY.getStrictEquality(X, Z);
    private final static ImmutableExpression EXPRESSION2 = TERM_FACTORY.getStrictNEquality(Y, Z);
    private final static ImmutableExpression EXPRESSION3 = TERM_FACTORY.getDBDefaultInequality(LT, Z, W);
    private final static ImmutableExpression EXPRESSION4 = TERM_FACTORY.getStrictEquality(Y, Z);
    private final static ImmutableExpression EXPRESSION5 = TERM_FACTORY.getStrictNEquality(Z, W);
    private final static ImmutableExpression EXPRESSION6 = TERM_FACTORY.getStrictEquality(X, W);
    private final static ImmutableExpression EXPRESSION7 = TERM_FACTORY.getStrictEquality(X, Y);

    @Test
    public void testJoiningCondition1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X, Y, Z, W);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getConjunction(EXPRESSION1,EXPRESSION2,EXPRESSION3));
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(X, Y));
        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(Z, W));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createNaryIQTree(joinNode1, ImmutableList.of(
                                dataNode1,
                                IQ_FACTORY.createNaryIQTree(joinNode2, ImmutableList.of(dataNode2, dataNode3))))));

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode3 = IQ_FACTORY.createInnerJoinNode(Optional.of(EXPRESSION2));
        InnerJoinNode joinNode4 = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getConjunction(EXPRESSION1,EXPRESSION3));

        IQ query2 = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode2,
                        IQ_FACTORY.createNaryIQTree(joinNode3, ImmutableList.of(
                                dataNode1,
                                IQ_FACTORY.createNaryIQTree(joinNode4, ImmutableList.of(dataNode2, dataNode3))))));

        optimizeAndCheck(query1, query2);
    }

    @Test
    public void testJoiningCondition2() {
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE2, X, Y, Z);
        DistinctVariableOnlyDataAtom projectionAtom2 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS2_PREDICATE1, X, Z);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode(Optional.of(EXPRESSION1));
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(projectionAtom2.getVariables());
        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(projectionAtom2.getVariables());
        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(projectionAtom2.getVariables());
        ConstructionNode constructionNode4 = IQ_FACTORY.createConstructionNode(projectionAtom2.getVariables());

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(X, Z));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createNaryIQTree(joinNode1, ImmutableList.of(
                                dataNode1,
                                IQ_FACTORY.createUnaryIQTree(constructionNode2,
                                        IQ_FACTORY.createNaryIQTree(unionNode1, ImmutableList.of(
                                                IQ_FACTORY.createUnaryIQTree(constructionNode3, dataNode2),
                                                IQ_FACTORY.createUnaryIQTree(constructionNode4, dataNode3))))))));

        ConstructionNode constructionNode5 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode constructionNode6 = IQ_FACTORY.createConstructionNode(projectionAtom2.getVariables());
        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(projectionAtom2.getVariables());
        ConstructionNode constructionNode7 = IQ_FACTORY.createConstructionNode(projectionAtom2.getVariables());
        ConstructionNode constructionNode8 = IQ_FACTORY.createConstructionNode(projectionAtom2.getVariables());
        FilterNode filterNode1 = IQ_FACTORY.createFilterNode(EXPRESSION1);
        FilterNode filterNode2 = IQ_FACTORY.createFilterNode(EXPRESSION1);

        IQ query2 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode5,
                        IQ_FACTORY.createNaryIQTree(joinNode2, ImmutableList.of(
                                dataNode1,
                                IQ_FACTORY.createUnaryIQTree(constructionNode6,
                                        IQ_FACTORY.createNaryIQTree(unionNode2, ImmutableList.of(
                                                IQ_FACTORY.createUnaryIQTree(constructionNode7, IQ_FACTORY.createUnaryIQTree(filterNode1, dataNode2)),
                                                IQ_FACTORY.createUnaryIQTree(constructionNode8, IQ_FACTORY.createUnaryIQTree(filterNode2, dataNode3)))))))));

        optimizeAndCheck(query1, query2);
    }

    @Test
    public void testJoiningCondition3() {
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X, Y, Z, W);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        FilterNode filterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getConjunction(EXPRESSION4, EXPRESSION5));
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE4_AR3, ImmutableList.of(Y, Z, W));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createUnaryIQTree(filterNode,
                                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, dataNode1, dataNode2))));

        IQ query2 = query1;

        optimizeAndCheck(query1, query2);
    }

    @Test
    public void testJoiningCondition4() {
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE3, X, Y, Z, W, A);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode(Optional.of(EXPRESSION1));
        LeftJoinNode leftJoinNode1 = IQ_FACTORY.createLeftJoinNode();
        LeftJoinNode leftJoinNode2 = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X, A));
        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE5_AR2, ImmutableList.of(X, W));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createNaryIQTree(joinNode1, ImmutableList.of(
                                dataNode1,
                                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode1,
                                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode2, dataNode2, dataNode3),
                                        dataNode4)))));

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        LeftJoinNode leftJoinNode3 = IQ_FACTORY.createLeftJoinNode();
        LeftJoinNode leftJoinNode4 = IQ_FACTORY.createLeftJoinNode();

        // TODO: same as above?
        IQ query2 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode2,
                        IQ_FACTORY.createNaryIQTree(joinNode1, ImmutableList.of(
                                dataNode1,
                                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode3,
                                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode4, dataNode2, dataNode3),
                                        dataNode4)))));

        optimizeAndCheck(query1, query2);
    }

    @Test
    public void testJoiningCondition5 () {
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X, Y, Z, W);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode(EXPRESSION6);
        LeftJoinNode leftJoinNode1 = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE5_AR2, ImmutableList.of(X, W));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createNaryIQTree(joinNode1, ImmutableList.of(
                                dataNode1,
                                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode1, dataNode2, dataNode3)))));

        IQ query2 = query1;

        optimizeAndCheck(query1, query2);
    }

    @Test
    public void testLeftJoinCondition1() {
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, W, X, Y, Z);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        LeftJoinNode leftJoinNode1 = IQ_FACTORY.createLeftJoinNode(EXPRESSION7);
        InnerJoinNode innerJoinNode1 = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X, W));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(X, Z));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode1,
                                IQ_FACTORY.createNaryIQTree(innerJoinNode1, ImmutableList.of(dataNode1, dataNode2)),
                                dataNode3)));

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        LeftJoinNode leftJoinNode2 = IQ_FACTORY.createLeftJoinNode(EXPRESSION7);
        InnerJoinNode innerJoinNode2 = IQ_FACTORY.createInnerJoinNode();

        // TODO: same as above?
        IQ query2 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode2,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode2,
                                IQ_FACTORY.createNaryIQTree(innerJoinNode2, ImmutableList.of(dataNode1, dataNode2)),
                                dataNode3)));

        optimizeAndCheck(query1, query2);
    }

    @Test
    public void testLeftJoinCondition2() {
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, W, X, Y, Z);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        LeftJoinNode leftJoinNode1 = IQ_FACTORY.createLeftJoinNode(EXPRESSION7);
        InnerJoinNode innerJoinNode1 = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X, W));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(X, Z));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode1,
                                dataNode2,
                                IQ_FACTORY.createNaryIQTree(innerJoinNode1, ImmutableList.of(dataNode1, dataNode3)))));

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        LeftJoinNode leftJoinNode2 = IQ_FACTORY.createLeftJoinNode();
        InnerJoinNode innerJoinNode2 = IQ_FACTORY.createInnerJoinNode(EXPRESSION7);

        IQ query2 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode2,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode2,
                                dataNode2,
                                IQ_FACTORY.createNaryIQTree(innerJoinNode2, ImmutableList.of(dataNode1, dataNode3)))));

        optimizeAndCheck(query1, query2);
    }

    @Test
    public void testLeftJoinAndFilterCondition1() {
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, W, X, Y, Z);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        FilterNode filterNode1 = IQ_FACTORY.createFilterNode(EXPRESSION6);
        LeftJoinNode leftJoinNode1 = IQ_FACTORY.createLeftJoinNode(EXPRESSION7);
        InnerJoinNode innerJoinNode1 = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X, W));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X, Z));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createUnaryIQTree(filterNode1,
                                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode1,
                                        IQ_FACTORY.createNaryIQTree(innerJoinNode1, ImmutableList.of(dataNode1, dataNode2)),
                                        dataNode3))));

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        LeftJoinNode leftJoinNode2 = IQ_FACTORY.createLeftJoinNode(EXPRESSION7);
        InnerJoinNode innerJoinNode2 = IQ_FACTORY.createInnerJoinNode(EXPRESSION6);

        IQ query2 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode2,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode2,
                                IQ_FACTORY.createNaryIQTree(innerJoinNode2, ImmutableList.of(dataNode1, dataNode2)),
                                dataNode3)));

        optimizeAndCheck(query1, query2);
    }

    @Test
    public void testLeftJoinAndFilterCondition2() {
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, W, X, Y, Z);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        FilterNode filterNode1 = IQ_FACTORY.createFilterNode(EXPRESSION1);
        LeftJoinNode leftJoinNode1 = IQ_FACTORY.createLeftJoinNode(EXPRESSION7);
        InnerJoinNode innerJoinNode1 = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X, W));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X, Z));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createUnaryIQTree(filterNode1,
                                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode1,
                                        IQ_FACTORY.createNaryIQTree(innerJoinNode1, ImmutableList.of(dataNode1, dataNode2)),
                                        dataNode3))));

        IQ query2 = query1;

        optimizeAndCheck(query1, query2);
    }

    private void optimizeAndCheck(IQ initialIQ, IQ expectedID) {
        System.out.println("\nBefore optimization: \n" +  initialIQ);
        IQTree newTree = PUSH_DOWN_BOOLEAN_EXPRESSION_TRANSFORMER.transform(initialIQ.getTree());
        IQ optimizedQuery = IQ_FACTORY.createIQ(initialIQ.getProjectionAtom(), newTree);
        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        System.out.println("\nExpected: \n" +  expectedID);

        assertEquals(expectedID, optimizedQuery);
    }
}
