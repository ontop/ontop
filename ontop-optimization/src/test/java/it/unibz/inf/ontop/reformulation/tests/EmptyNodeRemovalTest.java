package it.unibz.inf.ontop.reformulation.tests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.model.impl.URITemplatePredicateImpl;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.*;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeTracker;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeTrackingResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.RemoveEmptyNodeProposalImpl;
import org.junit.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static it.unibz.inf.ontop.pivotalrepr.BinaryOrderedOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.pivotalrepr.BinaryOrderedOperatorNode.ArgumentPosition.RIGHT;
import static it.unibz.inf.ontop.pivotalrepr.equivalence.IQSyntacticEquivalenceChecker.areEquivalent;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

public class EmptyNodeRemovalTest {

    private static AtomPredicate ANS1_PREDICATE = new AtomPredicateImpl("ans1", 2);
    private static Variable X = DATA_FACTORY.getVariable("x");
    private static Variable Y = DATA_FACTORY.getVariable("y");
    private static Variable A = DATA_FACTORY.getVariable("a");
    private static Variable B = DATA_FACTORY.getVariable("b");
    private static Variable B1 = DATA_FACTORY.getVariable("b1");
    private static Variable C = DATA_FACTORY.getVariable("c");
    private static DistinctVariableOnlyDataAtom PROJECTION_ATOM = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
            ANS1_PREDICATE, ImmutableList.of(X, Y));
    private static URITemplatePredicate URI_PREDICATE =  new URITemplatePredicateImpl(2);
    private static Constant URI_TEMPLATE_STR_1 =  DATA_FACTORY.getConstantLiteral("http://example.org/ds1/{}");
    private static AtomPredicate TABLE_1 = new AtomPredicateImpl("table1", 2);
    private static AtomPredicate TABLE_2 = new AtomPredicateImpl("table2", 1);
    private static ExtensionalDataNode DATA_NODE_1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, A, B));
    private static ExtensionalDataNode DATA_NODE_2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_2, A));
    private static final EmptyNode EMPTY_NODE_1 = new EmptyNodeImpl(ImmutableSet.of(A, C));

    /**
     * TODO: Put the UNION as the root instead of the construction node (when this will become legal)
     */
    @Test
    public void testUnionRemoval1NoTopSubstitution() throws EmptyQueryException {
        ImmutableSubstitutionImpl<ImmutableTerm> topBindings = new ImmutableSubstitutionImpl<>(
                ImmutableMap.of());
        ImmutableSubstitutionImpl<ImmutableTerm> leftBindings = new ImmutableSubstitutionImpl<>(
                ImmutableMap.of(X, generateURI1(A), Y, generateURI1(B)));
        EmptyNode emptyNode = new EmptyNodeImpl(PROJECTION_ATOM.getVariables());
        IntermediateQuery query = generateQueryWithUnion(topBindings, leftBindings, emptyNode);

        /**
         * Expected query
         */
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode rootNode = query.getRootConstructionNode();
        expectedQueryBuilder.init(PROJECTION_ATOM, rootNode);
        ConstructionNode secondConstructionNode = new ConstructionNodeImpl(PROJECTION_ATOM.getVariables(), leftBindings,
                Optional.empty());
        expectedQueryBuilder.addChild(rootNode, secondConstructionNode);
        expectedQueryBuilder.addChild(secondConstructionNode, DATA_NODE_1);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery, emptyNode);
    }

    @Test
    public void testUnionRemoval2() throws EmptyQueryException {
        ImmutableSubstitutionImpl<ImmutableTerm> topBindings = new ImmutableSubstitutionImpl<>(
                ImmutableMap.of(X, generateURI1(A)));
        ImmutableSubstitutionImpl<ImmutableTerm> leftBindings = new ImmutableSubstitutionImpl<>(
                ImmutableMap.of(Y, generateURI1(B)));
        EmptyNode emptyNode = new EmptyNodeImpl(ImmutableSet.of(Y, A));
        IntermediateQuery query = generateQueryWithUnion(topBindings, leftBindings, emptyNode);

        /**
         * Expected query
         */
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);

        //ImmutableSubstitution<ImmutableTerm> expectedTopBindings = topBindings.union(leftBindings)
        //        .orElseThrow(() -> new IllegalStateException("Wrong bindings (union cannot be computed)"));

        ConstructionNode rootNode = query.getRootConstructionNode();
        expectedQueryBuilder.init(PROJECTION_ATOM, rootNode);
        ConstructionNode secondConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(Y, A), leftBindings,
                Optional.empty());
        expectedQueryBuilder.addChild(rootNode, secondConstructionNode);
        expectedQueryBuilder.addChild(secondConstructionNode, DATA_NODE_1);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery, emptyNode);
    }

    @Test
    public void testUnionRemoval3() throws EmptyQueryException {
        ImmutableSubstitutionImpl<ImmutableTerm> topBindings = new ImmutableSubstitutionImpl<>(
                ImmutableMap.of(X, generateURI1(A), Y, generateURI1(B)));
        ImmutableSubstitutionImpl<ImmutableTerm> leftBindings = new ImmutableSubstitutionImpl<>(
                ImmutableMap.of());
        EmptyNode emptyNode = new EmptyNodeImpl(ImmutableSet.of(A, B));
        IntermediateQuery query = generateQueryWithUnion(topBindings, leftBindings, emptyNode);

        /**
         * Expected query
         */
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = query.getRootConstructionNode();
        expectedQueryBuilder.init(PROJECTION_ATOM, rootNode);
        expectedQueryBuilder.addChild(rootNode, DATA_NODE_1);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery, emptyNode);
    }

    @Test
    public void testUnionNoNullPropagation() throws EmptyQueryException {
        ImmutableSet<Variable> projectedVariables = PROJECTION_ATOM.getVariables();

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode rootNode = new ConstructionNodeImpl(projectedVariables,
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A), Y, generateURI1(B))),
                Optional.empty());
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        ImmutableSet<Variable> subQueryProjectedVariables = ImmutableSet.of(A, B);
        UnionNode unionNode = new UnionNodeImpl(subQueryProjectedVariables);
        queryBuilder.addChild(rootNode, unionNode);

        queryBuilder.addChild(unionNode, DATA_NODE_1);

        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(unionNode, leftJoinNode);
        queryBuilder.addChild(leftJoinNode, DATA_NODE_2, LEFT);
        EmptyNode emptyNode = new EmptyNodeImpl(subQueryProjectedVariables);
        queryBuilder.addChild(leftJoinNode, emptyNode, RIGHT);


        /**
         * Expected query
         */
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);

        expectedQueryBuilder.init(PROJECTION_ATOM, rootNode);
        expectedQueryBuilder.addChild(rootNode, unionNode);
        expectedQueryBuilder.addChild(unionNode, DATA_NODE_1);
        ConstructionNode rightConstructionNode = new ConstructionNodeImpl(subQueryProjectedVariables,
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(B, OBDAVocabulary.NULL)), Optional.empty());
        expectedQueryBuilder.addChild(unionNode, rightConstructionNode);
        expectedQueryBuilder.addChild(rightConstructionNode, DATA_NODE_2);

        optimizeAndCompare(queryBuilder.build(), expectedQueryBuilder.build(), emptyNode);
    }




    private static IntermediateQuery generateQueryWithUnion(ImmutableSubstitution<ImmutableTerm> topBindings,
                                                            ImmutableSubstitution<ImmutableTerm> leftBindings,
                                                            EmptyNode emptyNode) {
        ImmutableSet<Variable> subQueryProjectedVariables = emptyNode.getVariables();

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ImmutableSet<Variable> projectedVariables = PROJECTION_ATOM.getVariables();
        ConstructionNode rootNode = new ConstructionNodeImpl(projectedVariables, topBindings, Optional.empty());
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        UnionNode unionNode = new UnionNodeImpl(subQueryProjectedVariables);
        queryBuilder.addChild(rootNode, unionNode);

        if (leftBindings.isEmpty()) {
            queryBuilder.addChild(unionNode, DATA_NODE_1);
        }
        else {
            ConstructionNode leftConstructionNode = new ConstructionNodeImpl(subQueryProjectedVariables, leftBindings,
                    Optional.empty());
            queryBuilder.addChild(unionNode, leftConstructionNode);

            queryBuilder.addChild(leftConstructionNode, DATA_NODE_1);
        };
        queryBuilder.addChild(unionNode, emptyNode);

        return queryBuilder.build();
    }

    @Test
    public void testLJ1() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(PROJECTION_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A), Y, generateURI1(B))),
                Optional.empty());
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(rootNode, leftJoinNode);

        queryBuilder.addChild(leftJoinNode, DATA_NODE_2, LEFT);
        EmptyNode emptyNode = new EmptyNodeImpl(ImmutableSet.of(B));
        queryBuilder.addChild(leftJoinNode, emptyNode, RIGHT);

        IntermediateQuery query = queryBuilder.build();

        /**
         * Expected query
         */
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode newRootNode = new ConstructionNodeImpl(
                PROJECTION_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A), Y, OBDAVocabulary.NULL)),
                Optional.empty());

        expectedQueryBuilder.init(PROJECTION_ATOM, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, DATA_NODE_2);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery, emptyNode);
    }

    @Test(expected = EmptyQueryException.class)
    public void testLJ2() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(PROJECTION_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A), Y, generateURI1(B))),
                Optional.empty());
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(rootNode, leftJoinNode);

        EmptyNode emptyNode = new EmptyNodeImpl(ImmutableSet.of(A));
        queryBuilder.addChild(leftJoinNode, emptyNode, LEFT);
        queryBuilder.addChild(leftJoinNode, DATA_NODE_1, RIGHT);

        IntermediateQuery query = queryBuilder.build();

        System.out.println("\n Unsatisfiable query: \n" +  query);

        query.applyProposal(new RemoveEmptyNodeProposalImpl(emptyNode, false));
    }

    @Test
    public void testLJ3() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

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
        EmptyNode emptyNode = new EmptyNodeImpl(ImmutableSet.of(A, B, C));
        queryBuilder.addChild(rightLeftJoinNode, emptyNode, RIGHT);

        IntermediateQuery query = queryBuilder.build();

        /**
         * Expected query
         */
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);

        expectedQueryBuilder.init(PROJECTION_ATOM, rootNode);
        expectedQueryBuilder.addChild(rootNode, topLeftJoinNode);
        expectedQueryBuilder.addChild(topLeftJoinNode, DATA_NODE_2, LEFT);
        expectedQueryBuilder.addChild(topLeftJoinNode, DATA_NODE_1, RIGHT);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        NodeTracker tracker = optimizeAndCompare(query, expectedQuery, emptyNode);
        NodeTracker.NodeUpdate<LeftJoinNode> secondLJUpdate = tracker.getUpdate(query, rightLeftJoinNode);
        assertFalse(secondLJUpdate.getNewNode().isPresent());

        assertTrue(secondLJUpdate.getReplacingChild().isPresent());
        QueryNode replacingChildOfSecondLJ = secondLJUpdate.getReplacingChild().get();
        assertTrue(query.contains(replacingChildOfSecondLJ));
        assertTrue(replacingChildOfSecondLJ.isSyntacticallyEquivalentTo(DATA_NODE_1));

        assertTrue(secondLJUpdate.getOptionalClosestAncestor(query).isPresent());
        assertTrue(secondLJUpdate.getOptionalClosestAncestor(query).get().isSyntacticallyEquivalentTo(topLeftJoinNode));
        assertFalse(secondLJUpdate.getOptionalNextSibling(query).isPresent());
    }

    @Test
    public void testLJ4() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(PROJECTION_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A), Y, generateURI1(B))),
                Optional.empty());
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        LeftJoinNode topLeftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(rootNode, topLeftJoinNode);

        queryBuilder.addChild(topLeftJoinNode, DATA_NODE_2, LEFT);

        LeftJoinNode rightLeftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(topLeftJoinNode, rightLeftJoinNode, RIGHT);

        EmptyNode emptyNode = new EmptyNodeImpl(ImmutableSet.of(A));
        queryBuilder.addChild(rightLeftJoinNode, emptyNode, LEFT);
        queryBuilder.addChild(rightLeftJoinNode, DATA_NODE_1, RIGHT);

        IntermediateQuery query = queryBuilder.build();

        /**
         * Expected query
         */
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode newRootNode = new ConstructionNodeImpl(
                PROJECTION_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A), Y, OBDAVocabulary.NULL)),
                Optional.empty());

        expectedQueryBuilder.init(PROJECTION_ATOM, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, DATA_NODE_2);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery, emptyNode);
    }

    @Test(expected = EmptyQueryException.class)
    public void testJoin1() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(PROJECTION_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A), Y, generateURI1(B))),
                Optional.empty());
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(rootNode, joinNode);

        queryBuilder.addChild(joinNode, DATA_NODE_1);
        EmptyNode emptyNode = new EmptyNodeImpl(ImmutableSet.of(A, C));
        queryBuilder.addChild(joinNode, emptyNode);

        IntermediateQuery query = queryBuilder.build();

        System.out.println("\n Unsatisfiable query: \n" +  query);

        // Should throw an exception
        query.applyProposal(new RemoveEmptyNodeProposalImpl(emptyNode, true));
        System.err.println("\n Failure: this query should have been declared as unsatisfiable: \n" +  query);
    }

    @Test(expected = EmptyQueryException.class)
    public void testJoin2() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(PROJECTION_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A), Y, generateURI1(B))),
                Optional.empty());
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(rootNode, joinNode);

        queryBuilder.addChild(joinNode, DATA_NODE_1);

        ConstructionNode constructionNode2 = new ConstructionNodeImpl(ImmutableSet.of(A));
        queryBuilder.addChild(joinNode, constructionNode2);
        EmptyNode emptyNode = new EmptyNodeImpl(ImmutableSet.of(A, C));
        queryBuilder.addChild(constructionNode2, emptyNode);

        IntermediateQuery query = queryBuilder.build();

        System.out.println("\n Unsatisfiable query: \n" +  query);

        // Should throw an exception
        query.applyProposal(new RemoveEmptyNodeProposalImpl(emptyNode, true));
        System.err.println("\n Failure: this query should have been declared as unsatisfiable: \n" +  query);
    }

    @Test(expected = EmptyQueryException.class)
    public void testJoin3() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(PROJECTION_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A), Y, generateURI1(B))),
                Optional.empty());
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(rootNode, joinNode);

        queryBuilder.addChild(joinNode, DATA_NODE_1);

        EmptyNode emptyNode = new EmptyNodeImpl(ImmutableSet.of());
        queryBuilder.addChild(joinNode, emptyNode);

        IntermediateQuery query = queryBuilder.build();

        System.out.println("\n Unsatisfiable query: \n" +  query);

        // Should throw an exception
        query.applyProposal(new RemoveEmptyNodeProposalImpl(emptyNode, true));
        System.err.println("\n Failure: this query should have been declared as unsatisfiable: \n" +  query);
    }

    @Test(expected = EmptyQueryException.class)
    public void testJoinLJ1() throws EmptyQueryException {
        IntermediateQuery query = generateJoinLJInitialQuery(
                Optional.of(DATA_FACTORY.getImmutableExpression(ExpressionOperation.EQ, B, C)), B);

        System.out.println("\n Unsatisfiable query: \n" +  query);

        // Should throw an exception
        query.applyProposal(new RemoveEmptyNodeProposalImpl(EMPTY_NODE_1, true));
        System.err.println("\n Failure: this query should have been declared as unsatisfiable: \n" +  query);
    }

    @Test(expected = EmptyQueryException.class)
    public void testJoinLJ2() throws EmptyQueryException {

        IntermediateQuery query = generateJoinLJInitialQuery(Optional.of(DATA_FACTORY.getImmutableExpression(
                ExpressionOperation.IS_NOT_NULL, C)), B);

        System.out.println("\n Unsatisfiable query: \n" +  query);

        // Should throw an exception
        query.applyProposal(new RemoveEmptyNodeProposalImpl(EMPTY_NODE_1, true));
        System.err.println("\n Failure: this query should have been declared as unsatisfiable: \n" +  query);
    }

    @Test
    public void testJoinLJ3() throws EmptyQueryException {

        IntermediateQuery query = generateJoinLJInitialQuery(Optional.of(DATA_FACTORY.getImmutableExpression(
                ExpressionOperation.IS_NULL, C)), B);

        /**
         * Expected query
         */
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode newRootNode = new ConstructionNodeImpl(
                PROJECTION_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A), Y, generateURI1(B))),
                Optional.empty());

        expectedQueryBuilder.init(PROJECTION_ATOM, newRootNode);
        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        expectedQueryBuilder.addChild(newRootNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, DATA_NODE_1);
        expectedQueryBuilder.addChild(joinNode, DATA_NODE_2);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery, EMPTY_NODE_1);
    }

    @Test
    public void testJoinLJ4() throws EmptyQueryException {

        IntermediateQuery query = generateJoinLJInitialQuery(Optional.empty(), C);

        /**
         * Expected query
         */
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode newRootNode = new ConstructionNodeImpl(
                PROJECTION_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A), Y, OBDAVocabulary.NULL)),
                Optional.empty());

        expectedQueryBuilder.init(PROJECTION_ATOM, newRootNode);
        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        expectedQueryBuilder.addChild(newRootNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, DATA_NODE_1);
        expectedQueryBuilder.addChild(joinNode, DATA_NODE_2);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery, EMPTY_NODE_1);
    }


    private static IntermediateQuery generateJoinLJInitialQuery(Optional<ImmutableExpression> joiningCondition,
                                                                Variable variableForBuildingY) {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(PROJECTION_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A),
                        Y, generateURI1(variableForBuildingY))),
                Optional.empty());
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(joiningCondition);
        queryBuilder.addChild(rootNode, joinNode);

        queryBuilder.addChild(joinNode, DATA_NODE_1);

        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(joinNode, leftJoinNode);
        queryBuilder.addChild(leftJoinNode, DATA_NODE_2, LEFT);
        queryBuilder.addChild(leftJoinNode, EMPTY_NODE_1, RIGHT);

        return queryBuilder.build();
    }

    @Test(expected = EmptyQueryException.class)
    public void testFilter1() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(PROJECTION_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A),
                        Y, generateURI1(B))),
                Optional.empty());
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        FilterNode filterNode = new FilterNodeImpl(DATA_FACTORY.getImmutableExpression(
                ExpressionOperation.IS_NOT_NULL, C));
        queryBuilder.addChild(rootNode, filterNode);

        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(filterNode, leftJoinNode);
        queryBuilder.addChild(leftJoinNode, DATA_NODE_2, LEFT);
        EmptyNode emptyNode = new EmptyNodeImpl(ImmutableSet.of(A, B, C));
        queryBuilder.addChild(leftJoinNode, emptyNode, RIGHT);

        IntermediateQuery query = queryBuilder.build();

        System.out.println("\n Unsatisfiable query: \n" +  query);

        // Should throw an exception
        query.applyProposal(new RemoveEmptyNodeProposalImpl(emptyNode, true));
        System.err.println("\n Failure: this query should have been declared as unsatisfiable: \n" +  query);
    }

    @Test
    public void testFilter2() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(PROJECTION_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A),
                        Y, generateURI1(B))),
                Optional.empty());
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        FilterNode filterNode = new FilterNodeImpl(DATA_FACTORY.getImmutableExpression(
                ExpressionOperation.IS_NULL, C));
        queryBuilder.addChild(rootNode, filterNode);

        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(filterNode, leftJoinNode);
        queryBuilder.addChild(leftJoinNode, DATA_NODE_2, LEFT);
        EmptyNode emptyNode = new EmptyNodeImpl(ImmutableSet.of(A, B, C));
        queryBuilder.addChild(leftJoinNode, emptyNode, RIGHT);

        /**
         * Expected query
         */
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode newRootNode = new ConstructionNodeImpl(PROJECTION_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A),
                        Y, OBDAVocabulary.NULL)),
                Optional.empty());
        expectedQueryBuilder.init(PROJECTION_ATOM, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, DATA_NODE_2);

        optimizeAndCompare(queryBuilder.build(), expectedQueryBuilder.build(), emptyNode);
    }


    @Test
    public void testComplexTreeWithJoinCondition() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(PROJECTION_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A),
                        Y, generateURI1(B))),
                Optional.empty());
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        LeftJoinNode lj1 = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(rootNode, lj1);

        queryBuilder.addChild(lj1, DATA_NODE_2, LEFT);

        InnerJoinNode join = new InnerJoinNodeImpl(Optional.of(DATA_FACTORY.getImmutableExpression(
                ExpressionOperation.IS_NOT_NULL, C)));
        queryBuilder.addChild(lj1, join, RIGHT);

        queryBuilder.addChild(join, DATA_NODE_1);

        LeftJoinNode lj2 = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(join, lj2);
        queryBuilder.addChild(lj2, DATA_NODE_2.clone(), LEFT);
        EmptyNode emptyNode = new EmptyNodeImpl(ImmutableSet.of(A, C));
        queryBuilder.addChild(lj2, emptyNode, RIGHT);

        /**
         * Expected query
         */
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode newRootNode = new ConstructionNodeImpl(PROJECTION_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A),
                        Y, OBDAVocabulary.NULL)),
                Optional.empty());
        expectedQueryBuilder.init(PROJECTION_ATOM, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, DATA_NODE_2);

        optimizeAndCompare(queryBuilder.build(), expectedQueryBuilder.build(), emptyNode);
    }

    /**
     * Follows the SQL semantics
     */
    @Test
    public void testLJRemovalDueToNull1() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(PROJECTION_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A),
                        Y, generateURI1(B))),
                Optional.empty());
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        LeftJoinNode lj1 = new LeftJoinNodeImpl(Optional.of(DATA_FACTORY.getImmutableExpression(
                ExpressionOperation.EQ, B, B1)));
        queryBuilder.addChild(rootNode, lj1);
        queryBuilder.addChild(lj1, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, A, B1)), RIGHT);

        LeftJoinNode lj2 = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(lj1, lj2, LEFT);
        queryBuilder.addChild(lj2, DATA_NODE_2, LEFT);
        EmptyNode emptyNode = new EmptyNodeImpl(ImmutableSet.of(A, B));
                queryBuilder.addChild(lj2, emptyNode, RIGHT);


        /**
         * Expected query
         */
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode newRootNode = new ConstructionNodeImpl(PROJECTION_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A),
                        Y, OBDAVocabulary.NULL)),
                Optional.empty());
        expectedQueryBuilder.init(PROJECTION_ATOM, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, DATA_NODE_2);

        optimizeAndCompare(queryBuilder.build(), expectedQueryBuilder.build(), emptyNode);
    }

    /**
     * Follows the SQL semantics. Equivalent to testLJRemovalDueToNull1.
     */
    @Test
    public void testLJRemovalDueToNull2() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(PROJECTION_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A),
                        Y, generateURI1(B))),
                Optional.empty());
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        LeftJoinNode lj1 = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(rootNode, lj1);
        queryBuilder.addChild(lj1, DATA_NODE_1, RIGHT);

        LeftJoinNode lj2 = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(lj1, lj2, LEFT);
        queryBuilder.addChild(lj2, DATA_NODE_2, LEFT);
        EmptyNode emptyNode = new EmptyNodeImpl(ImmutableSet.of(A, B));
        queryBuilder.addChild(lj2, emptyNode, RIGHT);


        /**
         * Expected query
         */
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode newRootNode = new ConstructionNodeImpl(PROJECTION_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A),
                        Y, OBDAVocabulary.NULL)),
                Optional.empty());
        expectedQueryBuilder.init(PROJECTION_ATOM, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, DATA_NODE_2);

        optimizeAndCompare(queryBuilder.build(), expectedQueryBuilder.build(), emptyNode);
    }

    @Test
    public void testLJRemovalDueToNull3() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(PROJECTION_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(C),
                        Y, generateURI1(B))),
                Optional.empty());
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        LeftJoinNode lj1 = new LeftJoinNodeImpl(Optional.of(DATA_FACTORY.getImmutableExpression(
                ExpressionOperation.EQ, B, B1)));
        queryBuilder.addChild(rootNode, lj1);
        queryBuilder.addChild(lj1, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, B1, C)), RIGHT);

        LeftJoinNode lj2 = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(lj1, lj2, LEFT);
        queryBuilder.addChild(lj2, DATA_NODE_2, LEFT);
        EmptyNode emptyNode = new EmptyNodeImpl(ImmutableSet.of(A, B));
        queryBuilder.addChild(lj2, emptyNode, RIGHT);


        /**
         * Expected query
         */
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode newRootNode = new ConstructionNodeImpl(PROJECTION_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, OBDAVocabulary.NULL,
                        Y, OBDAVocabulary.NULL)),
                Optional.empty());
        expectedQueryBuilder.init(PROJECTION_ATOM, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, DATA_NODE_2);

        optimizeAndCompare(queryBuilder.build(), expectedQueryBuilder.build(), emptyNode);
    }

//    @Test
//    public void testGroup1() throws EmptyQueryException {
//        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
//
//        ConstructionNode rootNode = new ConstructionNodeImpl(PROJECTION_ATOM.getVariables(),
//                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A),
//                        Y, generateURI1(B))),
//                Optional.empty());
//        queryBuilder.init(PROJECTION_ATOM, rootNode);
//
//        GroupNode groupNode = new GroupNodeImpl(ImmutableList.of(A, B));
//        queryBuilder.addChild(rootNode, groupNode);
//
//        LeftJoinNode lj1 = new LeftJoinNodeImpl(Optional.empty());
//        queryBuilder.addChild(groupNode, lj1);
//        queryBuilder.addChild(lj1, DATA_NODE_2, LEFT);
//        EmptyNode emptyNode = new EmptyNodeImpl(ImmutableSet.of(B));
//        queryBuilder.addChild(lj1, emptyNode, RIGHT);
//
//        /**
//         * Expected query
//         */
//        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
//        ConstructionNode newRootNode = new ConstructionNodeImpl(PROJECTION_ATOM.getVariables(),
//                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A),
//                        Y, OBDAVocabulary.NULL)),
//                Optional.empty());
//        expectedQueryBuilder.init(PROJECTION_ATOM, newRootNode);
//
//        GroupNode newGroupNode = new GroupNodeImpl(ImmutableList.of(A));
//        expectedQueryBuilder.addChild(newRootNode, newGroupNode);
//        expectedQueryBuilder.addChild(newGroupNode, DATA_NODE_2);
//
//        optimizeAndCompare(queryBuilder.build(), expectedQueryBuilder.build(), emptyNode);
//    }
//
//    @Test
//    public void testGroup2() throws EmptyQueryException {
//        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
//
//        ConstructionNode rootNode = new ConstructionNodeImpl(PROJECTION_ATOM.getVariables(),
//                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A),
//                        Y, DATA_FACTORY.getImmutableExpression(ExpressionOperation.AVG, B))),
//                Optional.empty());
//        queryBuilder.init(PROJECTION_ATOM, rootNode);
//
//        GroupNode groupNode = new GroupNodeImpl(ImmutableList.of(A));
//        queryBuilder.addChild(rootNode, groupNode);
//
//        LeftJoinNode lj1 = new LeftJoinNodeImpl(Optional.empty());
//        queryBuilder.addChild(groupNode, lj1);
//        queryBuilder.addChild(lj1, DATA_NODE_2, LEFT);
//        EmptyNode emptyNode = new EmptyNodeImpl(ImmutableSet.of(B));
//        queryBuilder.addChild(lj1, emptyNode, RIGHT);
//
//        /**
//         * Expected query
//         */
//        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
//        ConstructionNode newRootNode = new ConstructionNodeImpl(PROJECTION_ATOM.getVariables(),
//                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A),
//                        Y, OBDAVocabulary.NULL)),
//                Optional.empty());
//        expectedQueryBuilder.init(PROJECTION_ATOM, newRootNode);
//
//        GroupNode newGroupNode = new GroupNodeImpl(ImmutableList.of(A));
//        expectedQueryBuilder.addChild(newRootNode, newGroupNode);
//        expectedQueryBuilder.addChild(newGroupNode, DATA_NODE_2);
//
//        optimizeAndCompare(queryBuilder.build(), expectedQueryBuilder.build(), emptyNode);
//    }
//
//    /**
//     * Not sure about grouping by "null".
//     */
//    @Test
//    public void testGroup3() throws EmptyQueryException {
//        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
//
//        ConstructionNode rootNode = new ConstructionNodeImpl(PROJECTION_ATOM.getVariables(),
//                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
//                        X, DATA_FACTORY.getImmutableExpression(ExpressionOperation.AVG, A),
//                        Y, generateURI1(B))),
//                Optional.empty());
//        queryBuilder.init(PROJECTION_ATOM, rootNode);
//
//        GroupNode groupNode = new GroupNodeImpl(ImmutableList.of(B));
//        queryBuilder.addChild(rootNode, groupNode);
//
//        LeftJoinNode lj1 = new LeftJoinNodeImpl(Optional.empty());
//        queryBuilder.addChild(groupNode, lj1);
//        queryBuilder.addChild(lj1, DATA_NODE_2, LEFT);
//        EmptyNode emptyNode = new EmptyNodeImpl(ImmutableSet.of(B));
//        queryBuilder.addChild(lj1, emptyNode, RIGHT);
//
//        /**
//         * Expected query
//         */
//        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
//        ConstructionNode newRootNode = new ConstructionNodeImpl(PROJECTION_ATOM.getVariables(),
//                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
//                        X, DATA_FACTORY.getImmutableExpression(ExpressionOperation.AVG, A),
//                        Y, OBDAVocabulary.NULL)),
//                Optional.empty());
//        expectedQueryBuilder.init(PROJECTION_ATOM, newRootNode);
//
//        expectedQueryBuilder.addChild(newRootNode, DATA_NODE_2);
//
//        optimizeAndCompare(queryBuilder.build(), expectedQueryBuilder.build(), emptyNode);
//    }


    private static NodeTracker optimizeAndCompare(IntermediateQuery query, IntermediateQuery expectedQuery,
                                                  EmptyNode emptyNode)
            throws EmptyQueryException {

        System.out.println("\n Original query: \n" +  query);
        System.out.println("\n Expected query: \n" +  expectedQuery);

        // Updates the query (in-place optimization)
        NodeTrackingResults<EmptyNode> results = query.applyProposal(new RemoveEmptyNodeProposalImpl(emptyNode, true));

        System.out.println("\n Optimized query: \n" +  query);

        assertTrue(areEquivalent(query, expectedQuery));

        Optional<NodeTracker> optionalTracker = results.getOptionalTracker();
        assertTrue(optionalTracker.isPresent());
        return optionalTracker.get();

    }

//    @Test
//    public void testGroupIsNotNullBinding() throws EmptyQueryException {
//        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
//
//        ConstructionNode rootNode = new ConstructionNodeImpl(PROJECTION_ATOM.getVariables(),
//                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A),
//                        Y, DATA_FACTORY.getImmutableExpression(ExpressionOperation.IS_NOT_NULL, B))),
//                Optional.empty());
//        queryBuilder.init(PROJECTION_ATOM, rootNode);
//
//        GroupNode groupNode = new GroupNodeImpl(ImmutableList.of(A));
//        queryBuilder.addChild(rootNode, groupNode);
//
//        LeftJoinNode lj1 = new LeftJoinNodeImpl(Optional.empty());
//        queryBuilder.addChild(groupNode, lj1);
//        queryBuilder.addChild(lj1, DATA_NODE_2, LEFT);
//        EmptyNode emptyNode = new EmptyNodeImpl(ImmutableSet.of(B));
//        queryBuilder.addChild(lj1, emptyNode, RIGHT);
//
//        /**
//         * Expected query
//         */
//        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
//        ConstructionNode newRootNode = new ConstructionNodeImpl(PROJECTION_ATOM.getVariables(),
//                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A),
//                        Y, DATA_FACTORY.getImmutableExpression(ExpressionOperation.IS_NOT_NULL, OBDAVocabulary.NULL))),
//                Optional.empty());
//        expectedQueryBuilder.init(PROJECTION_ATOM, newRootNode);
//
//        GroupNode newGroupNode = new GroupNodeImpl(ImmutableList.of(A));
//        expectedQueryBuilder.addChild(newRootNode, newGroupNode);
//        expectedQueryBuilder.addChild(newGroupNode, DATA_NODE_2);
//
//        optimizeAndCompare(queryBuilder.build(), expectedQueryBuilder.build(), emptyNode);
//    }


    private static ImmutableFunctionalTerm generateURI1(VariableOrGroundTerm argument) {
        return DATA_FACTORY.getImmutableFunctionalTerm(URI_PREDICATE, URI_TEMPLATE_STR_1, argument);
    }

}
