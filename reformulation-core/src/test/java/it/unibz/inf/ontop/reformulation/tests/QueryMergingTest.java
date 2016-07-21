package it.unibz.inf.ontop.reformulation.tests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.impl.URITemplatePredicateImpl;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.*;
import it.unibz.inf.ontop.pivotalrepr.impl.tree.DefaultIntermediateQueryBuilder;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.QueryMergingProposalImpl;
import org.junit.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.model.Predicate.COL_TYPE.INTEGER;
import static it.unibz.inf.ontop.pivotalrepr.equivalence.IQSyntacticEquivalenceChecker.areEquivalent;
import static org.junit.Assert.assertTrue;

public class QueryMergingTest {

    private static MetadataForQueryOptimization METADATA = new EmptyMetadataForQueryOptimization();
    private static boolean REQUIRE_USING_IN_PLACE_EXECUTOR = true;
    private static final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
    private static AtomPredicate ANS1_PREDICATE = new AtomPredicateImpl("ans1", 2);
    private static AtomPredicate ANS2_PREDICATE = new AtomPredicateImpl("ans1", 1);
    private static AtomPredicate ANS4_PREDICATE = new AtomPredicateImpl("ans1", 3);
    private static AtomPredicate P1_PREDICATE = new AtomPredicateImpl("p1", 2);
    private static AtomPredicate P2_PREDICATE = new AtomPredicateImpl("p2", 3);
    private static Variable X = DATA_FACTORY.getVariable("x");
    private static Variable Y = DATA_FACTORY.getVariable("y");
    private static Variable S = DATA_FACTORY.getVariable("s");
    private static Variable T = DATA_FACTORY.getVariable("t");
    private static Variable R = DATA_FACTORY.getVariable("r");
    private static Variable U = DATA_FACTORY.getVariable("u");
    private static Variable A = DATA_FACTORY.getVariable("a");
    private static Variable B = DATA_FACTORY.getVariable("b");
    private static Variable B1 = DATA_FACTORY.getVariable("b1");
    private static Variable C = DATA_FACTORY.getVariable("c");
    private static DistinctVariableOnlyDataAtom ANS1_XY_ATOM = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
            ANS1_PREDICATE, ImmutableList.of(X, Y));
    private static DistinctVariableOnlyDataAtom ANS1_X_ATOM = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
            ANS2_PREDICATE, ImmutableList.of(X));
    private static DistinctVariableOnlyDataAtom P1_ATOM = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
            P1_PREDICATE, ImmutableList.of(S, T));
    private static DistinctVariableOnlyDataAtom P2_ATOM = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
            P1_PREDICATE, ImmutableList.of(S, T));
    private static URITemplatePredicate URI_PREDICATE =  new URITemplatePredicateImpl(2);
    private static Constant URI_TEMPLATE_STR_1 =  DATA_FACTORY.getConstantLiteral("http://example.org/ds1/{}");
    private static Constant URI_TEMPLATE_STR_2 =  DATA_FACTORY.getConstantLiteral("http://example.org/ds2/{}");
    private static Constant ONE = DATA_FACTORY.getConstantLiteral("1", INTEGER);
    private static DatatypePredicate XSD_INTEGER = DATA_FACTORY.getDatatypeFactory().getTypePredicate(INTEGER);
    private static Constant THREE = DATA_FACTORY.getConstantLiteral("3", INTEGER);
    private static GroundTerm INT_OF_THREE = (GroundTerm) DATA_FACTORY.getImmutableFunctionalTerm(XSD_INTEGER, THREE);
    private static GroundTerm INT_OF_ONE = (GroundTerm) DATA_FACTORY.getImmutableFunctionalTerm(XSD_INTEGER, ONE);
    private static ImmutableFunctionalTerm INT_OF_B = DATA_FACTORY.getImmutableFunctionalTerm(XSD_INTEGER, B);
    private static AtomPredicate TABLE_1 = new AtomPredicateImpl("table1", 2);
    private static AtomPredicate TABLE_2 = new AtomPredicateImpl("table2", 1);
    private static AtomPredicate TABLE_3 = new AtomPredicateImpl("table3", 2);
    private static AtomPredicate TABLE_4 = new AtomPredicateImpl("table4", 3);
    private static ExtensionalDataNode DATA_NODE_1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, A, B));
    private static ExtensionalDataNode DATA_NODE_2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_2, A));
    private static ExtensionalDataNode DATA_NODE_3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_3, B, C));
    private static ExtensionalDataNode DATA_NODE_4 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, S, T));


    @Test
    public void testPruning1() throws EmptyQueryException {
        GroundFunctionalTerm xValue = (GroundFunctionalTerm) generateURI1(ONE);

        IntermediateQuery mainQuery = createBasicSparqlQuery(ImmutableMap.of(X, xValue), xValue, Y);

        /**
         * Sub-query
         */
        IntermediateQueryBuilder subQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode subQueryRoot = new ConstructionNodeImpl(P1_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(T, generateURI1(B))), Optional.empty());
        subQueryBuilder.init(P1_ATOM, subQueryRoot);
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
        IntermediateQueryBuilder expectedBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode expectedRootNode = mainQuery.getRootConstructionNode();
        expectedBuilder.init(mainQuery.getProjectionAtom(), expectedRootNode);
        ConstructionNode remainingConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(Y, generateURI1(B))), Optional.empty());
        expectedBuilder.addChild(expectedRootNode, remainingConstructionNode);

        ExtensionalDataNode expectedDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_3, B, ONE));
        expectedBuilder.addChild(remainingConstructionNode, expectedDataNode);


        optimizeAndCompare(mainQuery, subQueryBuilder.build(), expectedBuilder.build());
    }


    @Test
    public void testEx1() throws EmptyQueryException {

        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);

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
        IntermediateQueryBuilder subQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode subQueryRoot = new ConstructionNodeImpl(P1_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(S, generateURI1(A), T, INT_OF_B)), Optional.empty());
        subQueryBuilder.init(P1_ATOM, subQueryRoot);
        subQueryBuilder.addChild(subQueryRoot, DATA_NODE_1);


        /**
         * Expected
         */
        IntermediateQueryBuilder expectedBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode expectedRootNode = mainQuery.getRootConstructionNode();
        expectedBuilder.init(mainQuery.getProjectionAtom(), expectedRootNode);
        ConstructionNode remainingConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A))), Optional.empty());
        expectedBuilder.addChild(expectedRootNode, remainingConstructionNode);

        ExtensionalDataNode expectedDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, A, THREE));
        expectedBuilder.addChild(remainingConstructionNode, expectedDataNode);

        optimizeAndCompare(mainQuery, subQueryBuilder.build(), expectedBuilder.build());
    }

    @Test
    public void testEx2() throws EmptyQueryException {

        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);

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
        IntermediateQueryBuilder subQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode subQueryRoot = new ConstructionNodeImpl(P1_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(S, generateURI1(A), T, generateURI1(B))), Optional.empty());
        subQueryBuilder.init(P1_ATOM, subQueryRoot);
        subQueryBuilder.addChild(subQueryRoot, DATA_NODE_1);

        /**
         * Expected
         */
        IntermediateQueryBuilder expectedBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode expectedRootNode = mainQuery.getRootConstructionNode();
        expectedBuilder.init(mainQuery.getProjectionAtom(), expectedRootNode);
        ConstructionNode remainingConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(B))), Optional.empty());
        expectedBuilder.addChild(expectedRootNode, remainingConstructionNode);

        ExtensionalDataNode expectedDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, B, B));
        expectedBuilder.addChild(remainingConstructionNode, expectedDataNode);

        optimizeAndCompare(mainQuery, subQueryBuilder.build(), expectedBuilder.build());
    }

    @Test
    public void testEx3() throws EmptyQueryException {

        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);

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
        IntermediateQueryBuilder subQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode subQueryRoot = new ConstructionNodeImpl(P1_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());
        subQueryBuilder.init(P1_ATOM, subQueryRoot);
        subQueryBuilder.addChild(subQueryRoot, DATA_NODE_4);

        /**
         * Expected
         */
        IntermediateQueryBuilder expectedBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode expectedRootNode = mainQuery.getRootConstructionNode();
        expectedBuilder.init(mainQuery.getProjectionAtom(), expectedRootNode);

        ExtensionalDataNode expectedDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, X, INT_OF_THREE));
        expectedBuilder.addChild(expectedRootNode, expectedDataNode);

        optimizeAndCompare(mainQuery, subQueryBuilder.build(), expectedBuilder.build());
    }

    @Test
    public void testEx4() throws EmptyQueryException {

        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);

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
        IntermediateQueryBuilder subQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode subQueryRoot = new ConstructionNodeImpl(P1_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());
        subQueryBuilder.init(P1_ATOM, subQueryRoot);
        subQueryBuilder.addChild(subQueryRoot, DATA_NODE_4);

        /**
         * Expected
         */
        IntermediateQueryBuilder expectedBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode expectedRootNode = mainQuery.getRootConstructionNode();
        expectedBuilder.init(mainQuery.getProjectionAtom(), expectedRootNode);

        ExtensionalDataNode expectedDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, X, X));
        expectedBuilder.addChild(expectedRootNode, expectedDataNode);

        optimizeAndCompare(mainQuery, subQueryBuilder.build(), expectedBuilder.build());
    }

    @Test
    public void testEx5() throws EmptyQueryException {

        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);

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
        IntermediateQueryBuilder subQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode subQueryRoot = new ConstructionNodeImpl(P1_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(T, S)), Optional.empty());
        subQueryBuilder.init(P1_ATOM, subQueryRoot);
        subQueryBuilder.addChild(subQueryRoot, dataNodeSubquery);

        /**
         * Expected
         */
        IntermediateQueryBuilder expectedBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode expectedRootNode = mainQuery.getRootConstructionNode();
        expectedBuilder.init(mainQuery.getProjectionAtom(), expectedRootNode);
        ConstructionNode remainingConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X, Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(Y, X)), Optional.empty());
        expectedBuilder.addChild(expectedRootNode, remainingConstructionNode);

        ExtensionalDataNode expectedDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, X, U));
        expectedBuilder.addChild(remainingConstructionNode, expectedDataNode);

        optimizeAndCompare(mainQuery, subQueryBuilder.build(), expectedBuilder.build());
    }

    @Test
    public void testEx6() throws EmptyQueryException {

        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(ANS1_XY_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());

        queryBuilder.init(ANS1_XY_ATOM, rootNode);

        IntensionalDataNode dataNode = new IntensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(P1_PREDICATE, X, INT_OF_THREE));
        queryBuilder.addChild(rootNode, dataNode);

        IntermediateQuery mainQuery = queryBuilder.build();

        /**
         * Sub-query
         */
        ExtensionalDataNode dataNodeSubquery = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, S, THREE));
        IntermediateQueryBuilder subQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode subQueryRoot = new ConstructionNodeImpl(P1_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(T, INT_OF_B)), Optional.empty());
        subQueryBuilder.init(P1_ATOM, subQueryRoot);
        subQueryBuilder.addChild(subQueryRoot, dataNodeSubquery);

        /**
         * Expected
         */
        IntermediateQueryBuilder expectedBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode expectedRootNode = mainQuery.getRootConstructionNode();
        expectedBuilder.init(ANS1_XY_ATOM, expectedRootNode);

        ExtensionalDataNode expectedDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, X, THREE));
        expectedBuilder.addChild(expectedRootNode, expectedDataNode);

        optimizeAndCompare(mainQuery, subQueryBuilder.build(), expectedBuilder.build());
    }

    @Test
    public void testEx7() throws EmptyQueryException {

        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);

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
        IntermediateQueryBuilder subQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode subQueryRoot = new ConstructionNodeImpl(p1Atom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(S, generateURI1(A), T, generateURI1(B),
                        U, generateURI1(C))), Optional.empty());
        subQueryBuilder.init(p1Atom, subQueryRoot);
        subQueryBuilder.addChild(subQueryRoot, dataNodeSubquery);

        /**
         * Expected
         */
        IntermediateQueryBuilder expectedBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode expectedRootNode = mainQuery.getRootConstructionNode();
        expectedBuilder.init(mainQuery.getProjectionAtom(), expectedRootNode);
        ConstructionNode remainingConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(C))), Optional.empty());
        expectedBuilder.addChild(expectedRootNode, remainingConstructionNode);

        ExtensionalDataNode expectedDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_4, C, C, C));
        expectedBuilder.addChild(remainingConstructionNode, expectedDataNode);

        optimizeAndCompare(mainQuery, subQueryBuilder.build(), expectedBuilder.build());
    }

    @Test
    public void testEx8WithEx15() throws EmptyQueryException {

        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);

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
        IntermediateQueryBuilder subQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
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
        IntermediateQueryBuilder expectedBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode expectedRootNode = mainQuery.getRootConstructionNode();
        expectedBuilder.init(mainQuery.getProjectionAtom(), expectedRootNode);
        ConstructionNode remainingConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A))), Optional.empty());
        expectedBuilder.addChild(expectedRootNode, remainingConstructionNode);

        ExtensionalDataNode expectedDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, A, A));
        expectedBuilder.addChild(remainingConstructionNode, expectedDataNode);

        optimizeAndCompare(mainQuery, subQueryBuilder.build(), expectedBuilder.build());
    }

    @Test
    public void testEx9() throws EmptyQueryException {

        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);

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

        IntermediateQueryBuilder subQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
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
        IntermediateQueryBuilder expectedBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode expectedRootNode = mainQuery.getRootConstructionNode();
        expectedBuilder.init(mainQuery.getProjectionAtom(), expectedRootNode);
        ConstructionNode remainingConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A))), Optional.empty());
        expectedBuilder.addChild(expectedRootNode, remainingConstructionNode);

        ExtensionalDataNode expectedDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(tableSubquery, A, A, A));
        expectedBuilder.addChild(remainingConstructionNode, expectedDataNode);

        optimizeAndCompare(mainQuery, subQueryBuilder.build(), expectedBuilder.build());
    }

    @Test
    public void testEx10() throws EmptyQueryException {

        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);

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
        IntermediateQueryBuilder subQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
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
        IntermediateQueryBuilder expectedBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode expectedRootNode = mainQuery.getRootConstructionNode();
        expectedBuilder.init(mainQuery.getProjectionAtom(), expectedRootNode);
        ConstructionNode remainingConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(B))), Optional.empty());
        expectedBuilder.addChild(expectedRootNode, remainingConstructionNode);

        ExtensionalDataNode expectedDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, B, B));
        expectedBuilder.addChild(remainingConstructionNode, expectedDataNode);

        optimizeAndCompare(mainQuery, subQueryBuilder.build(), expectedBuilder.build());
    }

    @Test
    public void testEx11() throws EmptyQueryException {

        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);

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
        ExtensionalDataNode dataNodeSubquery = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, A, INT_OF_ONE));
        IntermediateQueryBuilder subQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode subQueryRoot = new ConstructionNodeImpl(P1_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(S, generateURI1(A), T, generateURI1(INT_OF_ONE))), Optional.empty());
        subQueryBuilder.init(P1_ATOM, subQueryRoot);
        subQueryBuilder.addChild(subQueryRoot, dataNodeSubquery);

        /**
         * Expected
         */
        IntermediateQueryBuilder expectedBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode expectedRootNode = mainQuery.getRootConstructionNode();
        expectedBuilder.init(mainQuery.getProjectionAtom(), expectedRootNode);
        ConstructionNode remainingConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X, Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A), Y, generateURI1(INT_OF_ONE))), Optional.empty());
        expectedBuilder.addChild(expectedRootNode, remainingConstructionNode);

        ExtensionalDataNode expectedDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, A, INT_OF_ONE));
        expectedBuilder.addChild(remainingConstructionNode, expectedDataNode);

        optimizeAndCompare(mainQuery, subQueryBuilder.build(), expectedBuilder.build());
    }

    @Test
    public void testEx12() throws EmptyQueryException {

        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);

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
        IntermediateQueryBuilder subQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode subQueryRoot = new ConstructionNodeImpl(P1_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(S, generateURI1(A), T, generateURI1(INT_OF_ONE))), Optional.empty());
        subQueryBuilder.init(P1_ATOM, subQueryRoot);
        subQueryBuilder.addChild(subQueryRoot, dataNodeSubquery);

        /**
         * Expected
         */
        IntermediateQueryBuilder expectedBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode expectedRootNode = mainQuery.getRootConstructionNode();
        expectedBuilder.init(mainQuery.getProjectionAtom(), expectedRootNode);
        ConstructionNode remainingConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(INT_OF_ONE))), Optional.empty());
        expectedBuilder.addChild(expectedRootNode, remainingConstructionNode);

        ExtensionalDataNode expectedDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, INT_OF_ONE, INT_OF_ONE));
        expectedBuilder.addChild(remainingConstructionNode, expectedDataNode);

        optimizeAndCompare(mainQuery, subQueryBuilder.build(), expectedBuilder.build());
    }

    @Test
    public void testEx13() throws EmptyQueryException {

        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);

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
        IntermediateQueryBuilder subQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode subQueryRoot = new ConstructionNodeImpl(P1_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(S, generateURI1(INT_OF_ONE), T, generateURI1(B))), Optional.empty());
        subQueryBuilder.init(P1_ATOM, subQueryRoot);
        subQueryBuilder.addChild(subQueryRoot, dataNodeSubquery);

        /**
         * Expected
         */
        IntermediateQueryBuilder expectedBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode expectedRootNode = mainQuery.getRootConstructionNode();
        expectedBuilder.init(mainQuery.getProjectionAtom(), expectedRootNode);
        ConstructionNode remainingConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(INT_OF_ONE))), Optional.empty());
        expectedBuilder.addChild(expectedRootNode, remainingConstructionNode);

        ExtensionalDataNode expectedDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, INT_OF_ONE, INT_OF_ONE));
        expectedBuilder.addChild(remainingConstructionNode, expectedDataNode);

        optimizeAndCompare(mainQuery, subQueryBuilder.build(), expectedBuilder.build());
    }

    @Test
    public void testEx14() throws EmptyQueryException {

        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);

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
        IntermediateQueryBuilder subQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode subQueryRoot = new ConstructionNodeImpl(P1_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(S, generateURI1(INT_OF_ONE), T, generateURI1(INT_OF_ONE))), Optional.empty());
        subQueryBuilder.init(P1_ATOM, subQueryRoot);
        ExtensionalDataNode tableNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, DATA_FACTORY.getConstantLiteral("2"),
                DATA_FACTORY.getConstantLiteral("2")));
        subQueryBuilder.addChild(subQueryRoot, tableNode);

        /**
         * Expected
         */
        IntermediateQueryBuilder expectedBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode expectedRootNode = mainQuery.getRootConstructionNode();
        expectedBuilder.init(mainQuery.getProjectionAtom(), expectedRootNode);
        expectedBuilder.addChild(expectedRootNode, tableNode);

        optimizeAndCompare(mainQuery, subQueryBuilder.build(), expectedBuilder.build());
    }

    @Test
    public void testEx16() throws EmptyQueryException {
        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);

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
        IntermediateQueryBuilder subQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
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
        IntermediateQueryBuilder expectedBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode expectedRootNode = mainQuery.getRootConstructionNode();
        expectedBuilder.init(mainQuery.getProjectionAtom(), expectedRootNode);
        ConstructionNode remainingConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A))), Optional.empty());
        expectedBuilder.addChild(expectedRootNode, remainingConstructionNode);

        ExtensionalDataNode expectedDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, A, A));
        expectedBuilder.addChild(remainingConstructionNode, expectedDataNode);

        optimizeAndCompare(mainQuery, subQueryBuilder.build(), expectedBuilder.build());
    }


    @Test
    public void testEx17() throws EmptyQueryException {

        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);

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
        IntermediateQueryBuilder subQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
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
        IntermediateQueryBuilder expectedBuilder = new DefaultIntermediateQueryBuilder(METADATA);
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

        optimizeAndCompare(mainQuery, subQueryBuilder.build(), expectedBuilder.build());
    }

    private static IntermediateQuery createBasicSparqlQuery(
            ImmutableMap<Variable, ImmutableTerm> topBindings,
            VariableOrGroundTerm p1Arg1, VariableOrGroundTerm p1Arg2) {

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode rootNode = new ConstructionNodeImpl(ANS1_XY_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(topBindings), Optional.empty());

        queryBuilder.init(ANS1_XY_ATOM, rootNode);

        IntensionalDataNode dataNode = new IntensionalDataNodeImpl(
                DATA_FACTORY.getDataAtom(P1_PREDICATE, p1Arg1, p1Arg2));
        queryBuilder.addChild(rootNode, dataNode);

        return queryBuilder.build();
    }

    private static void optimizeAndCompare(IntermediateQuery mainQuery, IntermediateQuery subQuery,
                                           IntermediateQuery expectedQuery)
            throws EmptyQueryException {

        System.out.println("\n Original query: \n" +  mainQuery);
        System.out.println("\n Sub-query: \n" +  subQuery);
        System.out.println("\n Expected query: \n" +  expectedQuery);

        // Updates the query (in-place optimization)
        mainQuery.applyProposal(new QueryMergingProposalImpl(subQuery), REQUIRE_USING_IN_PLACE_EXECUTOR);

        System.out.println("\n Optimized query: \n" +  mainQuery);

        assertTrue(areEquivalent(mainQuery, expectedQuery));

    }

    private static ImmutableFunctionalTerm generateURI1(VariableOrGroundTerm argument) {
        return DATA_FACTORY.getImmutableFunctionalTerm(URI_PREDICATE, URI_TEMPLATE_STR_1, argument);
    }

    private static ImmutableFunctionalTerm generateURI2(VariableOrGroundTerm argument) {
        return DATA_FACTORY.getImmutableFunctionalTerm(URI_PREDICATE, URI_TEMPLATE_STR_2, argument);
    }
}


