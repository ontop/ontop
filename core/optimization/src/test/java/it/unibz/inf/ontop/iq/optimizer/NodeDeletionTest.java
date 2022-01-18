package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import org.junit.Test;
import it.unibz.inf.ontop.iq.*;

import static it.unibz.inf.ontop.NoDependencyTestDBMetadata.*;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * TODO: test
 */
public class NodeDeletionTest {

    private static final Variable X = TERM_FACTORY.getVariable("x");
    private static final Variable Y = TERM_FACTORY.getVariable("y");

    private static final ImmutableExpression FALSE_CONDITION =
            TERM_FACTORY.getIsTrue(
                TERM_FACTORY.getDBBooleanConstant(false));

    @Test
    public void testSimpleJoin() {
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate( 1), X);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(FALSE_CONDITION);
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(X));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(X));

        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createNaryIQTree(
                        joinNode,
                        ImmutableList.of(dataNode1, dataNode2)));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        IQ optimizedQuery = optimize(initialIQ);
        assertTrue(optimizedQuery.getTree().isDeclaredAsEmpty());
    }

    @Test
    public void testInvalidRightPartOfLeftJoin1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate( 2), X, Y);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        LeftJoinNode ljNode = IQ_FACTORY.createLeftJoinNode();

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(X));

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(FALSE_CONDITION);
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(X, Y));

        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createBinaryNonCommutativeIQTree(
                        ljNode,
                        dataNode1,
                        IQ_FACTORY.createNaryIQTree(
                                joinNode,
                                ImmutableList.of(dataNode2, dataNode3))));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        IQ optimizedIQ = optimize(initialIQ);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(Y, TERM_FACTORY.getNullConstant()));
        IQTree newTree = IQ_FACTORY.createUnaryIQTree(constructionNode1, dataNode1);
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        System.out.println("Expected query : " + expectedIQ);

        assertEquals(expectedIQ, optimizedIQ);
    }

    @Test
    public void testUnion1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate( 2), X, Y);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        UnionNode topUnion = IQ_FACTORY.createUnionNode(projectionAtom.getVariables());

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(X, Y));

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode(FALSE_CONDITION);
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(X, Y));

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode(FALSE_CONDITION);
        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE4_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode5 = createExtensionalDataNode(TABLE5_AR2, ImmutableList.of(X, Y));

        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createNaryIQTree(
                        topUnion,
                        ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(
                                        constructionNode1,
                                        dataNode1),
                                IQ_FACTORY.createUnaryIQTree(
                                        constructionNode2,
                                        IQ_FACTORY.createNaryIQTree(
                                                joinNode1,
                                                ImmutableList.of(dataNode2, dataNode3))),
                                IQ_FACTORY.createUnaryIQTree(
                                        constructionNode3,
                                        IQ_FACTORY.createNaryIQTree(
                                                joinNode2,
                                                ImmutableList.of(dataNode4, dataNode5))))));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        IQ optimizedIQ = optimize(initialIQ);

        IQTree newTree = dataNode1;
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        System.out.println("Expected query : " + expectedIQ);

        assertEquals(expectedIQ, optimizedIQ);
    }

    @Test
    public void testUnion2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate( 2), X, Y);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        UnionNode topUnion = IQ_FACTORY.createUnionNode(projectionAtom.getVariables());

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(X, Y));

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode(FALSE_CONDITION);
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(X, Y));

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE4_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode5 = createExtensionalDataNode(TABLE5_AR2, ImmutableList.of(X, Y));

        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createNaryIQTree(
                        topUnion,
                        ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(
                                        constructionNode1,
                                        dataNode1),
                                IQ_FACTORY.createUnaryIQTree(
                                        constructionNode2,
                                        IQ_FACTORY.createNaryIQTree(
                                                joinNode1,
                                                ImmutableList.of(dataNode2, dataNode3))),
                                IQ_FACTORY.createUnaryIQTree(
                                        constructionNode3,
                                        IQ_FACTORY.createNaryIQTree(
                                                joinNode2,
                                                ImmutableList.of(dataNode4, dataNode5))))));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        IQ optimizedIQ = optimize(initialIQ);

        IQTree newTree = IQ_FACTORY.createNaryIQTree(
                topUnion,
                ImmutableList.of(
                        dataNode1,
                        IQ_FACTORY.createNaryIQTree(
                                joinNode2,
                                ImmutableList.of(dataNode4, dataNode5))));
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);
        System.out.println("Expected query : " + expectedIQ);

        assertEquals(expectedIQ, optimizedIQ);
    }

    @Test
    public void testInvalidLeftPartOfLeftJoin() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate( 2), X, Y);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        LeftJoinNode ljNode = IQ_FACTORY.createLeftJoinNode();
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(FALSE_CONDITION);
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE4_AR1, ImmutableList.of(X));

        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createBinaryNonCommutativeIQTree(
                        ljNode,
                        IQ_FACTORY.createNaryIQTree(joinNode,
                                ImmutableList.of(dataNode2, dataNode3)),
                        dataNode4));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        IQ optimizedQuery = optimize(initialIQ);
        assertTrue(optimizedQuery.getTree().isDeclaredAsEmpty());
    }

    private IQ optimize(IQ initialIQ)  {
        System.out.println("Initial query: " + initialIQ);

        IQ optimizedIQ = JOIN_LIKE_OPTIMIZER.optimize(initialIQ);
        System.out.println("Optimized query: " + optimizedIQ);

        return optimizedIQ;
    }
}
