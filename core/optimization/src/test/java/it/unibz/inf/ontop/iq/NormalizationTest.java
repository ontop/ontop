package it.unibz.inf.ontop.iq;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.*;
import org.junit.Ignore;
import org.junit.Test;

import static it.unibz.inf.ontop.DependencyTestDBMetadata.*;
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
        testDistinctInjective(createInjectiveFunctionalTerm1(A));
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
                SUBSTITUTION_FACTORY.getSubstitution(C, createInjectiveFunctionalTerm1(A)));
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
                SUBSTITUTION_FACTORY.getSubstitution(X, createInjectiveFunctionalTerm1(A)));

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
                SUBSTITUTION_FACTORY.getSubstitution(C, createInjectiveFunctionalTerm1(A)));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, X);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, createInjectiveFunctionalTerm1(C)));

        UnaryIQTree downIqTree = IQ_FACTORY.createUnaryIQTree(downConstructionNode, extensionalDataNode);

        UnaryIQTree iqTree = IQ_FACTORY.createUnaryIQTree(topConstructionNode, downIqTree);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, iqTree);

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, createInjectiveFunctionalTerm1(createInjectiveFunctionalTerm1(A))));

        UnaryIQTree expectedIqTree = IQ_FACTORY.createUnaryIQTree(newConstructionNode, extensionalDataNode);
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedIqTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testConstructionMerge5() {
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, A, B);
        ConstructionNode downConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(C, D),
                SUBSTITUTION_FACTORY.getSubstitution(C, createInjectiveFunctionalTerm1(A),
                        D, createInjectiveFunctionalTerm1(B)));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, X);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, createInjectiveFunctionalTerm1(C)));

        UnaryIQTree downIqTree = IQ_FACTORY.createUnaryIQTree(downConstructionNode, extensionalDataNode);

        UnaryIQTree iqTree = IQ_FACTORY.createUnaryIQTree(topConstructionNode, downIqTree);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, iqTree);

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, createInjectiveFunctionalTerm1(createInjectiveFunctionalTerm1(A))));

        UnaryIQTree expectedIqTree = IQ_FACTORY.createUnaryIQTree(newConstructionNode, extensionalDataNode);
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedIqTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testConstructionMerge6() {
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, A, B);
        ConstructionNode downConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, B, C),
                SUBSTITUTION_FACTORY.getSubstitution(C, createInjectiveFunctionalTerm1(A)));

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
                SUBSTITUTION_FACTORY.getSubstitution(X, createInjectiveFunctionalTerm1(A)));

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
                SUBSTITUTION_FACTORY.getSubstitution(X, createInjectiveFunctionalTerm1(A)));

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

        ImmutableFunctionalTerm xDefinition = createInjectiveFunctionalTerm1(A);

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

        ImmutableFunctionalTerm xDefinition = createInjectiveFunctionalTerm1(A);

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

    @Test
    public void testLJ1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, C);

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, A, B);
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR2, A, C);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();

        IQTree tree = IQ_FACTORY.createUnaryIQTree(constructionNode,
                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, extensionalDataNode1, extensionalDataNode2));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);
        normalizeAndCompare(initialIQ, initialIQ);
    }

    @Test
    public void testLJ2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, C);

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, A, B);
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR2, C, D);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getImmutableExpression(EQ, A, C));

        IQTree tree = IQ_FACTORY.createUnaryIQTree(constructionNode,
                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, extensionalDataNode1, extensionalDataNode2));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        ExtensionalDataNode newExtensionalDataNode2 = createExtensionalDataNode(TABLE2_AR2, A, D);
        LeftJoinNode newLeftJoin = IQ_FACTORY.createLeftJoinNode();
        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(C, createIfIsNotNullElseNull(D, A)));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(newConstructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(newLeftJoin,
                                extensionalDataNode1, newExtensionalDataNode2)));

        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLJBindings1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, A, B);
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR2, C, D);

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, createNonInjectiveFunctionalTerm(A, B)));

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, createNonInjectiveFunctionalTerm(C, D),
                        Y, createNonInjectiveFunctionalTerm(D, C)));

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();

        IQTree tree = IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                        IQ_FACTORY.createUnaryIQTree(leftConstructionNode, extensionalDataNode1),
                        IQ_FACTORY.createUnaryIQTree(rightConstructionNode, extensionalDataNode2));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        LeftJoinNode newLeftJoinNode = IQ_FACTORY.createLeftJoinNode(
                TERM_FACTORY.getImmutableExpression(EQ,
                        createNonInjectiveFunctionalTerm(A, B),
                        createNonInjectiveFunctionalTerm(C, D)));

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, createNonInjectiveFunctionalTerm(A, B),
                        Y, createNonInjectiveFunctionalTerm(D, C)));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(newConstructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(newLeftJoinNode,
                                extensionalDataNode1, extensionalDataNode2)));

        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLJBindings2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, A, B);
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE3_AR1, E);
        ExtensionalDataNode extensionalDataNode3 = createExtensionalDataNode(TABLE2_AR2, C, D);

        UnionNode leftUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));

        ConstructionNode leftConstructionNode1 = IQ_FACTORY.createConstructionNode(leftUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, createNonInjectiveFunctionalTerm(A, B)));

        ConstructionNode leftConstructionNode2 = IQ_FACTORY.createConstructionNode(leftUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, createInjectiveFunctionalTerm2(E)));

        NaryIQTree leftUnionTree = IQ_FACTORY.createNaryIQTree(leftUnionNode, ImmutableList.of(
                IQ_FACTORY.createUnaryIQTree(leftConstructionNode1, extensionalDataNode1),
                IQ_FACTORY.createUnaryIQTree(leftConstructionNode2, extensionalDataNode2)));

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, createNonInjectiveFunctionalTerm(C, D),
                        Y, createNonInjectiveFunctionalTerm(D, C)));

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();

        IQTree tree = IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                leftUnionTree,
                IQ_FACTORY.createUnaryIQTree(rightConstructionNode, extensionalDataNode3));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        LeftJoinNode newLeftJoinNode = IQ_FACTORY.createLeftJoinNode(
                TERM_FACTORY.getImmutableExpression(EQ, createNonInjectiveFunctionalTerm(C, D), X));

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        Y, createNonInjectiveFunctionalTerm(D, C)));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(topConstructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(newLeftJoinNode,
                                leftUnionTree, extensionalDataNode3)));

        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLJDistinct1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, C);

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, A, B);
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR2, A, C);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();

        IQTree tree = IQ_FACTORY.createUnaryIQTree(constructionNode,
                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                        IQ_FACTORY.createUnaryIQTree(distinctNode, extensionalDataNode1),
                        IQ_FACTORY.createUnaryIQTree(distinctNode, extensionalDataNode2)));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(constructionNode,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                                extensionalDataNode1, extensionalDataNode2)));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLJDistinct2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, C);

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(PK_TABLE1_AR2, A, B);
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR2, A, C);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();

        IQTree tree = IQ_FACTORY.createUnaryIQTree(constructionNode,
                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                        extensionalDataNode1,
                        IQ_FACTORY.createUnaryIQTree(distinctNode, extensionalDataNode2)));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(constructionNode,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                                extensionalDataNode1, extensionalDataNode2)));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLJDistinct3() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, C);

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, A, B);
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(PK_TABLE2_AR2, A, C);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();

        IQTree tree = IQ_FACTORY.createUnaryIQTree(constructionNode,
                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                        IQ_FACTORY.createUnaryIQTree(distinctNode, extensionalDataNode1),
                        extensionalDataNode2));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(constructionNode,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                                extensionalDataNode1, extensionalDataNode2)));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLJDistinct4() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, C);

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, A, B);
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR2, A, C);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();

        IQTree tree = IQ_FACTORY.createUnaryIQTree(constructionNode,
                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                        extensionalDataNode1,
                        IQ_FACTORY.createUnaryIQTree(distinctNode, extensionalDataNode2)));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        normalizeAndCompare(initialIQ, initialIQ);
    }

    @Test
    public void testLJDistinct5() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, C);

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, A, B);
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR2, A, C);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();

        IQTree tree = IQ_FACTORY.createUnaryIQTree(constructionNode,
                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                        IQ_FACTORY.createUnaryIQTree(distinctNode, extensionalDataNode1),
                        extensionalDataNode2));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);
        normalizeAndCompare(initialIQ, initialIQ);
    }

    @Test
    public void testLJDistinct6() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, C);

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(PK_TABLE1_AR2, A, B);
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(PK_TABLE2_AR2, A, C);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();

        IQTree tree = IQ_FACTORY.createUnaryIQTree(constructionNode,
                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                        extensionalDataNode1,
                        extensionalDataNode2));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);
        normalizeAndCompare(initialIQ, initialIQ);
    }

    @Test
    public void testLJDistinctAndBindings1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, A, B);
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR2, C, D);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, createNonInjectiveFunctionalTerm(A, B)));

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, createNonInjectiveFunctionalTerm(C, D),
                        Y, createNonInjectiveFunctionalTerm(D, C)));

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();

        IQTree tree = IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                        IQ_FACTORY.createUnaryIQTree(leftConstructionNode,
                                IQ_FACTORY.createUnaryIQTree(distinctNode, extensionalDataNode1)),
                        IQ_FACTORY.createUnaryIQTree(rightConstructionNode,
                                IQ_FACTORY.createUnaryIQTree(distinctNode, extensionalDataNode2)));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, createNonInjectiveFunctionalTerm(A, B),
                        Y, createNonInjectiveFunctionalTerm(D, C)));

        LeftJoinNode newLeftJoinNode = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getImmutableExpression(EQ,
                createNonInjectiveFunctionalTerm(A, B), createNonInjectiveFunctionalTerm(C, D)));

        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(topConstructionNode,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(newLeftJoinNode,
                                extensionalDataNode1, extensionalDataNode2)));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLJDistinctAndBindings2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, A, B);
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR2, C, D);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, createNonInjectiveFunctionalTerm(A, B)));

        ConstructionNode topRightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, createNonInjectiveFunctionalTerm(C, D)));

        ConstructionNode lowerRightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y, C, D),
                SUBSTITUTION_FACTORY.getSubstitution(
                        Y, createNonInjectiveFunctionalTerm(D, C)));

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();

        IQTree tree = IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                IQ_FACTORY.createUnaryIQTree(leftConstructionNode,
                        IQ_FACTORY.createUnaryIQTree(distinctNode, extensionalDataNode1)),
                IQ_FACTORY.createUnaryIQTree(topRightConstructionNode,
                        IQ_FACTORY.createUnaryIQTree(distinctNode,
                                IQ_FACTORY.createUnaryIQTree(lowerRightConstructionNode, extensionalDataNode2))));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, createNonInjectiveFunctionalTerm(A, B)));

        ConstructionNode newLowerConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y, A, B, C, D),
                SUBSTITUTION_FACTORY.getSubstitution(
                        Y, createNonInjectiveFunctionalTerm(D, C)));

        LeftJoinNode newLeftJoinNode = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getImmutableExpression(EQ,
                createNonInjectiveFunctionalTerm(A, B), createNonInjectiveFunctionalTerm(C, D)));

        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(topConstructionNode,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createUnaryIQTree(newLowerConstructionNode,
                            IQ_FACTORY.createBinaryNonCommutativeIQTree(newLeftJoinNode,
                                    extensionalDataNode1, extensionalDataNode2))));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLJFilter1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR3_PREDICATE, A, B, C);

        FilterNode leftFilterNode = IQ_FACTORY.createFilterNode(createExpression(B));

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, A, B);
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR2, A, C);

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();

        IQTree tree = IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                IQ_FACTORY.createUnaryIQTree(leftFilterNode, extensionalDataNode1), extensionalDataNode2);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(leftFilterNode,
                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                        extensionalDataNode1, extensionalDataNode2));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLJFilter2() {
        testLJSimpleRightFilter(C);
    }

    @Test
    public void testLJFilter3() {
        testLJSimpleRightFilter(A);
    }

    private void testLJSimpleRightFilter(Variable rightVariableToTest) {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR3_PREDICATE, A, B, C);

        FilterNode rightFilterNode = IQ_FACTORY.createFilterNode(createExpression(rightVariableToTest));

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, A, B);
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR2, A, C);

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();

        IQTree tree = IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                extensionalDataNode1, IQ_FACTORY.createUnaryIQTree(rightFilterNode, extensionalDataNode2));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        LeftJoinNode newLeftJoinNode = IQ_FACTORY.createLeftJoinNode(rightFilterNode.getFilterCondition());

        IQTree expectedTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(newLeftJoinNode,
                extensionalDataNode1, extensionalDataNode2);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLJInnerJoin1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR4_PREDICATE, A, B, C, D);

        InnerJoinNode leftInnerJoinNode = IQ_FACTORY.createInnerJoinNode(createExpression(B));

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, A, B);
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE1_AR2, B, C);
        ExtensionalDataNode extensionalDataNode3 = createExtensionalDataNode(TABLE2_AR2, A, D);

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();

        IQTree tree = IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                IQ_FACTORY.createNaryIQTree(leftInnerJoinNode,
                        ImmutableList.of(extensionalDataNode1, extensionalDataNode2)), extensionalDataNode3);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        FilterNode newFilterNode = IQ_FACTORY.createFilterNode(leftInnerJoinNode.getOptionalFilterCondition().get());
        InnerJoinNode newJoinNode = IQ_FACTORY.createInnerJoinNode();

        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(newFilterNode,
                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                        IQ_FACTORY.createNaryIQTree(newJoinNode, ImmutableList.of(extensionalDataNode1, extensionalDataNode2)),
                        extensionalDataNode3));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLJInnerJoin2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR4_PREDICATE, A, B, C, D);

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, A, B);
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR2, A, C);
        ExtensionalDataNode extensionalDataNode3 = createExtensionalDataNode(TABLE2_AR2, C, D);

        InnerJoinNode rightInnerJoinNode = IQ_FACTORY.createInnerJoinNode(createExpression(C));

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();

        IQTree tree = IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                extensionalDataNode1, IQ_FACTORY.createNaryIQTree(rightInnerJoinNode,
                        ImmutableList.of(extensionalDataNode2, extensionalDataNode3)));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        LeftJoinNode newLeftJoinNode = IQ_FACTORY.createLeftJoinNode(rightInnerJoinNode.getOptionalFilterCondition());
        InnerJoinNode newRightInnerJoinNode = IQ_FACTORY.createInnerJoinNode();

        IQTree expectedTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(newLeftJoinNode,
                extensionalDataNode1, IQ_FACTORY.createNaryIQTree(newRightInnerJoinNode,
                        ImmutableList.of(extensionalDataNode2, extensionalDataNode3)));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoin1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR3_PREDICATE, A, B, C);

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, A, B);
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR2, A, C);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();

        NaryIQTree initialTree = IQ_FACTORY.createNaryIQTree(joinNode,
                ImmutableList.of(extensionalDataNode1, extensionalDataNode2));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        normalizeAndCompare(initialIQ, initialIQ);
    }

    @Test
    public void testJoinDistinct1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR3_PREDICATE, A, B, C);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, A, B);
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR2, A, C);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();

        NaryIQTree initialTree = IQ_FACTORY.createNaryIQTree(joinNode,
                ImmutableList.of(
                        IQ_FACTORY.createUnaryIQTree(distinctNode, extensionalDataNode1),
                        IQ_FACTORY.createUnaryIQTree(distinctNode, extensionalDataNode2)));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(distinctNode,
                IQ_FACTORY.createNaryIQTree(joinNode,
                        ImmutableList.of(extensionalDataNode1, extensionalDataNode2)));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);

        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinDistinct2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR3_PREDICATE, A, B, C);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, A, B);
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(PK_TABLE2_AR2, A, C);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();

        NaryIQTree initialTree = IQ_FACTORY.createNaryIQTree(joinNode,
                ImmutableList.of(
                        IQ_FACTORY.createUnaryIQTree(distinctNode, extensionalDataNode1),
                        extensionalDataNode2));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(distinctNode,
                IQ_FACTORY.createNaryIQTree(joinNode,
                        ImmutableList.of(extensionalDataNode1, extensionalDataNode2)));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);

        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinDistinct3() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR3_PREDICATE, A, B, C);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, A, B);
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR2, A, C);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();

        NaryIQTree initialTree = IQ_FACTORY.createNaryIQTree(joinNode,
                ImmutableList.of(
                        IQ_FACTORY.createUnaryIQTree(distinctNode, extensionalDataNode1),
                        extensionalDataNode2));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        normalizeAndCompare(initialIQ, initialIQ);
    }

    @Test
    public void testJoinDistinct4() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR3_PREDICATE, X, Y, Z);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR3, A, B, C);
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(PK_TABLE2_AR2, D, E);

        ConstructionNode topLeftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                    SUBSTITUTION_FACTORY.getSubstitution(X, createNonInjectiveFunctionalTerm(A, B)));

        ConstructionNode lowerLeftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y, A, B),
                SUBSTITUTION_FACTORY.getSubstitution(Y, createNonInjectiveFunctionalTerm(B, C)));

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Z),
                SUBSTITUTION_FACTORY.getSubstitution(Z, createNonInjectiveFunctionalTerm(D, E)));

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();

        NaryIQTree initialTree = IQ_FACTORY.createNaryIQTree(joinNode,
                ImmutableList.of(
                        IQ_FACTORY.createUnaryIQTree(topLeftConstructionNode,
                                IQ_FACTORY.createUnaryIQTree(distinctNode,
                                        IQ_FACTORY.createUnaryIQTree(lowerLeftConstructionNode, extensionalDataNode1))),
                        IQ_FACTORY.createUnaryIQTree(rightConstructionNode, extensionalDataNode2)));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        ConstructionNode newTopConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, createNonInjectiveFunctionalTerm(A, B),
                        Z, createNonInjectiveFunctionalTerm(D, E)));

        ConstructionNode newLowerConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y, A, B, D, E),
                SUBSTITUTION_FACTORY.getSubstitution(Y, createNonInjectiveFunctionalTerm(B, C)));

        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(newTopConstructionNode,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createUnaryIQTree(newLowerConstructionNode,
                            IQ_FACTORY.createNaryIQTree(joinNode,
                                    ImmutableList.of(extensionalDataNode1, extensionalDataNode2)))));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);

        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinDistinct5() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR3_PREDICATE, X, Y, Z);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR3, A, B, C);
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(PK_TABLE2_AR2, D, E);

        ConstructionNode topLeftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, createNonInjectiveFunctionalTerm(A, B)));

        ConstructionNode lowerLeftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y, A, B),
                SUBSTITUTION_FACTORY.getSubstitution(Y, createNonInjectiveFunctionalTerm(B, C)));

        FilterNode leftFilterNode = IQ_FACTORY.createFilterNode(createExpression(A));

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Z),
                SUBSTITUTION_FACTORY.getSubstitution(Z, createNonInjectiveFunctionalTerm(D, E)));

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();

        NaryIQTree initialTree = IQ_FACTORY.createNaryIQTree(joinNode,
                ImmutableList.of(
                        IQ_FACTORY.createUnaryIQTree(topLeftConstructionNode,
                                IQ_FACTORY.createUnaryIQTree(distinctNode,
                                        IQ_FACTORY.createUnaryIQTree(lowerLeftConstructionNode,
                                                IQ_FACTORY.createUnaryIQTree(leftFilterNode, extensionalDataNode1)))),
                        IQ_FACTORY.createUnaryIQTree(rightConstructionNode, extensionalDataNode2)));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        ConstructionNode newTopConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, createNonInjectiveFunctionalTerm(A, B),
                        Z, createNonInjectiveFunctionalTerm(D, E)));

        ConstructionNode newLowerConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y, A, B, D, E),
                SUBSTITUTION_FACTORY.getSubstitution(Y, createNonInjectiveFunctionalTerm(B, C)));

        InnerJoinNode newJoinNode = IQ_FACTORY.createInnerJoinNode(leftFilterNode.getFilterCondition());

        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(newTopConstructionNode,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createUnaryIQTree(newLowerConstructionNode,
                                IQ_FACTORY.createNaryIQTree(newJoinNode,
                                        ImmutableList.of(extensionalDataNode1, extensionalDataNode2)))));

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

    private ImmutableFunctionalTerm createInjectiveFunctionalTerm1(ImmutableTerm term) {
        return TERM_FACTORY.getImmutableExpression(CONCAT, TERM_FACTORY.getConstantLiteral("-something"),
                term);
    }

    private ImmutableFunctionalTerm createInjectiveFunctionalTerm2(ImmutableTerm term) {
        return TERM_FACTORY.getImmutableExpression(ENCODE_FOR_URI, term);
    }

    private ImmutableExpression createIfIsNotNullElseNull(Variable rightSpecificVariable, ImmutableTerm value) {
        return TERM_FACTORY.getImmutableExpression(IF_ELSE_NULL,
                TERM_FACTORY.getImmutableExpression(IS_NOT_NULL, rightSpecificVariable), value);
    }

    private static ImmutableExpression createExpression(Variable stringVariable) {
        return TERM_FACTORY.getImmutableExpression(EQ,
                TERM_FACTORY.getImmutableExpression(STRLEN, stringVariable), ONE);
    }
}
