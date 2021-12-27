package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import org.junit.Test;

import static it.unibz.inf.ontop.NoDependencyTestDBMetadata.*;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static org.junit.Assert.assertEquals;

public class TrueNodesRemovalOptimizerTest {

    private final AtomPredicate ANS1_ARITY_0_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(0);
    private final AtomPredicate ANS1_ARITY_1_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(1);
    private final AtomPredicate ANS1_ARITY_2_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(2);

    private final Variable A = TERM_FACTORY.getVariable("a");
    private final Variable B = TERM_FACTORY.getVariable("b");
    private final Variable X = TERM_FACTORY.getVariable("x");
    private final Variable Y = TERM_FACTORY.getVariable("y");

    private final ExtensionalDataNode DATA_NODE_1 = createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(A));
    private final ExtensionalDataNode DATA_NODE_2 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(B));
    private final ExtensionalDataNode DATA_NODE_3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(A, B));

    private ImmutableFunctionalTerm generateInt(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getRDFLiteralFunctionalTerm(argument, XSD.INTEGER);
    }


    @Test
    public void testSingleTrueNodeRemoval_innerJoinParent1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_1_PREDICATE, X);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateInt(A)));
        InnerJoinNode jn = IQ_FACTORY.createInnerJoinNode();
        TrueNode trueNode = IQ_FACTORY.createTrueNode();

        IQ unOptimizedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createNaryIQTree(jn, ImmutableList.of(trueNode, DATA_NODE_1))));

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode, DATA_NODE_1));

        optimizeAndCompare(unOptimizedQuery, expectedQuery);
    }

    @Test
    public void testSingleTrueNodeRemoval_innerJoinParent2() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_1_PREDICATE, X);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateInt(A)));
        ImmutableExpression expression = TERM_FACTORY.getStrictNEquality(A, B);
        InnerJoinNode jn = IQ_FACTORY.createInnerJoinNode(expression);
        TrueNode trueNode = IQ_FACTORY.createTrueNode();

        IQ unOptimizedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createNaryIQTree(jn, ImmutableList.of(trueNode, DATA_NODE_3))));

        FilterNode filterNode = IQ_FACTORY.createFilterNode(expression);
        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createUnaryIQTree(filterNode, DATA_NODE_3)));

        optimizeAndCompare(unOptimizedQuery, expectedQuery);
    }

    @Test
    public void testSingleTrueNodeRemoval_innerJoinParent3() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, X, Y);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateInt(A), Y, generateInt(B)));
        InnerJoinNode jn = IQ_FACTORY.createInnerJoinNode();

        IQ unOptimizedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createNaryIQTree(jn, ImmutableList.of(IQ_FACTORY.createTrueNode(), DATA_NODE_1, DATA_NODE_2))));

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createNaryIQTree(jn, ImmutableList.of(DATA_NODE_1, DATA_NODE_2))));

        optimizeAndCompare(unOptimizedQuery, expectedQuery);
    }

    @Test
    public void testSingleTrueNodeRemoval_leftJoinParent() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_1_PREDICATE, X);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateInt(A)));
        LeftJoinNode ljn = IQ_FACTORY.createLeftJoinNode();

        IQ unOptimizedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(ljn, DATA_NODE_1, IQ_FACTORY.createTrueNode())));

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode, DATA_NODE_1));

        optimizeAndCompare(unOptimizedQuery, expectedQuery);
    }


    @Test
    public void testSingleTrueNodeChainRemoval() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_1_PREDICATE, X);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateInt(A)));

        IQ unOptimizedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode, DATA_NODE_1));

        IQ expectedQuery = unOptimizedQuery;

        optimizeAndCompare(unOptimizedQuery, expectedQuery);
    }



    @Test
    public void testSingleTrueNodeNonRemoval_leftJoinParent() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_1_PREDICATE, X);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateInt(A)));
        LeftJoinNode ljn = IQ_FACTORY.createLeftJoinNode();

        IQ unOptimizedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(ljn, IQ_FACTORY.createTrueNode(), DATA_NODE_1)));

        IQ expectedQuery = unOptimizedQuery;

        optimizeAndCompare(unOptimizedQuery, expectedQuery);
    }

    @Test
    public void testSingleTrueNodeNonRemoval_UnionParent() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_1_PREDICATE, X);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, NULL));
        UnionNode un = IQ_FACTORY.createUnionNode(ImmutableSet.of());
        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR1, ImmutableMap.of());

        IQ unOptimizedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createNaryIQTree(un, ImmutableList.of(dataNode, IQ_FACTORY.createTrueNode()))));

        IQ expectedQuery = unOptimizedQuery;

        optimizeAndCompare(unOptimizedQuery, expectedQuery);
    }

    @Test
    public void testMultipleTrueNodesRemoval1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_1_PREDICATE, X);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateInt(A)));
        InnerJoinNode jn1 = IQ_FACTORY.createInnerJoinNode();
        InnerJoinNode jn2 = IQ_FACTORY.createInnerJoinNode();

        IQ unOptimizedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createNaryIQTree(jn1, ImmutableList.of(
                                IQ_FACTORY.createTrueNode(),
                                IQ_FACTORY.createNaryIQTree(jn2, ImmutableList.of(IQ_FACTORY.createTrueNode(), DATA_NODE_1))))));

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode, DATA_NODE_1));

        optimizeAndCompare(unOptimizedQuery, expectedQuery);
    }

    @Test
    public void testMultipleTrueNodesRemoval2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_1_PREDICATE, X);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateInt(A)));
        InnerJoinNode jn = IQ_FACTORY.createInnerJoinNode();
        LeftJoinNode ljn = IQ_FACTORY.createLeftJoinNode();

        IQ unOptimizedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createNaryIQTree(jn, ImmutableList.of(
                                IQ_FACTORY.createTrueNode(),
                                IQ_FACTORY.createBinaryNonCommutativeIQTree(ljn, DATA_NODE_1, IQ_FACTORY.createTrueNode())))));

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode, DATA_NODE_1));

        optimizeAndCompare(unOptimizedQuery, expectedQuery);
    }



    @Test
    public void testTrueNodesPartialRemoval1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_1_PREDICATE, X);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateInt(A)));
        InnerJoinNode jn = IQ_FACTORY.createInnerJoinNode();
        LeftJoinNode ljn = IQ_FACTORY.createLeftJoinNode();

        IQ unOptimizedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createNaryIQTree(jn, ImmutableList.of(
                                IQ_FACTORY.createTrueNode(),
                                IQ_FACTORY.createBinaryNonCommutativeIQTree(ljn, IQ_FACTORY.createTrueNode(), DATA_NODE_1)))));

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(ljn, IQ_FACTORY.createTrueNode(), DATA_NODE_1)));

        optimizeAndCompare(unOptimizedQuery, expectedQuery);
    }

    @Test
    public void testTrueNodesPartialRemoval2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_0_PREDICATE);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of());
        InnerJoinNode jn = IQ_FACTORY.createInnerJoinNode();
        UnionNode un = IQ_FACTORY.createUnionNode(ImmutableSet.of());
        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR1, ImmutableMap.of());

        IQ unOptimizedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createNaryIQTree(jn, ImmutableList.of(
                                IQ_FACTORY.createTrueNode(),
                                IQ_FACTORY.createNaryIQTree(un, ImmutableList.of(IQ_FACTORY.createTrueNode(), dataNode))))));

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(un, ImmutableList.of(IQ_FACTORY.createTrueNode(), dataNode)));

        optimizeAndCompare(unOptimizedQuery, expectedQuery);
    }

    private static void optimizeAndCompare(IQ unOptimizedQuery, IQ expectedQuery) {
        System.out.println("\nInitial query: \n" + unOptimizedQuery);
        IQ optimizedQuery = UNION_AND_BINDING_LIFT_OPTIMIZER.optimize(unOptimizedQuery);
        System.out.println("\nOptimized query: \n" + optimizedQuery);

        System.out.println("\nExpected query: \n" + expectedQuery);
        assertEquals(expectedQuery, optimizedQuery);
    }

}
