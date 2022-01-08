package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.iq.*;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.NoDependencyTestDBMetadata.*;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel.GTE;
import static junit.framework.TestCase.assertEquals;

public class PushUpBooleanExpressionOptimizerTest {
    
    private final static AtomPredicate ANS1_PREDICATE1 = ATOM_FACTORY.getRDFAnswerPredicate( 1);
    private final static AtomPredicate ANS1_PREDICATE3 = ATOM_FACTORY.getRDFAnswerPredicate( 3);
    private final static Variable U = TERM_FACTORY.getVariable("U");
    private final static Variable V = TERM_FACTORY.getVariable("V");
    private final static Variable W = TERM_FACTORY.getVariable("W");
    private final static Variable X = TERM_FACTORY.getVariable("X");
    private final static Variable Y = TERM_FACTORY.getVariable("Y");
    private final static Variable Z = TERM_FACTORY.getVariable("Z");

    private final static ImmutableExpression EXPRESSION1 = TERM_FACTORY.getDBNonStrictNumericEquality(X, Z);
    private final static ImmutableExpression EXPRESSION2 = TERM_FACTORY.getStrictNEquality(Y, Z);
    private final static ImmutableExpression EXPRESSION3 = TERM_FACTORY.getDBDefaultInequality(GTE, W, Z);
    private final static ImmutableExpression EXPRESSION5 = TERM_FACTORY.getStrictNEquality(X, TERM_FACTORY.getDBStringConstant("a"));

    @Test
    public void testPropagationFomInnerJoinProvider() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE3, X, Y, Z);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode(Optional.empty());
        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getConjunction (EXPRESSION1, EXPRESSION2));
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE4_AR3, ImmutableList.of(X, Y, Z));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(Z));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createNaryIQTree(joinNode1, ImmutableList.of(
                                dataNode1,
                                IQ_FACTORY.createNaryIQTree(joinNode2, ImmutableList.of(dataNode2, dataNode3))))));

        IQ query2 = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(joinNode2, ImmutableList.of(dataNode1, dataNode2, dataNode3)));

        optimizeAndCheck(query1, query2);
    }

    @Test
    public void testNoPropagationFomInnerJoinProvider() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE3, X, Y, Z);
        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y, Z));
        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getConjunction (EXPRESSION1, EXPRESSION2));
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE4_AR3, ImmutableList.of(X, Y, Z));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(Z));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(unionNode, ImmutableList.of(
                        dataNode1,
                        IQ_FACTORY.createNaryIQTree(joinNode1, ImmutableList.of(dataNode2, dataNode3)))));

        IQ query2 = query1;

        optimizeAndCheck(query1, query2);
    }

    @Test
    public void testPropagationFomFilterNodeProvider() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE3, X, Y, Z);
        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode(Optional.empty());
        FilterNode filterNode = IQ_FACTORY.createFilterNode(EXPRESSION1);
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE4_AR3, ImmutableList.of(X, Y, Z));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(X, Y));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(joinNode1, ImmutableList.of(
                        dataNode2,
                        IQ_FACTORY.createUnaryIQTree(filterNode, dataNode1))));

        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode(Optional.of(EXPRESSION1));

        IQ query2 = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(joinNode2, ImmutableList.of(dataNode2, dataNode1)));

        optimizeAndCheck(query1, query2);
    }

    @Test
    public void testNoPropagationFomFilterNodeProvider() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE3, X, Y, Z);
        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y, Z));
        FilterNode filterNode = IQ_FACTORY.createFilterNode(EXPRESSION1);
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE3_AR3, ImmutableList.of(X, Y, Z));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE4_AR3, ImmutableList.of(X, Y, Z));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(unionNode, ImmutableList.of(
                        dataNode1,
                        IQ_FACTORY.createUnaryIQTree(filterNode, dataNode2))));

        IQ query2 = query1;

        optimizeAndCheck(query1, query2);
    }

    @Test
    public void testNoPropagationFomLeftJoinProvider() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE3, X, Y, Z);
        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode(Optional.empty());
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getConjunction (EXPRESSION1, EXPRESSION2));
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE4_AR3, ImmutableList.of(X, Y, Z));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode (TABLE1_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(Z));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                        IQ_FACTORY.createNaryIQTree(joinNode1, ImmutableList.of(dataNode2, dataNode1)),
                        dataNode3));

        IQ query2 = query1;

        optimizeAndCheck(query1, query2);
    }

    @Test
    public void testPropagationToExistingFilterRecipient() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE3, X, Y, Z);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        FilterNode filterNode1 = IQ_FACTORY.createFilterNode(EXPRESSION3);
        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getConjunction(EXPRESSION2));
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE4_AR3, ImmutableList.of(W, X, Y));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X, Z));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createUnaryIQTree(filterNode1,
                                IQ_FACTORY.createNaryIQTree(joinNode1, ImmutableList.of(dataNode1, dataNode2)))));

        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getConjunction(EXPRESSION3, EXPRESSION2));

        IQ query2 = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createNaryIQTree(joinNode2, ImmutableList.of(dataNode1, dataNode2))));

        optimizeAndCheck(query1, query2);
    }

    @Test
    public void testRecursivePropagation() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE3, X, Y, Z);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode(Optional.empty());
        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode(Optional.of(EXPRESSION1));
        FilterNode filterNode = IQ_FACTORY.createFilterNode(EXPRESSION3);
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE4_AR3, ImmutableList.of(X, Y, Z));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(W, Z));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createNaryIQTree(joinNode1, ImmutableList.of(
                                dataNode1,
                                IQ_FACTORY.createNaryIQTree(joinNode2, ImmutableList.of(
                                        dataNode2,
                                        IQ_FACTORY.createUnaryIQTree(filterNode, dataNode3)))))));

        InnerJoinNode joinNode3 = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getConjunction(EXPRESSION1, EXPRESSION3));

        IQ query2 = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createNaryIQTree(joinNode3, ImmutableList.of(dataNode1, dataNode2, dataNode3))));

        optimizeAndCheck(query1, query2);
    }


    @Test
    public void testPropagationToLeftJoinRecipient() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE3, X, Y, Z);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode leftJoinNode1 = IQ_FACTORY.createLeftJoinNode(Optional.empty());
        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getConjunction(EXPRESSION1, EXPRESSION2));
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE4_AR3, ImmutableList.of(X, Y, Z));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(Z));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode1,
                                dataNode1,
                                IQ_FACTORY.createNaryIQTree(joinNode1, ImmutableList.of(dataNode2, dataNode3)))));

        LeftJoinNode leftJoinNode2 = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getConjunction(EXPRESSION1, EXPRESSION2));
        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode(Optional.empty());

        IQ query2 = IQ_FACTORY.createIQ(projectionAtom,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode2,
                                dataNode1,
                                IQ_FACTORY.createNaryIQTree(joinNode2, ImmutableList.of(dataNode2, dataNode3))));

        optimizeAndCheck(query1, query2);
    }

    @Test
    public void testPropagationThroughLeftJoin() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE3, X, Y, Z);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode(Optional.empty());
        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getConjunction(EXPRESSION1, EXPRESSION2));
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE4_AR3, ImmutableList.of(X, Y, Z));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(Z));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                                IQ_FACTORY.createNaryIQTree(joinNode1, ImmutableList.of(dataNode2, dataNode3)),
                                dataNode1)));

        FilterNode filterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getConjunction(EXPRESSION1, EXPRESSION2));
        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode(Optional.empty());

        IQ query2 = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(filterNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                                IQ_FACTORY.createNaryIQTree(joinNode2, ImmutableList.of(dataNode2, dataNode3)),
                                dataNode1)));

        optimizeAndCheck(query1, query2);
    }

    @Ignore("TODO: support it")
    @Test
    public void testCompletePropagationThroughUnion() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        FilterNode filterNode1 = IQ_FACTORY.createFilterNode(EXPRESSION1);
        FilterNode filterNode2 = IQ_FACTORY.createFilterNode(EXPRESSION1);
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE3_AR3, ImmutableList.of(X, Y, Z));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE4_AR3, ImmutableList.of(W, X, Z));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createNaryIQTree(unionNode1, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(filterNode1, dataNode1),
                                IQ_FACTORY.createUnaryIQTree(filterNode2, dataNode2)))));

        FilterNode filterNode3 = IQ_FACTORY.createFilterNode(EXPRESSION1);
        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Z));

        IQ query2 = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createUnaryIQTree(filterNode3,
                                IQ_FACTORY.createNaryIQTree(unionNode2, ImmutableList.of(dataNode1, dataNode2)))));

        optimizeAndCheck(query1, query2);
    }

    @Test
    public void testNoPropagationThroughUnion() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);
        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        FilterNode filterNode1 = IQ_FACTORY.createFilterNode(EXPRESSION1);
        FilterNode filterNode2 = IQ_FACTORY.createFilterNode(EXPRESSION3);
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(X, Z));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE4_AR3, ImmutableList.of(W, X, Z));
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(unionNode1.getVariables());
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(unionNode1.getVariables());

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(unionNode1, ImmutableList.of(
                        IQ_FACTORY.createUnaryIQTree(constructionNode1, IQ_FACTORY.createUnaryIQTree(filterNode1, dataNode1)),
                        IQ_FACTORY.createUnaryIQTree(constructionNode2, IQ_FACTORY.createUnaryIQTree(filterNode2, dataNode2)))));

        IQ query2 = query1;

        optimizeAndCheck(query1, query2);
    }

    @Ignore("Shall we support it?")
    @Test
    public void testPartialPropagationThroughUnion() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        FilterNode filterNode1 = IQ_FACTORY.createFilterNode(TERM_FACTORY.getConjunction(EXPRESSION1, EXPRESSION2));
        FilterNode filterNode2 = IQ_FACTORY.createFilterNode(TERM_FACTORY.getConjunction(EXPRESSION1, EXPRESSION3));
        FilterNode filterNode3 = IQ_FACTORY.createFilterNode(EXPRESSION1);
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE3_AR3, ImmutableList.of(X, Y, Z));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE4_AR3, ImmutableList.of(W, X, Z));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE6_AR3, ImmutableList.of(X, V, Z));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createNaryIQTree(unionNode1, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(filterNode1, dataNode1),
                                IQ_FACTORY.createUnaryIQTree(filterNode2, dataNode2),
                                IQ_FACTORY.createUnaryIQTree(filterNode3, dataNode3)
                        ))));

        FilterNode filterNode4 = IQ_FACTORY.createFilterNode(EXPRESSION1);
        FilterNode filterNode5 = IQ_FACTORY.createFilterNode(EXPRESSION2);
        FilterNode filterNode6 = IQ_FACTORY.createFilterNode(EXPRESSION3);
        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Z));

        IQ query2 = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createUnaryIQTree(filterNode4,
                                IQ_FACTORY.createNaryIQTree(unionNode2, ImmutableList.of(
                                        IQ_FACTORY.createUnaryIQTree(filterNode5, dataNode1),
                                        IQ_FACTORY.createUnaryIQTree(filterNode6, dataNode2),
                                        dataNode3
                                )))));

        optimizeAndCheck(query1, query2);
    }

    @Ignore("TODO: support it")
    @Test
    public void testMultiplePropagationsThroughUnion() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI(Y)));
        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(Y));
        FilterNode filterNode1 = IQ_FACTORY.createFilterNode(TERM_FACTORY.getConjunction(EXPRESSION3, EXPRESSION5));
        FilterNode filterNode2 = IQ_FACTORY.createFilterNode(EXPRESSION3);
        FilterNode filterNode3 = IQ_FACTORY.createFilterNode(TERM_FACTORY.getConjunction(EXPRESSION3, EXPRESSION2));
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE3_AR3, ImmutableList.of(X, W, Z));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE4_AR3, ImmutableList.of(W, Y, Z));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE7_AR4, ImmutableList.of(Y, W, U, Z));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createNaryIQTree(unionNode1, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(filterNode1, dataNode1),
                                IQ_FACTORY.createUnaryIQTree(constructionNode2,
                                        IQ_FACTORY.createNaryIQTree(unionNode2, ImmutableList.of(
                                                IQ_FACTORY.createUnaryIQTree(filterNode2, dataNode2),
                                                IQ_FACTORY.createUnaryIQTree(filterNode3, dataNode3))))))));

        FilterNode filterNode4 = IQ_FACTORY.createFilterNode(EXPRESSION3);
        FilterNode filterNode5 = IQ_FACTORY.createFilterNode(EXPRESSION5);
        FilterNode filterNode6 = IQ_FACTORY.createFilterNode(EXPRESSION2);
        UnionNode unionNode3 = IQ_FACTORY.createUnionNode(ImmutableSet.of(W, X, Z));
        UnionNode unionNode4 = IQ_FACTORY.createUnionNode(ImmutableSet.of(W, Y, Z));
        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X, W, Z), SUBSTITUTION_FACTORY.getSubstitution(X, generateURI(Y)));

        IQ query2 = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createUnaryIQTree(filterNode4,
                                IQ_FACTORY.createNaryIQTree(unionNode3, ImmutableList.of(
                                        IQ_FACTORY.createUnaryIQTree(filterNode5, dataNode1),
                                        IQ_FACTORY.createUnaryIQTree(constructionNode3,
                                                IQ_FACTORY.createNaryIQTree(unionNode4, ImmutableList.of(
                                                        dataNode2,
                                                        IQ_FACTORY.createUnaryIQTree(filterNode6, dataNode3)))))))));

        optimizeAndCheck(query1, query2);
    }

    private void optimizeAndCheck(IQ initialIQ, IQ expectedIQ) {
        System.out.println("\nBefore optimization: \n" + initialIQ);
        IQ optimizedIQ = initialIQ.normalizeForOptimization();
        System.out.println("\nAfter optimization: \n" + optimizedIQ);

        System.out.println("\nExpected: \n" + expectedIQ);
        assertEquals(expectedIQ, optimizedIQ);
    }

    private static ImmutableFunctionalTerm generateURI(VariableOrGroundTerm... arguments) {
        Template.Builder builder = Template.builder();
        builder.addSeparator("http://example.org/ds1/");
        for (VariableOrGroundTerm argument : arguments)
            builder.addColumn();
        return TERM_FACTORY.getIRIFunctionalTerm(builder.build(), ImmutableList.copyOf(arguments));
    }

}
