package it.unibz.inf.ontop.reformulation.tests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.impl.URITemplatePredicateImpl;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.unfolding.QueryUnfolder;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.unfolding.impl.QueryUnfolderImpl;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.*;
import it.unibz.inf.ontop.pivotalrepr.proposal.QueryMergingProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.QueryMergingProposalImpl;
import org.junit.Test;

import java.util.*;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.Predicate.COL_TYPE.INTEGER;
import static it.unibz.inf.ontop.pivotalrepr.equivalence.IQSyntacticEquivalenceChecker.areEquivalent;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;

public class QueryMergingTest {

    private static boolean REQUIRE_USING_IN_PLACE_EXECUTOR = true;
    private static AtomPredicate ANS0_PREDICATE = new AtomPredicateImpl("ans1", 0);
    private static AtomPredicate ANS1_PREDICATE = new AtomPredicateImpl("ans1", 2);
    private static AtomPredicate ANS2_PREDICATE = new AtomPredicateImpl("ans1", 1);
    private static AtomPredicate ANS4_PREDICATE = new AtomPredicateImpl("ans1", 3);
    private static AtomPredicate P1_PREDICATE = new AtomPredicateImpl("p1", 2);
    private static AtomPredicate P2_PREDICATE = new AtomPredicateImpl("p2", 3);
    private static AtomPredicate P3_PREDICATE = new AtomPredicateImpl("p3", 1);
    private static AtomPredicate P4_PREDICATE = new AtomPredicateImpl("p4", 1);
    private static AtomPredicate P5_PREDICATE = new AtomPredicateImpl("p5", 1);
    private static Variable X = DATA_FACTORY.getVariable("x");
    private static Variable Y = DATA_FACTORY.getVariable("y");
    private static Variable Z = DATA_FACTORY.getVariable("z");
    private static Variable S = DATA_FACTORY.getVariable("s");
    private static Variable T = DATA_FACTORY.getVariable("t");
    private static Variable R = DATA_FACTORY.getVariable("r");
    private static Variable U = DATA_FACTORY.getVariable("u");
    private static Variable A = DATA_FACTORY.getVariable("a");
    private static Variable B = DATA_FACTORY.getVariable("b");
    private static Variable C = DATA_FACTORY.getVariable("c");
    private static Variable D = DATA_FACTORY.getVariable("d");
    private static Variable E = DATA_FACTORY.getVariable("e");
    private static DistinctVariableOnlyDataAtom ANS1_XY_ATOM = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
            ANS1_PREDICATE, ImmutableList.of(X, Y));
    private static DistinctVariableOnlyDataAtom ANS1_X_ATOM = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
            ANS2_PREDICATE, ImmutableList.of(X));
    private static DistinctVariableOnlyDataAtom ANS0_ATOM = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
            ANS0_PREDICATE, ImmutableList.of());
    private static DistinctVariableOnlyDataAtom P1_ST_ATOM = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
            P1_PREDICATE, ImmutableList.of(S, T));
    private static DistinctVariableOnlyDataAtom P2_ST_ATOM = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
            P1_PREDICATE, ImmutableList.of(S, T));
    private static DistinctVariableOnlyDataAtom P3_X_ATOM = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
            P3_PREDICATE, ImmutableList.of(X));
    private static DistinctVariableOnlyDataAtom P4_X_ATOM = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
            P4_PREDICATE, ImmutableList.of(X));
    private static DistinctVariableOnlyDataAtom P5_X_ATOM = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
            P5_PREDICATE, ImmutableList.of(X));
    private static URITemplatePredicate URI_PREDICATE_ONE_VAR = new URITemplatePredicateImpl(2);
    private static URITemplatePredicate URI_PREDICATE_TWO_VAR = new URITemplatePredicateImpl(3);
    private static Constant URI_TEMPLATE_STR_1 = DATA_FACTORY.getConstantLiteral("http://example.org/ds1/{}");
    private static Constant URI_TEMPLATE_STR_2 = DATA_FACTORY.getConstantLiteral("http://example.org/ds2/{}");
    private static Constant URI_TEMPLATE_STR_3 = DATA_FACTORY.getConstantLiteral("http://example.org/ds3/{}/{}");
    private static Constant ONE = DATA_FACTORY.getConstantLiteral("1", INTEGER);
    private static Constant TWO = DATA_FACTORY.getConstantLiteral("2", INTEGER);
    private static DatatypePredicate XSD_INTEGER = DATA_FACTORY.getDatatypeFactory().getTypePredicate(INTEGER);
    private static Constant THREE = DATA_FACTORY.getConstantLiteral("3", INTEGER);
    private static GroundTerm INT_OF_THREE = (GroundTerm) DATA_FACTORY.getImmutableFunctionalTerm(XSD_INTEGER, THREE);
    private static GroundTerm INT_OF_ONE = (GroundTerm) DATA_FACTORY.getImmutableFunctionalTerm(XSD_INTEGER, ONE);
    private static GroundTerm INT_OF_TWO = (GroundTerm) DATA_FACTORY.getImmutableFunctionalTerm(XSD_INTEGER, TWO);
    private static ImmutableFunctionalTerm INT_OF_B = DATA_FACTORY.getImmutableFunctionalTerm(XSD_INTEGER, B);
    private static AtomPredicate TABLE_1 = new AtomPredicateImpl("table1", 2);
    private static AtomPredicate TABLE_2 = new AtomPredicateImpl("table2", 1);
    private static AtomPredicate TABLE_3 = new AtomPredicateImpl("table3", 2);
    private static AtomPredicate TABLE_4 = new AtomPredicateImpl("table4", 3);
    private static ExtensionalDataNode DATA_NODE_1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, A, B));
    private static ExtensionalDataNode DATA_NODE_3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_3, B, C));
    private static ExtensionalDataNode DATA_NODE_4 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, S, T));

    @Test
    public void testPruning1() throws EmptyQueryException {
        GroundFunctionalTerm xValue = (GroundFunctionalTerm) generateURI1(ONE);

        IntermediateQuery mainQuery = createBasicSparqlQuery(ImmutableMap.of(X, xValue), xValue, Y);

        /**
         * Sub-query
         */
        IntermediateQueryBuilder subQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode subQueryRoot = new ConstructionNodeImpl(P1_ST_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(T, generateURI1(B))), Optional.empty());
        subQueryBuilder.init(P1_ST_ATOM, subQueryRoot);
        ImmutableSet<Variable> unionProjectedVariables = ImmutableSet.of(S, B);
        UnionNode unionNode = new UnionNodeImpl(unionProjectedVariables);
        subQueryBuilder.addChild(subQueryRoot, unionNode);

        ConstructionNode leftConstructionNode = new ConstructionNodeImpl(unionProjectedVariables,
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(S, generateURI2(A))), Optional.empty());
        subQueryBuilder.addChild(unionNode, leftConstructionNode);
        subQueryBuilder.addChild(leftConstructionNode, DATA_NODE_1);

        ConstructionNode rightConstructionNode = new ConstructionNodeImpl(unionProjectedVariables,
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(S, generateURI1(C))), Optional.empty());
        subQueryBuilder.addChild(unionNode, rightConstructionNode);
        subQueryBuilder.addChild(rightConstructionNode, DATA_NODE_3);

        /**
         * Expected
         */
        IntermediateQueryBuilder expectedBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode expectedRootNode = mainQuery.getRootConstructionNode();
        expectedBuilder.init(mainQuery.getProjectionAtom(), expectedRootNode);
        ConstructionNode remainingConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(Y, generateURI1(B))), Optional.empty());
        expectedBuilder.addChild(expectedRootNode, remainingConstructionNode);

        ExtensionalDataNode expectedDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_3, B, ONE));
        expectedBuilder.addChild(remainingConstructionNode, expectedDataNode);


        optimizeAndCompare(mainQuery, subQueryBuilder.build(), expectedBuilder.build(), mainQuery.getIntensionalNodes().findFirst().get());
    }


    @Test
    public void testEx1() throws EmptyQueryException {

        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(ANS1_X_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());

        queryBuilder.init(ANS1_X_ATOM, rootNode);

        IntensionalDataNode dataNode = new IntensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(P1_PREDICATE, X, INT_OF_THREE));
        queryBuilder.addChild(rootNode, dataNode);

        IntermediateQuery mainQuery = queryBuilder.build();

        /**
         * Sub-query
         */
        IntermediateQueryBuilder subQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode subQueryRoot = new ConstructionNodeImpl(P1_ST_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(S, generateURI1(A), T, INT_OF_B)), Optional.empty());
        subQueryBuilder.init(P1_ST_ATOM, subQueryRoot);
        subQueryBuilder.addChild(subQueryRoot, DATA_NODE_1);


        /**
         * Expected
         */
        IntermediateQueryBuilder expectedBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode expectedRootNode = mainQuery.getRootConstructionNode();
        expectedBuilder.init(mainQuery.getProjectionAtom(), expectedRootNode);
        ConstructionNode remainingConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A))), Optional.empty());
        expectedBuilder.addChild(expectedRootNode, remainingConstructionNode);

        ExtensionalDataNode expectedDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, A, THREE));
        expectedBuilder.addChild(remainingConstructionNode, expectedDataNode);

        optimizeAndCompare(mainQuery, subQueryBuilder.build(), expectedBuilder.build(), dataNode);
    }

    @Test
    public void testEx2() throws EmptyQueryException {

        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(ANS1_X_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());

        queryBuilder.init(ANS1_X_ATOM, rootNode);

        IntensionalDataNode dataNode = new IntensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(P1_PREDICATE, X, X));
        queryBuilder.addChild(rootNode, dataNode);

        IntermediateQuery mainQuery = queryBuilder.build();

        /**
         * Sub-query
         */
        IntermediateQueryBuilder subQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode subQueryRoot = new ConstructionNodeImpl(P1_ST_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(S, generateURI1(A), T, generateURI1(B))), Optional.empty());
        subQueryBuilder.init(P1_ST_ATOM, subQueryRoot);
        subQueryBuilder.addChild(subQueryRoot, DATA_NODE_1);

        /**
         * Expected
         */
        IntermediateQueryBuilder expectedBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode expectedRootNode = mainQuery.getRootConstructionNode();
        expectedBuilder.init(mainQuery.getProjectionAtom(), expectedRootNode);
        ConstructionNode remainingConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(B))), Optional.empty());
        expectedBuilder.addChild(expectedRootNode, remainingConstructionNode);

        ExtensionalDataNode expectedDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, B, B));
        expectedBuilder.addChild(remainingConstructionNode, expectedDataNode);

        optimizeAndCompare(mainQuery, subQueryBuilder.build(), expectedBuilder.build(), dataNode);
    }

    @Test
    public void testEx3() throws EmptyQueryException {

        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(ANS1_X_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());

        queryBuilder.init(ANS1_X_ATOM, rootNode);

        IntensionalDataNode dataNode = new IntensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(P1_PREDICATE, X, INT_OF_THREE));
        queryBuilder.addChild(rootNode, dataNode);

        IntermediateQuery mainQuery = queryBuilder.build();

        /**
         * Sub-query
         */
        IntermediateQueryBuilder subQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode subQueryRoot = new ConstructionNodeImpl(P1_ST_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());
        subQueryBuilder.init(P1_ST_ATOM, subQueryRoot);
        subQueryBuilder.addChild(subQueryRoot, DATA_NODE_4);

        /**
         * Expected
         */
        IntermediateQueryBuilder expectedBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode expectedRootNode = mainQuery.getRootConstructionNode();
        expectedBuilder.init(mainQuery.getProjectionAtom(), expectedRootNode);
        ExtensionalDataNode expectedDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, X, INT_OF_THREE));
        expectedBuilder.addChild(expectedRootNode, expectedDataNode);

        optimizeAndCompare(mainQuery, subQueryBuilder.build(), expectedBuilder.build(), dataNode);
    }

    @Test
    public void testEx4() throws EmptyQueryException {

        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(ANS1_X_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());

        queryBuilder.init(ANS1_X_ATOM, rootNode);

        IntensionalDataNode dataNode = new IntensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(P1_PREDICATE, X, X));
        queryBuilder.addChild(rootNode, dataNode);

        IntermediateQuery mainQuery = queryBuilder.build();

        /**
         * Sub-query
         */
        IntermediateQueryBuilder subQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode subQueryRoot = new ConstructionNodeImpl(P1_ST_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());
        subQueryBuilder.init(P1_ST_ATOM, subQueryRoot);
        subQueryBuilder.addChild(subQueryRoot, DATA_NODE_4);

        /**
         * Expected
         */
        IntermediateQueryBuilder expectedBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode expectedRootNode = mainQuery.getRootConstructionNode();
        expectedBuilder.init(mainQuery.getProjectionAtom(), expectedRootNode);


        ExtensionalDataNode expectedDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, X, X));
        expectedBuilder.addChild(expectedRootNode, expectedDataNode);

        optimizeAndCompare(mainQuery, subQueryBuilder.build(), expectedBuilder.build(), dataNode);
    }

    @Test
    public void testEx5() throws EmptyQueryException {

        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(ANS1_XY_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());

        queryBuilder.init(ANS1_XY_ATOM, rootNode);

        IntensionalDataNode dataNode = new IntensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(P1_PREDICATE, X, Y));
        queryBuilder.addChild(rootNode, dataNode);

        IntermediateQuery mainQuery = queryBuilder.build();

        /**
         * Sub-query
         */
        ExtensionalDataNode dataNodeSubquery = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, S, U));
        IntermediateQueryBuilder subQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode subQueryRoot = new ConstructionNodeImpl(P1_ST_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(T, S)), Optional.empty());
        subQueryBuilder.init(P1_ST_ATOM, subQueryRoot);
        subQueryBuilder.addChild(subQueryRoot, dataNodeSubquery);

        /**
         * Expected
         */
        IntermediateQueryBuilder expectedBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode expectedRootNode = mainQuery.getRootConstructionNode();
        expectedBuilder.init(mainQuery.getProjectionAtom(), expectedRootNode);
        ConstructionNode remainingConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X, Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(Y, X)), Optional.empty());
        expectedBuilder.addChild(expectedRootNode, remainingConstructionNode);
        ExtensionalDataNode expectedDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, X, U));
        expectedBuilder.addChild(remainingConstructionNode, expectedDataNode);

        optimizeAndCompare(mainQuery, subQueryBuilder.build(), expectedBuilder.build(), dataNode);
    }

    @Test
    public void testEx6() throws EmptyQueryException {

        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(ANS1_X_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());

        queryBuilder.init(ANS1_X_ATOM, rootNode);

        IntensionalDataNode dataNode = new IntensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(P1_PREDICATE, X, INT_OF_THREE));
        queryBuilder.addChild(rootNode, dataNode);

        IntermediateQuery mainQuery = queryBuilder.build();

        /**
         * Sub-query
         */
        ExtensionalDataNode dataNodeSubquery = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, S, B));
        IntermediateQueryBuilder subQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode subQueryRoot = new ConstructionNodeImpl(P1_ST_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(T, INT_OF_B)), Optional.empty());
        subQueryBuilder.init(P1_ST_ATOM, subQueryRoot);
        subQueryBuilder.addChild(subQueryRoot, dataNodeSubquery);

        /**
         * Expected
         */
        IntermediateQueryBuilder expectedBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode expectedRootNode = mainQuery.getRootConstructionNode();
        expectedBuilder.init(ANS1_X_ATOM, expectedRootNode);
        ExtensionalDataNode expectedDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, X, THREE));
        expectedBuilder.addChild(expectedRootNode, expectedDataNode);

        optimizeAndCompare(mainQuery, subQueryBuilder.build(), expectedBuilder.build(), dataNode);
    }

    @Test
    public void testEx7() throws EmptyQueryException {

        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(ANS1_X_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());

        queryBuilder.init(ANS1_X_ATOM, rootNode);

        IntensionalDataNode dataNode = new IntensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(P2_PREDICATE, X, X, X));
        queryBuilder.addChild(rootNode, dataNode);

        IntermediateQuery mainQuery = queryBuilder.build();

        /**
         * Sub-query
         */
        DistinctVariableOnlyDataAtom p1Atom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
                P2_PREDICATE, ImmutableList.of(S, T, U));
        ExtensionalDataNode dataNodeSubquery = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_4, A, B, C));
        IntermediateQueryBuilder subQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode subQueryRoot = new ConstructionNodeImpl(p1Atom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(S, generateURI1(A), T, generateURI1(B),
                        U, generateURI1(C))), Optional.empty());
        subQueryBuilder.init(p1Atom, subQueryRoot);
        subQueryBuilder.addChild(subQueryRoot, dataNodeSubquery);

        /**
         * Expected
         */
        IntermediateQueryBuilder expectedBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode expectedRootNode = mainQuery.getRootConstructionNode();
        expectedBuilder.init(mainQuery.getProjectionAtom(), expectedRootNode);
        ConstructionNode remainingConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(C))), Optional.empty());
        expectedBuilder.addChild(expectedRootNode, remainingConstructionNode);

        ExtensionalDataNode expectedDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_4, C, C, C));
        expectedBuilder.addChild(remainingConstructionNode, expectedDataNode);

        optimizeAndCompare(mainQuery, subQueryBuilder.build(), expectedBuilder.build(), dataNode);
    }

    @Test
    public void testEx8WithEx15() throws EmptyQueryException {

        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(ANS1_X_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());

        queryBuilder.init(ANS1_X_ATOM, rootNode);

        IntensionalDataNode dataNode = new IntensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(P1_PREDICATE, X, X));
        queryBuilder.addChild(rootNode, dataNode);

        IntermediateQuery mainQuery = queryBuilder.build();

        /**
         * Sub-query
         */
        DistinctVariableOnlyDataAtom p1Atom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
                P1_PREDICATE, ImmutableList.of(S, T));
        AtomPredicate tableSubquery = new AtomPredicateImpl("table1", 2);
        ExtensionalDataNode dataNodeSubquery = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(tableSubquery, A, B));
        IntermediateQueryBuilder subQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode subQueryRoot = new ConstructionNodeImpl(p1Atom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(S, generateURI1(A))), Optional.empty());
        subQueryBuilder.init(p1Atom, subQueryRoot);

        ConstructionNode constructionNode2 = new ConstructionNodeImpl(ImmutableSet.of(T, A),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        T, generateURI1(B)
                )), Optional.empty());
        subQueryBuilder.addChild(subQueryRoot, constructionNode2);

        subQueryBuilder.addChild(constructionNode2, dataNodeSubquery);

        /**
         * Expected
         */
        IntermediateQueryBuilder expectedBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode expectedRootNode = mainQuery.getRootConstructionNode();
        expectedBuilder.init(mainQuery.getProjectionAtom(), expectedRootNode);
        ConstructionNode remainingConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A))), Optional.empty());
        expectedBuilder.addChild(expectedRootNode, remainingConstructionNode);

        ExtensionalDataNode expectedDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, A, A));
        expectedBuilder.addChild(remainingConstructionNode, expectedDataNode);

        optimizeAndCompare(mainQuery, subQueryBuilder.build(), expectedBuilder.build(), dataNode);
    }

    @Test
    public void testEx9() throws EmptyQueryException {

        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(ANS1_X_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());

        queryBuilder.init(ANS1_X_ATOM, rootNode);

        IntensionalDataNode dataNode = new IntensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(P2_PREDICATE, X, X, X));
        queryBuilder.addChild(rootNode, dataNode);

        IntermediateQuery mainQuery = queryBuilder.build();

        /**
         * Sub-query
         */
        DistinctVariableOnlyDataAtom p1Atom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
                P2_PREDICATE, ImmutableList.of(S, T, U));
        AtomPredicate tableSubquery = new AtomPredicateImpl("table1", 3);

        IntermediateQueryBuilder subQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode subQueryRoot = new ConstructionNodeImpl(p1Atom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(S, generateURI1(A), T, generateURI1(B))), Optional.empty());
        subQueryBuilder.init(p1Atom, subQueryRoot);

        ConstructionNode constructionNode2 = new ConstructionNodeImpl(ImmutableSet.of(U, A, B),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        U, generateURI1(C)
                )), Optional.empty());
        subQueryBuilder.addChild(subQueryRoot, constructionNode2);

        ExtensionalDataNode dataNodeSubquery = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(tableSubquery, A, B, C));
        subQueryBuilder.addChild(constructionNode2, dataNodeSubquery);

        /**
         * Expected
         */
        IntermediateQueryBuilder expectedBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode expectedRootNode = mainQuery.getRootConstructionNode();
        expectedBuilder.init(mainQuery.getProjectionAtom(), expectedRootNode);
        ConstructionNode remainingConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A))), Optional.empty());
        expectedBuilder.addChild(expectedRootNode, remainingConstructionNode);

        ExtensionalDataNode expectedDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(tableSubquery, A, A, A));
        expectedBuilder.addChild(remainingConstructionNode, expectedDataNode);

        optimizeAndCompare(mainQuery, subQueryBuilder.build(), expectedBuilder.build(), dataNode);
    }

    @Test
    public void testEx10() throws EmptyQueryException {

        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(ANS1_X_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());

        queryBuilder.init(ANS1_X_ATOM, rootNode);

        IntensionalDataNode dataNode = new IntensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(P1_PREDICATE, X, X));
        queryBuilder.addChild(rootNode, dataNode);

        IntermediateQuery mainQuery = queryBuilder.build();

        /**
         * Sub-query
         */
        DistinctVariableOnlyDataAtom p1Atom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
                P1_PREDICATE, ImmutableList.of(S, T));
        IntermediateQueryBuilder subQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode subQueryRoot = new ConstructionNodeImpl(p1Atom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(T, generateURI1(B))), Optional.empty());
        subQueryBuilder.init(p1Atom, subQueryRoot);
        ConstructionNode subQueryConstruction2 = new ConstructionNodeImpl(ImmutableSet.of(S, B),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(S, generateURI1(A))), Optional.empty());
        subQueryBuilder.addChild(subQueryRoot, subQueryConstruction2);
        ExtensionalDataNode dataNodeSubquery = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, A, B));
        subQueryBuilder.addChild(subQueryConstruction2, dataNodeSubquery);

        /**
         * Expected
         */
        IntermediateQueryBuilder expectedBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode expectedRootNode = mainQuery.getRootConstructionNode();
        expectedBuilder.init(mainQuery.getProjectionAtom(), expectedRootNode);
        ConstructionNode remainingConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(B))), Optional.empty());
        expectedBuilder.addChild(expectedRootNode, remainingConstructionNode);

        ExtensionalDataNode expectedDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, B, B));
        expectedBuilder.addChild(remainingConstructionNode, expectedDataNode);

        optimizeAndCompare(mainQuery, subQueryBuilder.build(), expectedBuilder.build(), dataNode);
    }

    @Test
    public void testEx11() throws EmptyQueryException {

        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(ANS1_XY_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());

        queryBuilder.init(DATA_FACTORY.getDistinctVariableOnlyDataAtom(P1_PREDICATE, ImmutableList.of(X, Y)), rootNode);

        IntensionalDataNode dataNode = new IntensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(P1_PREDICATE, X, Y));
        queryBuilder.addChild(rootNode, dataNode);

        IntermediateQuery mainQuery = queryBuilder.build();


        /**
         * Sub-query
         */
        ExtensionalDataNode dataNodeSubquery = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_2, A));
        IntermediateQueryBuilder subQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode subQueryRoot = new ConstructionNodeImpl(P1_ST_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(S, generateURI1(A), T, generateURI1(INT_OF_ONE))), Optional.empty());
        subQueryBuilder.init(P1_ST_ATOM, subQueryRoot);
        subQueryBuilder.addChild(subQueryRoot, dataNodeSubquery);

        /**
         * Expected
         */
        IntermediateQueryBuilder expectedBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode expectedRootNode = mainQuery.getRootConstructionNode();
        expectedBuilder.init(mainQuery.getProjectionAtom(), expectedRootNode);
        ConstructionNode remainingConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X, Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A), Y, generateURI1(INT_OF_ONE))), Optional.empty());
        expectedBuilder.addChild(expectedRootNode, remainingConstructionNode);

        ExtensionalDataNode expectedDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_2, A));
        expectedBuilder.addChild(remainingConstructionNode, expectedDataNode);

        optimizeAndCompare(mainQuery, subQueryBuilder.build(), expectedBuilder.build(), dataNode);
    }

    @Test
    public void testEx12() throws EmptyQueryException {

        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(ANS1_X_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());

        queryBuilder.init(ANS1_X_ATOM, rootNode);

        IntensionalDataNode dataNode = new IntensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(P1_PREDICATE, X, X));
        queryBuilder.addChild(rootNode, dataNode);

        IntermediateQuery mainQuery = queryBuilder.build();

        /**
         * Sub-query
         */
        ExtensionalDataNode dataNodeSubquery = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, A, INT_OF_ONE));
        IntermediateQueryBuilder subQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode subQueryRoot = new ConstructionNodeImpl(P1_ST_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(S, generateURI1(A), T, generateURI1(INT_OF_ONE))), Optional.empty());
        subQueryBuilder.init(P1_ST_ATOM, subQueryRoot);
        subQueryBuilder.addChild(subQueryRoot, dataNodeSubquery);

        /**
         * Expected
         */
        IntermediateQueryBuilder expectedBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode expectedRootNode = mainQuery.getRootConstructionNode();
        expectedBuilder.init(mainQuery.getProjectionAtom(), expectedRootNode);
        ConstructionNode remainingConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(INT_OF_ONE))), Optional.empty());
        expectedBuilder.addChild(expectedRootNode, remainingConstructionNode);

        ExtensionalDataNode expectedDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, INT_OF_ONE, INT_OF_ONE));
        expectedBuilder.addChild(remainingConstructionNode, expectedDataNode);

        optimizeAndCompare(mainQuery, subQueryBuilder.build(), expectedBuilder.build(), dataNode);
    }

    @Test
    public void testEx13() throws EmptyQueryException {

        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(ANS1_X_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());

        queryBuilder.init(ANS1_X_ATOM, rootNode);

        IntensionalDataNode dataNode = new IntensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(P1_PREDICATE, X, X));
        queryBuilder.addChild(rootNode, dataNode);

        IntermediateQuery mainQuery = queryBuilder.build();

        /**
         * Sub-query
         */
        ExtensionalDataNode dataNodeSubquery = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, INT_OF_ONE, B));
        IntermediateQueryBuilder subQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode subQueryRoot = new ConstructionNodeImpl(P1_ST_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(S, generateURI1(INT_OF_ONE), T, generateURI1(B))), Optional.empty());
        subQueryBuilder.init(P1_ST_ATOM, subQueryRoot);
        subQueryBuilder.addChild(subQueryRoot, dataNodeSubquery);

        /**
         * Expected
         */
        IntermediateQueryBuilder expectedBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode expectedRootNode = mainQuery.getRootConstructionNode();
        expectedBuilder.init(mainQuery.getProjectionAtom(), expectedRootNode);
        ConstructionNode remainingConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(INT_OF_ONE))), Optional.empty());
        expectedBuilder.addChild(expectedRootNode, remainingConstructionNode);

        ExtensionalDataNode expectedDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, INT_OF_ONE, INT_OF_ONE));
        expectedBuilder.addChild(remainingConstructionNode, expectedDataNode);

        optimizeAndCompare(mainQuery, subQueryBuilder.build(), expectedBuilder.build(), dataNode);
    }

    @Test
    public void testEx14() throws EmptyQueryException {

        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        AtomPredicate emptyAns1 = new AtomPredicateImpl("ans1", 0);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
                emptyAns1, ImmutableList.of());

        ConstructionNode rootNode = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());
        queryBuilder.init(projectionAtom, rootNode);
        IntensionalDataNode dataNode = new IntensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(P1_PREDICATE,
                        (GroundFunctionalTerm) generateURI1(INT_OF_ONE),
                        (GroundFunctionalTerm) generateURI1(INT_OF_ONE)));
        queryBuilder.addChild(rootNode, dataNode);
        IntermediateQuery mainQuery = queryBuilder.build();

        /**
         * Sub-query
         */
        IntermediateQueryBuilder subQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode subQueryRoot = new ConstructionNodeImpl(P1_ST_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(S, generateURI1(INT_OF_ONE), T, generateURI1(INT_OF_ONE))), Optional.empty());
        subQueryBuilder.init(P1_ST_ATOM, subQueryRoot);
        ExtensionalDataNode tableNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, DATA_FACTORY.getConstantLiteral("2"),
                DATA_FACTORY.getConstantLiteral("2")));
        subQueryBuilder.addChild(subQueryRoot, tableNode);

        /**
         * Expected
         */
        IntermediateQueryBuilder expectedBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode expectedRootNode = mainQuery.getRootConstructionNode();
        expectedBuilder.init(mainQuery.getProjectionAtom(), expectedRootNode);
        expectedBuilder.addChild(expectedRootNode, tableNode);

        optimizeAndCompare(mainQuery, subQueryBuilder.build(), expectedBuilder.build(), dataNode);
    }

    @Test
    public void testEx16() throws EmptyQueryException {
        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(ANS1_X_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());

        queryBuilder.init(ANS1_X_ATOM, rootNode);

        IntensionalDataNode dataNode = new IntensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(P1_PREDICATE, X, X));
        queryBuilder.addChild(rootNode, dataNode);

        IntermediateQuery mainQuery = queryBuilder.build();

        /**
         * Sub-query
         */
        DistinctVariableOnlyDataAtom p1Atom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
                P1_PREDICATE, ImmutableList.of(S, T));
        AtomPredicate tableSubquery = new AtomPredicateImpl("table1", 2);

        ExtensionalDataNode dataNodeSubquery = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(tableSubquery, A, B));
        IntermediateQueryBuilder subQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode subQueryRoot = new ConstructionNodeImpl(p1Atom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(S, generateURI1(A))), Optional.empty());
        subQueryBuilder.init(p1Atom, subQueryRoot);

        ConstructionNode constructionNode2 = new ConstructionNodeImpl(ImmutableSet.of(T, A),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        T, R
                )), Optional.empty());
        subQueryBuilder.addChild(subQueryRoot, constructionNode2);

        ConstructionNode constructionNode3 = new ConstructionNodeImpl(ImmutableSet.of(R, A),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        R, generateURI1(B)
                )), Optional.empty());
        subQueryBuilder.addChild(constructionNode2, constructionNode3);
        subQueryBuilder.addChild(constructionNode3, dataNodeSubquery);

        /**
         * Expected
         */
        IntermediateQueryBuilder expectedBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode expectedRootNode = mainQuery.getRootConstructionNode();
        expectedBuilder.init(mainQuery.getProjectionAtom(), expectedRootNode);
        ConstructionNode remainingConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A))), Optional.empty());
        expectedBuilder.addChild(expectedRootNode, remainingConstructionNode);

        ExtensionalDataNode expectedDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, A, A));
        expectedBuilder.addChild(remainingConstructionNode, expectedDataNode);

        optimizeAndCompare(mainQuery, subQueryBuilder.build(), expectedBuilder.build(), dataNode);
    }


    @Test
    public void testEx17() throws EmptyQueryException {

        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(ANS1_X_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());

        queryBuilder.init(ANS1_X_ATOM, rootNode);

        IntensionalDataNode dataNode = new IntensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(P1_PREDICATE, X, X));
        queryBuilder.addChild(rootNode, dataNode);

        IntermediateQuery mainQuery = queryBuilder.build();

        /**
         * Sub-query
         */
        DistinctVariableOnlyDataAtom p1Atom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
                P1_PREDICATE, ImmutableList.of(S, T));
        AtomPredicate tableSubquery = new AtomPredicateImpl("table1", 2);
        ExtensionalDataNode dataNodeSubquery = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(tableSubquery, B, C));
        IntermediateQueryBuilder subQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode subQueryRoot = new ConstructionNodeImpl(p1Atom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(S, generateURI1(A))), Optional.empty());
        subQueryBuilder.init(p1Atom, subQueryRoot);

        ConstructionNode constructionNode2 = new ConstructionNodeImpl(ImmutableSet.of(T, A),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        T, generateURI1(B), A, C
                )), Optional.empty());
        subQueryBuilder.addChild(subQueryRoot, constructionNode2);

        subQueryBuilder.addChild(constructionNode2, dataNodeSubquery);

        /**
         * Expected
         */
        IntermediateQueryBuilder expectedBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode expectedRootNode = mainQuery.getRootConstructionNode();
        expectedBuilder.init(mainQuery.getProjectionAtom(), expectedRootNode);
        ConstructionNode firstRemainingConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A))), Optional.empty());
        expectedBuilder.addChild(expectedRootNode, firstRemainingConstructionNode);
        ConstructionNode secondRemainingConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(A),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(A, C)), Optional.empty());
        expectedBuilder.addChild(firstRemainingConstructionNode, secondRemainingConstructionNode);

        ExtensionalDataNode expectedDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, C, C));
        expectedBuilder.addChild(secondRemainingConstructionNode, expectedDataNode);

        optimizeAndCompare(mainQuery, subQueryBuilder.build(), expectedBuilder.build(), dataNode);
    }

    @Test
    public void testEx18() throws EmptyQueryException {

        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(ANS1_X_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());

        queryBuilder.init(ANS1_X_ATOM, rootNode);

        IntensionalDataNode dataNode = new IntensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(P1_PREDICATE, X, X));
        queryBuilder.addChild(rootNode, dataNode);

        IntermediateQuery mainQuery = queryBuilder.build();

        /**
         * Sub-query
         */
        DistinctVariableOnlyDataAtom p1Atom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
                P1_PREDICATE, ImmutableList.of(S, T));
        IntermediateQueryBuilder subQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode subQueryRoot = new ConstructionNodeImpl(p1Atom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(S, generateURI3(A, B))), Optional.empty());
        subQueryBuilder.init(p1Atom, subQueryRoot);

        ConstructionNode constructionNode2 = new ConstructionNodeImpl(ImmutableSet.of(T, A, B),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        T, generateURI3(INT_OF_ONE, INT_OF_TWO)
                )), Optional.empty());
        subQueryBuilder.addChild(subQueryRoot, constructionNode2);
        ExtensionalDataNode dataNodeSubquery = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, A, B));
        subQueryBuilder.addChild(constructionNode2, dataNodeSubquery);

        /**
         * Expected
         */
        IntermediateQueryBuilder expectedBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode expectedRootNode = mainQuery.getRootConstructionNode();
        expectedBuilder.init(mainQuery.getProjectionAtom(), expectedRootNode);
        ConstructionNode firstRemainingConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI3(A, B))), Optional.empty());
        expectedBuilder.addChild(expectedRootNode, firstRemainingConstructionNode);
        ConstructionNode secondRemainingConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(A, B),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        A, INT_OF_ONE,
                        B, INT_OF_TWO)), Optional.empty());
        expectedBuilder.addChild(firstRemainingConstructionNode, secondRemainingConstructionNode);

        ExtensionalDataNode expectedDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, INT_OF_ONE,
                INT_OF_TWO));
        expectedBuilder.addChild(secondRemainingConstructionNode, expectedDataNode);

        optimizeAndCompare(mainQuery, subQueryBuilder.build(), expectedBuilder.build(), dataNode);
    }

    @Test
    public void testEx19() throws EmptyQueryException {

        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(ANS1_X_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());

        queryBuilder.init(ANS1_X_ATOM, rootNode);

        IntensionalDataNode dataNode = new IntensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(P1_PREDICATE, X, X));
        queryBuilder.addChild(rootNode, dataNode);

        IntermediateQuery mainQuery = queryBuilder.build();

        /**
         * Sub-query
         */
        DistinctVariableOnlyDataAtom p1Atom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
                P1_PREDICATE, ImmutableList.of(S, T));
        IntermediateQueryBuilder subQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode subQueryRoot = new ConstructionNodeImpl(p1Atom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(S, generateURI3(A, B))), Optional.empty());
        subQueryBuilder.init(p1Atom, subQueryRoot);

        ConstructionNode constructionNode2 = new ConstructionNodeImpl(ImmutableSet.of(T, A, B),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        T, generateURI3(C, D)
                )), Optional.empty());
        subQueryBuilder.addChild(subQueryRoot, constructionNode2);
        AtomPredicate tableSubquery = new AtomPredicateImpl("table5", 4);
        ExtensionalDataNode dataNodeSubquery = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(tableSubquery, A, B, C, D));
        subQueryBuilder.addChild(constructionNode2, dataNodeSubquery);

        /**
         * Expected
         */
        IntermediateQueryBuilder expectedBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode expectedRootNode = mainQuery.getRootConstructionNode();
        expectedBuilder.init(mainQuery.getProjectionAtom(), expectedRootNode);
        ConstructionNode firstRemainingConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI3(A, B))), Optional.empty());
        expectedBuilder.addChild(expectedRootNode, firstRemainingConstructionNode);

        ExtensionalDataNode expectedDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(tableSubquery, A, B, A, B));
        expectedBuilder.addChild(firstRemainingConstructionNode, expectedDataNode);

        optimizeAndCompare(mainQuery, subQueryBuilder.build(), expectedBuilder.build(), dataNode);
    }

    @Test
    public void testConflictingVariables() throws EmptyQueryException {

        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
                ANS4_PREDICATE, ImmutableList.of(X, Y, Z));
        ConstructionNode rootNode = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());
        queryBuilder.init(projectionAtom, rootNode);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(rootNode, joinNode);

        AtomPredicate firstNamePredicate = new AtomPredicateImpl("firstName", 2);
        AtomPredicate lastNamePredicate = new AtomPredicateImpl("lastName", 2);

        IntensionalDataNode firstIntentional = new IntensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(firstNamePredicate, X, Y));
        IntensionalDataNode lastIntentional = new IntensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(lastNamePredicate, X, Z));
        queryBuilder.addChild(joinNode, firstIntentional);
        queryBuilder.addChild(joinNode, lastIntentional);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("Initial query: \n" + query);


        /**
         * First name mapping
         */
        IntermediateQueryBuilder firstMappingBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom firstMappingAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
                firstNamePredicate, ImmutableList.of(S, T));
        ConstructionNode firstMappingRootNode = new ConstructionNodeImpl(firstMappingAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(S, generateURI1(A), T, generateString(B))), Optional.empty());

        firstMappingBuilder.init(firstMappingAtom, firstMappingRootNode);

        ExtensionalDataNode firstNameDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_4, A, B, C));
        firstMappingBuilder.addChild(firstMappingRootNode, firstNameDataNode);

        IntermediateQuery firstMapping = firstMappingBuilder.build();
        System.out.println("First name mapping: \n" + firstMapping);

        /**
         * Last name mapping
         */
        IntermediateQueryBuilder lastMappingBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom lastMappingAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
                lastNamePredicate, ImmutableList.of(S, T));
        ConstructionNode lastMappingRootNode = new ConstructionNodeImpl(lastMappingAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(S, generateURI1(D),
                        T, generateString(B))), Optional.empty());

        lastMappingBuilder.init(lastMappingAtom, lastMappingRootNode);

        ExtensionalDataNode lastNameDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_4, D, E, B));
        lastMappingBuilder.addChild(lastMappingRootNode, lastNameDataNode);

        IntermediateQuery lastMapping = lastMappingBuilder.build();
        System.out.println("Last name mapping: \n" + lastMapping);

        query.applyProposal(new QueryMergingProposalImpl(firstIntentional, Optional.of(firstMapping)),
                REQUIRE_USING_IN_PLACE_EXECUTOR);
        System.out.println("\n After merging the first mapping: \n" + query);

        query.applyProposal(new QueryMergingProposalImpl(lastIntentional, Optional.of(lastMapping)),
                REQUIRE_USING_IN_PLACE_EXECUTOR);
        System.out.println("\n After merging the last mapping: \n" + query);

        /**
         * Expected query
         */
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        expectedQueryBuilder.init(projectionAtom, rootNode);
        expectedQueryBuilder.addChild(rootNode, joinNode);

        ConstructionNode leftConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X, Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A), Y, generateString(B))),
                Optional.empty());
        expectedQueryBuilder.addChild(joinNode, leftConstructionNode);
        expectedQueryBuilder.addChild(leftConstructionNode, firstNameDataNode);

        Variable b1 = DATA_FACTORY.getVariable("bf0");
        ConstructionNode rightConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X, Z),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(D), Z, generateString(b1))),
                Optional.empty());
        expectedQueryBuilder.addChild(joinNode, rightConstructionNode);

        ExtensionalDataNode expectedlastNameDataNode = new ExtensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(TABLE_4, D, E, b1));
        expectedQueryBuilder.addChild(rightConstructionNode, expectedlastNameDataNode);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("Expected query: \n" + expectedQuery);

        assertTrue(areEquivalent(query, expectedQuery));
    }

    @Test
    public void testUnionSameVariable() {

        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());

        queryBuilder.init(ANS1_X_ATOM, rootNode);

        IntensionalDataNode intensionalDataNode = new IntensionalDataNodeImpl(
                P3_X_ATOM);
        queryBuilder.addChild(rootNode, intensionalDataNode);

        IntermediateQuery mainQuery = queryBuilder.build();

        System.out.println("main query:\n" + mainQuery.getProjectionAtom() + ":-\n" +
                mainQuery);

        /**
         * Mapping
         */
        IntermediateQueryBuilder mappingBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode mappingRootNode = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());

        mappingBuilder.init(P3_X_ATOM, mappingRootNode);

        UnionNode unionNode = new UnionNodeImpl(ImmutableSet.of(X));
        mappingBuilder.addChild(mappingRootNode, unionNode);


        ExtensionalDataNode extensionalDataNode1 = new ExtensionalDataNodeImpl(P4_X_ATOM);
        mappingBuilder.addChild(unionNode, extensionalDataNode1);
        ExtensionalDataNode extensionalDataNode2 = new ExtensionalDataNodeImpl(P5_X_ATOM);
        mappingBuilder.addChild(unionNode, extensionalDataNode2);

        IntermediateQuery mapping = mappingBuilder.build();
        System.out.println("query to be merged:\n" + mapping.getProjectionAtom() + ":-\n" +
                mapping);

        QueryMergingProposal queryMerging = new QueryMergingProposalImpl(intensionalDataNode, Optional.ofNullable(mapping));
        try {
            mainQuery.applyProposal(queryMerging, true);
        } catch (EmptyQueryException e) {
            e.printStackTrace();
        }

        System.out.println("merged query:\n" + mainQuery.getProjectionAtom() + ":-\n" +
                mainQuery);

        /**
         * Expected query
         */

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);


        expectedQueryBuilder.init(ANS1_X_ATOM, rootNode);
        expectedQueryBuilder.addChild(rootNode, unionNode);
        expectedQueryBuilder.addChild(unionNode, extensionalDataNode1);
        expectedQueryBuilder.addChild(unionNode, extensionalDataNode2);

        IntermediateQuery expectedQuery = queryBuilder.build();
        System.out.println("expected query:\n" + expectedQuery.getProjectionAtom() + ":-\n" +
                expectedQuery);


        assertTrue(areEquivalent(mainQuery, expectedQuery));
    }



    @Test
    public void testDescendingSubstitutionOnRenamedNode() {
        /**
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
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());

        queryBuilder.init(ANS1_X_ATOM, rootNode);

        IntensionalDataNode intensionalDataNode = new IntensionalDataNodeImpl(
                P3_X_ATOM);
        queryBuilder.addChild(rootNode, intensionalDataNode);

        IntermediateQuery mainQuery = queryBuilder.build();

        System.out.println("main query:\n"+mainQuery);

        /**
         * Mapping
         */
        IntermediateQueryBuilder mappingBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode mappingRootNode = new ConstructionNodeImpl(ImmutableSet.of(X));
        mappingBuilder.init(P3_X_ATOM, mappingRootNode);
        ExtensionalDataNode extensionalDataNode1 = new ExtensionalDataNodeImpl(P4_X_ATOM);
        mappingBuilder.addChild(mappingRootNode, extensionalDataNode1);
        IntermediateQuery mapping = mappingBuilder.build();
        System.out.println("query to be merged:\n" +mapping);

        /**
         * Merging
         */
        QueryMergingProposal queryMerging = new QueryMergingProposalImpl(intensionalDataNode, Optional.ofNullable(mapping));
        try {
            mainQuery.applyProposal(queryMerging, true);
        }catch (IllegalArgumentException|EmptyQueryException e){
            e.printStackTrace();
            fail();
        }

        System.out.println("merged query:\n" + mainQuery.getProjectionAtom() + ":-\n" +
                mainQuery);
    }

    @Test
    public void testOfflineMappingAssertionsRenaming() {

        List<IntermediateQuery> mappingAssertions = new ArrayList<>();
        DataAtom[] dataAtoms = new DataAtom[]{
                P3_X_ATOM,
                P3_X_ATOM,
                P1_ST_ATOM
        };
        DistinctVariableOnlyDataAtom[] projectionAtoms = new DistinctVariableOnlyDataAtom[]{
                P4_X_ATOM,
                P5_X_ATOM,
                P2_ST_ATOM
        };

        /**
         * Mappings assertions
         */
        for (int i =0; i < projectionAtoms.length;  i++){
            IntermediateQueryBuilder mappingBuilder = createQueryBuilder(EMPTY_METADATA);
            ConstructionNode mappingRootNode = new ConstructionNodeImpl(projectionAtoms[i].getVariables());
            mappingBuilder.init(projectionAtoms[i], mappingRootNode);
            ExtensionalDataNode extensionalDataNode = new ExtensionalDataNodeImpl(dataAtoms[i]);
            mappingBuilder.addChild(mappingRootNode, extensionalDataNode);
            IntermediateQuery mappingAssertion = mappingBuilder.build();
            mappingAssertions.add(mappingAssertion);
            System.out.println("Mapping assertion "+i+":\n" +mappingAssertion);
        }

        /**
         * Renaming
         */
        QueryUnfolder queryUnfolder= new QueryUnfolderImpl(mappingAssertions.stream());

        /**
         * Test whether two mapping assertions share a variable
         */
        System.out.println("After renaming:");
        Set<Variable> variableUnion = new HashSet<Variable>();
        for (IntermediateQuery mappingAssertion: queryUnfolder.getMappingIndex().values()){
            System.out.println(mappingAssertion);
            ImmutableSet<Variable> mappingAssertionVariables = mappingAssertion.getVariables(mappingAssertion.getRootConstructionNode());
            if(Stream.of(mappingAssertionVariables)
                    .anyMatch(v -> variableUnion.contains(v))){
                fail();
                break;
            }
            variableUnion.addAll(mappingAssertionVariables);
            System.out.println("All variables thus far: "+variableUnion+"\n");
        }
    }

    @Test
    public void testTrueNodeCreation() throws EmptyQueryException {

        /**
         *  Main  query.
         */
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(ImmutableSet.of());

        queryBuilder.init(ANS0_ATOM, rootNode);
        IntensionalDataNode intensionalDataNode = new IntensionalDataNodeImpl(DATA_FACTORY.getDataAtom(P3_PREDICATE,
                DATA_FACTORY.getConstantLiteral("1", INTEGER)));
        queryBuilder.addChild(rootNode, intensionalDataNode);
        IntermediateQuery mainQuery = queryBuilder.build();

        System.out.println("main query:\n"+mainQuery);

        /**
         * Mapping
         */
        IntermediateQueryBuilder mappingBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode mappingRootNode = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(
                        ImmutableMap.of(X, DATA_FACTORY.getConstantLiteral("1", INTEGER))),
                Optional.empty());
        mappingBuilder.init(DATA_FACTORY.getDistinctVariableOnlyDataAtom(P3_PREDICATE, X), mappingRootNode);
        IntermediateQuery mapping = mappingBuilder.build();
        System.out.println("query to be merged:\n" +mapping);

        /**
         * Expected query
         */
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        expectedQueryBuilder.init(ANS0_ATOM, rootNode);
        expectedQueryBuilder.addChild(rootNode, new TrueNodeImpl());
        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("expected query:\n"+expectedQuery);

        /**
         * Merging
         */
        QueryMergingProposal queryMerging = new QueryMergingProposalImpl(intensionalDataNode, Optional.ofNullable(mapping));
        try {
            mainQuery.applyProposal(queryMerging, true);
        }catch (IllegalArgumentException|EmptyQueryException e){
            e.printStackTrace();
            fail();
        }
        System.out.println("merged query:\n"+ mainQuery);

        /**
        * Test
         */
        assertTrue(areEquivalent(mainQuery, expectedQuery));
    }


    private static IntermediateQuery createBasicSparqlQuery(
            ImmutableMap<Variable, ImmutableTerm> topBindings,
            VariableOrGroundTerm p1Arg1, VariableOrGroundTerm p1Arg2) {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode rootNode = new ConstructionNodeImpl(ANS1_XY_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(topBindings), Optional.empty());

        queryBuilder.init(ANS1_XY_ATOM, rootNode);

        IntensionalDataNode dataNode = new IntensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(P1_PREDICATE, p1Arg1, p1Arg2));
        queryBuilder.addChild(rootNode, dataNode);

        return queryBuilder.build();
    }

    private static void optimizeAndCompare(IntermediateQuery mainQuery, IntermediateQuery subQuery,
                                           IntermediateQuery expectedQuery, IntensionalDataNode intensionalNode)
            throws EmptyQueryException {

        System.out.println("\n Original query: \n" + mainQuery);
        System.out.println("\n Sub-query: \n" + subQuery);
        System.out.println("\n Expected query: \n" + expectedQuery);

        // Updates the query (in-place optimization)
        mainQuery.applyProposal(new QueryMergingProposalImpl(intensionalNode, Optional.of(subQuery)), REQUIRE_USING_IN_PLACE_EXECUTOR);

        System.out.println("\n Optimized query: \n" + mainQuery);

        assertTrue(areEquivalent(mainQuery, expectedQuery));

    }

    private static ImmutableFunctionalTerm generateURI1(VariableOrGroundTerm argument) {
        return DATA_FACTORY.getImmutableFunctionalTerm(URI_PREDICATE_ONE_VAR, URI_TEMPLATE_STR_1, argument);
    }

    private static ImmutableFunctionalTerm generateURI2(VariableOrGroundTerm argument) {
        return DATA_FACTORY.getImmutableFunctionalTerm(URI_PREDICATE_ONE_VAR, URI_TEMPLATE_STR_2, argument);
    }

    private static ImmutableFunctionalTerm generateURI3(VariableOrGroundTerm arg1, VariableOrGroundTerm arg2) {
        return DATA_FACTORY.getImmutableFunctionalTerm(URI_PREDICATE_TWO_VAR, URI_TEMPLATE_STR_3, arg1, arg2);
    }

    private static ImmutableFunctionalTerm generateString(VariableOrGroundTerm argument) {
        return DATA_FACTORY.getImmutableFunctionalTerm(
                DATA_FACTORY.getDatatypeFactory().getTypePredicate(Predicate.COL_TYPE.STRING), argument);
    }
}


