package it.unibz.inf.ontop.reformulation.tests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.model.impl.URITemplatePredicateImpl;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.*;
import it.unibz.inf.ontop.pivotalrepr.impl.tree.DefaultIntermediateQueryBuilder;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.RemoveUnsatisfiableNodesProposalImpl;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition.RIGHT;
import static it.unibz.inf.ontop.pivotalrepr.equivalence.IQSyntacticEquivalenceChecker.areEquivalent;
import static org.junit.Assert.assertTrue;

@Ignore
public class EmptyNodeRemovalTest {

    private static MetadataForQueryOptimization METADATA = new MetadataForQueryOptimizationImpl(
            ImmutableMultimap.of(),
            new UriTemplateMatcher());
    private static boolean REQUIRE_USING_IN_PLACE_EXECUTOR = true;
    private static final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
    private static AtomPredicate ANS1_PREDICATE = new AtomPredicateImpl("ans1", 2);
    private static Variable X = DATA_FACTORY.getVariable("x");
    private static Variable Y = DATA_FACTORY.getVariable("y");
    private static Variable A = DATA_FACTORY.getVariable("a");
    private static Variable B = DATA_FACTORY.getVariable("b");
    private static Variable C = DATA_FACTORY.getVariable("c");
    private static DistinctVariableOnlyDataAtom PROJECTION_ATOM = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
            ANS1_PREDICATE, ImmutableList.of(X, Y));
    private static URITemplatePredicate URI_PREDICATE =  new URITemplatePredicateImpl(2);
    private static Constant URI_TEMPLATE_STR_1 =  DATA_FACTORY.getConstantLiteral("http://example.org/ds1/{}");
    private static AtomPredicate TABLE_1 = new AtomPredicateImpl("table1", 2);
    private static AtomPredicate TABLE_2 = new AtomPredicateImpl("table2", 1);
    private static ExtensionalDataNode DATA_NODE_1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, A, B));
    private static ExtensionalDataNode DATA_NODE_2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_2, A));

    @Test
    public void testUnionRemoval1NoTopSubstitution() throws EmptyQueryException {
        ImmutableSubstitutionImpl<ImmutableTerm> topBindings = new ImmutableSubstitutionImpl<>(
                ImmutableMap.of());
        ImmutableSubstitutionImpl<ImmutableTerm> leftBindings = new ImmutableSubstitutionImpl<>(
                ImmutableMap.of(X, generateURI1(A), Y, generateURI1(B)));
        IntermediateQuery query = generateQueryWithUnion(topBindings, leftBindings, PROJECTION_ATOM.getVariables());

        /**
         * Expected query
         */
        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode newRootNode = new ConstructionNodeImpl(PROJECTION_ATOM.getVariables(), leftBindings,
                Optional.empty());
        expectedQueryBuilder.init(PROJECTION_ATOM, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, DATA_NODE_1);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery);
    }

    @Test
    public void testUnionRemoval2() throws EmptyQueryException {
        ImmutableSubstitutionImpl<ImmutableTerm> topBindings = new ImmutableSubstitutionImpl<>(
                ImmutableMap.of(X, generateURI1(A)));
        ImmutableSubstitutionImpl<ImmutableTerm> leftBindings = new ImmutableSubstitutionImpl<>(
                ImmutableMap.of(Y, generateURI1(B)));
        IntermediateQuery query = generateQueryWithUnion(topBindings, leftBindings, ImmutableSet.of(Y, A));

        /**
         * Expected query
         */
        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);

        ImmutableSubstitution<ImmutableTerm> expectedTopBindings = topBindings.union(leftBindings)
                .orElseThrow(() -> new IllegalStateException("Wrong bindings (union cannot be computed)"));

        ConstructionNode newRootNode = new ConstructionNodeImpl(PROJECTION_ATOM.getVariables(), expectedTopBindings,
                Optional.empty());
        expectedQueryBuilder.init(PROJECTION_ATOM, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, DATA_NODE_1);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery);
    }

    @Test
    public void testUnionRemoval3() throws EmptyQueryException {
        ImmutableSubstitutionImpl<ImmutableTerm> topBindings = new ImmutableSubstitutionImpl<>(
                ImmutableMap.of(X, generateURI1(A), Y, generateURI1(B)));
        ImmutableSubstitutionImpl<ImmutableTerm> leftBindings = new ImmutableSubstitutionImpl<>(
                ImmutableMap.of());
        IntermediateQuery query = generateQueryWithUnion(topBindings, leftBindings, ImmutableSet.of(A, B));

        /**
         * Expected query
         */
        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);

        ConstructionNode newRootNode = new ConstructionNodeImpl(PROJECTION_ATOM.getVariables(), topBindings,
                Optional.empty());
        expectedQueryBuilder.init(PROJECTION_ATOM, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, DATA_NODE_1);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery);
    }

    private static IntermediateQuery generateQueryWithUnion(ImmutableSubstitution<ImmutableTerm> topBindings,
                                                            ImmutableSubstitution<ImmutableTerm> leftBindings,
                                                            ImmutableSet<Variable> subQueryProjectedVariables) {
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(PROJECTION_ATOM.getVariables(), topBindings, Optional.empty());
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        UnionNode unionNode = new UnionNodeImpl();
        queryBuilder.addChild(rootNode, unionNode);

        ConstructionNode leftConstructionNode = new ConstructionNodeImpl(subQueryProjectedVariables, leftBindings,
                Optional.empty());
        queryBuilder.addChild(unionNode, leftConstructionNode);

        queryBuilder.addChild(leftConstructionNode, DATA_NODE_1);

        EmptyNode emptyNode = new EmptyNodeImpl(subQueryProjectedVariables);
        queryBuilder.addChild(unionNode, emptyNode);

        return queryBuilder.build();
    }

    @Test
    public void testLJ1() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(PROJECTION_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A), Y, generateURI1(B))),
                Optional.empty());
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(rootNode, leftJoinNode);

        queryBuilder.addChild(leftJoinNode, DATA_NODE_2, LEFT);
        queryBuilder.addChild(leftJoinNode, new EmptyNodeImpl(ImmutableSet.of(B)), RIGHT);

        IntermediateQuery query = queryBuilder.build();

        /**
         * Expected query
         */
        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);

        ConstructionNode newRootNode = new ConstructionNodeImpl(
                PROJECTION_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A), Y, OBDAVocabulary.NULL)),
                Optional.empty());

        expectedQueryBuilder.init(PROJECTION_ATOM, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, DATA_NODE_2);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery);
    }

    @Test(expected = EmptyQueryException.class)
    public void testLJ2() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(PROJECTION_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A), Y, generateURI1(B))),
                Optional.empty());
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(rootNode, leftJoinNode);

        queryBuilder.addChild(leftJoinNode, new EmptyNodeImpl(ImmutableSet.of(A)), LEFT);
        queryBuilder.addChild(leftJoinNode, DATA_NODE_1, RIGHT);

        IntermediateQuery query = queryBuilder.build();

        System.out.println("\n Unsatisfiable query: \n" +  query);

        query.applyProposal(new RemoveUnsatisfiableNodesProposalImpl(), REQUIRE_USING_IN_PLACE_EXECUTOR);
    }

    @Test
    public void testLJ3() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(PROJECTION_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A), Y, generateURI1(B))),
                Optional.empty());
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        LeftJoinNode topLeftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(rootNode, topLeftJoinNode);

        queryBuilder.addChild(topLeftJoinNode, DATA_NODE_2, LEFT);

        LeftJoinNode rightLeftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(topLeftJoinNode, rightLeftJoinNode, RIGHT);

        queryBuilder.addChild(rightLeftJoinNode, DATA_NODE_1, LEFT);
        queryBuilder.addChild(rightLeftJoinNode, new EmptyNodeImpl(ImmutableSet.of(A, B, C)), RIGHT);

        IntermediateQuery query = queryBuilder.build();

        /**
         * Expected query
         */
        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);

        expectedQueryBuilder.init(PROJECTION_ATOM, rootNode);
        expectedQueryBuilder.addChild(rootNode, topLeftJoinNode);
        expectedQueryBuilder.addChild(topLeftJoinNode, DATA_NODE_2, LEFT);
        expectedQueryBuilder.addChild(topLeftJoinNode, DATA_NODE_1, RIGHT);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery);
    }

    @Test
    public void testLJ4() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(PROJECTION_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A), Y, generateURI1(B))),
                Optional.empty());
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        LeftJoinNode topLeftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(rootNode, topLeftJoinNode);

        queryBuilder.addChild(topLeftJoinNode, DATA_NODE_2, LEFT);

        LeftJoinNode rightLeftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(topLeftJoinNode, rightLeftJoinNode, RIGHT);

        queryBuilder.addChild(rightLeftJoinNode, new EmptyNodeImpl(ImmutableSet.of(A)), LEFT);
        queryBuilder.addChild(rightLeftJoinNode, DATA_NODE_1, RIGHT);

        IntermediateQuery query = queryBuilder.build();

        /**
         * Expected query
         */
        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);

        ConstructionNode newRootNode = new ConstructionNodeImpl(
                PROJECTION_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A), Y, OBDAVocabulary.NULL)),
                Optional.empty());

        expectedQueryBuilder.init(PROJECTION_ATOM, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, DATA_NODE_2);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery);
    }

    @Test(expected = EmptyQueryException.class)
    public void testJoin1() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(PROJECTION_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A), Y, generateURI1(B))),
                Optional.empty());
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(rootNode, joinNode);

        queryBuilder.addChild(joinNode, DATA_NODE_1);
        queryBuilder.addChild(joinNode, new EmptyNodeImpl(ImmutableSet.of(A, C)));

        IntermediateQuery query = queryBuilder.build();

        System.out.println("\n Unsatisfiable query: \n" +  query);

        // Should throw an exception
        query.applyProposal(new RemoveUnsatisfiableNodesProposalImpl(), REQUIRE_USING_IN_PLACE_EXECUTOR);
        System.err.println("\n Failure: this query should have been declared as unsatisfiable: \n" +  query);
    }

    @Test(expected = EmptyQueryException.class)
    public void testJoin2() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(PROJECTION_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A), Y, generateURI1(B))),
                Optional.empty());
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(rootNode, joinNode);

        queryBuilder.addChild(joinNode, DATA_NODE_1);

        ConstructionNode constructionNode2 = new ConstructionNodeImpl(ImmutableSet.of(A));
        queryBuilder.addChild(joinNode, constructionNode2);
        queryBuilder.addChild(constructionNode2, new EmptyNodeImpl(ImmutableSet.of(A, C)));

        IntermediateQuery query = queryBuilder.build();

        System.out.println("\n Unsatisfiable query: \n" +  query);

        // Should throw an exception
        query.applyProposal(new RemoveUnsatisfiableNodesProposalImpl(), REQUIRE_USING_IN_PLACE_EXECUTOR);
        System.err.println("\n Failure: this query should have been declared as unsatisfiable: \n" +  query);
    }



    private static void optimizeAndCompare(IntermediateQuery query, IntermediateQuery expectedQuery)
            throws EmptyQueryException {

        System.out.println("\n Original query: \n" +  query);

        // Updates the query (in-place optimization)
        query.applyProposal(new RemoveUnsatisfiableNodesProposalImpl(), REQUIRE_USING_IN_PLACE_EXECUTOR);

        System.out.println("\n Optimized query: \n" +  query);
        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertTrue(areEquivalent(query, expectedQuery));

    }

    private static ImmutableFunctionalTerm generateURI1(VariableOrGroundTerm argument) {
        return DATA_FACTORY.getImmutableFunctionalTerm(URI_PREDICATE, URI_TEMPLATE_STR_1, argument);
    }

}
