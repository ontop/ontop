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
import static junit.framework.TestCase.assertEquals;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;

public class ProjectionShrinkingOptimizerTest {

    private final static AtomPredicate ANS1_PREDICATE1 = ATOM_FACTORY.getRDFAnswerPredicate(1);
    private final static Variable X = TERM_FACTORY.getVariable("X");
    private final static Variable Y = TERM_FACTORY.getVariable("Y");
    private final static Variable Z = TERM_FACTORY.getVariable("Z");
    private final static Variable W = TERM_FACTORY.getVariable("W");
    private final static Variable A = TERM_FACTORY.getVariable("A");
    private final static Variable B = TERM_FACTORY.getVariable("B");

    private final static ImmutableExpression EXPRESSION1 = TERM_FACTORY.getStrictNEquality(Y, Z);
    private final static ImmutableExpression EXPRESSION2 = TERM_FACTORY.getStrictNEquality(W, X);


    private ImmutableFunctionalTerm generateInt(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getRDFLiteralFunctionalTerm(argument, XSD.INTEGER);

    }

    @Test
    public void testUnion() {
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X,Y));
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X, Y));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createNaryIQTree(unionNode1, ImmutableList.of(dataNode1, dataNode2))));

        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, X));
        ExtensionalDataNode newDataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2, ImmutableMap.of(0, X));

        IQ query2 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createNaryIQTree(unionNode2, ImmutableList.of(newDataNode1, newDataNode2)));

        optimizeAndCheck(query1, query2);
    }

    @Test
    public void testUnionAndImplicitJoinCondition1()  {
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        InnerJoinNode innerJoinNode1 = IQ_FACTORY.createInnerJoinNode();
        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X,Y));
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, Y));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(X, Y));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createNaryIQTree(innerJoinNode1, ImmutableList.of(
                                dataNode1,
                                IQ_FACTORY.createNaryIQTree(unionNode1, ImmutableList.of(dataNode2, dataNode3))))));

        IQ query2 = query1;

        optimizeAndCheck(query1, query2);
    }

    @Test
    public void testUnionAndImplicitJoinCondition2() {
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        InnerJoinNode innerJoinNode1 = IQ_FACTORY.createInnerJoinNode();
        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X,Y));
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(X, Y));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createNaryIQTree(innerJoinNode1, ImmutableList.of(
                                dataNode1,
                                IQ_FACTORY.createNaryIQTree(unionNode1, ImmutableList.of(dataNode2, dataNode3))))));

        InnerJoinNode innerJoinNode2 = IQ_FACTORY.createInnerJoinNode();
        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, X));
        ExtensionalDataNode newDataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2, ImmutableMap.of(0, X));
        ExtensionalDataNode newDataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE3_AR2, ImmutableMap.of(0, X));

        IQ query2 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createNaryIQTree(innerJoinNode2, ImmutableList.of(
                        newDataNode1,
                        IQ_FACTORY.createNaryIQTree(unionNode2, ImmutableList.of(newDataNode2, newDataNode3)))));

        optimizeAndCheck(query1, query2);
    }


    @Test
    public void testUnionAndExplicitJoinCondition1() {
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        InnerJoinNode innerJoinNode1 = IQ_FACTORY.createInnerJoinNode(EXPRESSION1);
        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X,Y));
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(X, Y));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createNaryIQTree(innerJoinNode1, ImmutableList.of(
                                dataNode1,
                                IQ_FACTORY.createNaryIQTree(unionNode1, ImmutableList.of(dataNode2, dataNode3))))));

        IQ query2 = query1;

        optimizeAndCheck(query1, query2);
    }

    @Test
    public void testUnionAndExplicitJoinCondition2() {
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, Z);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        InnerJoinNode innerJoinNode1 = IQ_FACTORY.createInnerJoinNode(EXPRESSION2);
        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X,Y));
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(W, Z));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(X, Y));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createNaryIQTree(innerJoinNode1, ImmutableList.of(
                                dataNode1,
                                IQ_FACTORY.createNaryIQTree(unionNode1, ImmutableList.of(dataNode2, dataNode3))))));

        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        ExtensionalDataNode newDataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2, ImmutableMap.of(0, X));
        ExtensionalDataNode newDataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE3_AR2, ImmutableMap.of(0, X));

        IQ query2 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createNaryIQTree(innerJoinNode1, ImmutableList.of(
                                dataNode1,
                                IQ_FACTORY.createNaryIQTree(unionNode2, ImmutableList.of(newDataNode2, newDataNode3))))));

        optimizeAndCheck(query1, query2);
    }

    @Test
    public void testUnionAndFilter() {
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, Z);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        FilterNode filterNode1 = IQ_FACTORY.createFilterNode(EXPRESSION1);
        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X,Y,Z));
        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE4_AR3, ImmutableList.of(X, Y, Z));
        ExtensionalDataNode dataNode5 = createExtensionalDataNode(TABLE5_AR3, ImmutableList.of(X, Y, Z));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createUnaryIQTree(filterNode1,
                                IQ_FACTORY.createNaryIQTree(unionNode1, ImmutableList.of(dataNode4, dataNode5)))));

        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(Y,Z));
        ExtensionalDataNode newDataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE4_AR3, ImmutableMap.of(1, Y, 2, Z));
        ExtensionalDataNode newDataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE5_AR3, ImmutableMap.of(1, Y, 2, Z));

        IQ query2 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createUnaryIQTree(filterNode1,
                                IQ_FACTORY.createNaryIQTree(unionNode2, ImmutableList.of(newDataNode4, newDataNode5)))));

        optimizeAndCheck(query1, query2);
    }

    @Test
    public void testConstructionNode() {
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(X,generateInt(A),Y,generateInt(B)));
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createUnaryIQTree(constructionNode2, dataNode1)));

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X,generateInt(A)));
        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, A));

        IQ query2 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode3, newDataNode1));

        optimizeAndCheck(query1, query2);
    }

    @Test
    public void testConstructionNodeAndImplicitJoinCondition2() {
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, Z);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        InnerJoinNode innerJoinNode1 = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(X,generateInt(A),Y,generateInt(B)));
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createNaryIQTree(innerJoinNode1, ImmutableList.of(
                                dataNode1,
                                IQ_FACTORY.createUnaryIQTree(constructionNode2, dataNode2)))));

        Variable XF0 = TERM_FACTORY.getVariable("Xf0");
        InnerJoinNode newInnerJoinNode1 = IQ_FACTORY.createInnerJoinNode(
                TERM_FACTORY.getStrictEquality(TERM_FACTORY.getRDFFunctionalTerm(A,
                        TERM_FACTORY.getRDFTermTypeConstant(TYPE_FACTORY.getXsdIntegerDatatype())), XF0));

        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, XF0,1, Z));
        ExtensionalDataNode newDataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, A));

        IQ query2 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createNaryIQTree(newInnerJoinNode1, ImmutableList.of(newDataNode1, newDataNode2))));

        optimizeAndCheck(query1, query2);
    }

    private static void optimizeAndCheck(IQ initialIQ, IQ expectedIQ) {
        System.out.println("\nBefore optimization: \n" +  initialIQ);
        IQ optimizedQuery =  initialIQ.normalizeForOptimization();
        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        System.out.println("\nExpected: \n" +  expectedIQ);

        assertEquals(expectedIQ, optimizedQuery);

    }
}
