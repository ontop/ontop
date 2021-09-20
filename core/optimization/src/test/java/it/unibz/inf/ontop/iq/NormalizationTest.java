package it.unibz.inf.ontop.iq;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.OptimizationTestingTools;
import it.unibz.inf.ontop.dbschema.NamedRelationDefinition;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.SPARQLFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.vocabulary.XPathFunction;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.DependencyTestDBMetadata.*;
import static it.unibz.inf.ontop.NoDependencyTestDBMetadata.*;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel.GT;
import static it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel.LT;
import static junit.framework.TestCase.assertEquals;

public class NormalizationTest {

    private static final GroundFunctionalTerm GROUND_FUNCTIONAL_TERM =
            (GroundFunctionalTerm) TERM_FACTORY.getNullRejectingDBConcatFunctionalTerm(ImmutableList.of(
                    TERM_FACTORY.getDBStringConstant("this-"),
                    TERM_FACTORY.getDBStringConstant("that")));

    @Test
    public void testDistinct1() {
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
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
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));

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
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));

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
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        UnaryIQTree iqTree = IQ_FACTORY.createUnaryIQTree(constructionNode, extensionalDataNode);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, iqTree);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, extensionalDataNode);

        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testConstructionUseless2() {
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
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
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
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
        ExtensionalDataNode extensionalDataNode = IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR2,
                ImmutableMap.of(0, A));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, A);
        ConstructionNode downConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        UnaryIQTree downIqTree = IQ_FACTORY.createUnaryIQTree(downConstructionNode, extensionalDataNode);

        UnaryIQTree iqTree = IQ_FACTORY.createUnaryIQTree(topConstructionNode, downIqTree);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, iqTree);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, extensionalDataNode);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testConstructionMerge2() {
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, A);
        ConstructionNode downConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, B));
        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        UnaryIQTree downIqTree = IQ_FACTORY.createUnaryIQTree(downConstructionNode, extensionalDataNode);

        UnaryIQTree iqTree = IQ_FACTORY.createUnaryIQTree(topConstructionNode, downIqTree);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, iqTree);

        IQTree expectedIqTree = IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR2, ImmutableMap.of(0, A));
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedIqTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testConstructionMerge3() {
        ExtensionalDataNode extensionalDataNode = IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR2,
                ImmutableMap.of(0, A));
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
        ExtensionalDataNode extensionalDataNode = IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR2,
                ImmutableMap.of(0, A));
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
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
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

        ExtensionalDataNode newExtensionalDataNode = IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR2, ImmutableMap.of(0, A));

        UnaryIQTree expectedIqTree = IQ_FACTORY.createUnaryIQTree(newConstructionNode, newExtensionalDataNode);
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedIqTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testConstructionMerge6() {
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ConstructionNode downConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, B, C),
                SUBSTITUTION_FACTORY.getSubstitution(C, createInjectiveFunctionalTerm1(A)));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, A);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        UnaryIQTree downIqTree = IQ_FACTORY.createUnaryIQTree(downConstructionNode, extensionalDataNode);

        UnaryIQTree iqTree = IQ_FACTORY.createUnaryIQTree(topConstructionNode, downIqTree);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, iqTree);

        ExtensionalDataNode newExtensionalDataNode = IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR2, ImmutableMap.of(0, A));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newExtensionalDataNode);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testConstructionMerge7() {
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ConstructionNode downConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, C),
                SUBSTITUTION_FACTORY.getSubstitution(C, TERM_FACTORY.getDBIsNotNull(B)));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, C);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        UnaryIQTree downIqTree = IQ_FACTORY.createUnaryIQTree(downConstructionNode, extensionalDataNode);

        UnaryIQTree iqTree = IQ_FACTORY.createUnaryIQTree(topConstructionNode, downIqTree);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, iqTree);

        ConstructionNode newTopConstructionNode = IQ_FACTORY.createConstructionNode(
                projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(C, TRUE));

        ExtensionalDataNode newExtensionalDataNode = IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR2, ImmutableMap.of(0, A));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(newTopConstructionNode, newExtensionalDataNode));
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testConstructionRequiredVariableRemoval() {
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, C);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(C, TERM_FACTORY.getDBIsNotNull(B)));

        UnaryIQTree iqTree = IQ_FACTORY.createUnaryIQTree(constructionNode, extensionalDataNode);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, iqTree);

        ConstructionNode newTopConstructionNode = IQ_FACTORY.createConstructionNode(
                projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(C, TRUE));

        ExtensionalDataNode newExtensionalDataNode = IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR2, ImmutableMap.of(0, A));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(newTopConstructionNode, newExtensionalDataNode));
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testConstructionDistinct1() {
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));

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
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));

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
        ExtensionalDataNode extensionalDataNode = IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR2, ImmutableMap.of(0, A));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, X);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, createInjectiveFunctionalTerm1(A)));

        FilterNode filterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getStrictEquality(
                TERM_FACTORY.getDBCharLength(A), ONE));

        UnaryIQTree subTree = IQ_FACTORY.createUnaryIQTree(filterNode, extensionalDataNode);

        UnaryIQTree iqTree = IQ_FACTORY.createUnaryIQTree(constructionNode, subTree);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, iqTree);

        normalizeAndCompare(initialIQ, initialIQ);
    }

    @Test
    public void testConstructionFilter2() {
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, A);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        FilterNode filterNode = IQ_FACTORY.createFilterNode(createExpression(A));

        UnaryIQTree subTree = IQ_FACTORY.createUnaryIQTree(filterNode, extensionalDataNode);

        UnaryIQTree iqTree = IQ_FACTORY.createUnaryIQTree(constructionNode, subTree);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, iqTree);

        ExtensionalDataNode newExtensionalDataNode = IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR2, ImmutableMap.of(0, A));

        UnaryIQTree newIQTree = IQ_FACTORY.createUnaryIQTree(filterNode, newExtensionalDataNode);
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newIQTree);

        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinDifferentIRITemplate1b() {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, A));

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2, ImmutableMap.of(0, B, 1, C));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, X);

        ImmutableList<Template.Component> iriTemplate1 = Template.builder().addSeparator("http://example.org/house/")
                .addColumn()
                .addSeparator("#zzz")
                .build();

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(iriTemplate1, ImmutableList.of(A))));

        ImmutableList<Template.Component> iriTemplate2 = Template.builder().addSeparator("http://example.org/house/")
                .addColumn()
                .addSeparator("#z")
                .addColumn()
                .addSeparator("z")
                .build();

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(iriTemplate2,
                        ImmutableList.of(B, C))));

        IQTree iqTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(
                        IQ_FACTORY.createUnaryIQTree(constructionNode1, dataNode1),
                        IQ_FACTORY.createUnaryIQTree(constructionNode2, dataNode2)));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, iqTree);

        ExtensionalDataNode newDataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2, ImmutableMap.of(0, A,
                1, TERM_FACTORY.getDBStringConstant("z")));

        IQTree newJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, newDataNode2));

        UnaryIQTree newIQTree = IQ_FACTORY.createUnaryIQTree(constructionNode1, newJoinTree);
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newIQTree);

        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinDifferentIRITemplate1() {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, A));

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2, ImmutableMap.of(0, B, 1, C));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, X);

        ImmutableList<Template.Component> iriTemplate1 = Template.builder().addSeparator("http://example.org/house/")
                .addColumn()
                .addSeparator("#z")
                .build();

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(iriTemplate1, ImmutableList.of(A))));

        ImmutableList<Template.Component> iriTemplate2 = Template.builder().addSeparator("http://example.org/house/")
                .addColumn()
                .addSeparator("#")
                .addColumn()
                .build();

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(iriTemplate2,
                        ImmutableList.of(B, C))));

        IQTree iqTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(
                        IQ_FACTORY.createUnaryIQTree(constructionNode1, dataNode1),
                        IQ_FACTORY.createUnaryIQTree(constructionNode2, dataNode2)));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, iqTree);

        ExtensionalDataNode newDataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2, ImmutableMap.of(0, A,
                1, TERM_FACTORY.getDBStringConstant("z")));

        IQTree newJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, newDataNode2));

        UnaryIQTree newIQTree = IQ_FACTORY.createUnaryIQTree(constructionNode1, newJoinTree);
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newIQTree);

        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinDifferentIRITemplate2() {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, A));

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2, ImmutableMap.of(0, B, 1, C));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, X);

        ImmutableList<Template.Component> iriTemplate1 = Template.builder().addSeparator("http://example.org/house/")
                .addColumn()
                .addSeparator("/z")
                .build();

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(iriTemplate1, ImmutableList.of(A))));

        ImmutableList<Template.Component> iriTemplate2 = Template.builder().addSeparator("http://example.org/house/")
                .addColumn()
                .addSeparator("/")
                .addColumn()
                .build();

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(iriTemplate2,
                        ImmutableList.of(B, C))));

        IQTree iqTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(
                        IQ_FACTORY.createUnaryIQTree(constructionNode1, dataNode1),
                        IQ_FACTORY.createUnaryIQTree(constructionNode2, dataNode2)));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, iqTree);

        ExtensionalDataNode newDataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2, ImmutableMap.of(0, A,
                1, TERM_FACTORY.getDBStringConstant("z")));

        IQTree newJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, newDataNode2));

        UnaryIQTree newIQTree = IQ_FACTORY.createUnaryIQTree(constructionNode1, newJoinTree);
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newIQTree);

        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinDifferentIRITemplate3() {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, A));

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2, ImmutableMap.of(0, B, 1, C));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, X);

        ImmutableList<Template.Component> iriTemplate1 = Template.builder().addSeparator("http://example.org/house/")
                .addColumn()
                .addSeparator(".z")
                .build();

        ImmutableFunctionalTerm iriTerm1 = TERM_FACTORY.getIRIFunctionalTerm(iriTemplate1, ImmutableList.of(A));

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, iriTerm1));

        ImmutableList<Template.Component> iriTemplate2 = Template.builder().addSeparator("http://example.org/house/")
                .addColumn()
                .addSeparator(".")
                .addColumn()
                .build();

        ImmutableFunctionalTerm iriTerm2 = TERM_FACTORY.getIRIFunctionalTerm(iriTemplate2,
                ImmutableList.of(B, C));

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, iriTerm2));

        IQTree iqTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(
                        IQ_FACTORY.createUnaryIQTree(constructionNode1, dataNode1),
                        IQ_FACTORY.createUnaryIQTree(constructionNode2, dataNode2)));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, iqTree);

        IQTree newJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(
                        TERM_FACTORY.getStrictEquality(
                                TERM_FACTORY.getNullRejectingDBConcatFunctionalTerm(
                                        ImmutableList.of(A, TERM_FACTORY.getDBStringConstant(".z"))),
                                TERM_FACTORY.getNullRejectingDBConcatFunctionalTerm(
                                        ImmutableList.of(B, TERM_FACTORY.getDBStringConstant("."), C)))),
                ImmutableList.of(dataNode1, dataNode2));

        UnaryIQTree newIQTree = IQ_FACTORY.createUnaryIQTree(constructionNode1, newJoinTree);
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newIQTree);

        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinDifferentIRITemplate4() {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, A));

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2, ImmutableMap.of(0, B, 1, C));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, X);

        ImmutableList<Template.Component> iriTemplate1 = Template.builder().addSeparator("http://example.org/house/")
                .addColumn()
                .addSeparator("#%2F")
                .build();

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(iriTemplate1, ImmutableList.of(A))));

        ImmutableList<Template.Component> iriTemplate2 = Template.builder().addSeparator("http://example.org/house/")
                .addColumn()
                .addSeparator("#")
                .addColumn()
                .build();

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(iriTemplate2,
                        ImmutableList.of(B, C))));

        IQTree iqTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(
                        IQ_FACTORY.createUnaryIQTree(constructionNode1, dataNode1),
                        IQ_FACTORY.createUnaryIQTree(constructionNode2, dataNode2)));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, iqTree);

        ExtensionalDataNode newDataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2, ImmutableMap.of(0, A,
                1, TERM_FACTORY.getDBStringConstant("/")));

        IQTree newJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, newDataNode2));

        UnaryIQTree newIQTree = IQ_FACTORY.createUnaryIQTree(constructionNode1, newJoinTree);
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newIQTree);

        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinDifferentIRITemplate5() {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, A));

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2, ImmutableMap.of(0, B, 1, C));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, X);

        ImmutableList<Template.Component> iriTemplate1 = Template.builder().addSeparator("http://example.org/house/")
                .addColumn()
                .addSeparator("%2Fz")
                .build();

        ImmutableFunctionalTerm iriTerm1 = TERM_FACTORY.getIRIFunctionalTerm(iriTemplate1, ImmutableList.of(A));

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, iriTerm1));

        ImmutableList<Template.Component> iriTemplate2 = Template.builder().addSeparator("http://example.org/house/")
                .addColumn()
                .addSeparator("%2F")
                .addColumn()
                .build();

        ImmutableFunctionalTerm iriTerm2 = TERM_FACTORY.getIRIFunctionalTerm(iriTemplate2,
                ImmutableList.of(B, C));

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, iriTerm2));

        IQTree iqTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(
                        IQ_FACTORY.createUnaryIQTree(constructionNode1, dataNode1),
                        IQ_FACTORY.createUnaryIQTree(constructionNode2, dataNode2)));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, iqTree);

        IQTree newJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(
                        TERM_FACTORY.getStrictEquality(
                                TERM_FACTORY.getNullRejectingDBConcatFunctionalTerm(
                                        ImmutableList.of(A, TERM_FACTORY.getDBStringConstant("/z"))),
                                TERM_FACTORY.getNullRejectingDBConcatFunctionalTerm(
                                        ImmutableList.of(B, TERM_FACTORY.getDBStringConstant("/"), C)))),
                ImmutableList.of(dataNode1, dataNode2));

        UnaryIQTree newIQTree = IQ_FACTORY.createUnaryIQTree(constructionNode1, newJoinTree);
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newIQTree);

        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testFilter1() {
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ImmutableExpression expression = createExpression(A);
        FilterNode filterNode = IQ_FACTORY.createFilterNode(expression);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(filterNode, extensionalDataNode);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        normalizeAndCompare(initialIQ, initialIQ);
    }

    @Test
    public void testFilter2() {
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ImmutableExpression expression = TERM_FACTORY.getStrictEquality( A, B);
        FilterNode filterNode = IQ_FACTORY.createFilterNode(expression);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(filterNode, extensionalDataNode);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        ExtensionalDataNode newExtensionalDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(B, B));
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(A, B));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createUnaryIQTree(constructionNode,
                newExtensionalDataNode));

        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testFilterUseless1() {
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ImmutableExpression expression = TERM_FACTORY.getStrictEquality(A, A);
        FilterNode filterNode = IQ_FACTORY.createFilterNode(expression);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(filterNode, extensionalDataNode);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, extensionalDataNode);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testFilterSubstituable1() {
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ImmutableExpression expression = TERM_FACTORY.getStrictEquality(A, B);
        FilterNode filterNode = IQ_FACTORY.createFilterNode(expression);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(filterNode, extensionalDataNode);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        ExtensionalDataNode newExtensionalDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(B, B));
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(A, B));

        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(constructionNode, newExtensionalDataNode);
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testFilterUnsatisfiable1() {
        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ImmutableExpression expression = TERM_FACTORY.getStrictEquality(ONE, TWO);
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

        ExtensionalDataNode extensionalDataNode = IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR2, ImmutableMap.of(0, A));

        ImmutableFunctionalTerm xDefinition = createInjectiveFunctionalTerm1(A);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(
                projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, xDefinition));

        ImmutableExpression expression = TERM_FACTORY.getDBDefaultInequality(LT, X, TWO);
        FilterNode filterNode = IQ_FACTORY.createFilterNode(expression);

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(filterNode,
                IQ_FACTORY.createUnaryIQTree(constructionNode, extensionalDataNode));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        FilterNode newFilterNode = IQ_FACTORY.createFilterNode(
                TERM_FACTORY.getDBDefaultInequality(LT, xDefinition, TWO));

        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(constructionNode,
                IQ_FACTORY.createUnaryIQTree(newFilterNode, extensionalDataNode));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testFilterBindings2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, X);

        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));

        ImmutableFunctionalTerm xDefinition = createInjectiveFunctionalTerm1(A);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(
                projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, xDefinition));

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        ImmutableExpression expression = TERM_FACTORY.getDBDefaultInequality(LT, X, TWO);
        FilterNode filterNode = IQ_FACTORY.createFilterNode(expression);

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(filterNode,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createUnaryIQTree(distinctNode, extensionalDataNode)));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        FilterNode newFilterNode = IQ_FACTORY.createFilterNode(
                TERM_FACTORY.getDBDefaultInequality(LT, xDefinition, TWO));

        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(constructionNode,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createUnaryIQTree(newFilterNode, extensionalDataNode)));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testFilterDistinct1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);

        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        ImmutableExpression expression = TERM_FACTORY.getDBDefaultInequality(LT, A, TWO);
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

        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        ImmutableExpression expression = TERM_FACTORY.getDBDefaultInequality(LT, A, TWO);
        FilterNode filterNode = IQ_FACTORY.createFilterNode(expression);

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(filterNode,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createUnaryIQTree(constructionNode, extensionalDataNode)));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        ExtensionalDataNode newExtensionalDataNode = IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR2, ImmutableMap.of(0, A));

        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createUnaryIQTree(filterNode, newExtensionalDataNode));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testFilterDistinct3() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);

        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        ImmutableExpression expression = TERM_FACTORY.getDBDefaultInequality(LT, B, TWO);
        FilterNode filterNode = IQ_FACTORY.createFilterNode(expression);

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(filterNode,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createUnaryIQTree(constructionNode, extensionalDataNode)));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createUnaryIQTree(filterNode, extensionalDataNode));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testFilterMerge1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);

        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));

        ImmutableExpression expression1 = TERM_FACTORY.getDBDefaultInequality(LT, A, TWO);
        FilterNode topFilterNode = IQ_FACTORY.createFilterNode(expression1);

        ImmutableExpression expression2 = TERM_FACTORY.getDBDefaultInequality(GT, B, ONE);
        FilterNode secondFilterNode = IQ_FACTORY.createFilterNode(expression2);

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(topFilterNode,
                IQ_FACTORY.createUnaryIQTree(secondFilterNode, extensionalDataNode));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        FilterNode newFilterNode = IQ_FACTORY.createFilterNode(
                TERM_FACTORY.getConjunction(expression1, expression2));

        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(newFilterNode, extensionalDataNode);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testFilterMerge2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);

        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        ImmutableExpression expression1 = TERM_FACTORY.getDBDefaultInequality(LT, A, TWO);
        FilterNode topFilterNode = IQ_FACTORY.createFilterNode(expression1);

        ImmutableExpression expression2 = TERM_FACTORY.getDBDefaultInequality(GT, B, ONE);
        FilterNode secondFilterNode = IQ_FACTORY.createFilterNode(expression2);

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(topFilterNode,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createUnaryIQTree(secondFilterNode, extensionalDataNode)));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        FilterNode newFilterNode = IQ_FACTORY.createFilterNode(
                TERM_FACTORY.getConjunction(expression1, expression2));

        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(distinctNode,
                IQ_FACTORY.createUnaryIQTree(newFilterNode, extensionalDataNode));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testFilterMerge3() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, A);

        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        ImmutableExpression expression1 = TERM_FACTORY.getDBDefaultInequality(LT, A, TWO);
        FilterNode topFilterNode = IQ_FACTORY.createFilterNode(expression1);

        ImmutableExpression expression2 = TERM_FACTORY.getDBDefaultInequality(GT, B, ONE);
        FilterNode secondFilterNode = IQ_FACTORY.createFilterNode(expression2);

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(topFilterNode,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createUnaryIQTree(secondFilterNode, extensionalDataNode)));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        FilterNode newFilterNode = IQ_FACTORY.createFilterNode(
                TERM_FACTORY.getConjunction(expression1, expression2));

        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(constructionNode,
                IQ_FACTORY.createUnaryIQTree(newFilterNode, extensionalDataNode));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testFilterMerge4() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, A);

        ExtensionalDataNode extensionalDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        ImmutableExpression expression1 = TERM_FACTORY.getDBDefaultInequality(LT, A, TWO);
        FilterNode topFilterNode = IQ_FACTORY.createFilterNode(expression1);

        ImmutableExpression expression2 = TERM_FACTORY.getDBDefaultInequality(GT, B, ONE);
        FilterNode secondFilterNode = IQ_FACTORY.createFilterNode(expression2);

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(topFilterNode,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createUnaryIQTree(distinctNode,
                                IQ_FACTORY.createUnaryIQTree(secondFilterNode, extensionalDataNode))));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        FilterNode newFilterNode = IQ_FACTORY.createFilterNode(
                TERM_FACTORY.getConjunction(expression1, expression2));

        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(constructionNode,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createUnaryIQTree(newFilterNode, extensionalDataNode)));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testFilterMergeJoin1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(A));

        ImmutableExpression expression1 = TERM_FACTORY.getDBDefaultInequality(LT, A, TWO);
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

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(A));

        ImmutableExpression expression1 = TERM_FACTORY.getDBDefaultInequality(LT, A, TWO);
        FilterNode topFilterNode = IQ_FACTORY.createFilterNode(expression1);

        ImmutableExpression expression2 = TERM_FACTORY.getDBDefaultInequality(GT, B, ONE);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(expression2);

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(topFilterNode,
                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(extensionalDataNode1, extensionalDataNode2)));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        InnerJoinNode newJoinNode = IQ_FACTORY.createInnerJoinNode(
                TERM_FACTORY.getConjunction(expression1, expression2));

        IQTree expectedTree = IQ_FACTORY.createNaryIQTree(newJoinNode,
                ImmutableList.of(extensionalDataNode1, extensionalDataNode2));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testFilterMergeJoin3() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, A);

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(A));

        ImmutableExpression expression1 = TERM_FACTORY.getDBDefaultInequality(LT, A, TWO);
        FilterNode topFilterNode = IQ_FACTORY.createFilterNode(expression1);

        ImmutableExpression expression2 = TERM_FACTORY.getDBDefaultInequality(GT, B, ONE);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(expression2);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(topFilterNode,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                    IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(extensionalDataNode1, extensionalDataNode2))));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        InnerJoinNode newJoinNode = IQ_FACTORY.createInnerJoinNode(
                TERM_FACTORY.getConjunction(expression1, expression2));

        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(constructionNode,
                IQ_FACTORY.createNaryIQTree(newJoinNode, ImmutableList.of(extensionalDataNode1, extensionalDataNode2)));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testFilterMergeJoin4() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(A));

        ImmutableExpression expression1 = TERM_FACTORY.getDBDefaultInequality(LT, A, TWO);
        FilterNode topFilterNode = IQ_FACTORY.createFilterNode(expression1);

        ImmutableExpression expression2 = TERM_FACTORY.getDBDefaultInequality(GT, B, ONE);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(expression2);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(topFilterNode,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(extensionalDataNode1, extensionalDataNode2))));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        InnerJoinNode newJoinNode = IQ_FACTORY.createInnerJoinNode(
                TERM_FACTORY.getConjunction(expression1, expression2));

        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(distinctNode,
                IQ_FACTORY.createNaryIQTree(newJoinNode, ImmutableList.of(extensionalDataNode1, extensionalDataNode2)));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLJ1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, C);

        ExtensionalDataNode extensionalDataNode1 = IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR2, ImmutableMap.of(0, A));

        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, C));

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();

        IQTree tree = IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, extensionalDataNode1, extensionalDataNode2);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);
        normalizeAndCompare(initialIQ, initialIQ);
    }

    @Test
    public void testLJ2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, C);

        ExtensionalDataNode extensionalDataNode1 = IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR2, ImmutableMap.of(0, A));

        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, D));

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getStrictEquality(A, C));

        IQTree tree = IQ_FACTORY.createUnaryIQTree(constructionNode,
                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, extensionalDataNode1, extensionalDataNode2));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        ExtensionalDataNode newExtensionalDataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, D));
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

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, D));

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
                TERM_FACTORY.getStrictEquality(
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

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE3_AR1, ImmutableList.of(E));
        ExtensionalDataNode extensionalDataNode3 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, D));

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
                TERM_FACTORY.getStrictEquality(X, createNonInjectiveFunctionalTerm(C, D)));

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

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, C));

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

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(PK_TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, C));

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

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(PK_TABLE2_AR2, ImmutableList.of(A, C));

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

        ExtensionalDataNode extensionalDataNode1 = IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR2, ImmutableMap.of(0, A));
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, C));

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();

        IQTree ljTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                extensionalDataNode1,
                IQ_FACTORY.createUnaryIQTree(distinctNode, extensionalDataNode2));

        IQTree tree = IQ_FACTORY.createUnaryIQTree(constructionNode, ljTree);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, ljTree);

        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLJDistinct5() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, C);

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, C));

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

        ExtensionalDataNode extensionalDataNode1 = IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR2, ImmutableMap.of(0, A));
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(PK_TABLE2_AR2, ImmutableList.of(A, C));

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();

        IQTree tree = IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                        extensionalDataNode1,
                        extensionalDataNode2);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);
        normalizeAndCompare(initialIQ, initialIQ);
    }

    @Test
    public void testLJDistinctAndBindings1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, D));

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

        LeftJoinNode newLeftJoinNode = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getStrictEquality(
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

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, D));

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
                        X, createNonInjectiveFunctionalTerm(A, B),
                        Y, createNonInjectiveFunctionalTerm(D, C)));

        LeftJoinNode newLeftJoinNode = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getStrictEquality(
                createNonInjectiveFunctionalTerm(A, B), createNonInjectiveFunctionalTerm(C, D)));

        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(topConstructionNode,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(newLeftJoinNode,
                                extensionalDataNode1, extensionalDataNode2)));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLJFilter1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR3_PREDICATE, A, B, C);

        FilterNode leftFilterNode = IQ_FACTORY.createFilterNode(createExpression(B));

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, C));

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

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, C));

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

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(B, C));
        ExtensionalDataNode extensionalDataNode3 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, D));

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

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, C));
        ExtensionalDataNode extensionalDataNode3 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, D));

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
    public void testLJTrue1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, C));

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(D),
                SUBSTITUTION_FACTORY.getSubstitution(D, TERM_FACTORY.getProvenanceSpecialConstant()));

        IQTree rightChild = IQ_FACTORY.createUnaryIQTree(
                rightConstructionNode,
                IQ_FACTORY.createTrueNode());

        IQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                extensionalDataNode1, rightChild);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(B, TERM_FACTORY.getIfElseNull(TERM_FACTORY.getDBIsNotNull(D), C)));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(topConstructionNode, leftJoinTree));

        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, extensionalDataNode2);

        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLJTrue2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));

        IQTree rightChild = IQ_FACTORY.createTrueNode();

        IQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                extensionalDataNode1, rightChild);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, leftJoinTree);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, extensionalDataNode1);

        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLJTrue3() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, C));

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(D),
                SUBSTITUTION_FACTORY.getSubstitution(D, TERM_FACTORY.getProvenanceSpecialConstant()));

        IQTree rightChild = IQ_FACTORY.createUnaryIQTree(
                rightConstructionNode,
                IQ_FACTORY.createTrueNode());

        ImmutableExpression condition = TERM_FACTORY.getStrictEquality(A, C);

        IQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(condition),
                extensionalDataNode1, rightChild);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(B, TERM_FACTORY.getIfElseNull(TERM_FACTORY.getDBIsNotNull(D), C)));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(topConstructionNode, leftJoinTree));

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(B, TERM_FACTORY.getIfElseNull(condition, C)));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(newConstructionNode, extensionalDataNode1));

        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoin1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR3_PREDICATE, A, B, C);

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, C));

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();

        NaryIQTree initialTree = IQ_FACTORY.createNaryIQTree(joinNode,
                ImmutableList.of(extensionalDataNode1, extensionalDataNode2));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        normalizeAndCompare(initialIQ, initialIQ);
    }

    @Test
    public void testJoin2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR0_PREDICATE);

        TrueNode trueNode = IQ_FACTORY.createTrueNode();

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();

        NaryIQTree initialTree = IQ_FACTORY.createNaryIQTree(joinNode,
                ImmutableList.of(trueNode, trueNode));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, trueNode);

        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinDistinct1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR3_PREDICATE, A, B, C);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, C));

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

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(PK_TABLE2_AR2, ImmutableList.of(A, C));

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

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, C));

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

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR3, ImmutableList.of(A, B, C));
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(PK_TABLE2_AR2, ImmutableList.of(D, E));

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
                        Y, createNonInjectiveFunctionalTerm(B, C),
                        Z, createNonInjectiveFunctionalTerm(D, E)));

        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(newTopConstructionNode,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createNaryIQTree(joinNode,
                                ImmutableList.of(extensionalDataNode1, extensionalDataNode2))));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);

        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinDistinct5() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR3_PREDICATE, X, Y, Z);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR3, ImmutableList.of(A, B, C));
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(PK_TABLE2_AR2, ImmutableList.of(D, E));

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
                        Y, createNonInjectiveFunctionalTerm(B, C),
                        Z, createNonInjectiveFunctionalTerm(D, E)));

        InnerJoinNode newJoinNode = IQ_FACTORY.createInnerJoinNode(leftFilterNode.getFilterCondition());

        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(newTopConstructionNode,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createNaryIQTree(newJoinNode,
                                ImmutableList.of(extensionalDataNode1, extensionalDataNode2))));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);

        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinDistinct6() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, Y, Z);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        ExtensionalDataNode extensionalDataNode1 = IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR3,
                ImmutableMap.of(1, B, 2, C));
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(PK_TABLE2_AR2, ImmutableList.of(D, E));

        ConstructionNode lowerLeftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                SUBSTITUTION_FACTORY.getSubstitution(Y, createNonInjectiveFunctionalTerm(B, C)));

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Z),
                SUBSTITUTION_FACTORY.getSubstitution(Z, createNonInjectiveFunctionalTerm(D, E)));

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();

        NaryIQTree initialTree = IQ_FACTORY.createNaryIQTree(joinNode,
                ImmutableList.of(
                        IQ_FACTORY.createUnaryIQTree(distinctNode,
                                IQ_FACTORY.createUnaryIQTree(lowerLeftConstructionNode, extensionalDataNode1)),
                        IQ_FACTORY.createUnaryIQTree(rightConstructionNode, extensionalDataNode2)));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        ConstructionNode newTopConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        Z, createNonInjectiveFunctionalTerm(D, E)));

        ConstructionNode newLowerConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y, D, E),
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
    public void testJoinMerge1() {
        testJoinSimpleMerge(Optional.empty());
    }

    @Test
    public void testJoinMerge2() {
        testJoinSimpleMerge(Optional.of(TERM_FACTORY.getStrictNEquality(B, C)));
    }

    private void testJoinSimpleMerge(Optional<ImmutableExpression> leftExpression) {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR5_PREDICATE, A, B, C, D, E);

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, C));
        ExtensionalDataNode extensionalDataNode3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(A, D));
        ExtensionalDataNode extensionalDataNode4 = createExtensionalDataNode(TABLE4_AR2, ImmutableList.of(A, E));

        InnerJoinNode lowerJoinNode = IQ_FACTORY.createInnerJoinNode(leftExpression);

        NaryIQTree tree = IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(
                        IQ_FACTORY.createNaryIQTree(lowerJoinNode,
                                ImmutableList.of(extensionalDataNode1, extensionalDataNode2)),
                        extensionalDataNode3,
                        extensionalDataNode4));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createNaryIQTree(lowerJoinNode,
                ImmutableList.of(extensionalDataNode1, extensionalDataNode2, extensionalDataNode3, extensionalDataNode4)));

        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinMerge3() {
        testJoinSimpleMerge2(Optional.empty());
    }

    @Test
    public void testJoinMerge4() {
        testJoinSimpleMerge2(Optional.of(TERM_FACTORY.getStrictNEquality(B, E)));
    }

    private void testJoinSimpleMerge2(Optional<ImmutableExpression> topExpression) {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR5_PREDICATE, A, B, C, D, E);

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, C));
        ExtensionalDataNode extensionalDataNode3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(A, D));
        ExtensionalDataNode extensionalDataNode4 = createExtensionalDataNode(TABLE4_AR2, ImmutableList.of(A, E));

        ImmutableExpression leftExpression = TERM_FACTORY.getStrictNEquality( B, C);
        ImmutableExpression rightExpression = TERM_FACTORY.getStrictNEquality( A, D);

        NaryIQTree tree = IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(topExpression),
                ImmutableList.of(
                        IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(leftExpression),
                                ImmutableList.of(extensionalDataNode1, extensionalDataNode2)),
                        IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(rightExpression), extensionalDataNode3),
                        extensionalDataNode4));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        ImmutableExpression newExpression = topExpression
                .map(e -> TERM_FACTORY.getConjunction(e, leftExpression, rightExpression))
                .orElseGet(() -> TERM_FACTORY.getConjunction(leftExpression, rightExpression));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(newExpression),
                ImmutableList.of(extensionalDataNode1, extensionalDataNode2, extensionalDataNode3, extensionalDataNode4)));

        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinMerge5() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR5_PREDICATE, A, B, C, D, E);

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, C));
        ExtensionalDataNode extensionalDataNode3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(A, D));
        ExtensionalDataNode extensionalDataNode4 = createExtensionalDataNode(TABLE4_AR2, ImmutableList.of(A, E));

        ImmutableExpression leftExpression = TERM_FACTORY.getStrictNEquality( B, C);
        ImmutableExpression rightExpression = TERM_FACTORY.getStrictNEquality( D, E);

        NaryIQTree tree = IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(
                        IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(leftExpression),
                                ImmutableList.of(extensionalDataNode1, extensionalDataNode2)),
                        IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(rightExpression),
                        ImmutableList.of(extensionalDataNode3, extensionalDataNode4))));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        ImmutableExpression newExpression = TERM_FACTORY.getConjunction(leftExpression, rightExpression);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(newExpression),
                ImmutableList.of(extensionalDataNode1, extensionalDataNode2, extensionalDataNode3, extensionalDataNode4)));

        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinConstraintPropagation1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR3_PREDICATE, A, X, D);

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, C));
        ExtensionalDataNode extensionalDataNode3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(A, D));

        ConstructionNode leftConstruction = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, X),
                SUBSTITUTION_FACTORY.getSubstitution(X, createInjectiveFunctionalTerm1(B)));

        ConstructionNode rightConstruction = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, X),
                SUBSTITUTION_FACTORY.getSubstitution(X, NULL));

        ImmutableExpression topExpression = TERM_FACTORY.getDBIsNotNull(X);

        NaryIQTree unionTree = IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createUnionNode(leftConstruction.getVariables()),
                ImmutableList.of(
                        IQ_FACTORY.createUnaryIQTree(leftConstruction, extensionalDataNode1),
                        IQ_FACTORY.createUnaryIQTree(rightConstruction, extensionalDataNode2)));

        NaryIQTree tree = IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(topExpression),
                ImmutableList.of(unionTree, extensionalDataNode3));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        ConstructionNode newConstruction = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, X, D),
                SUBSTITUTION_FACTORY.getSubstitution(X, createInjectiveFunctionalTerm1(B)));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(newConstruction,
                        IQ_FACTORY.createNaryIQTree(
                                IQ_FACTORY.createInnerJoinNode(),
                                ImmutableList.of(extensionalDataNode1, extensionalDataNode3))));

        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinConstraintPropagation2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);

        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(INT_TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(INT_TABLE2_AR2, ImmutableList.of(A, B));

        ImmutableExpression expression = TERM_FACTORY.getDBDefaultInequality(LT, A, B);

        NaryIQTree tree = IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(
                        IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(expression), extensionalDataNode1),
                        IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(expression), extensionalDataNode2)));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(expression),
                ImmutableList.of(extensionalDataNode1, extensionalDataNode2)));

        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testProvenanceVariableOnLJDistinct1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR3_PREDICATE, A, B, C);

        ExtensionalDataNode leftNode = createExtensionalDataNode(INT_TABLE1_NULL_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode rightNode = IQ_FACTORY.createExtensionalDataNode(
                INT_TABLE2_NULL_AR2,
                ImmutableMap.of(1, B));

        ImmutableSubstitution<ImmutableTerm> provenanceSubstitution = SUBSTITUTION_FACTORY.getSubstitution(E, TERM_FACTORY.getProvenanceSpecialConstant());

        ConstructionNode rightTopConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(B, E), provenanceSubstitution);
        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        ConstructionNode subConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(B));

        UnaryIQTree leftChild = IQ_FACTORY.createUnaryIQTree(distinctNode, leftNode);

        UnaryIQTree rightChild = IQ_FACTORY.createUnaryIQTree(
                rightTopConstructionNode,
                IQ_FACTORY.createUnaryIQTree(
                        distinctNode,
                        IQ_FACTORY.createUnaryIQTree(
                                subConstructionNode,
                                rightNode)));

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, leftChild, rightChild);

        ConstructionNode rootConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(C,
                        TERM_FACTORY.getIfElseNull(
                                TERM_FACTORY.getDBIsNotNull(E),
                                TERM_FACTORY.getDBStringConstant("ok"))));

        UnaryIQTree initialTree = IQ_FACTORY.createUnaryIQTree(rootConstructionNode, leftJoinTree);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(
                rootConstructionNode,
                    IQ_FACTORY.createUnaryIQTree(
                    distinctNode,
                    IQ_FACTORY.createBinaryNonCommutativeIQTree(
                            leftJoinNode,
                            leftNode,
                            IQ_FACTORY.createUnaryIQTree(
                                    rightTopConstructionNode,
                                    rightNode))));
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    /**
     * Left is not distinct, no change
     */
    @Test
    public void testProvenanceVariableOnLJDistinct2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR3_PREDICATE, A, B, C);

        ExtensionalDataNode leftNode = createExtensionalDataNode(INT_TABLE1_NULL_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode rightNode = IQ_FACTORY.createExtensionalDataNode(
                INT_TABLE2_NULL_AR2,
                ImmutableMap.of(1, B));

        ImmutableSubstitution<ImmutableTerm> provenanceSubstitution = SUBSTITUTION_FACTORY.getSubstitution(E, TERM_FACTORY.getProvenanceSpecialConstant());

        ConstructionNode rightTopConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(B, E), provenanceSubstitution);
        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        ConstructionNode subConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(B));

        UnaryIQTree rightChild = IQ_FACTORY.createUnaryIQTree(
                rightTopConstructionNode, IQ_FACTORY.createUnaryIQTree(
                        distinctNode,
                        IQ_FACTORY.createUnaryIQTree(
                                subConstructionNode,
                                rightNode)));

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, leftNode, rightChild);

        ConstructionNode rootConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(C,
                        TERM_FACTORY.getIfElseNull(
                                TERM_FACTORY.getDBIsNotNull(E),
                                TERM_FACTORY.getDBStringConstant("ok"))));

        UnaryIQTree initialTree = IQ_FACTORY.createUnaryIQTree(rootConstructionNode, leftJoinTree);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        UnaryIQTree newRightChild = IQ_FACTORY.createUnaryIQTree(
                rightTopConstructionNode, IQ_FACTORY.createUnaryIQTree(
                        distinctNode,
                        rightNode));


        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                leftNode, newRightChild);
        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(rootConstructionNode, newLeftJoinTree);
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);

        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testProvenanceVariableOnLJDistinct3() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR3_PREDICATE, A, B, C);

        ExtensionalDataNode leftNode = createExtensionalDataNode(INT_TABLE1_NULL_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode rightNode = createExtensionalDataNode(INT_TABLE2_NULL_AR2, ImmutableList.of(D, B));

        ImmutableSubstitution<ImmutableTerm> provenanceSubstitution = SUBSTITUTION_FACTORY.getSubstitution(E, TERM_FACTORY.getProvenanceSpecialConstant());

        ConstructionNode rightTopConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(B, E), provenanceSubstitution);
        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        ConstructionNode subConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(D, B));

        UnaryIQTree leftChild = IQ_FACTORY.createUnaryIQTree(distinctNode, leftNode);

        UnaryIQTree rightChild = IQ_FACTORY.createUnaryIQTree(
                rightTopConstructionNode,
                IQ_FACTORY.createUnaryIQTree(
                        distinctNode,
                        IQ_FACTORY.createUnaryIQTree(
                                subConstructionNode,
                                rightNode)));

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, leftChild, rightChild);

        ConstructionNode rootConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(C,
                        TERM_FACTORY.getIfElseNull(
                                TERM_FACTORY.getDBIsNotNull(E),
                                TERM_FACTORY.getDBStringConstant("ok"))));

        UnaryIQTree initialTree = IQ_FACTORY.createUnaryIQTree(rootConstructionNode, leftJoinTree);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);


        ConstructionNode newRightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(B, D, E), provenanceSubstitution);

        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(
                rootConstructionNode,
                IQ_FACTORY.createUnaryIQTree(
                        distinctNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(
                                leftJoinNode,
                                leftNode,
                                IQ_FACTORY.createUnaryIQTree(
                                        newRightConstructionNode,
                                        rightNode))));
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testProvenanceVariableOnLJFilter1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR3_PREDICATE, A, B, C);

        ExtensionalDataNode leftNode = createExtensionalDataNode(INT_TABLE2_NULL_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode rightNode = IQ_FACTORY.createExtensionalDataNode(INT_TABLE1_NULL_AR3,
                ImmutableMap.of(0, D, 1,B));

        ImmutableSubstitution<ImmutableTerm> provenanceSubstitution = SUBSTITUTION_FACTORY.getSubstitution(E, TERM_FACTORY.getProvenanceSpecialConstant());

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(B, E), provenanceSubstitution);
        FilterNode filterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBNumericInequality(LT, D, ONE));

        UnaryIQTree rightChild = IQ_FACTORY.createUnaryIQTree(
                rightConstructionNode,
                IQ_FACTORY.createUnaryIQTree(filterNode, rightNode));

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, leftNode, rightChild);

        ConstructionNode rootConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(C,
                        TERM_FACTORY.getIfElseNull(
                                TERM_FACTORY.getDBIsNotNull(E),
                                TERM_FACTORY.getDBStringConstant("ok"))));

        UnaryIQTree initialTree = IQ_FACTORY.createUnaryIQTree(rootConstructionNode, leftJoinTree);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        LeftJoinNode newLeftJoinNode = IQ_FACTORY.createLeftJoinNode(filterNode.getFilterCondition());

        ConstructionNode newRightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(B, D, E), provenanceSubstitution);

        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(
                rootConstructionNode,
                IQ_FACTORY.createBinaryNonCommutativeIQTree(
                        newLeftJoinNode,
                        leftNode,
                        IQ_FACTORY.createUnaryIQTree(
                                newRightConstructionNode,
                                rightNode)));
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testProvenanceVariableOnLJFilter2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR3_PREDICATE, A, B, C);

        ExtensionalDataNode leftNode = createExtensionalDataNode(INT_TABLE2_NULL_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode rightNode = IQ_FACTORY.createExtensionalDataNode(INT_TABLE1_NULL_AR3,
                ImmutableMap.of(0, D, 1,B));

        ImmutableExpression ljExpression = TERM_FACTORY.getDBNumericInequality(GT, B, ONE);
        ImmutableExpression filterExpression = TERM_FACTORY.getDBNumericInequality(LT, D, ONE);

        ImmutableSubstitution<ImmutableTerm> provenanceSubstitution = SUBSTITUTION_FACTORY.getSubstitution(E, TERM_FACTORY.getProvenanceSpecialConstant());

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(B, E), provenanceSubstitution);
        FilterNode filterNode = IQ_FACTORY.createFilterNode(filterExpression);

        UnaryIQTree rightChild = IQ_FACTORY.createUnaryIQTree(
                rightConstructionNode,
                IQ_FACTORY.createUnaryIQTree(filterNode, rightNode));

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode(ljExpression);
        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, leftNode, rightChild);

        ConstructionNode rootConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(C,
                        TERM_FACTORY.getIfElseNull(
                                TERM_FACTORY.getDBIsNotNull(E),
                                TERM_FACTORY.getDBStringConstant("ok"))));

        UnaryIQTree initialTree = IQ_FACTORY.createUnaryIQTree(rootConstructionNode, leftJoinTree);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        LeftJoinNode newLeftJoinNode = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getConjunction(ljExpression, filterExpression));

        ConstructionNode newRightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(B, D, E), provenanceSubstitution);

        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(
                rootConstructionNode,
                IQ_FACTORY.createBinaryNonCommutativeIQTree(
                        newLeftJoinNode,
                        leftNode,
                        IQ_FACTORY.createUnaryIQTree(
                                newRightConstructionNode,
                                rightNode)));
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testProvenanceVariableOnLJInnerJoin1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR3_PREDICATE, A, B, C);

        ExtensionalDataNode leftNode = createExtensionalDataNode(INT_TABLE1_NULL_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode rightNode1 = createExtensionalDataNode(INT_TABLE2_NULL_AR2, ImmutableList.of(D, B));
        ExtensionalDataNode rightNode2 = IQ_FACTORY.createExtensionalDataNode(INT_TABLE2_NULL_AR2,
                ImmutableMap.of(0, D));

        ImmutableExpression joinExpression = TERM_FACTORY.getDBNumericInequality(LT, D, ONE);

        ImmutableSubstitution<ImmutableTerm> provenanceSubstitution = SUBSTITUTION_FACTORY.getSubstitution(E, TERM_FACTORY.getProvenanceSpecialConstant());

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(B, E), provenanceSubstitution);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(joinExpression);

        UnaryIQTree rightChild = IQ_FACTORY.createUnaryIQTree(
                rightConstructionNode,
                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(rightNode1, rightNode2)));

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, leftNode, rightChild);

        ConstructionNode rootConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(C,
                        TERM_FACTORY.getIfElseNull(
                                TERM_FACTORY.getDBIsNotNull(E),
                                TERM_FACTORY.getDBStringConstant("ok"))));

        UnaryIQTree initialTree = IQ_FACTORY.createUnaryIQTree(rootConstructionNode, leftJoinTree);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        LeftJoinNode newLeftJoinNode = IQ_FACTORY.createLeftJoinNode(joinExpression);

        ConstructionNode newRightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(B, D, E), provenanceSubstitution);

        NaryIQTree newJoinTree = IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(rightNode1, rightNode2));

        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(
                rootConstructionNode,
                IQ_FACTORY.createBinaryNonCommutativeIQTree(
                        newLeftJoinNode,
                        leftNode,
                        IQ_FACTORY.createUnaryIQTree(
                                newRightConstructionNode,
                                newJoinTree)));
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testProvenanceVariableOnLJInnerJoin2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR3_PREDICATE, A, B, C);

        ExtensionalDataNode leftNode = createExtensionalDataNode(INT_TABLE1_NULL_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode rightNode1 = createExtensionalDataNode(INT_TABLE2_NULL_AR2, ImmutableList.of(D, B));
        ExtensionalDataNode rightNode2 = IQ_FACTORY.createExtensionalDataNode(INT_TABLE2_NULL_AR2,
                ImmutableMap.of(0, D));

        ImmutableExpression joinExpression = TERM_FACTORY.getDBNumericInequality(LT, D, ONE);
        ImmutableExpression ljExpression = TERM_FACTORY.getDBNumericInequality(GT, B, ONE);

        ImmutableSubstitution<ImmutableTerm> provenanceSubstitution = SUBSTITUTION_FACTORY.getSubstitution(E, TERM_FACTORY.getProvenanceSpecialConstant());

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(B, E), provenanceSubstitution);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(joinExpression);

        UnaryIQTree rightChild = IQ_FACTORY.createUnaryIQTree(
                rightConstructionNode,
                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(rightNode1, rightNode2)));

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode(ljExpression);
        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, leftNode, rightChild);

        ConstructionNode rootConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(C,
                        TERM_FACTORY.getIfElseNull(
                                TERM_FACTORY.getDBIsNotNull(E),
                                TERM_FACTORY.getDBStringConstant("ok"))));

        UnaryIQTree initialTree = IQ_FACTORY.createUnaryIQTree(rootConstructionNode, leftJoinTree);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        LeftJoinNode newLeftJoinNode = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getConjunction(ljExpression, joinExpression));

        ConstructionNode newRightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(B, D, E), provenanceSubstitution);

        NaryIQTree newJoinTree = IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(rightNode1, rightNode2));

        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(
                rootConstructionNode,
                IQ_FACTORY.createBinaryNonCommutativeIQTree(
                        newLeftJoinNode,
                        leftNode,
                        IQ_FACTORY.createUnaryIQTree(
                                newRightConstructionNode,
                                newJoinTree)));
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testSelfEquality() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, A);

        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(INT_TABLE1_NULL_AR2,
                ImmutableMap.of(0, A));

        UnaryIQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createFilterNode(TERM_FACTORY.getStrictEquality(A, A)),
                dataNode);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(A)),
                dataNode);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);

        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testSelfInequality() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, A);

        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(INT_TABLE1_NULL_AR2,
                ImmutableMap.of(0, A));

        UnaryIQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createFilterNode(TERM_FACTORY.getStrictNEquality(A, A)),
                dataNode);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        IQTree expectedTree = IQ_FACTORY.createEmptyNode(projectionAtom.getVariables());

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);

        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testDisjunctionSameIsNotNull() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, X);

        RDFTermTypeConstant enTerm = TERM_FACTORY.getRDFTermTypeConstant(TYPE_FACTORY.getLangTermType("en"));

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X,
                        TERM_FACTORY.getRDFFunctionalTerm(
                                TERM_FACTORY.getDBCoalesce(B, B),
                                TERM_FACTORY.getIfElseNull(
                                        TERM_FACTORY.getDisjunction(
                                                TERM_FACTORY.getDBIsNotNull(B),
                                                TERM_FACTORY.getDBIsNotNull(B)),
                                        enTerm))
                                ));


        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(INT_TABLE1_NULL_AR2,
                ImmutableMap.of( 1, B));

        UnaryIQTree initialTree = IQ_FACTORY.createUnaryIQTree(constructionNode1, dataNode);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X,
                        TERM_FACTORY.getRDFFunctionalTerm(
                                B,
                                TERM_FACTORY.getIfElseNull(
                                        TERM_FACTORY.getDBIsNotNull(B),
                                        enTerm))));

        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(newConstructionNode, dataNode);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testDisjunctionSimilarIsNotNull() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, X);

        RDFTermTypeConstant enTerm = TERM_FACTORY.getRDFTermTypeConstant(TYPE_FACTORY.getLangTermType("en"));

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X,
                        TERM_FACTORY.getRDFFunctionalTerm(
                                TERM_FACTORY.getDBCoalesce(B, B),
                                TERM_FACTORY.getIfElseNull(
                                        TERM_FACTORY.getDisjunction(
                                                TERM_FACTORY.getDBIsNotNull(TERM_FACTORY.getNullRejectingDBConcatFunctionalTerm(ImmutableList.of(B, TERM_FACTORY.getDBStringConstant("stuff")))),
                                                TERM_FACTORY.getDBIsNotNull(TERM_FACTORY.getNullRejectingDBConcatFunctionalTerm(ImmutableList.of(B, TERM_FACTORY.getDBStringConstant("other"))))),
                                        enTerm))
                ));


        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(INT_TABLE1_NULL_AR2,
                ImmutableMap.of( 1, B));

        UnaryIQTree initialTree = IQ_FACTORY.createUnaryIQTree(constructionNode1, dataNode);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X,
                        TERM_FACTORY.getRDFFunctionalTerm(
                                B,
                                TERM_FACTORY.getIfElseNull(
                                        TERM_FACTORY.getDBIsNotNull(B),
                                        enTerm))));

        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(newConstructionNode, dataNode);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);
        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLJOnConstantWithLangTags() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, X);

        DBConstant intConstant = TERM_FACTORY.getDBIntegerConstant(54);

        DBTypeFactory dbTypeFactory = TYPE_FACTORY.getDBTypeFactory();
        ImmutableFunctionalTerm lexicalTerm = TERM_FACTORY.getDBCastFunctionalTerm(
                dbTypeFactory.getDBLargeIntegerType(), dbTypeFactory.getDBStringType(), A);


        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getRDFFunctionalTerm(lexicalTerm, L)));

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode(
                TERM_FACTORY.getStrictEquality(TERM_FACTORY.getDBStringConstant("en"), TERM_FACTORY.getLangTypeFunctionalTerm(L)));

        ExtensionalDataNode leftChild = IQ_FACTORY.createExtensionalDataNode(INT_TABLE1_AR2, ImmutableMap.of(0, intConstant));


        UnionNode unionNode = IQ_FACTORY.createUnionNode(topConstructionNode.getChildVariables());

        RDFTermTypeConstant enConstant = TERM_FACTORY.getRDFTermTypeConstant(TYPE_FACTORY.getLangTermType("en"));

        ConstructionNode rightConstruction1 = IQ_FACTORY.createConstructionNode(unionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        L, enConstant));

        ConstructionNode rightConstruction2 = IQ_FACTORY.createConstructionNode(unionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        L, TERM_FACTORY.getRDFTermTypeConstant(TYPE_FACTORY.getLangTermType("de"))));

        ExtensionalDataNode rightDataNode1 = IQ_FACTORY.createExtensionalDataNode(INT_TABLE1_AR2, ImmutableMap.of(0, intConstant, 1, A));
        ExtensionalDataNode rightDataNode2 = IQ_FACTORY.createExtensionalDataNode(INT_TABLE2_AR2, ImmutableMap.of(0, intConstant, 1, A));

        NaryIQTree unionTree = IQ_FACTORY.createNaryIQTree(unionNode,
                ImmutableList.of(
                        IQ_FACTORY.createUnaryIQTree(rightConstruction1, rightDataNode1),
                        IQ_FACTORY.createUnaryIQTree(rightConstruction2, rightDataNode2)));

        UnaryIQTree initialTree = IQ_FACTORY.createUnaryIQTree(topConstructionNode,
                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, leftChild, unionTree));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        ConstructionNode newTopConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getRDFFunctionalTerm(lexicalTerm,
                        TERM_FACTORY.getIfElseNull(
                                TERM_FACTORY.getDBIsNotNull(A),
                                enConstant))));

        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(
                newTopConstructionNode,
                IQ_FACTORY.createBinaryNonCommutativeIQTree(
                        IQ_FACTORY.createLeftJoinNode(),
                        leftChild,
                        rightDataNode1));

        normalizeAndCompare(initialIQ, IQ_FACTORY.createIQ(projectionAtom, expectedTree));
    }

    @Test
    public void testDistinctLiftNullableUC1() {
        OptimizationTestingTools.OfflineMetadataProviderBuilder3 builder = createMetadataProviderBuilder();
        NamedRelationDefinition table50 = builder.createRelationWithUC(50, 2, true);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR1, ImmutableMap.of(0, A));
        IQTree firstChild = IQ_FACTORY.createUnaryIQTree(distinctNode, dataNode1);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(table50, ImmutableMap.of(0, A, 1, B));

        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode();

        NaryIQTree initialTree = IQ_FACTORY.createNaryIQTree(
                innerJoinNode,
                ImmutableList.of(firstChild, dataNode2));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(
                distinctNode,
                IQ_FACTORY.createNaryIQTree(innerJoinNode,
                        ImmutableList.of(dataNode1, dataNode2)));
        normalizeAndCompare(initialIQ, IQ_FACTORY.createIQ(projectionAtom, expectedTree));
    }

    @Test
    public void testDistinctLiftNullableUC2() {
        OptimizationTestingTools.OfflineMetadataProviderBuilder3 builder = createMetadataProviderBuilder();
        NamedRelationDefinition table50 = builder.createRelationWithUC(50, 2, true);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR1, ImmutableMap.of(0, A));
        IQTree firstChild = IQ_FACTORY.createUnaryIQTree(distinctNode, dataNode1);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(table50, ImmutableMap.of(0, B));

        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getDBIsNotNull(B));

        NaryIQTree initialTree = IQ_FACTORY.createNaryIQTree(
                innerJoinNode,
                ImmutableList.of(firstChild, dataNode2));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(
                distinctNode,
                IQ_FACTORY.createNaryIQTree(innerJoinNode,
                        ImmutableList.of(dataNode1, dataNode2)));
        normalizeAndCompare(initialIQ, IQ_FACTORY.createIQ(projectionAtom, expectedTree));
    }

    @Test
    public void testDistinctLiftNullableUC3() {
        OptimizationTestingTools.OfflineMetadataProviderBuilder3 builder = createMetadataProviderBuilder();
        NamedRelationDefinition table50 = builder.createRelationWithUC(50, 2, true);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR1, ImmutableMap.of(0, A));
        IQTree firstChild = IQ_FACTORY.createUnaryIQTree(distinctNode, dataNode1);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(table50, ImmutableMap.of(0, B));

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getDBIsNotNull(B));

        IQTree initialTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                leftJoinNode,
                firstChild, dataNode2);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(
                distinctNode,
                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, dataNode1, dataNode2));
        normalizeAndCompare(initialIQ, IQ_FACTORY.createIQ(projectionAtom, expectedTree));
    }

    @Test
    public void testCoalesceLJ1() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);

        ImmutableList<Template.Component> template1 = Template.builder().addSeparator("http://example.org/ds1/").addColumn().build();
        ImmutableList<Template.Component> template2 = Template.builder().addSeparator("http://example.org/ds2/").addColumn().build();

        ImmutableSet<Variable> unionVariables = ImmutableSet.of(X, D);

        ExtensionalDataNode leftDataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, A, 1, D));
        UnaryIQTree leftChild1 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(unionVariables,
                        SUBSTITUTION_FACTORY.getSubstitution(
                                X, TERM_FACTORY.getIRIFunctionalTerm(template1, ImmutableList.of(A)).getTerm(0))),
                leftDataNode1);

        ExtensionalDataNode leftDataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2, ImmutableMap.of(0, B, 1, D));
        UnaryIQTree leftChild2 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(unionVariables,
                        SUBSTITUTION_FACTORY.getSubstitution(
                                X, TERM_FACTORY.getIRIFunctionalTerm(template2, ImmutableList.of(B)).getTerm(0))),
                leftDataNode2);

        NaryIQTree unionTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(unionVariables),
                ImmutableList.of(leftChild1, leftChild2));


        ExtensionalDataNode rightDataNode = IQ_FACTORY.createExtensionalDataNode(TABLE13_AR2, ImmutableMap.of(0, C, 1, Y));
        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                unionTree, rightDataNode);

        ImmutableTerm rightXDef = TERM_FACTORY.getIRIFunctionalTerm(template1, ImmutableList.of(C)).getTerm(0);
        ImmutableTerm zDef = TERM_FACTORY.getIRIFunctionalTerm(template2, ImmutableList.of(D)).getTerm(0);

        UnaryIQTree filterTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createFilterNode(TERM_FACTORY.getStrictEquality(X,
                        TERM_FACTORY.getDBCoalesce(rightXDef, zDef))),
                leftJoinTree);

        UnaryIQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(projectionAtom.getVariables()),
                filterTree);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);


        UnaryIQTree newFilterTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBBooleanCoalesce(
                        ImmutableList.of(
                                TERM_FACTORY.getStrictEquality(rightXDef, X),
                                TERM_FACTORY.getStrictEquality(zDef, X)))),
                leftJoinTree);


        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(projectionAtom.getVariables()),
                newFilterTree);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);

        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testCoalesceLJ2() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);

        ImmutableList<Template.Component> template1 = Template.builder().addSeparator("http://example.org/ds1/").addColumn().build();
        ImmutableList<Template.Component> template2 = Template.builder().addSeparator("http://example.org/ds2/").addColumn().build();

        ImmutableSet<Variable> unionVariables = ImmutableSet.of(X);

        ExtensionalDataNode leftDataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR1, ImmutableMap.of(0, A));
        UnaryIQTree leftChild1 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(unionVariables,
                        SUBSTITUTION_FACTORY.getSubstitution(
                                X, TERM_FACTORY.getIRIFunctionalTerm(template1, ImmutableList.of(A)).getTerm(0))),
                leftDataNode1);

        ExtensionalDataNode leftDataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR1, ImmutableMap.of(0, B));
        UnaryIQTree leftChild2 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(unionVariables,
                        SUBSTITUTION_FACTORY.getSubstitution(
                                X, TERM_FACTORY.getIRIFunctionalTerm(template2, ImmutableList.of(B)).getTerm(0))),
                leftDataNode2);

        NaryIQTree unionTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(unionVariables),
                ImmutableList.of(leftChild1, leftChild2));

        ExtensionalDataNode leftDataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE4_AR1, ImmutableMap.of(0, D));

        NaryIQTree leftTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(unionTree, leftDataNode3));


        ExtensionalDataNode rightDataNode = IQ_FACTORY.createExtensionalDataNode(TABLE13_AR2, ImmutableMap.of(0, C, 1, Y));
        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                leftTree, rightDataNode);

        ImmutableTerm rightXDef = TERM_FACTORY.getIRIFunctionalTerm(template1, ImmutableList.of(C)).getTerm(0);
        ImmutableTerm zDef = TERM_FACTORY.getIRIFunctionalTerm(template2, ImmutableList.of(D)).getTerm(0);

        UnaryIQTree filterTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createFilterNode(TERM_FACTORY.getStrictEquality(X,
                        TERM_FACTORY.getDBCoalesce(rightXDef, zDef))),
                leftJoinTree);

        UnaryIQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(projectionAtom.getVariables()),
                filterTree);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        UnaryIQTree newFilterTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBBooleanCoalesce(
                        ImmutableList.of(
                                TERM_FACTORY.getStrictEquality(rightXDef, X),
                                TERM_FACTORY.getStrictEquality(zDef, X)))),
                leftJoinTree);


        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(projectionAtom.getVariables()),
                newFilterTree);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);

        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testInnerJoinCoalesce1() {
        SPARQLFunctionSymbol sparqlCoalesce2FunctionSymbol = FUNCTION_SYMBOL_FACTORY.getSPARQLFunctionSymbol("COALESCE", 2)
                .orElseThrow(() -> new RuntimeException("Should have been available"));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE13_AR2, ImmutableMap.of(0, D, 1, E));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE13_AR2, ImmutableMap.of(0, F));

        ImmutableFunctionalTerm defA = TERM_FACTORY.getRDFFunctionalTerm(
                D,
                TERM_FACTORY.getIfElseNull(
                        TERM_FACTORY.getDBIsNotNull(D),
                        TERM_FACTORY.getRDFTermTypeConstant(TYPE_FACTORY.getLangTermType("fr"))));

        ImmutableFunctionalTerm defB = TERM_FACTORY.getRDFFunctionalTerm(
                E,
                TERM_FACTORY.getIfElseNull(
                        TERM_FACTORY.getDBIsNotNull(E),
                        TERM_FACTORY.getRDFTermTypeConstant(TYPE_FACTORY.getLangTermType("en"))));

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(A, B),
                SUBSTITUTION_FACTORY.getSubstitution(
                        A, defA,
                        B, defB));

        ImmutableFunctionalTerm defC = TERM_FACTORY.getRDFFunctionalTerm(
                F,
                TERM_FACTORY.getIfElseNull(
                        TERM_FACTORY.getDBIsNotNull(F),
                        TERM_FACTORY.getRDFTermTypeConstant(TYPE_FACTORY.getLangTermType("de"))));

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(C),
                SUBSTITUTION_FACTORY.getSubstitution(
                        C, defC));

        ImmutableExpression condition = TERM_FACTORY.getRDF2DBBooleanFunctionalTerm(
                TERM_FACTORY.getSPARQLNonStrictEquality(
                        TERM_FACTORY.getImmutableFunctionalTerm(
                                FUNCTION_SYMBOL_FACTORY.getSPARQLFunctionSymbol("LANG", 1).get(),
                                TERM_FACTORY.getImmutableFunctionalTerm(
                                        sparqlCoalesce2FunctionSymbol,
                                        TERM_FACTORY.getImmutableFunctionalTerm(sparqlCoalesce2FunctionSymbol, A, B), C)
                        ),
                        TERM_FACTORY.getRDFLiteralFunctionalTerm(TERM_FACTORY.getDBStringConstant("de"), TYPE_FACTORY.getXsdStringDatatype())
                )
        );

        IQTree joinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(condition),
                ImmutableList.of(
                IQ_FACTORY.createUnaryIQTree(
                        leftConstructionNode,
                        dataNode1),
                IQ_FACTORY.createUnaryIQTree(
                        rightConstructionNode,
                        dataNode2)));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR3_PREDICATE, A, B, C);

        IQ initialIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                joinTree);

        //JOIN AND3(IS_NOT_NULL(f),IS_NULL(d),IS_NULL(e))
        IQTree innerJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getConjunction(
                        TERM_FACTORY.getDBIsNotNull(F),
                        TERM_FACTORY.getDBIsNull(D),
                        TERM_FACTORY.getDBIsNull(E))),
                ImmutableList.of(
                        dataNode1,
                        dataNode2));

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(
                projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        A, defA,
                        B, defB,
                        C, TERM_FACTORY.getRDFFunctionalTerm(
                                F,
                                TERM_FACTORY.getRDFTermTypeConstant(TYPE_FACTORY.getLangTermType("de")))));

        IQ expectedIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        topConstructionNode,
                        innerJoinTree));

        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testIRITemplateContextuallyInjective1() {
        ImmutableList<Template.Component> notAlwaysInjectiveTemplate = Template.builder()
                .addSeparator("http://www.Department")
                .addColumn()
                .addSeparator(".University1")
                .addColumn()
                .addSeparator(".edu/GraduateStudent")
                .addColumn()
                .build();

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(2), ImmutableList.of(B, F));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(INT_TABLE1_AR3, ImmutableMap.of(0, A, 1, B, 2, C));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(INT_TABLE1_AR3, ImmutableMap.of(0, D, 1, E, 2, F));

        DBTypeFactory dbTypeFactory = TYPE_FACTORY.getDBTypeFactory();

        DBTypeConversionFunctionSymbol castFunction = FUNCTION_SYMBOL_FACTORY.getDBFunctionSymbolFactory()
                .getDBCastFunctionSymbol(dbTypeFactory.getDBLargeIntegerType(), dbTypeFactory.getDBStringType());

        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode(
                TERM_FACTORY.getStrictEquality(
                        TERM_FACTORY.getIRIFunctionalTerm(notAlwaysInjectiveTemplate,
                                ImmutableList.of(
                                        TERM_FACTORY.getImmutableFunctionalTerm(castFunction, A),
                                        TERM_FACTORY.getImmutableFunctionalTerm(castFunction, B),
                                        TERM_FACTORY.getImmutableFunctionalTerm(castFunction, C))),
                        TERM_FACTORY.getIRIFunctionalTerm(notAlwaysInjectiveTemplate,
                                ImmutableList.of(
                                        TERM_FACTORY.getImmutableFunctionalTerm(castFunction, D),
                                        TERM_FACTORY.getImmutableFunctionalTerm(castFunction, E),
                                        TERM_FACTORY.getImmutableFunctionalTerm(castFunction, F)))));

        IQTree joinTree = IQ_FACTORY.createNaryIQTree(
                innerJoinNode,
                ImmutableList.of(dataNode1, dataNode2));

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQ initialIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode, joinTree));

        ExtensionalDataNode newDataNode = IQ_FACTORY.createExtensionalDataNode(INT_TABLE1_AR3, ImmutableMap.of(0, D, 1, B, 2, F));

        IQ expectedIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createNaryIQTree(
                                IQ_FACTORY.createInnerJoinNode(),
                                ImmutableList.of(newDataNode, newDataNode))
                ));


        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testIRITemplateContextuallyInjective2() {
        ImmutableList<Template.Component> notAlwaysInjectiveTemplate = Template.builder()
                .addSeparator("http://www.Department")
                .addColumn()
                .addSeparator(".University")
                .addColumn()
                .addSeparator(".edu/GraduateStudent")
                .addColumn()
                .build();

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(2), ImmutableList.of(B, F));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(INT_TABLE1_AR3, ImmutableMap.of(0, A, 1, B, 2, C));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR3, ImmutableMap.of(0, D, 1, E, 2, F));

        DBTypeFactory dbTypeFactory = TYPE_FACTORY.getDBTypeFactory();

        DBTypeConversionFunctionSymbol castFunction = FUNCTION_SYMBOL_FACTORY.getDBFunctionSymbolFactory()
                .getDBCastFunctionSymbol(dbTypeFactory.getDBLargeIntegerType(), dbTypeFactory.getDBStringType());

        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode(
                TERM_FACTORY.getStrictEquality(
                        TERM_FACTORY.getIRIFunctionalTerm(notAlwaysInjectiveTemplate,
                                ImmutableList.of(
                                        TERM_FACTORY.getImmutableFunctionalTerm(castFunction, A),
                                        TERM_FACTORY.getImmutableFunctionalTerm(castFunction, B),
                                        TERM_FACTORY.getImmutableFunctionalTerm(castFunction, C))),
                        TERM_FACTORY.getIRIFunctionalTerm(notAlwaysInjectiveTemplate,
                                ImmutableList.of(
                                        D,
                                        E,
                                        F))));

        IQTree joinTree = IQ_FACTORY.createNaryIQTree(
                innerJoinNode,
                ImmutableList.of(dataNode1, dataNode2));

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQ initialIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode, joinTree));

        IQ expectedIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createNaryIQTree(
                                IQ_FACTORY.createInnerJoinNode(
                                        TERM_FACTORY.getConjunction(
                                                TERM_FACTORY.getStrictEquality(TERM_FACTORY.getImmutableFunctionalTerm(castFunction, A), D),
                                                TERM_FACTORY.getStrictEquality(TERM_FACTORY.getImmutableFunctionalTerm(castFunction, B), E),
                                                TERM_FACTORY.getStrictEquality(TERM_FACTORY.getImmutableFunctionalTerm(castFunction, C), F))),
                                ImmutableList.of(dataNode1, dataNode2))));


        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testIRITemplateContextuallyInjective3() {
        ImmutableList<Template.Component> notAlwaysInjectiveTemplate = Template.builder()
                .addSeparator("http://www.Department")
                .addColumn()
                .addSeparator(".University")
                .addColumn()
                .addSeparator(".edu/GraduateStudent")
                .addColumn()
                .build();

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(2), ImmutableList.of(B, F));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(INT_TABLE1_AR3, ImmutableMap.of(0, A, 1, B, 2, C));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR3, ImmutableMap.of(0, D, 1, E, 2, F));

        DBTypeFactory dbTypeFactory = TYPE_FACTORY.getDBTypeFactory();

        DBTypeConversionFunctionSymbol castFunction = FUNCTION_SYMBOL_FACTORY.getDBFunctionSymbolFactory()
                .getDBCastFunctionSymbol(dbTypeFactory.getDBLargeIntegerType(), dbTypeFactory.getDBStringType());

        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode(
                TERM_FACTORY.getStrictEquality(
                        TERM_FACTORY.getIRIFunctionalTerm(notAlwaysInjectiveTemplate,
                                ImmutableList.of(
                                        D,
                                        E,
                                        F)),
                        TERM_FACTORY.getIRIFunctionalTerm(notAlwaysInjectiveTemplate,
                                ImmutableList.of(
                                        TERM_FACTORY.getImmutableFunctionalTerm(castFunction, A),
                                        TERM_FACTORY.getImmutableFunctionalTerm(castFunction, B),
                                        TERM_FACTORY.getImmutableFunctionalTerm(castFunction, C)))));

        IQTree joinTree = IQ_FACTORY.createNaryIQTree(
                innerJoinNode,
                ImmutableList.of(dataNode1, dataNode2));

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQ initialIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode, joinTree));

        IQ expectedIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createNaryIQTree(
                                IQ_FACTORY.createInnerJoinNode(
                                        TERM_FACTORY.getConjunction(
                                                TERM_FACTORY.getStrictEquality(D, TERM_FACTORY.getImmutableFunctionalTerm(castFunction, A)),
                                                TERM_FACTORY.getStrictEquality(E, TERM_FACTORY.getImmutableFunctionalTerm(castFunction, B)),
                                                TERM_FACTORY.getStrictEquality(F, TERM_FACTORY.getImmutableFunctionalTerm(castFunction, C)))),
                                ImmutableList.of(dataNode1, dataNode2))));


        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testIRITemplateContextuallyInjective4() {
        ImmutableList<Template.Component> notAlwaysInjectiveTemplate = Template.builder()
                .addSeparator("http://www.Department")
                .addColumn()
                .addSeparator(".University1")
                .addColumn()
                .addSeparator(".edu/GraduateStudent")
                .addColumn()
                .build();

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(2), ImmutableList.of(B, F));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(INT_TABLE1_AR3, ImmutableMap.of(0, A, 1, B, 2, C));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(INT_TABLE1_AR3, ImmutableMap.of(0, D, 1, E, 2, F));

        DBTypeFactory dbTypeFactory = TYPE_FACTORY.getDBTypeFactory();

        DBTypeConversionFunctionSymbol castFunction = FUNCTION_SYMBOL_FACTORY.getDBFunctionSymbolFactory()
                .getDBCastFunctionSymbol(dbTypeFactory.getDBLargeIntegerType(), dbTypeFactory.getDBStringType());

        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode(
                TERM_FACTORY.getStrictEquality(
                        TERM_FACTORY.getIRIFunctionalTerm(notAlwaysInjectiveTemplate,
                                ImmutableList.of(
                                        TERM_FACTORY.getImmutableFunctionalTerm(castFunction, A),
                                        TERM_FACTORY.getImmutableFunctionalTerm(castFunction, B),
                                        TERM_FACTORY.getImmutableFunctionalTerm(castFunction, C))),
                        TERM_FACTORY.getIRIFunctionalTerm(notAlwaysInjectiveTemplate,
                                ImmutableList.of(
                                        TERM_FACTORY.getImmutableFunctionalTerm(castFunction, D),
                                        TERM_FACTORY.getImmutableFunctionalTerm(castFunction, E),
                                        TERM_FACTORY.getImmutableFunctionalTerm(castFunction, F)))));

        IQTree joinTree = IQ_FACTORY.createNaryIQTree(
                innerJoinNode,
                ImmutableList.of(dataNode1, dataNode2));

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQ initialIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode, joinTree));

        ExtensionalDataNode newDataNode = IQ_FACTORY.createExtensionalDataNode(INT_TABLE1_AR3, ImmutableMap.of(0, D, 1, B, 2, F));

        IQ expectedIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createNaryIQTree(
                                IQ_FACTORY.createInnerJoinNode(),
                                ImmutableList.of(newDataNode, newDataNode))
                ));


        normalizeAndCompare(initialIQ, expectedIQ);
    }


    @Test
    public void testIRITemplateContextuallyInjective5() {
        ImmutableList<Template.Component> notAlwaysInjectiveTemplate = Template.builder()
                .addSeparator("http://www.Department")
                .addColumn()
                .addSeparator(".University")
                .addColumn()
                .addSeparator(".edu-GraduateStudent")
                .addColumn()
                .build();

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(2), ImmutableList.of(B, F));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR3, ImmutableMap.of(0, A, 1, B, 2, C));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR3, ImmutableMap.of(0, D, 2, F));


        DBConstant constant = TERM_FACTORY.getDBStringConstant(".edu-GraduateStudent.University");

        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode(
                TERM_FACTORY.getStrictEquality(
                        TERM_FACTORY.getIRIFunctionalTerm(notAlwaysInjectiveTemplate,
                                ImmutableList.of(
                                        A,
                                        constant,
                                        C)),
                        TERM_FACTORY.getIRIFunctionalTerm(notAlwaysInjectiveTemplate,
                                ImmutableList.of(
                                        D,
                                        constant,
                                        F))));

        IQTree joinTree = IQ_FACTORY.createNaryIQTree(
                innerJoinNode,
                ImmutableList.of(dataNode1, dataNode2));

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQ initialIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode, joinTree));

        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR3, ImmutableMap.of(0, D, 1, B, 2, F));
        ExtensionalDataNode newDataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR3, ImmutableMap.of(0, D, 2, F));

        IQ expectedIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createNaryIQTree(
                                IQ_FACTORY.createInnerJoinNode(),
                                ImmutableList.of(newDataNode1, newDataNode2))
                ));


        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testIRITemplateContextuallyInjective6() {
        ImmutableList<Template.Component> notAlwaysInjectiveTemplate = Template.builder()
                .addSeparator("http://ex.org/")
                .addColumn()
                .addSeparator("1fg")
                .addColumn()
                .build();

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(2), ImmutableList.of(B, D));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(UUID_TABLE1_AR3, ImmutableMap.of(0, A, 1, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(UUID_TABLE1_AR3, ImmutableMap.of(0, D, 1, E));

        DBTypeFactory dbTypeFactory = TYPE_FACTORY.getDBTypeFactory();

        DBTypeConversionFunctionSymbol castFunction = FUNCTION_SYMBOL_FACTORY.getDBFunctionSymbolFactory()
                .getDBCastFunctionSymbol(dbTypeFactory.getDBTermType("UUID"), dbTypeFactory.getDBStringType());

        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode(
                TERM_FACTORY.getStrictEquality(
                        TERM_FACTORY.getIRIFunctionalTerm(notAlwaysInjectiveTemplate,
                                ImmutableList.of(
                                        TERM_FACTORY.getImmutableFunctionalTerm(castFunction, A),
                                        TERM_FACTORY.getImmutableFunctionalTerm(castFunction, B))),
                        TERM_FACTORY.getIRIFunctionalTerm(notAlwaysInjectiveTemplate,
                                ImmutableList.of(
                                        TERM_FACTORY.getImmutableFunctionalTerm(castFunction, D),
                                        TERM_FACTORY.getImmutableFunctionalTerm(castFunction, E)))));

        IQTree joinTree = IQ_FACTORY.createNaryIQTree(
                innerJoinNode,
                ImmutableList.of(dataNode1, dataNode2));

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQ initialIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode, joinTree));

        ExtensionalDataNode newDataNode = IQ_FACTORY.createExtensionalDataNode(UUID_TABLE1_AR3, ImmutableMap.of(0, D, 1, B));

        IQ expectedIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                    IQ_FACTORY.createNaryIQTree(
                            IQ_FACTORY.createInnerJoinNode(),
                            ImmutableList.of(newDataNode, newDataNode))
                );


        normalizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testIRITemplateContextuallyNotInjective1() {
        ImmutableList<Template.Component> notAlwaysInjectiveTemplate = Template.builder()
                .addSeparator("http://www.Department")
                .addColumn()
                .addSeparator(".University")
                .addColumn()
                .addSeparator(".edu/GraduateStudent")
                .addColumn()
                .build();

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(2), ImmutableList.of(B, F));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR3, ImmutableMap.of(0, A, 1, B, 2, C));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR3, ImmutableMap.of(0, D, 1, E, 2, F));

        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode(
                TERM_FACTORY.getStrictEquality(
                        TERM_FACTORY.getIRIFunctionalTerm(notAlwaysInjectiveTemplate, ImmutableList.of(A, B, C)).getTerm(0),
                        TERM_FACTORY.getIRIFunctionalTerm(notAlwaysInjectiveTemplate, ImmutableList.of(D, E, F)).getTerm(0)));

        IQTree joinTree = IQ_FACTORY.createNaryIQTree(
                innerJoinNode,
                ImmutableList.of(dataNode1, dataNode2));

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQ initialIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode, joinTree));

        normalizeAndCompare(initialIQ, initialIQ);
    }

    @Test
    public void testIRITemplateContextuallyNotInjective2() {
        ImmutableList<Template.Component> template = Template.builder()
                .addSeparator("http://www.Department")
                .addColumn()
                .addSeparator("1")
                .addColumn()
                .addSeparator(".edu/GraduateStudent")
                .addColumn()
                .build();

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(2), ImmutableList.of(B, F));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(INT_TABLE1_AR3, ImmutableMap.of(0, A, 1, B, 2, C));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(INT_TABLE1_AR3, ImmutableMap.of(0, D, 1, E, 2, F));

        DBTypeFactory dbTypeFactory = TYPE_FACTORY.getDBTypeFactory();

        DBTypeConversionFunctionSymbol castFunction = FUNCTION_SYMBOL_FACTORY.getDBFunctionSymbolFactory()
                .getDBCastFunctionSymbol(dbTypeFactory.getDBLargeIntegerType(), dbTypeFactory.getDBStringType());

        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode(
                TERM_FACTORY.getStrictEquality(
                        TERM_FACTORY.getIRIFunctionalTerm(template,
                                ImmutableList.of(
                                        TERM_FACTORY.getImmutableFunctionalTerm(castFunction, A),
                                        TERM_FACTORY.getImmutableFunctionalTerm(castFunction, B),
                                        TERM_FACTORY.getImmutableFunctionalTerm(castFunction, C))).getTerm(0),
                        TERM_FACTORY.getIRIFunctionalTerm(template,
                                ImmutableList.of(
                                        TERM_FACTORY.getImmutableFunctionalTerm(castFunction, D),
                                        TERM_FACTORY.getImmutableFunctionalTerm(castFunction, E),
                                        TERM_FACTORY.getImmutableFunctionalTerm(castFunction, F))).getTerm(0)));

        IQTree joinTree = IQ_FACTORY.createNaryIQTree(
                innerJoinNode,
                ImmutableList.of(dataNode1, dataNode2));

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQ initialIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode, joinTree));

        normalizeAndCompare(initialIQ, initialIQ);
    }

    @Test
    public void testIRITemplateContextuallyNotInjective3() {
        ImmutableList<Template.Component> notAlwaysInjectiveTemplate = Template.builder()
                .addSeparator("http://www.Department")
                .addColumn()
                .addSeparator(".University")
                .addColumn()
                .addSeparator(".edu/GraduateStudent")
                .addColumn()
                .build();

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(2), ImmutableList.of(C, F));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(INT_TABLE1_AR3, ImmutableMap.of(0, A, 1, B, 2, C));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR3, ImmutableMap.of(0, D, 1, E, 2, F));

        DBTypeFactory dbTypeFactory = TYPE_FACTORY.getDBTypeFactory();

        DBTypeConversionFunctionSymbol castFunction = FUNCTION_SYMBOL_FACTORY.getDBFunctionSymbolFactory()
                .getDBCastFunctionSymbol(dbTypeFactory.getDBLargeIntegerType(), dbTypeFactory.getDBStringType());

        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode(
                TERM_FACTORY.getStrictEquality(
                        TERM_FACTORY.getIRIFunctionalTerm(notAlwaysInjectiveTemplate, ImmutableList.of(
                                TERM_FACTORY.getImmutableFunctionalTerm(castFunction, A), E, F)).getTerm(0),
                        TERM_FACTORY.getIRIFunctionalTerm(notAlwaysInjectiveTemplate, ImmutableList.of(
                                TERM_FACTORY.getImmutableFunctionalTerm(castFunction, B), D, E)).getTerm(0)));

        IQTree joinTree = IQ_FACTORY.createNaryIQTree(
                innerJoinNode,
                ImmutableList.of(dataNode1, dataNode2));

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQ initialIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode, joinTree));

        normalizeAndCompare(initialIQ, initialIQ);
    }

    @Test
    public void testIRITemplateContextuallyNotInjective4() {
        ImmutableList<Template.Component> notAlwaysInjectiveTemplate = Template.builder()
                .addSeparator("http://www.Department")
                .addColumn()
                .addSeparator(".University")
                .addColumn()
                .addSeparator(".edu-GraduateStudent")
                .addColumn()
                .build();

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(2), ImmutableList.of(B, F));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR3, ImmutableMap.of(0, A, 1, B, 2, C));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR3, ImmutableMap.of(0, D, 2, F));


        DBConstant constant = TERM_FACTORY.getDBStringConstant(".University.edu-GraduateStudent");

        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode(
                TERM_FACTORY.getStrictEquality(
                        TERM_FACTORY.getIRIFunctionalTerm(notAlwaysInjectiveTemplate,
                                ImmutableList.of(
                                        A,
                                        constant,
                                        C)).getTerm(0),
                        TERM_FACTORY.getIRIFunctionalTerm(notAlwaysInjectiveTemplate,
                                ImmutableList.of(
                                        D,
                                        constant,
                                        F)).getTerm(0)));

        IQTree joinTree = IQ_FACTORY.createNaryIQTree(
                innerJoinNode,
                ImmutableList.of(dataNode1, dataNode2));

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQ initialIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode, joinTree));

        normalizeAndCompare(initialIQ, initialIQ);
    }

    @Test
    public void testIRITemplateContextuallyNotInjective5() {
        ImmutableList<Template.Component> notAlwaysInjectiveTemplate = Template.builder()
                .addSeparator("http://ex.org/")
                .addColumn()
                .addSeparator("1ff")
                .addColumn()
                .build();

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(2), ImmutableList.of(B, D));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(UUID_TABLE1_AR3, ImmutableMap.of(0, A, 1, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(UUID_TABLE1_AR3, ImmutableMap.of(0, D, 1, E));

        DBTypeFactory dbTypeFactory = TYPE_FACTORY.getDBTypeFactory();

        DBTypeConversionFunctionSymbol castFunction = FUNCTION_SYMBOL_FACTORY.getDBFunctionSymbolFactory()
                .getDBCastFunctionSymbol(dbTypeFactory.getDBTermType("UUID"), dbTypeFactory.getDBStringType());

        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode(
                TERM_FACTORY.getStrictEquality(
                        TERM_FACTORY.getIRIFunctionalTerm(notAlwaysInjectiveTemplate,
                                ImmutableList.of(
                                        TERM_FACTORY.getImmutableFunctionalTerm(castFunction, A),
                                        TERM_FACTORY.getImmutableFunctionalTerm(castFunction, B))).getTerm(0),
                        TERM_FACTORY.getIRIFunctionalTerm(notAlwaysInjectiveTemplate,
                                ImmutableList.of(
                                        TERM_FACTORY.getImmutableFunctionalTerm(castFunction, D),
                                        TERM_FACTORY.getImmutableFunctionalTerm(castFunction, E))).getTerm(0)));

        IQTree joinTree = IQ_FACTORY.createNaryIQTree(
                innerJoinNode,
                ImmutableList.of(dataNode1, dataNode2));

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQ initialIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode, joinTree));

        ExtensionalDataNode newDataNode = IQ_FACTORY.createExtensionalDataNode(UUID_TABLE1_AR3, ImmutableMap.of(0, D, 1, B));

        IQ expectedIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createNaryIQTree(
                                IQ_FACTORY.createInnerJoinNode(),
                                ImmutableList.of(newDataNode, newDataNode))
                ));


        normalizeAndCompare(initialIQ, initialIQ);
    }


    private static void normalizeAndCompare(IQ initialIQ, IQ expectedIQ) {
        System.out.println("Initial IQ: " + initialIQ );
        System.out.println("Expected IQ: " + expectedIQ);

        IQ normalizedIQ = initialIQ.normalizeForOptimization();
        System.out.println("Normalized IQ: " + normalizedIQ);

        assertEquals(expectedIQ, normalizedIQ);
    }

    private ImmutableFunctionalTerm createNonInjectiveFunctionalTerm(Variable stringV1, Variable stringV2) {
        return TERM_FACTORY.getNullRejectingDBConcatFunctionalTerm(ImmutableList.of(stringV1, stringV2));
    }

    private ImmutableFunctionalTerm createInjectiveFunctionalTerm1(ImmutableTerm term) {
        return TERM_FACTORY.getNullRejectingDBConcatFunctionalTerm(
                ImmutableList.of(TERM_FACTORY.getDBStringConstant("-something"), term));
    }

    private ImmutableFunctionalTerm createInjectiveFunctionalTerm2(ImmutableTerm term) {
        return TERM_FACTORY.getImmutableFunctionalTerm(
                FUNCTION_SYMBOL_FACTORY.getRequiredSPARQLFunctionSymbol(XPathFunction.ENCODE_FOR_URI.getIRIString(), 1),
                term);
    }

    private ImmutableFunctionalTerm createIfIsNotNullElseNull(Variable rightSpecificVariable, ImmutableTerm value) {
        return TERM_FACTORY.getIfElseNull(
                TERM_FACTORY.getDBIsNotNull(rightSpecificVariable), value);
    }

    private static ImmutableExpression createExpression(Variable stringVariable) {
        return TERM_FACTORY.getStrictEquality(TERM_FACTORY.getDBCharLength(stringVariable), ONE);
    }
}
