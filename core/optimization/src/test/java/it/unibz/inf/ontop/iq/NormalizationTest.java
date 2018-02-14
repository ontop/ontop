package it.unibz.inf.ontop.iq;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.*;
import org.junit.Ignore;
import org.junit.Test;

import static it.unibz.inf.ontop.NoDependencyTestDBMetadata.*;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.*;
import static junit.framework.TestCase.assertEquals;

public class NormalizationTest {

    private static GroundFunctionalTerm GROUND_FUNCTIONAL_TERM =
            (GroundFunctionalTerm) TERM_FACTORY.getImmutableFunctionalTerm(CONCAT,
                    TERM_FACTORY.getConstantLiteral("this-"),
                    TERM_FACTORY.getConstantLiteral("that"));

    @Test
    public void testDistinct1() {
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, A, B);
        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        UnaryIQTree iqTree = IQ_FACTORY.createUnaryIQTree(distinctNode, extensionalDataNode);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, iqTree);

        normalizeAndCompare(initialIQ, initialIQ);
    }

    @Ignore("TODO: support it")
    @Test
    public void testDistinctInjective1() {
        testDistinctInjective(createInjectiveFunctionalTerm(A));
    }

    @Ignore("TODO: support it")
    @Test
    public void testDistinctInjective2() {
        testDistinctInjective(ONE);
    }

    @Ignore("TODO: support it")
    @Test
    public void testDistinctInjective3() {
        testDistinctInjective(GROUND_FUNCTIONAL_TERM);
    }

    private static void testDistinctInjective(ImmutableTerm injectiveTerm) {
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, A, B);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, X);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, injectiveTerm));

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(distinctNode,
                IQ_FACTORY.createUnaryIQTree(constructionNode, extensionalDataNode));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(constructionNode,
                IQ_FACTORY.createUnaryIQTree(distinctNode, extensionalDataNode));

        normalizeAndCompare(initialIQ, IQ_FACTORY.createIQ(projectionAtom, expectedTree));
    }

    @Test
    public void testDistinct2() {
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, A, B);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, X);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, createNonInjectiveFunctionalTerm(A, B)));

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        UnaryIQTree iqTree = IQ_FACTORY.createUnaryIQTree(distinctNode,
                IQ_FACTORY.createUnaryIQTree(constructionNode, extensionalDataNode));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, iqTree);

        normalizeAndCompare(initialIQ, initialIQ);
    }

    @Test
    public void testConstructionUseless1() {
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, A, B);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        UnaryIQTree iqTree = IQ_FACTORY.createUnaryIQTree(constructionNode, extensionalDataNode);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, iqTree);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, extensionalDataNode);

        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testConstructionUseless2() {
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, A, B);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);
        ConstructionNode downConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        UnaryIQTree downIqTree = IQ_FACTORY.createUnaryIQTree(downConstructionNode, extensionalDataNode);

        UnaryIQTree iqTree = IQ_FACTORY.createUnaryIQTree(topConstructionNode, downIqTree);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, iqTree);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, extensionalDataNode);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testConstructionUseless3() {
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, A, B);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);
        ConstructionNode downConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, B, C),
                SUBSTITUTION_FACTORY.getSubstitution(C, createInjectiveFunctionalTerm(A)));
        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        UnaryIQTree downIqTree = IQ_FACTORY.createUnaryIQTree(downConstructionNode, extensionalDataNode);

        UnaryIQTree iqTree = IQ_FACTORY.createUnaryIQTree(topConstructionNode, downIqTree);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, iqTree);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, extensionalDataNode);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testConstructionMerge1() {
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, A, B);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, A);
        ConstructionNode downConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        UnaryIQTree downIqTree = IQ_FACTORY.createUnaryIQTree(downConstructionNode, extensionalDataNode);

        UnaryIQTree iqTree = IQ_FACTORY.createUnaryIQTree(topConstructionNode, downIqTree);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, iqTree);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, downIqTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testConstructionMerge2() {
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, A, B);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, A);
        ConstructionNode downConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, B));
        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        UnaryIQTree downIqTree = IQ_FACTORY.createUnaryIQTree(downConstructionNode, extensionalDataNode);

        UnaryIQTree iqTree = IQ_FACTORY.createUnaryIQTree(topConstructionNode, downIqTree);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, iqTree);

        UnaryIQTree expectedIqTree = IQ_FACTORY.createUnaryIQTree(topConstructionNode, extensionalDataNode);
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedIqTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testConstructionMerge3() {
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, A, B);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, X);

        ConstructionNode downConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, createInjectiveFunctionalTerm(A)));

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        UnaryIQTree downIqTree = IQ_FACTORY.createUnaryIQTree(downConstructionNode, extensionalDataNode);

        UnaryIQTree iqTree = IQ_FACTORY.createUnaryIQTree(topConstructionNode, downIqTree);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, iqTree);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, downIqTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testConstructionMerge4() {
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, A, B);
        ConstructionNode downConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(C),
                SUBSTITUTION_FACTORY.getSubstitution(C, createInjectiveFunctionalTerm(A)));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, X);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, createInjectiveFunctionalTerm(C)));

        UnaryIQTree downIqTree = IQ_FACTORY.createUnaryIQTree(downConstructionNode, extensionalDataNode);

        UnaryIQTree iqTree = IQ_FACTORY.createUnaryIQTree(topConstructionNode, downIqTree);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, iqTree);

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, createInjectiveFunctionalTerm(createInjectiveFunctionalTerm(A))));

        UnaryIQTree expectedIqTree = IQ_FACTORY.createUnaryIQTree(newConstructionNode, extensionalDataNode);
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedIqTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testConstructionMerge5() {
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, A, B);
        ConstructionNode downConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(C, D),
                SUBSTITUTION_FACTORY.getSubstitution(C, createInjectiveFunctionalTerm(A),
                        D, createInjectiveFunctionalTerm(B)));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, X);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, createInjectiveFunctionalTerm(C)));

        UnaryIQTree downIqTree = IQ_FACTORY.createUnaryIQTree(downConstructionNode, extensionalDataNode);

        UnaryIQTree iqTree = IQ_FACTORY.createUnaryIQTree(topConstructionNode, downIqTree);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, iqTree);

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, createInjectiveFunctionalTerm(createInjectiveFunctionalTerm(A))));

        UnaryIQTree expectedIqTree = IQ_FACTORY.createUnaryIQTree(newConstructionNode, extensionalDataNode);
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedIqTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testConstructionMerge6() {
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, A, B);
        ConstructionNode downConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, B, C),
                SUBSTITUTION_FACTORY.getSubstitution(C, createInjectiveFunctionalTerm(A)));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, A);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        UnaryIQTree downIqTree = IQ_FACTORY.createUnaryIQTree(downConstructionNode, extensionalDataNode);

        UnaryIQTree iqTree = IQ_FACTORY.createUnaryIQTree(topConstructionNode, downIqTree);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, iqTree);

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        UnaryIQTree expectedIqTree = IQ_FACTORY.createUnaryIQTree(newConstructionNode, extensionalDataNode);
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedIqTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testConstructionDistinct1() {
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, A, B);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, X);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, createInjectiveFunctionalTerm(A)));

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        UnaryIQTree distinctIqTree = IQ_FACTORY.createUnaryIQTree(distinctNode, extensionalDataNode);

        UnaryIQTree iqTree = IQ_FACTORY.createUnaryIQTree(constructionNode, distinctIqTree);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, iqTree);

        normalizeAndCompare(initialIQ, initialIQ);
    }

    @Test
    public void testConstructionDistinct2() {
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, A, B);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, A);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        UnaryIQTree distinctIqTree = IQ_FACTORY.createUnaryIQTree(distinctNode, extensionalDataNode);

        UnaryIQTree iqTree = IQ_FACTORY.createUnaryIQTree(constructionNode, distinctIqTree);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, iqTree);

        normalizeAndCompare(initialIQ, initialIQ);
    }

    @Test
    public void testConstructionFilter1() {
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, A, B);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, X);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, createInjectiveFunctionalTerm(A)));

        FilterNode filterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getImmutableExpression(EQ,
                TERM_FACTORY.getImmutableExpression(STRLEN, A), ONE));

        UnaryIQTree subTree = IQ_FACTORY.createUnaryIQTree(filterNode, extensionalDataNode);

        UnaryIQTree iqTree = IQ_FACTORY.createUnaryIQTree(constructionNode, subTree);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, iqTree);

        normalizeAndCompare(initialIQ, initialIQ);
    }

    @Test
    public void testConstructionFilter2() {
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, A, B);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, A);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        FilterNode filterNode = IQ_FACTORY.createFilterNode(createExpression(A));

        UnaryIQTree subTree = IQ_FACTORY.createUnaryIQTree(filterNode, extensionalDataNode);

        UnaryIQTree iqTree = IQ_FACTORY.createUnaryIQTree(constructionNode, subTree);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, iqTree);

        normalizeAndCompare(initialIQ, initialIQ);
    }

    @Test
    public void testFilter1() {
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, A, B);
        ImmutableExpression expression = createExpression(A);
        FilterNode filterNode = IQ_FACTORY.createFilterNode(expression);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(filterNode, extensionalDataNode);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        normalizeAndCompare(initialIQ, initialIQ);
    }

    @Test
    public void testFilter2() {
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, A, B);
        ImmutableExpression expression = TERM_FACTORY.getImmutableExpression(EQ, A, B);
        FilterNode filterNode = IQ_FACTORY.createFilterNode(expression);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(filterNode, extensionalDataNode);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        ExtensionalDataNode newExtensionalDataNode = createExtensionalDataNode(TABLE1_AR2, B, B);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(A, B));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createUnaryIQTree(constructionNode,
                newExtensionalDataNode));

        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testFilterUseless1() {
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, A, B);
        ImmutableExpression expression = TERM_FACTORY.getImmutableExpression(EQ, A, A);
        FilterNode filterNode = IQ_FACTORY.createFilterNode(expression);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(filterNode, extensionalDataNode);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, extensionalDataNode);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testFilterSubstituable1() {
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, A, B);
        ImmutableExpression expression = TERM_FACTORY.getImmutableExpression(EQ, A, B);
        FilterNode filterNode = IQ_FACTORY.createFilterNode(expression);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(filterNode, extensionalDataNode);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        ExtensionalDataNode newExtensionalDataNode = createExtensionalDataNode(TABLE1_AR2, B, B);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(A, B));

        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(constructionNode, newExtensionalDataNode);
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testFilterUnsatisfiable1() {
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, A, B);
        ImmutableExpression expression = TERM_FACTORY.getImmutableExpression(EQ, ONE, TWO);
        FilterNode filterNode = IQ_FACTORY.createFilterNode(expression);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(filterNode, extensionalDataNode);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createEmptyNode(projectionAtom.getVariables()));
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testFilterBindings1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, X);

        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, A, B);

        ImmutableFunctionalTerm xDefinition = createInjectiveFunctionalTerm(A);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(
                projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, xDefinition));

        ImmutableExpression expression = TERM_FACTORY.getImmutableExpression(LT, X, TWO);
        FilterNode filterNode = IQ_FACTORY.createFilterNode(expression);

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(filterNode,
                IQ_FACTORY.createUnaryIQTree(constructionNode, extensionalDataNode));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        FilterNode newFilterNode = IQ_FACTORY.createFilterNode(
                TERM_FACTORY.getImmutableExpression(LT, xDefinition, TWO));

        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(constructionNode,
                IQ_FACTORY.createUnaryIQTree(newFilterNode, extensionalDataNode));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testFilterBindings2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, X);

        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, A, B);

        ImmutableFunctionalTerm xDefinition = createInjectiveFunctionalTerm(A);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(
                projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, xDefinition));

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        ImmutableExpression expression = TERM_FACTORY.getImmutableExpression(LT, X, TWO);
        FilterNode filterNode = IQ_FACTORY.createFilterNode(expression);

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(filterNode,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createUnaryIQTree(distinctNode, extensionalDataNode)));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        FilterNode newFilterNode = IQ_FACTORY.createFilterNode(
                TERM_FACTORY.getImmutableExpression(LT, xDefinition, TWO));

        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(constructionNode,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createUnaryIQTree(newFilterNode, extensionalDataNode)));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testFilterDistinct1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);

        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, A, B);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        ImmutableExpression expression = TERM_FACTORY.getImmutableExpression(LT, A, TWO);
        FilterNode filterNode = IQ_FACTORY.createFilterNode(expression);

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(filterNode,
                        IQ_FACTORY.createUnaryIQTree(distinctNode, extensionalDataNode));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createUnaryIQTree(filterNode, extensionalDataNode));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testFilterDistinct2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, A);

        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, A, B);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        ImmutableExpression expression = TERM_FACTORY.getImmutableExpression(LT, A, TWO);
        FilterNode filterNode = IQ_FACTORY.createFilterNode(expression);

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(filterNode,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createUnaryIQTree(constructionNode, extensionalDataNode)));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(distinctNode,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createUnaryIQTree(filterNode, extensionalDataNode)));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testFilterMerge1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);

        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, A, B);

        ImmutableExpression expression1 = TERM_FACTORY.getImmutableExpression(LT, A, TWO);
        FilterNode topFilterNode = IQ_FACTORY.createFilterNode(expression1);

        ImmutableExpression expression2 = TERM_FACTORY.getImmutableExpression(GT, B, ONE);
        FilterNode secondFilterNode = IQ_FACTORY.createFilterNode(expression2);

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(topFilterNode,
                IQ_FACTORY.createUnaryIQTree(secondFilterNode, extensionalDataNode));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        FilterNode newFilterNode = IQ_FACTORY.createFilterNode(
                TERM_FACTORY.getImmutableExpression(AND, expression1, expression2));

        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(newFilterNode, extensionalDataNode);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testFilterMerge2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);

        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, A, B);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        ImmutableExpression expression1 = TERM_FACTORY.getImmutableExpression(LT, A, TWO);
        FilterNode topFilterNode = IQ_FACTORY.createFilterNode(expression1);

        ImmutableExpression expression2 = TERM_FACTORY.getImmutableExpression(GT, B, ONE);
        FilterNode secondFilterNode = IQ_FACTORY.createFilterNode(expression2);

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(topFilterNode,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createUnaryIQTree(secondFilterNode, extensionalDataNode)));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        FilterNode newFilterNode = IQ_FACTORY.createFilterNode(
                TERM_FACTORY.getImmutableExpression(AND, expression1, expression2));

        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(distinctNode,
                IQ_FACTORY.createUnaryIQTree(newFilterNode, extensionalDataNode));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testFilterMerge3() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, A);

        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, A, B);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        ImmutableExpression expression1 = TERM_FACTORY.getImmutableExpression(LT, A, TWO);
        FilterNode topFilterNode = IQ_FACTORY.createFilterNode(expression1);

        ImmutableExpression expression2 = TERM_FACTORY.getImmutableExpression(GT, B, ONE);
        FilterNode secondFilterNode = IQ_FACTORY.createFilterNode(expression2);

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(topFilterNode,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createUnaryIQTree(secondFilterNode, extensionalDataNode)));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        FilterNode newFilterNode = IQ_FACTORY.createFilterNode(
                TERM_FACTORY.getImmutableExpression(AND, expression1, expression2));

        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(constructionNode,
                IQ_FACTORY.createUnaryIQTree(newFilterNode, extensionalDataNode));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testFilterMerge4() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, A);

        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, A, B);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        ImmutableExpression expression1 = TERM_FACTORY.getImmutableExpression(LT, A, TWO);
        FilterNode topFilterNode = IQ_FACTORY.createFilterNode(expression1);

        ImmutableExpression expression2 = TERM_FACTORY.getImmutableExpression(GT, B, ONE);
        FilterNode secondFilterNode = IQ_FACTORY.createFilterNode(expression2);

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(topFilterNode,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createUnaryIQTree(distinctNode,
                                IQ_FACTORY.createUnaryIQTree(secondFilterNode, extensionalDataNode))));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        FilterNode newFilterNode = IQ_FACTORY.createFilterNode(
                TERM_FACTORY.getImmutableExpression(AND, expression1, expression2));

        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(constructionNode,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createUnaryIQTree(newFilterNode, extensionalDataNode)));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testFilterMergeJoin1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, A, B);
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR1, A);

        ImmutableExpression expression1 = TERM_FACTORY.getImmutableExpression(LT, A, TWO);
        FilterNode topFilterNode = IQ_FACTORY.createFilterNode(expression1);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(topFilterNode,
                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(extensionalDataNode1, extensionalDataNode2)));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        InnerJoinNode newJoinNode = IQ_FACTORY.createInnerJoinNode(expression1);

        IQTree expectedTree = IQ_FACTORY.createNaryIQTree(newJoinNode,
                ImmutableList.of(extensionalDataNode1, extensionalDataNode2));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testFilterMergeJoin2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, A, B);
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR1, A);

        ImmutableExpression expression1 = TERM_FACTORY.getImmutableExpression(LT, A, TWO);
        FilterNode topFilterNode = IQ_FACTORY.createFilterNode(expression1);

        ImmutableExpression expression2 = TERM_FACTORY.getImmutableExpression(GT, B, ONE);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(expression2);

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(topFilterNode,
                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(extensionalDataNode1, extensionalDataNode2)));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        InnerJoinNode newJoinNode = IQ_FACTORY.createInnerJoinNode(
                TERM_FACTORY.getImmutableExpression(AND, expression1, expression2));

        IQTree expectedTree = IQ_FACTORY.createNaryIQTree(newJoinNode,
                ImmutableList.of(extensionalDataNode1, extensionalDataNode2));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testFilterMergeJoin3() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, A);

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, A, B);
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR1, A);

        ImmutableExpression expression1 = TERM_FACTORY.getImmutableExpression(LT, A, TWO);
        FilterNode topFilterNode = IQ_FACTORY.createFilterNode(expression1);

        ImmutableExpression expression2 = TERM_FACTORY.getImmutableExpression(GT, B, ONE);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(expression2);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(topFilterNode,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                    IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(extensionalDataNode1, extensionalDataNode2))));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        InnerJoinNode newJoinNode = IQ_FACTORY.createInnerJoinNode(
                TERM_FACTORY.getImmutableExpression(AND, expression1, expression2));

        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(constructionNode,
                IQ_FACTORY.createNaryIQTree(newJoinNode, ImmutableList.of(extensionalDataNode1, extensionalDataNode2)));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testFilterMergeJoin4() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, A, B);
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR1, A);

        ImmutableExpression expression1 = TERM_FACTORY.getImmutableExpression(LT, A, TWO);
        FilterNode topFilterNode = IQ_FACTORY.createFilterNode(expression1);

        ImmutableExpression expression2 = TERM_FACTORY.getImmutableExpression(GT, B, ONE);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(expression2);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(topFilterNode,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(extensionalDataNode1, extensionalDataNode2))));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        InnerJoinNode newJoinNode = IQ_FACTORY.createInnerJoinNode(
                TERM_FACTORY.getImmutableExpression(AND, expression1, expression2));

        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(distinctNode,
                IQ_FACTORY.createNaryIQTree(newJoinNode, ImmutableList.of(extensionalDataNode1, extensionalDataNode2)));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }


    private static void normalizeAndCompare(IQ initialIQ, IQ expectedIQ) {
        System.out.println("Initial IQ: " + initialIQ );
        System.out.println("Expected IQ: " + expectedIQ);

        IQ normalizedIQ = initialIQ.normalizeForOptimization();
        System.out.println("Normalized IQ: " + normalizedIQ);

        assertEquals(expectedIQ, normalizedIQ);
    }

    private static ExtensionalDataNode createExtensionalDataNode(RelationPredicate predicate,
                                                                 VariableOrGroundTerm... terms) {
        return IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(predicate, terms));
    }

    private ImmutableFunctionalTerm createNonInjectiveFunctionalTerm(Variable stringV1, Variable stringV2) {
        return TERM_FACTORY.getImmutableExpression(CONCAT, stringV1, stringV2);
    }

    private ImmutableFunctionalTerm createInjectiveFunctionalTerm(ImmutableTerm term) {
        return TERM_FACTORY.getImmutableExpression(CONCAT, TERM_FACTORY.getConstantLiteral("-something"),
                term);
    }

    private static ImmutableExpression createExpression(Variable stringVariable) {
        return TERM_FACTORY.getImmutableExpression(EQ,
                TERM_FACTORY.getImmutableExpression(STRLEN, A), ONE);
    }
}
