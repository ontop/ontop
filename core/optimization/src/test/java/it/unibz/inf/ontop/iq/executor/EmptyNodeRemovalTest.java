package it.unibz.inf.ontop.iq.executor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.proposal.NodeTracker;
import it.unibz.inf.ontop.iq.proposal.NodeTrackingResults;
import it.unibz.inf.ontop.iq.proposal.impl.RemoveEmptyNodeProposalImpl;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.term.functionsymbol.URITemplatePredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.RIGHT;
import static it.unibz.inf.ontop.iq.equivalence.IQSyntacticEquivalenceChecker.areEquivalent;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

public class EmptyNodeRemovalTest {

    private static AtomPredicate ANS1_PREDICATE = ATOM_FACTORY.getAtomPredicate("ans1", 2);
    private static Variable X = TERM_FACTORY.getVariable("x");
    private static Variable Y = TERM_FACTORY.getVariable("y");
    private static Variable A = TERM_FACTORY.getVariable("a");
    private static Variable B = TERM_FACTORY.getVariable("b");
    private static Variable B1 = TERM_FACTORY.getVariable("b1");
    private static Variable C = TERM_FACTORY.getVariable("c");
    private static DistinctVariableOnlyDataAtom PROJECTION_ATOM = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
            ANS1_PREDICATE, ImmutableList.of(X, Y));
    private static URITemplatePredicate URI_PREDICATE =  TERM_FACTORY.getURITemplatePredicate(2);
    private static Constant URI_TEMPLATE_STR_1 =  TERM_FACTORY.getConstantLiteral("http://example.org/ds1/{}");
    private static AtomPredicate TABLE_1 = ATOM_FACTORY.getAtomPredicate("table1", 2);
    private static AtomPredicate TABLE_2 = ATOM_FACTORY.getAtomPredicate("table2", 1);
    private static ExtensionalDataNode DATA_NODE_1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE_1, A, B));
    private static ExtensionalDataNode DATA_NODE_2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE_2, A));
    private static final EmptyNode EMPTY_NODE_1 = IQ_FACTORY.createEmptyNode(ImmutableSet.of(A, C));

    /**
     * TODO: Put the UNION as the root instead of the construction node (when this will become legal)
     */
    @Test
    public void testUnionRemoval1NoTopSubstitution() throws EmptyQueryException {
        ImmutableSubstitution<ImmutableTerm> topBindings = SUBSTITUTION_FACTORY.getSubstitution();
        ImmutableSubstitution<ImmutableTerm> leftBindings = SUBSTITUTION_FACTORY.getSubstitution(
                ImmutableMap.of(X, generateURI1(A), Y, generateURI1(B)));
        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(PROJECTION_ATOM.getVariables());
        IntermediateQuery query = generateQueryWithUnion(topBindings, leftBindings, emptyNode);

        /**
         * Expected query
         */
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        QueryNode rootNode = query.getRootNode();
        expectedQueryBuilder.init(PROJECTION_ATOM, rootNode);
        ConstructionNode secondConstructionNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                leftBindings);
        expectedQueryBuilder.addChild(rootNode, secondConstructionNode);
        expectedQueryBuilder.addChild(secondConstructionNode, DATA_NODE_1);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery, emptyNode);
    }

    @Test
    public void testUnionRemoval2() throws EmptyQueryException {
        ImmutableSubstitution<ImmutableTerm> topBindings = SUBSTITUTION_FACTORY.getSubstitution(
                ImmutableMap.of(X, generateURI1(A)));
        ImmutableSubstitution<ImmutableTerm> leftBindings = SUBSTITUTION_FACTORY.getSubstitution(
                ImmutableMap.of(Y, generateURI1(B)));
        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(Y, A));
        IntermediateQuery query = generateQueryWithUnion(topBindings, leftBindings, emptyNode);

        /**
         * Expected query
         */
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);

        //ImmutableSubstitution<ImmutableTerm> expectedTopBindings = topBindings.union(leftBindings)
        //        .orElseThrow(() -> new IllegalStateException("Wrong bindings (union cannot be computed)"));

        QueryNode rootNode = query.getRootNode();
        expectedQueryBuilder.init(PROJECTION_ATOM, rootNode);
        ConstructionNode secondConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y, A), leftBindings,
                Optional.empty());
        expectedQueryBuilder.addChild(rootNode, secondConstructionNode);
        expectedQueryBuilder.addChild(secondConstructionNode, DATA_NODE_1);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery, emptyNode);
    }

    @Test
    public void testUnionRemoval3() throws EmptyQueryException {
        ImmutableSubstitution<ImmutableTerm> topBindings = SUBSTITUTION_FACTORY.getSubstitution(
                ImmutableMap.of(X, generateURI1(A), Y, generateURI1(B)));
        ImmutableSubstitution<ImmutableTerm> leftBindings = SUBSTITUTION_FACTORY.getSubstitution(
                ImmutableMap.of());
        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(A, B));
        IntermediateQuery query = generateQueryWithUnion(topBindings, leftBindings, emptyNode);

        /**
         * Expected query
         */
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);

        QueryNode rootNode = query.getRootNode();
        expectedQueryBuilder.init(PROJECTION_ATOM, rootNode);
        expectedQueryBuilder.addChild(rootNode, DATA_NODE_1);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery, emptyNode);
    }

    @Test
    public void testUnionNoNullPropagation() throws EmptyQueryException {
        ImmutableSet<Variable> projectedVariables = PROJECTION_ATOM.getVariables();

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectedVariables,
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A), Y, generateURI1(B))),
                Optional.empty());
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        ImmutableSet<Variable> subQueryProjectedVariables = ImmutableSet.of(A, B);
        UnionNode unionNode = IQ_FACTORY.createUnionNode(subQueryProjectedVariables);
        queryBuilder.addChild(rootNode, unionNode);

        queryBuilder.addChild(unionNode, DATA_NODE_1);

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(unionNode, leftJoinNode);
        queryBuilder.addChild(leftJoinNode, DATA_NODE_2, LEFT);
        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(subQueryProjectedVariables);
        queryBuilder.addChild(leftJoinNode, emptyNode, RIGHT);


        /**
         * Expected query
         */
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);

        expectedQueryBuilder.init(PROJECTION_ATOM, rootNode);
        expectedQueryBuilder.addChild(rootNode, unionNode);
        expectedQueryBuilder.addChild(unionNode, DATA_NODE_1);
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(subQueryProjectedVariables,
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(B, NULL)));
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
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectedVariables, topBindings);
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        UnionNode unionNode = IQ_FACTORY.createUnionNode(subQueryProjectedVariables);
        queryBuilder.addChild(rootNode, unionNode);

        if (leftBindings.isEmpty()) {
            queryBuilder.addChild(unionNode, DATA_NODE_1);
        }
        else {
            ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(subQueryProjectedVariables,
                    leftBindings);
            queryBuilder.addChild(unionNode, leftConstructionNode);

            queryBuilder.addChild(leftConstructionNode, DATA_NODE_1);
        };
        queryBuilder.addChild(unionNode, emptyNode);

        return queryBuilder.build();
    }

    @Test
    public void testLJ1() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A), Y, generateURI1(B))),
                Optional.empty());
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(rootNode, leftJoinNode);

        queryBuilder.addChild(leftJoinNode, DATA_NODE_2, LEFT);
        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(B));
        queryBuilder.addChild(leftJoinNode, emptyNode, RIGHT);

        IntermediateQuery query = queryBuilder.build();

        /**
         * Expected query
         */
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(
                PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A), Y, NULL)),
                Optional.empty());

        expectedQueryBuilder.init(PROJECTION_ATOM, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, DATA_NODE_2);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery, emptyNode);
    }

    @Test(expected = EmptyQueryException.class)
    public void testLJ2() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A), Y, generateURI1(B))),
                Optional.empty());
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(rootNode, leftJoinNode);

        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(A));
        queryBuilder.addChild(leftJoinNode, emptyNode, LEFT);
        queryBuilder.addChild(leftJoinNode, DATA_NODE_1, RIGHT);

        IntermediateQuery query = queryBuilder.build();

        System.out.println("\n Unsatisfiable query: \n" +  query);

        query.applyProposal(new RemoveEmptyNodeProposalImpl(emptyNode, false));
    }

    @Ignore("TODO: support it")
    @Test
    public void testLJ3Optimal() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A), Y, generateURI1(B))),
                Optional.empty());
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        LeftJoinNode topLeftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(rootNode, topLeftJoinNode);

        queryBuilder.addChild(topLeftJoinNode, DATA_NODE_2, LEFT);

        LeftJoinNode rightLeftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(topLeftJoinNode, rightLeftJoinNode, RIGHT);

        queryBuilder.addChild(rightLeftJoinNode, DATA_NODE_1, LEFT);
        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(A, B, C));
        queryBuilder.addChild(rightLeftJoinNode, emptyNode, RIGHT);

        IntermediateQuery query = queryBuilder.build();

        /*
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
    public void testLJ3() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A), Y, generateURI1(B))),
                Optional.empty());
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        LeftJoinNode topLeftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(rootNode, topLeftJoinNode);

        queryBuilder.addChild(topLeftJoinNode, DATA_NODE_2, LEFT);

        LeftJoinNode rightLeftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(topLeftJoinNode, rightLeftJoinNode, RIGHT);

        queryBuilder.addChild(rightLeftJoinNode, DATA_NODE_1, LEFT);
        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(A, B, C));
        queryBuilder.addChild(rightLeftJoinNode, emptyNode, RIGHT);

        IntermediateQuery query = queryBuilder.build();

        /*
         * Expected query
         */
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);

        expectedQueryBuilder.init(PROJECTION_ATOM, rootNode);
        expectedQueryBuilder.addChild(rootNode, topLeftJoinNode);
        expectedQueryBuilder.addChild(topLeftJoinNode, DATA_NODE_2, LEFT);

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(A, B, C),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(C, NULL)));
        expectedQueryBuilder.addChild(topLeftJoinNode, rightConstructionNode, RIGHT);
        expectedQueryBuilder.addChild(rightConstructionNode, DATA_NODE_1);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery, emptyNode);
    }

    @Test
    public void testLJ4() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A), Y, generateURI1(B))),
                Optional.empty());
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        LeftJoinNode topLeftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(rootNode, topLeftJoinNode);

        queryBuilder.addChild(topLeftJoinNode, DATA_NODE_2, LEFT);

        LeftJoinNode rightLeftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(topLeftJoinNode, rightLeftJoinNode, RIGHT);

        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(A));
        queryBuilder.addChild(rightLeftJoinNode, emptyNode, LEFT);
        queryBuilder.addChild(rightLeftJoinNode, DATA_NODE_1, RIGHT);

        IntermediateQuery query = queryBuilder.build();

        /**
         * Expected query
         */
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(
                PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A), Y, NULL)),
                Optional.empty());

        expectedQueryBuilder.init(PROJECTION_ATOM, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, DATA_NODE_2);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery, emptyNode);
    }

    @Test(expected = EmptyQueryException.class)
    public void testJoin1() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A), Y, generateURI1(B))),
                Optional.empty());
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(rootNode, joinNode);

        queryBuilder.addChild(joinNode, DATA_NODE_1);
        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(A, C));
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

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A), Y, generateURI1(B))),
                Optional.empty());
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(rootNode, joinNode);

        queryBuilder.addChild(joinNode, DATA_NODE_1);

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A));
        queryBuilder.addChild(joinNode, constructionNode2);
        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(A, C));
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

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A), Y, generateURI1(B))),
                Optional.empty());
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(rootNode, joinNode);

        queryBuilder.addChild(joinNode, DATA_NODE_1);

        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of());
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
                Optional.of(TERM_FACTORY.getImmutableExpression(ExpressionOperation.EQ, B, C)), B);

        System.out.println("\n Unsatisfiable query: \n" +  query);

        // Should throw an exception
        query.applyProposal(new RemoveEmptyNodeProposalImpl(EMPTY_NODE_1, true));
        System.err.println("\n Failure: this query should have been declared as unsatisfiable: \n" +  query);
    }

    @Test(expected = EmptyQueryException.class)
    public void testJoinLJ2() throws EmptyQueryException {

        IntermediateQuery query = generateJoinLJInitialQuery(Optional.of(TERM_FACTORY.getImmutableExpression(
                ExpressionOperation.IS_NOT_NULL, C)), B);

        System.out.println("\n Unsatisfiable query: \n" +  query);

        // Should throw an exception
        query.applyProposal(new RemoveEmptyNodeProposalImpl(EMPTY_NODE_1, true));
        System.err.println("\n Failure: this query should have been declared as unsatisfiable: \n" +  query);
    }

    @Test
    public void testJoinLJ3() throws EmptyQueryException {

        IntermediateQuery query = generateJoinLJInitialQuery(Optional.of(TERM_FACTORY.getImmutableExpression(
                ExpressionOperation.IS_NULL, C)), B);

        /**
         * Expected query
         */
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(
                PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A), Y, generateURI1(B))),
                Optional.empty());

        expectedQueryBuilder.init(PROJECTION_ATOM, newRootNode);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
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

        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(
                PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A), Y, NULL)),
                Optional.empty());

        expectedQueryBuilder.init(PROJECTION_ATOM, newRootNode);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        expectedQueryBuilder.addChild(newRootNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, DATA_NODE_1);
        expectedQueryBuilder.addChild(joinNode, DATA_NODE_2);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery, EMPTY_NODE_1);
    }


    private static IntermediateQuery generateJoinLJInitialQuery(Optional<ImmutableExpression> joiningCondition,
                                                                Variable variableForBuildingY) {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A),
                        Y, generateURI1(variableForBuildingY))));
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(joiningCondition);
        queryBuilder.addChild(rootNode, joinNode);

        queryBuilder.addChild(joinNode, DATA_NODE_1);

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(joinNode, leftJoinNode);
        queryBuilder.addChild(leftJoinNode, DATA_NODE_2, LEFT);
        queryBuilder.addChild(leftJoinNode, EMPTY_NODE_1, RIGHT);

        return queryBuilder.build();
    }

    @Test(expected = EmptyQueryException.class)
    public void testFilter1() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A),
                        Y, generateURI1(B))),
                Optional.empty());
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        FilterNode filterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getImmutableExpression(
                ExpressionOperation.IS_NOT_NULL, C));
        queryBuilder.addChild(rootNode, filterNode);

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(filterNode, leftJoinNode);
        queryBuilder.addChild(leftJoinNode, DATA_NODE_2, LEFT);
        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(A, B, C));
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

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A),
                        Y, generateURI1(B))),
                Optional.empty());
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        FilterNode filterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getImmutableExpression(
                ExpressionOperation.IS_NULL, C));
        queryBuilder.addChild(rootNode, filterNode);

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(filterNode, leftJoinNode);
        queryBuilder.addChild(leftJoinNode, DATA_NODE_2, LEFT);
        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(A, B, C));
        queryBuilder.addChild(leftJoinNode, emptyNode, RIGHT);

        /**
         * Expected query
         */
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A),
                        Y, NULL)),
                Optional.empty());
        expectedQueryBuilder.init(PROJECTION_ATOM, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, DATA_NODE_2);

        optimizeAndCompare(queryBuilder.build(), expectedQueryBuilder.build(), emptyNode);
    }


    @Test
    public void testComplexTreeWithJoinCondition() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A),
                        Y, generateURI1(B))));
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        LeftJoinNode lj1 = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(rootNode, lj1);

        queryBuilder.addChild(lj1, DATA_NODE_2, LEFT);

        InnerJoinNode join = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getImmutableExpression(
                ExpressionOperation.IS_NOT_NULL, C));
        queryBuilder.addChild(lj1, join, RIGHT);

        queryBuilder.addChild(join, DATA_NODE_1);

        LeftJoinNode lj2 = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(join, lj2);
        queryBuilder.addChild(lj2, DATA_NODE_2.clone(), LEFT);
        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(A, C));
        queryBuilder.addChild(lj2, emptyNode, RIGHT);

        /**
         * Expected query
         */
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A),
                        Y, NULL)));
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

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A),
                        Y, generateURI1(B))));
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        LeftJoinNode lj1 = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getImmutableExpression(
                ExpressionOperation.EQ, B, B1));
        queryBuilder.addChild(rootNode, lj1);
        queryBuilder.addChild(lj1, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE_1, A, B1)), RIGHT);

        LeftJoinNode lj2 = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(lj1, lj2, LEFT);
        queryBuilder.addChild(lj2, DATA_NODE_2, LEFT);
        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(A, B));
                queryBuilder.addChild(lj2, emptyNode, RIGHT);


        /**
         * Expected query
         */
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A),
                        Y, NULL)));
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

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A),
                        Y, generateURI1(B))));
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        LeftJoinNode lj1 = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(rootNode, lj1);
        queryBuilder.addChild(lj1, DATA_NODE_1, RIGHT);

        LeftJoinNode lj2 = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(lj1, lj2, LEFT);
        queryBuilder.addChild(lj2, DATA_NODE_2, LEFT);
        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(A, B));
        queryBuilder.addChild(lj2, emptyNode, RIGHT);


        /**
         * Expected query
         */
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A),
                        Y, NULL)));
        expectedQueryBuilder.init(PROJECTION_ATOM, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, DATA_NODE_2);

        optimizeAndCompare(queryBuilder.build(), expectedQueryBuilder.build(), emptyNode);
    }

    @Test
    public void testLJRemovalDueToNull3() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(C),
                        Y, generateURI1(B))));
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        LeftJoinNode lj1 = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getImmutableExpression(
                ExpressionOperation.EQ, B, B1));
        queryBuilder.addChild(rootNode, lj1);
        queryBuilder.addChild(lj1, IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE_1, B1, C)), RIGHT);

        LeftJoinNode lj2 = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(lj1, lj2, LEFT);
        queryBuilder.addChild(lj2, DATA_NODE_2, LEFT);
        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(A, B));
        queryBuilder.addChild(lj2, emptyNode, RIGHT);


        /**
         * Expected query
         */
        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, NULL,
                        Y, NULL)));
        expectedQueryBuilder.init(PROJECTION_ATOM, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, DATA_NODE_2);

        optimizeAndCompare(queryBuilder.build(), expectedQueryBuilder.build(), emptyNode);
    }

//    @Test
//    public void testGroup1() throws EmptyQueryException {
//        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);
//
//        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
//                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A),
//                        Y, generateURI1(B))),
//                Optional.empty());
//        queryBuilder.init(PROJECTION_ATOM, rootNode);
//
//        GroupNode groupNode = new GroupNodeImpl(ImmutableList.of(A, B));
//        queryBuilder.addChild(rootNode, groupNode);
//
//        LeftJoinNode lj1 = IQ_FACTORY.createLeftJoinNode();
//        queryBuilder.addChild(groupNode, lj1);
//        queryBuilder.addChild(lj1, DATA_NODE_2, LEFT);
//        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(B));
//        queryBuilder.addChild(lj1, emptyNode, RIGHT);
//
//        /**
//         * Expected query
//         */
//        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
//        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
//                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A),
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
//        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
//                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A),
//                        Y, TERM_FACTORY.getImmutableExpression(ExpressionOperation.AVG, B))),
//                Optional.empty());
//        queryBuilder.init(PROJECTION_ATOM, rootNode);
//
//        GroupNode groupNode = new GroupNodeImpl(ImmutableList.of(A));
//        queryBuilder.addChild(rootNode, groupNode);
//
//        LeftJoinNode lj1 = IQ_FACTORY.createLeftJoinNode();
//        queryBuilder.addChild(groupNode, lj1);
//        queryBuilder.addChild(lj1, DATA_NODE_2, LEFT);
//        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(B));
//        queryBuilder.addChild(lj1, emptyNode, RIGHT);
//
//        /**
//         * Expected query
//         */
//        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
//        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
//                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A),
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
//        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
//                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
//                        X, TERM_FACTORY.getImmutableExpression(ExpressionOperation.AVG, A),
//                        Y, generateURI1(B))),
//                Optional.empty());
//        queryBuilder.init(PROJECTION_ATOM, rootNode);
//
//        GroupNode groupNode = new GroupNodeImpl(ImmutableList.of(B));
//        queryBuilder.addChild(rootNode, groupNode);
//
//        LeftJoinNode lj1 = IQ_FACTORY.createLeftJoinNode();
//        queryBuilder.addChild(groupNode, lj1);
//        queryBuilder.addChild(lj1, DATA_NODE_2, LEFT);
//        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(B));
//        queryBuilder.addChild(lj1, emptyNode, RIGHT);
//
//        /**
//         * Expected query
//         */
//        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(EMPTY_METADATA);
//        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
//                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
//                        X, TERM_FACTORY.getImmutableExpression(ExpressionOperation.AVG, A),
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
//        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
//                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A),
//                        Y, TERM_FACTORY.getImmutableExpression(ExpressionOperation.IS_NOT_NULL, B))),
//                Optional.empty());
//        queryBuilder.init(PROJECTION_ATOM, rootNode);
//
//        GroupNode groupNode = new GroupNodeImpl(ImmutableList.of(A));
//        queryBuilder.addChild(rootNode, groupNode);
//
//        LeftJoinNode lj1 = IQ_FACTORY.createLeftJoinNode();
//        queryBuilder.addChild(groupNode, lj1);
//        queryBuilder.addChild(lj1, DATA_NODE_2, LEFT);
//        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(B));
//        queryBuilder.addChild(lj1, emptyNode, RIGHT);
//
//        /**
//         * Expected query
//         */
//        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
//        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
//                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A),
//                        Y, TERM_FACTORY.getImmutableExpression(ExpressionOperation.IS_NOT_NULL, OBDAVocabulary.NULL))),
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
        return TERM_FACTORY.getImmutableFunctionalTerm(URI_PREDICATE, URI_TEMPLATE_STR_1, argument);
    }

}
