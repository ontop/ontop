package it.unibz.inf.ontop.iq.executor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.optimizer.impl.AbstractIntensionalQueryMerger;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.apache.commons.rdf.api.IRI;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;

import static it.unibz.inf.ontop.NoDependencyTestDBMetadata.*;
import static junit.framework.TestCase.assertEquals;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;

public class QueryMergingTest {

    private static final AtomPredicate ANS0_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate( 0);
    private static final AtomPredicate ANS1_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate( 2);
    private static final AtomPredicate ANS2_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate( 1);
    private static final AtomPredicate ANS4_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate( 3);

    private static final IRI P1_IRI = RDF_FACTORY.createIRI("http://example.com/voc#p1");
    private static final IRI C3_IRI = RDF_FACTORY.createIRI("http://example.com/voc#C3");
    private static final Variable X = TERM_FACTORY.getVariable("x");
    private static final Variable Y = TERM_FACTORY.getVariable("y");
    private static final Variable Z = TERM_FACTORY.getVariable("z");
    private static final Variable S = TERM_FACTORY.getVariable("s");
    private static final Variable T = TERM_FACTORY.getVariable("t");
    private static final Variable P = TERM_FACTORY.getVariable("p");
    private static final Variable O = TERM_FACTORY.getVariable("o");
    private static final Variable R = TERM_FACTORY.getVariable("r");
    private static final Variable U = TERM_FACTORY.getVariable("u");
    private static final Variable A = TERM_FACTORY.getVariable("a");
    private static final Variable B = TERM_FACTORY.getVariable("b");
    private static final Variable BF3F7 = TERM_FACTORY.getVariable("bf3f7");
    private static final Variable C = TERM_FACTORY.getVariable("c");
    private static final Variable D = TERM_FACTORY.getVariable("d");
    private static final Variable DF6 = TERM_FACTORY.getVariable("df6");
    private static final Variable E = TERM_FACTORY.getVariable("e");
    private static final DistinctVariableOnlyDataAtom ANS1_XY_ATOM = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
            ANS1_PREDICATE, ImmutableList.of(X, Y));
    private static final DistinctVariableOnlyDataAtom ANS1_X_ATOM = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
            ANS2_PREDICATE, ImmutableList.of(X));
    private static final DistinctVariableOnlyDataAtom ANS0_ATOM = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
            ANS0_PREDICATE, ImmutableList.of());
    private static final DistinctVariableOnlyDataAtom P1_ST_ATOM = ATOM_FACTORY.getDistinctTripleAtom(S, P, T);
    private static final ImmutableList<Template.Component> URI_TEMPLATE_STR_1 = Template.of("http://example.org/ds1/", 0);
    private static final ImmutableList<Template.Component> URI_TEMPLATE_STR_2 = Template.of("http://example.org/ds2/", 0);
    private static final Constant ONE = TERM_FACTORY.getDBConstant("1", TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType());
    private static final Constant ONE_STR = TERM_FACTORY.getDBConstant("1", TYPE_FACTORY.getDBTypeFactory().getDBStringType());
    private static final Constant THREE_STR = TERM_FACTORY.getDBConstant("3", TYPE_FACTORY.getDBTypeFactory().getDBStringType());
    private static final GroundTerm INT_OF_THREE = (GroundTerm) TERM_FACTORY.getRDFLiteralFunctionalTerm(THREE_STR, XSD.INTEGER);
    private static final GroundTerm INT_OF_ONE = (GroundTerm) TERM_FACTORY.getRDFLiteralFunctionalTerm(ONE_STR, XSD.INTEGER);
    private static final ImmutableFunctionalTerm INT_OF_B = TERM_FACTORY.getRDFLiteralFunctionalTerm(B, XSD.INTEGER);
    private static final ExtensionalDataNode DATA_NODE_1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
    private static final ExtensionalDataNode DATA_NODE_3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(B, C));
    private static final ExtensionalDataNode DATA_NODE_4 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(S, T));

    @Test
    public void testPruning1() {
        GroundFunctionalTerm xValue = (GroundFunctionalTerm) generateURI1(ONE_STR);

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(ANS1_XY_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, xValue));
        IntensionalDataNode dataNode = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(xValue, P1_IRI, Y));

        IQ mainQuery = IQ_FACTORY.createIQ(ANS1_XY_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode, dataNode));

        // Sub-query
        ConstructionNode subQueryRoot = IQ_FACTORY.createConstructionNode(P1_ST_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(T, generateURI1(B), P, generateConstant(P1_IRI)));
        ImmutableSet<Variable> unionProjectedVariables = ImmutableSet.of(S, B);
        UnionNode unionNode = IQ_FACTORY.createUnionNode(unionProjectedVariables);

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(unionProjectedVariables,
                SUBSTITUTION_FACTORY.getSubstitution(S, generateURI2(A)));
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(unionProjectedVariables,
                SUBSTITUTION_FACTORY.getSubstitution(S, generateURI1(C)));

        IQ subQuery = IQ_FACTORY.createIQ(P1_ST_ATOM,
                IQ_FACTORY.createUnaryIQTree(subQueryRoot,
                        IQ_FACTORY.createNaryIQTree(unionNode, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(leftConstructionNode, DATA_NODE_1),
                                IQ_FACTORY.createUnaryIQTree(rightConstructionNode, DATA_NODE_3)))));
        // Expected
        ConstructionNode remainingConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateURI1(B)));
        ExtensionalDataNode expectedDataNode = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(B, ONE_STR));

        IQ expected = IQ_FACTORY.createIQ(ANS1_XY_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createUnaryIQTree(remainingConstructionNode, expectedDataNode)));

        optimizeAndCompare(mainQuery, subQuery, expected, dataNode);
    }

    private IRIConstant generateConstant(IRI iri) {
        return TERM_FACTORY.getConstantIRI(iri);
    }


    @Test
    public void testEx1() {
        // Original query
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(ANS1_X_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());
        IntensionalDataNode dataNode = IQ_FACTORY.createIntensionalDataNode(
                ATOM_FACTORY.getIntensionalTripleAtom(X, P1_IRI, INT_OF_THREE));

        IQ mainQuery = IQ_FACTORY.createIQ(ANS1_X_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode, dataNode));


        // Sub-query
        ConstructionNode subQueryRoot = IQ_FACTORY.createConstructionNode(P1_ST_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(S, generateURI1(A), T, INT_OF_B, P, generateConstant(P1_IRI)));

        IQ subQuery = IQ_FACTORY.createIQ(P1_ST_ATOM,
                IQ_FACTORY.createUnaryIQTree(subQueryRoot, DATA_NODE_1));

        // Expected
        ConstructionNode remainingConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A)));
        ExtensionalDataNode expectedDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, THREE_STR));

        IQ expected = IQ_FACTORY.createIQ(ANS1_X_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createUnaryIQTree(remainingConstructionNode, expectedDataNode)));

        optimizeAndCompare(mainQuery, subQuery, expected, dataNode);
    }

    @Test
    public void testEx1Bis() {
         // Original query
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(ANS1_X_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());
        IntensionalDataNode dataNode = IQ_FACTORY.createIntensionalDataNode(
                ATOM_FACTORY.getIntensionalTripleAtom(X, P1_IRI, TERM_FACTORY.getRDFLiteralConstant("3", XSD.INTEGER)));

        IQ mainQuery = IQ_FACTORY.createIQ(ANS1_X_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode, dataNode));

        // Sub-query
        ConstructionNode subQueryRoot = IQ_FACTORY.createConstructionNode(P1_ST_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(S, generateURI1(A), T, INT_OF_B, P, generateConstant(P1_IRI)));

        IQ subQuery = IQ_FACTORY.createIQ(P1_ST_ATOM,
                IQ_FACTORY.createUnaryIQTree(subQueryRoot, DATA_NODE_1));

        // Expected
        ConstructionNode remainingConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A)));
        ExtensionalDataNode expectedDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, THREE_STR));

        IQ expected = IQ_FACTORY.createIQ(ANS1_X_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createUnaryIQTree(remainingConstructionNode, expectedDataNode)));

        optimizeAndCompare(mainQuery, subQuery, expected, dataNode);
    }

    @Test
    public void testEx2() {
        // Original query
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(ANS1_X_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());
        IntensionalDataNode dataNode = IQ_FACTORY.createIntensionalDataNode(
                ATOM_FACTORY.getIntensionalTripleAtom(X, P1_IRI, X));

        IQ mainQuery = IQ_FACTORY.createIQ(ANS1_X_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode, dataNode));

        // Sub-query
        ConstructionNode subQueryRoot = IQ_FACTORY.createConstructionNode(P1_ST_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(S, generateURI1(A), T, generateURI1(B), P, generateConstant(P1_IRI)));

        IQ subQuery = IQ_FACTORY.createIQ(P1_ST_ATOM,
                IQ_FACTORY.createUnaryIQTree(subQueryRoot, DATA_NODE_1));

        // Expected
        ConstructionNode remainingConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A)));
        ExtensionalDataNode expectedDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, A));

        IQ expected = IQ_FACTORY.createIQ(ANS1_X_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createUnaryIQTree(remainingConstructionNode, expectedDataNode)));

        optimizeAndCompare(mainQuery, subQuery, expected, dataNode);
    }

    @Test
    public void testEx3() {
        // Original query
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(ANS1_X_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());
        IntensionalDataNode dataNode = IQ_FACTORY.createIntensionalDataNode(
                ATOM_FACTORY.getIntensionalTripleAtom(X, P1_IRI, INT_OF_THREE));

        IQ mainQuery = IQ_FACTORY.createIQ(ANS1_X_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode, dataNode));

        // Sub-query
        ConstructionNode subQueryRoot = IQ_FACTORY.createConstructionNode(P1_ST_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(P, generateConstant(P1_IRI)));

        IQ subQuery = IQ_FACTORY.createIQ(P1_ST_ATOM,
                IQ_FACTORY.createUnaryIQTree(subQueryRoot, DATA_NODE_4));

        // Expected
        ExtensionalDataNode expectedDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(X, INT_OF_THREE));

        IQ expected = IQ_FACTORY.createIQ(ANS1_X_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode, expectedDataNode));

        optimizeAndCompare(mainQuery, subQuery, expected, dataNode);
    }

    @Test
    public void testEx4() {
        // Original query
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(ANS1_X_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());
        IntensionalDataNode dataNode = IQ_FACTORY.createIntensionalDataNode(
                ATOM_FACTORY.getIntensionalTripleAtom(X, P1_IRI, X));

        IQ mainQuery = IQ_FACTORY.createIQ(ANS1_X_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode, dataNode));

        // Sub-query
        ConstructionNode subQueryRoot = IQ_FACTORY.createConstructionNode(P1_ST_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(P, generateConstant(P1_IRI)));

        IQ subQuery = IQ_FACTORY.createIQ(P1_ST_ATOM,
                IQ_FACTORY.createUnaryIQTree(subQueryRoot, DATA_NODE_4));

        // Expected
        ExtensionalDataNode expectedDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(X, X));

        IQ expected = IQ_FACTORY.createIQ(ANS1_X_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode, expectedDataNode));

        optimizeAndCompare(mainQuery, subQuery, expected, dataNode);
    }

    @Test
    public void testEx5() {
        // Original query
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(ANS1_XY_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());
        IntensionalDataNode dataNode = IQ_FACTORY.createIntensionalDataNode(
                ATOM_FACTORY.getIntensionalTripleAtom(X, P1_IRI, Y));

        IQ mainQuery = IQ_FACTORY.createIQ(ANS1_XY_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode, dataNode));

        // Sub-query
        ConstructionNode subQueryRoot = IQ_FACTORY.createConstructionNode(P1_ST_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(T, S, P, generateConstant(P1_IRI)));
        ExtensionalDataNode dataNodeSubquery = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(S, U));

        IQ subQuery = IQ_FACTORY.createIQ(P1_ST_ATOM,
                IQ_FACTORY.createUnaryIQTree(subQueryRoot, dataNodeSubquery));

        // Expected
        ConstructionNode remainingConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, Y));
        ExtensionalDataNode expectedDataNode = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2,
                ImmutableMap.of(0, Y));

        IQ expected = IQ_FACTORY.createIQ(ANS1_XY_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createUnaryIQTree(remainingConstructionNode, expectedDataNode)));

        optimizeAndCompare(mainQuery, subQuery, expected, dataNode);
    }

    @Test
    public void testEx6() {
        // Original query
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(ANS1_X_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());
        IntensionalDataNode dataNode = IQ_FACTORY.createIntensionalDataNode(
                ATOM_FACTORY.getIntensionalTripleAtom(X, P1_IRI, INT_OF_THREE));

        IQ mainQuery = IQ_FACTORY.createIQ(ANS1_X_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode, dataNode));

        // Sub-query
        ExtensionalDataNode dataNodeSubquery = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(S, B));
        ConstructionNode subQueryRoot = IQ_FACTORY.createConstructionNode(P1_ST_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(T, INT_OF_B, P, generateConstant(P1_IRI)));

        IQ subQuery = IQ_FACTORY.createIQ(P1_ST_ATOM,
                IQ_FACTORY.createUnaryIQTree(subQueryRoot, dataNodeSubquery));

        // Expected
        ExtensionalDataNode expectedDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(X, THREE_STR));

        IQ expected = IQ_FACTORY.createIQ(ANS1_X_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode, expectedDataNode));

        optimizeAndCompare(mainQuery, subQuery, expected, dataNode);
    }

    @Test
    public void testEx7() {
        // Original query
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(ANS1_X_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());
        IntensionalDataNode dataNode = IQ_FACTORY.createIntensionalDataNode(
                ATOM_FACTORY.getIntensionalTripleAtom(X, X, X));

        IQ mainQuery = IQ_FACTORY.createIQ(ANS1_X_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode, dataNode));

        // Sub-query
        DistinctVariableOnlyDataAtom p1Atom = ATOM_FACTORY.getDistinctTripleAtom(S, T, U);
        ExtensionalDataNode dataNodeSubquery = createExtensionalDataNode(TABLE4_AR3, ImmutableList.of(A, B, C));
        ConstructionNode subQueryRoot = IQ_FACTORY.createConstructionNode(p1Atom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(S, generateURI1(A), T, generateURI1(B), U, generateURI1(C)));

        IQ subQuery = IQ_FACTORY.createIQ(p1Atom,
                IQ_FACTORY.createUnaryIQTree(subQueryRoot, dataNodeSubquery));

        // Expected
        ConstructionNode remainingConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A)));
        ExtensionalDataNode expectedDataNode = createExtensionalDataNode(TABLE4_AR3, ImmutableList.of(A, A, A));

        IQ expected = IQ_FACTORY.createIQ(ANS1_X_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createUnaryIQTree(remainingConstructionNode, expectedDataNode)));

        optimizeAndCompare(mainQuery, subQuery, expected, dataNode);
    }

    @Test
    public void testEx8WithEx15() {
        // Original query
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(ANS1_X_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());
        IntensionalDataNode dataNode = IQ_FACTORY.createIntensionalDataNode(
                ATOM_FACTORY.getIntensionalTripleAtom(X, P1_IRI, X));

        IQ mainQuery = IQ_FACTORY.createIQ(ANS1_X_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode, dataNode));

        // Sub-query
        ExtensionalDataNode dataNodeSubquery = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ConstructionNode subQueryRoot = IQ_FACTORY.createConstructionNode(P1_ST_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(S, generateURI1(A), P, generateConstant(P1_IRI)));
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(T, A),
                SUBSTITUTION_FACTORY.getSubstitution(T, generateURI1(B)));

        IQ subQuery = IQ_FACTORY.createIQ(P1_ST_ATOM,
                IQ_FACTORY.createUnaryIQTree(subQueryRoot,
                        IQ_FACTORY.createUnaryIQTree(constructionNode2, dataNodeSubquery)));

        // Expected
        ConstructionNode remainingConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A)));
        ExtensionalDataNode expectedDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, A));

        IQ expected = IQ_FACTORY.createIQ(ANS1_X_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createUnaryIQTree(remainingConstructionNode, expectedDataNode)));

        optimizeAndCompare(mainQuery, subQuery, expected, dataNode);
    }

    @Test
    public void testEx8_1() {
        // Original query
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(ANS1_X_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());
        IntensionalDataNode dataNode = IQ_FACTORY.createIntensionalDataNode(
                ATOM_FACTORY.getIntensionalTripleAtom(X, P1_IRI, X));

        IQ mainQuery = IQ_FACTORY.createIQ(ANS1_X_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode, dataNode));

        // Sub-query
        ExtensionalDataNode dataNodeSubquery = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ConstructionNode subQueryRoot = IQ_FACTORY.createConstructionNode(P1_ST_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(T, generateURI1(A), P, generateConstant(P1_IRI)));
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, A),
                SUBSTITUTION_FACTORY.getSubstitution(S, generateURI1(B)));

        IQ subQuery = IQ_FACTORY.createIQ(P1_ST_ATOM,
                IQ_FACTORY.createUnaryIQTree(subQueryRoot,
                        IQ_FACTORY.createUnaryIQTree(constructionNode2,dataNodeSubquery)));

        // Expected
        ConstructionNode remainingConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A)));
        ExtensionalDataNode expectedDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, A));

        IQ expected = IQ_FACTORY.createIQ(ANS1_X_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createUnaryIQTree(remainingConstructionNode, expectedDataNode)));

        optimizeAndCompare(mainQuery, subQuery, expected, dataNode);
    }

    @Test
    public void testEx9() {
         // Original query
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(ANS1_X_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());
        IntensionalDataNode dataNode = IQ_FACTORY.createIntensionalDataNode(
                ATOM_FACTORY.getIntensionalTripleAtom(X, X, X));

        IQ mainQuery = IQ_FACTORY.createIQ(ANS1_X_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode, dataNode));

        // Sub-query
        DistinctVariableOnlyDataAtom p1Atom = ATOM_FACTORY.getDistinctTripleAtom(S, T, U);
        ConstructionNode subQueryRoot = IQ_FACTORY.createConstructionNode(p1Atom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(S, generateURI1(A), T, generateURI1(B)));
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(U, A, B),
                SUBSTITUTION_FACTORY.getSubstitution(U, generateURI1(C)));
        ExtensionalDataNode dataNodeSubquery = createExtensionalDataNode(TABLE1_AR3, ImmutableList.of(A, B, C));

        IQ subQuery = IQ_FACTORY.createIQ(p1Atom,
                IQ_FACTORY.createUnaryIQTree(subQueryRoot,
                        IQ_FACTORY.createUnaryIQTree(constructionNode2, dataNodeSubquery)));

        // Expected
        ConstructionNode remainingConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A)));
        ExtensionalDataNode expectedDataNode = createExtensionalDataNode(TABLE1_AR3, ImmutableList.of(A, A, A));

        IQ expected = IQ_FACTORY.createIQ(ANS1_X_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createUnaryIQTree(remainingConstructionNode, expectedDataNode)));

        optimizeAndCompare(mainQuery, subQuery, expected, dataNode);
    }

    @Test
    public void testEx10() {
        // Original query
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(ANS1_X_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());
        IntensionalDataNode dataNode = IQ_FACTORY.createIntensionalDataNode(
                ATOM_FACTORY.getIntensionalTripleAtom(X, P1_IRI, X));

        IQ mainQuery = IQ_FACTORY.createIQ(ANS1_X_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode, dataNode));

        // Sub-query
        ConstructionNode subQueryRoot = IQ_FACTORY.createConstructionNode(P1_ST_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(T, generateURI1(B), P, generateConstant(P1_IRI)));
        ConstructionNode subQueryConstruction2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, B),
                SUBSTITUTION_FACTORY.getSubstitution(S, generateURI1(A)));
        ExtensionalDataNode dataNodeSubquery = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));

        IQ subQuery = IQ_FACTORY.createIQ(P1_ST_ATOM,
                IQ_FACTORY.createUnaryIQTree(subQueryRoot,
                        IQ_FACTORY.createUnaryIQTree(subQueryConstruction2, dataNodeSubquery)));

        // Expected
        ConstructionNode remainingConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(B)));
        ExtensionalDataNode expectedDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(B, B));

        IQ expected = IQ_FACTORY.createIQ(ANS1_X_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createUnaryIQTree(remainingConstructionNode, expectedDataNode)));

        optimizeAndCompare(mainQuery, subQuery, expected, dataNode);
    }

    @Test
    public void testEx11() {
        // Original query
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(ANS1_XY_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());
        IntensionalDataNode dataNode = IQ_FACTORY.createIntensionalDataNode(
                ATOM_FACTORY.getIntensionalTripleAtom(X, P1_IRI, Y));

        IQ mainQuery = IQ_FACTORY.createIQ(ANS1_XY_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode, dataNode));

        // Sub-query
        ExtensionalDataNode dataNodeSubquery = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(A));
        ConstructionNode subQueryRoot = IQ_FACTORY.createConstructionNode(P1_ST_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(S, generateURI1(A), T, INT_OF_ONE, P, generateConstant(P1_IRI)));

        IQ subQuery = IQ_FACTORY.createIQ(P1_ST_ATOM,
                IQ_FACTORY.createUnaryIQTree(subQueryRoot, dataNodeSubquery));

        // Expected
        ConstructionNode remainingConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A), Y, TERM_FACTORY.getRDFLiteralConstant("1", XSD.INTEGER)));
        ExtensionalDataNode expectedDataNode = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(A));

        IQ expected = IQ_FACTORY.createIQ(ANS1_XY_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createUnaryIQTree(remainingConstructionNode, expectedDataNode)));

        optimizeAndCompare(mainQuery, subQuery, expected, dataNode);
    }

    @Ignore("TODO: decide what to do with ground functional terms")
    @Test
    public void testEx12() {
        // Original query
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(ANS1_X_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());
        IntensionalDataNode dataNode = IQ_FACTORY.createIntensionalDataNode(
                ATOM_FACTORY.getIntensionalTripleAtom(X, P1_IRI, X));

        IQ mainQuery = IQ_FACTORY.createIQ(ANS1_X_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode, dataNode));

        // Sub-query
        ExtensionalDataNode dataNodeSubquery = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, INT_OF_ONE));
        ConstructionNode subQueryRoot = IQ_FACTORY.createConstructionNode(P1_ST_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(S, generateURI1(A), T, generateURI1(INT_OF_ONE), P, generateConstant(P1_IRI)));

        IQ subQuery = IQ_FACTORY.createIQ(P1_ST_ATOM,
                IQ_FACTORY.createUnaryIQTree(subQueryRoot, dataNodeSubquery));

        // Expected
        ConstructionNode remainingConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(INT_OF_ONE)));
        ExtensionalDataNode expectedDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(INT_OF_ONE, INT_OF_ONE));

        IQ expected = IQ_FACTORY.createIQ(ANS1_X_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createUnaryIQTree(remainingConstructionNode, expectedDataNode)));

        optimizeAndCompare(mainQuery, subQuery, expected, dataNode);
    }

    @Ignore("TODO: decide what to do with ground functional terms")
    @Test
    public void testEx13() {
        // Original query
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(ANS1_X_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());
        IntensionalDataNode dataNode = IQ_FACTORY.createIntensionalDataNode(
                ATOM_FACTORY.getIntensionalTripleAtom(X, P1_IRI, X));

        IQ mainQuery = IQ_FACTORY.createIQ(ANS1_X_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode, dataNode));

        // Sub-query
        ExtensionalDataNode dataNodeSubquery = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(INT_OF_ONE, B));
        ConstructionNode subQueryRoot = IQ_FACTORY.createConstructionNode(P1_ST_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(S, generateURI1(INT_OF_ONE), T, generateURI1(B), P, generateConstant(P1_IRI)));

        IQ subQuery = IQ_FACTORY.createIQ(P1_ST_ATOM,
                IQ_FACTORY.createUnaryIQTree(subQueryRoot, dataNodeSubquery));

        // Expected
        ConstructionNode remainingConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(INT_OF_ONE)));
        ExtensionalDataNode expectedDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(INT_OF_ONE, INT_OF_ONE));

        IQ expected = IQ_FACTORY.createIQ(ANS1_X_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createUnaryIQTree(remainingConstructionNode, expectedDataNode)));

        optimizeAndCompare(mainQuery, subQuery, expected, dataNode);
    }

    @Test
    public void testEx14() {
        // Original query
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate( 0), ImmutableList.of());
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());
        IntensionalDataNode dataNode = IQ_FACTORY.createIntensionalDataNode(
                ATOM_FACTORY.getIntensionalTripleAtom((GroundFunctionalTerm) generateURI1(ONE), P1_IRI, (GroundFunctionalTerm) generateURI1(ONE)));

        IQ mainQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode, dataNode));

        // Sub-query
        DBConstant two = TERM_FACTORY.getDBConstant("2", TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType());
        ConstructionNode subQueryRoot = IQ_FACTORY.createConstructionNode(P1_ST_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(S, generateURI1(ONE), T, generateURI1(ONE), P, generateConstant(P1_IRI)));
        ExtensionalDataNode tableNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(two, two));

        IQ subQuery = IQ_FACTORY.createIQ(P1_ST_ATOM,
                IQ_FACTORY.createUnaryIQTree(subQueryRoot, tableNode));

        // Expected
        IQ expected = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode, tableNode));

        optimizeAndCompare(mainQuery, subQuery, expected, dataNode);
    }

    @Test
    public void testConflictingVariables() {
        // Original query
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ANS4_PREDICATE, ImmutableList.of(X, Y, Z));
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        IRI firstNameIRI = RDF_FACTORY.createIRI("http://example.com/voc#firstName");
        IRI lastNameIRI = RDF_FACTORY.createIRI("http://example.com/voc#lastName");
        IntensionalDataNode firstIntentional = IQ_FACTORY.createIntensionalDataNode(
                ATOM_FACTORY.getIntensionalTripleAtom(X, firstNameIRI, Y));
        IntensionalDataNode lastIntentional = IQ_FACTORY.createIntensionalDataNode(
                ATOM_FACTORY.getIntensionalTripleAtom(X, lastNameIRI, Z));

        IQ query = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(firstIntentional, lastIntentional))));

        // First name mapping
        DistinctVariableOnlyDataAtom firstMappingAtom = ATOM_FACTORY.getDistinctTripleAtom(S, P, T);
        ConstructionNode firstMappingRootNode = IQ_FACTORY.createConstructionNode(firstMappingAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(S, generateURI1(A), T, generateString(B), P, generateConstant(firstNameIRI)));
        ExtensionalDataNode firstNameDataNode = createExtensionalDataNode(TABLE4_AR3, ImmutableList.of(A, B, C));

        IQ firstMapping = IQ_FACTORY.createIQ(firstMappingAtom,
                IQ_FACTORY.createUnaryIQTree(firstMappingRootNode, firstNameDataNode));

        // Last name mapping
        DistinctVariableOnlyDataAtom lastMappingAtom = ATOM_FACTORY.getDistinctTripleAtom(S, P, T);
        ConstructionNode lastMappingRootNode = IQ_FACTORY.createConstructionNode(lastMappingAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(S, generateURI1(D), T, generateString(B), P, generateConstant(lastNameIRI)));
        ExtensionalDataNode lastNameDataNode = createExtensionalDataNode(TABLE4_AR3, ImmutableList.of(D, E, B));

        IQ lastMapping = IQ_FACTORY.createIQ(lastMappingAtom,
                IQ_FACTORY.createUnaryIQTree(lastMappingRootNode, lastNameDataNode));

        IQ mergedMappingDefinition = UNION_BASED_QUERY_MERGER.mergeDefinitions(ImmutableList.of(firstMapping, lastMapping))
                .get();

        IQ newQuery = merge(query, mergedMappingDefinition, firstIntentional);


        // Expected query
        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A), Y, generateString(B)));
        ExtensionalDataNode newFirstNameDataNode = IQ_FACTORY.createExtensionalDataNode(TABLE4_AR3, ImmutableMap.of(0, A, 1, B));

        Variable b1 = BF3F7;
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Z),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(DF6), Z, generateString(b1)));
        ExtensionalDataNode expectedlastNameDataNode = IQ_FACTORY.createExtensionalDataNode(TABLE4_AR3, ImmutableMap.of(0, DF6, 2, b1));

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(leftConstructionNode, newFirstNameDataNode),
                                IQ_FACTORY.createUnaryIQTree(rightConstructionNode, expectedlastNameDataNode)))));

        optimizeAndCompare(newQuery, mergedMappingDefinition, expectedQuery, lastIntentional);
    }

    @Test
    public void testUnionSameVariable() {
        // Original query
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution());
        IntensionalDataNode intensionalDataNode = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(X, C3_IRI));

        IQ mainQuery = IQ_FACTORY.createIQ(ANS1_X_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode, intensionalDataNode));

        // Mapping
        DistinctVariableOnlyDataAtom mappingProjectionAtom = ATOM_FACTORY.getDistinctTripleAtom(X, P, O);
        ConstructionNode mappingRootNode = IQ_FACTORY.createConstructionNode(mappingProjectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(P, generateConstant(RDF.TYPE), O, generateConstant(C3_IRI)));
        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(X));
        ExtensionalDataNode extensionalDataNode2 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(X));

        IQ mapping = IQ_FACTORY.createIQ(mappingProjectionAtom,
                IQ_FACTORY.createUnaryIQTree(mappingRootNode,
                        IQ_FACTORY.createNaryIQTree(unionNode, ImmutableList.of(extensionalDataNode1, extensionalDataNode2))));

        // Expected query
        IQ expectedQuery = IQ_FACTORY.createIQ(ANS1_X_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createNaryIQTree(unionNode, ImmutableList.of(extensionalDataNode1, extensionalDataNode2))));

        optimizeAndCompare(mainQuery, mapping, expectedQuery, intensionalDataNode);
    }



    @Test
    public void testDescendingSubstitutionOnRenamedNode() {
        /*
         * The bug was the following: during query merging (QueryMergingExecutorImpl.analyze()),
         * variables of the current node were possibly renamed,
         * and then a (possible) substitution applied to the renamed node.
         * In some implementations (e.g. for ConstructionNode),
         * the substitution method (QueryNode.applyDescendingSubstitution(node, query)) may require information about
         * the node's parent/children.
         * But the renamed node not being (yet) part of the query (as opposed to the original node),
         * this raises an exception.
         * This behavior has been prevented by performing variable renaming beforehand for the whole merged query,
         * i.e. before starting substitution lift.
          */
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution());
        IntensionalDataNode intensionalDataNode = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(X, C3_IRI));

        IQ mainQuery = IQ_FACTORY.createIQ(ANS1_X_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode, intensionalDataNode));

        // Mapping
        DistinctVariableOnlyDataAtom mappingProjectionAtom = ATOM_FACTORY.getDistinctTripleAtom(X, P, O);
        ConstructionNode mappingRootNode = IQ_FACTORY.createConstructionNode(mappingProjectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(P, generateConstant(RDF.TYPE), O, generateConstant(C3_IRI)));
        ExtensionalDataNode extensionalDataNode1 = createExtensionalDataNode(TABLE4_AR1, ImmutableList.of(X));
        IQ mapping = IQ_FACTORY.createIQ(mappingProjectionAtom,
                IQ_FACTORY.createUnaryIQTree(mappingRootNode, extensionalDataNode1));

        IQ expectedQuery = IQ_FACTORY.createIQ(ANS1_X_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode, extensionalDataNode1));

        optimizeAndCompare(mainQuery, mapping, expectedQuery, intensionalDataNode);
    }


    @Test
    public void testTrueNodeCreation() {
        //  Main  query.
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of());
        IntensionalDataNode intensionalDataNode = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(
                TERM_FACTORY.getRDFLiteralConstant("1", XSD.INTEGER), C3_IRI));

        IQ mainQuery = IQ_FACTORY.createIQ(ANS0_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode, intensionalDataNode));

        // Mapping assertion
        DistinctVariableOnlyDataAtom mappingProjectionAtom = ATOM_FACTORY.getDistinctTripleAtom(X, P, O);
        ConstructionNode mappingRootNode = IQ_FACTORY.createConstructionNode(mappingProjectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, TERM_FACTORY.getRDFLiteralConstant("1", XSD.INTEGER), P, generateConstant(RDF.TYPE), O, generateConstant(C3_IRI)));

        IQ mapping = IQ_FACTORY.createIQ(mappingProjectionAtom,
                IQ_FACTORY.createUnaryIQTree(mappingRootNode, IQ_FACTORY.createTrueNode()));

        // Expected query
        IQ expectedQuery = IQ_FACTORY.createIQ(ANS0_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode, IQ_FACTORY.createTrueNode()));

        optimizeAndCompare(mainQuery, mapping, expectedQuery, intensionalDataNode);
    }


    private static void optimizeAndCompare(IQ mainQuery, IQ subQuery, IQ expectedQuery, IntensionalDataNode intensionalNode) {

        System.out.println("\n Original query: \n" + mainQuery);
        System.out.println("\n Sub-query: \n" + subQuery);
        System.out.println("\n Expected query: \n" + expectedQuery);

        IQ mergedQuery = merge(mainQuery, subQuery, intensionalNode);
        System.out.println("\n Optimized query: \n" + mergedQuery);

        assertEquals(expectedQuery, mergedQuery);
    }

    private static IQ merge(IQ mainQuery, IQ subQuery, IntensionalDataNode intensionalNode) {
        BasicIntensionalQueryMerger queryMerger = new BasicIntensionalQueryMerger(
                ImmutableMap.of(intensionalNode.getProjectionAtom().getPredicate(), subQuery));

        return queryMerger.optimize(mainQuery);
    }


    private static ImmutableFunctionalTerm generateURI1(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STR_1, ImmutableList.of(argument));
    }

    private static ImmutableFunctionalTerm generateURI2(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STR_2, ImmutableList.of(argument));
    }

    private static ImmutableFunctionalTerm generateString(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getRDFLiteralFunctionalTerm(argument, XSD.STRING);
    }


    /**
     * Basic implementation
     */
    private static class BasicIntensionalQueryMerger extends AbstractIntensionalQueryMerger {

        private final ImmutableMap<AtomPredicate, IQ> map;

        protected BasicIntensionalQueryMerger(ImmutableMap<AtomPredicate, IQ> map) {
            super(IQ_FACTORY);
            this.map = map;
        }

        @Override
        protected QueryMergingTransformer createTransformer(ImmutableSet<Variable> knownVariables) {
            VariableGenerator variableGenerator = CORE_UTILS_FACTORY.createVariableGenerator(knownVariables);
            return new BasicQueryMergingTransformer(variableGenerator);
        }

        private class BasicQueryMergingTransformer extends QueryMergingTransformer {

            protected BasicQueryMergingTransformer(VariableGenerator variableGenerator) {
                super(variableGenerator, IQ_FACTORY, SUBSTITUTION_FACTORY, ATOM_FACTORY, TRANSFORMER_FACTORY);
            }

            @Override
            protected Optional<IQ> getDefinition(IntensionalDataNode dataNode) {
                return Optional.ofNullable(map.get(dataNode.getProjectionAtom().getPredicate()));
            }

            @Override
            protected IQTree handleIntensionalWithoutDefinition(IntensionalDataNode dataNode) {
                return dataNode;
            }
        }

    }

}


