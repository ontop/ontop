package it.unibz.inf.ontop.owlrefplatform.core.optimization;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.ImmutableExpression;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.QueryNodeNavigationTools.NextNodeAndQuery;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.IllegalTreeException;
import it.unibz.inf.ontop.pivotalrepr.impl.IllegalTreeUpdateException;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.PushDownBooleanExpressionProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.PushDownBooleanExpressionProposalImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.owlrefplatform.core.optimization.QueryNodeNavigationTools.getDepthFirstNextNode;
import static it.unibz.inf.ontop.owlrefplatform.core.optimization.QueryNodeNavigationTools.getNextNodeAndQuery;
import static it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition.RIGHT;

/**
 * Extensible through hooks
 */
public class PushDownBooleanExpressionOptimizerImpl implements PushDownBooleanExpressionOptimizer {

    /**
     * A recipient node n receives a boolean expression e being propagated down
     * (only if all variables of e are projected out by the subtree rooted in n).
     * n is a direct recipient if it natively supports (non conditional) boolean expressions,
     * i.e. if it is a commutative join or filter node.
     * Otherwise n is an indirect recipient, and a parent filter node for n will be created to support e.
     */
    private static class Recipient {
        public final Optional<QueryNode> indirectRecipientNode;
        public final Optional<CommutativeJoinOrFilterNode> directRecipientNode;

        private Recipient(boolean isDirect, QueryNode recipientNode) {
            if (isDirect) {
                if (recipientNode instanceof CommutativeJoinOrFilterNode) {
                    directRecipientNode = Optional.of((CommutativeJoinOrFilterNode) recipientNode);
                    indirectRecipientNode = Optional.empty();
                }
                else {
                    throw new IllegalArgumentException("A direct recipient mode must be a CommutativeJoinOrFilterNode");
                }
            }
            else {
                indirectRecipientNode = Optional.of(recipientNode);
                directRecipientNode = Optional.empty();
            }
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Recipient recipient = (Recipient) o;

            if (!indirectRecipientNode.equals(recipient.indirectRecipientNode)) return false;
            return directRecipientNode.equals(recipient.directRecipientNode);

        }
        @Override
        public int hashCode() {
            int result = indirectRecipientNode.hashCode();
            result = 31 * result + directRecipientNode.hashCode();
            return result;
        }
    }


    /**
     * High-level method
     */
    @Override
    public IntermediateQuery optimize(final IntermediateQuery initialQuery) {
        return pushDownExpressions(initialQuery);
    }

    /**
     *
     * Tries to optimize all the JoinOrFilterNodes, ONE BY ONE.
     * Navigates in a top-down fashion.
     *
     */
    private IntermediateQuery pushDownExpressions(final IntermediateQuery initialQuery) {
        // Non-final
        Optional<QueryNode> optionalCurrentNode = initialQuery.getFirstChild(initialQuery.getRootConstructionNode());

        // Non-final
        IntermediateQuery currentQuery = initialQuery;

        while (optionalCurrentNode.isPresent()) {
            final QueryNode currentNode = optionalCurrentNode.get();

            /**
             * InnerJoinNode, LeftJoinNode, FilterNode or some extensions
             */
            if (currentNode instanceof JoinOrFilterNode) {
                NextNodeAndQuery nextNodeAndQuery = optimizeJoinOrFilter(currentQuery, (JoinOrFilterNode) currentNode);
                optionalCurrentNode = nextNodeAndQuery.getOptionalNextNode();
                currentQuery = nextNodeAndQuery.getNextQuery();
            }
            else {
                optionalCurrentNode = getDepthFirstNextNode(currentQuery, currentNode);
            }
        }
        return currentQuery;
    }


    /**
     * Tries to optimize one JoinOrFilterNode.
     * Returns information for the continuing the navigation in the possibly new IntermediateQuery.
     *
     */
    private NextNodeAndQuery optimizeJoinOrFilter(IntermediateQuery currentQuery, JoinOrFilterNode currentNode) {

        /**
         * Tries to build a PushDownBooleanExpressionProposal for the current node
         */
        Optional<PushDownBooleanExpressionProposal> optionalProposal = makeProposal(
                currentQuery, currentNode);

        /**
         * Applies it and extracts from the results the necessary information
         * for continuing the navigation
         */
        if (optionalProposal.isPresent()) {
            PushDownBooleanExpressionProposal proposal = optionalProposal.get();

            // Applies the proposal and casts the results
            NodeCentricOptimizationResults<JoinOrFilterNode> results;
            try {
                results = currentQuery.applyProposal(proposal);
            } catch (EmptyQueryException e) {
                throw new IllegalStateException("Unexpected empty query exception while pushing down boolean expressions");
            }

            return getNextNodeAndQuery(results);
        }
        else {
            return new NextNodeAndQuery(getDepthFirstNextNode(currentQuery, currentNode), currentQuery);
        }
    }

    /**
     * Routing method
     */
    private Optional<PushDownBooleanExpressionProposal> makeProposal(
            IntermediateQuery currentQuery, JoinOrFilterNode focusNode) {

        /**
         * Commutative joins and filters
         */
        if (focusNode instanceof CommutativeJoinOrFilterNode) {
            return makeProposalForJoinOrFilterNode(currentQuery, focusNode, currentQuery.getChildren(focusNode));
        }
        /**
         * Left join: can only push its conditions on the right side
         */
        else if (focusNode instanceof LeftJoinNode) {
            return makeProposalForJoinOrFilterNode(currentQuery, focusNode,
                    ImmutableList.of(currentQuery.getChild(focusNode, RIGHT)
                            .orElseThrow(() -> new IllegalStateException("Was not excepting to find a " +
                                    "LJ without a right element"))));
        }
        /**
         * Other node (useful for extension)
         */
        else {
            return makeProposalUnexpectedFocusNode(currentQuery, focusNode);
        }
    }

    /**
     * Hook (useful for extensions)
     *
     * By default, does not make a proposal
     */
    protected Optional<PushDownBooleanExpressionProposal> makeProposalUnexpectedFocusNode(IntermediateQuery currentQuery,
                                                                                          JoinOrFilterNode focusNode) {
        return Optional.empty();
    }


    /**
     * providerNode: provides the boolean expressions
     */
    private Optional<PushDownBooleanExpressionProposal> makeProposalForJoinOrFilterNode(
            IntermediateQuery currentQuery, JoinOrFilterNode providerNode, ImmutableList<QueryNode> preSelectedChildren) {

        /**
         * If there is no boolean expression, no proposal
         */
        Optional<ImmutableExpression> optionalNestedExpression = providerNode.getOptionalFilterCondition();

        if (!optionalNestedExpression.isPresent()) {
            return Optional.empty();
        }

        /**
         * Decomposes the boolean expressions as much as possible (conjunction)
         */
        ImmutableSet<ImmutableExpression> booleanExpressions = optionalNestedExpression.get().flattenAND();

        /**
         * For each boolean expression, looks for recipient nodes.
         *
         */
        ImmutableMultimap<Recipient, ImmutableExpression> recipientMap = booleanExpressions.stream()
                .flatMap(ex -> selectRecipients(currentQuery, providerNode, preSelectedChildren, ex)
                        .map(recipient -> new SimpleEntry<Recipient, ImmutableExpression>(recipient, ex)))
                .collect(ImmutableCollectors.toMultimap());

        return buildProposal(providerNode, recipientMap);
    }


    /**
     * For a given boolean expression e and (filter or join) node n providing this expression,
     * decides whether e should remain attached to n,
     * and/or to which children subtree(s) of n this expression should be propagated.
     * Also selects/creates the recipient node(s) for e in each recipient subtree.
     */
    private Stream<Recipient> selectRecipients(IntermediateQuery query, JoinOrFilterNode providerNode,
                                                     ImmutableList<QueryNode> preSelectedChildren,
                                                     ImmutableExpression expression) {
        // Roots of the sub-trees
        ImmutableList.Builder<QueryNode> recipientChildSubTreeBuilder = ImmutableList.builder();
        // Non-final
        boolean mustKeepInTree = false;
        boolean mustKeepAtParentLevel = false;


        for(QueryNode child : preSelectedChildren) {
            ImmutableSet<Variable> expressionVariables = expression.getVariables();
            ImmutableSet<Variable> projectedVariables = query.getVariables(child);
            /**
             * If all the expression's variables are projected out by the child's subtree,
             * then the child's subtree is a recipient
             */
            if (projectedVariables.containsAll(expressionVariables)) {
                recipientChildSubTreeBuilder.add(child);
                mustKeepInTree = true;
            }
            /**
             * If only some of the the expression variables are projected out by the child's subtree,
             * then the child's subtree is not a recipient, and the boolean expression must remain at the parent level
             * (although it may also be duplicated and propagated down in another child subtree)
             */
            else if (expressionVariables.stream().anyMatch(projectedVariables::contains)){
                mustKeepAtParentLevel=true;
            }
            /**
             * Default: if none of the expression's variables is projected out by the child's subtree,
             * then the child's subtree is not a recipient.
             */
        }

        Stream<Recipient> recipients =  mustKeepAtParentLevel
                ? Stream.of(new Recipient(true, providerNode))
                : Stream.empty();

        ImmutableList<QueryNode> recipientChildSubTrees = recipientChildSubTreeBuilder.build();

        Stream<Recipient> childRecipientStream = recipientChildSubTrees.stream()
                .flatMap(child -> findRecipients(query, child, providerNode, expression));

            return Stream.concat(recipients, childRecipientStream).distinct();

    }

    /**
     * TODO: explain
     */
    private Stream<Recipient> findRecipients(IntermediateQuery query, QueryNode currentNode, JoinOrFilterNode providerNode,
                                             ImmutableExpression expression) {

        if (currentNode instanceof CommutativeJoinOrFilterNode) {
            return findRecipientsInCommutativeJoinOrFilterRootedSubtree(query, expression, providerNode,
                    (CommutativeJoinOrFilterNode) currentNode);
        }
        /**
         * May be recursive
         */
        else if (currentNode instanceof LeftJoinNode) {
            return findRecipientsInLeftJoinRootedSubtree(query, expression, providerNode, (LeftJoinNode) currentNode);
        }
        else if (currentNode instanceof DataNode) {
            return findRecipientsInDataNodeRootedSubtree(query, expression, providerNode, (DataNode) currentNode);
        }
        /**
         * Recursive:
         * for ConstructionNodes and UnionNodes,
         * find the recipients nodes in the children subtree
         */
        else if ((currentNode instanceof ConstructionNode)
                || (currentNode instanceof UnionNode)) {

            ImmutableList<QueryNode> children = query.getChildren(currentNode);
            if (children.isEmpty()) {
                throw new IllegalStateException("Children expected for " + currentNode);
            }
            // Recursion
            return children.stream()
                    .flatMap(c -> findRecipients(query, c, providerNode, expression));
        }
        /**
         * Limit case (boolean expressions without variables)
         */
        else if (currentNode instanceof TrueNode) {
            if(expression.getVariables().isEmpty()){
                return findRecipientsInTrueNodeRootedSubtree(query, expression, providerNode, (TrueNode) currentNode);
            }
            throw new IllegalTreeUpdateException("a TrueNode does not project out variables");
        }
        else if(currentNode instanceof EmptyNode) {
            throw new IllegalTreeException("This query should not contain an EmptyNode");
        }
        /**
         * for GroupNodes only (not supported yet) ?
         */
        else{
            return selectRecipientsUnexceptedNode(query, expression, providerNode, currentNode)
        }
    }

    private Stream<Recipient> findRecipientsInCommutativeJoinOrFilterRootedSubtree(IntermediateQuery query,
                                                                                   ImmutableExpression expression,
                                                                                   JoinOrFilterNode providerNode,
                                                                                   CommutativeJoinOrFilterNode currentNode) {
        return Stream.of(new Recipient(true, currentNode));
    }

    /**
     * Note that this methods only propagates down expressions coming from a parent of the LJ node,
     * and not the joining condition of the LJ.
     *
     * Propagation to the right cannot be performed in all cases (e.g. if the expression is of the form ?x = NULL).
     * So by default, only propagation to the left is implemented.
     */
    private Stream<Recipient> findRecipientsInLeftJoinRootedSubtree(IntermediateQuery query,
                                                                    ImmutableExpression expression,
                                                                    JoinOrFilterNode providerNode,
                                                                    LeftJoinNode currentNode) {
        QueryNode leftChild = query.getChild(currentNode, LEFT)
                .orElseThrow(() -> new IllegalStateException("The LJ node was expected to have a left child"));

        ImmutableSet<Variable> projectedVariablesOnTheLeft = query.getVariables(leftChild);

        /**
         * Can propagate the expression to the left child
         */
        if (projectedVariablesOnTheLeft.containsAll(expression.getVariables())) {
            // Recursion on findRecipients()
            return findRecipients(query, leftChild, providerNode, expression);
        }
        /**
         * Tries to reuse the filter node above if it is its parent
         */
        else if (query.getParent(currentNode)
                    .filter(p -> p == providerNode)
                    .isPresent()) {
            /**
             * Keep the expression in the provider node
             */
            return Stream.of(new Recipient(true, providerNode));
        }
        else {
            /**
             * Inserts a Filter node above the LJ
             */
            return Stream.of(new Recipient(false, currentNode));

        }


    }


    private Stream<Recipient> findRecipientsInDataNodeRootedSubtree(IntermediateQuery query, ImmutableExpression expression,
                                                                    JoinOrFilterNode providerNode, DataNode currentNode) {
        if (query.getParent(currentNode)
                .filter(p -> p == providerNode)
                .isPresent()) {
            /**
             * Keep the expression in the provider node
             */
            return Stream.of(new Recipient(true, providerNode));
        }
        else {
            /**
             * Ask for a filter node to be created above this data node
             */
            return Stream.of(new Recipient(false, currentNode));
        }
    }

    private Stream<Recipient> findRecipientsInTrueNodeRootedSubtree(IntermediateQuery query, ImmutableExpression expression,
                                                                    JoinOrFilterNode providerNode, TrueNode currentNode) {
        if (query.getParent(currentNode)
                .filter(p -> p == providerNode)
                .isPresent()) {
            /**
             * Keep the expression in the provider node
             */
            return Stream.of(new Recipient(true, providerNode));
        }
        else {
            /**
             * Ask for a filter node to be created above this data node
             */
            return Stream.of(new Recipient(false, currentNode));
        }
    }

    /**
     * Hook
     *
     * By default, does not push down (no optimization)
     */
    protected Stream<Recipient> selectRecipientsUnexceptedNode(IntermediateQuery currentQuery,
                                                               ImmutableExpression expression,
                                                               JoinOrFilterNode providerNode, QueryNode currentNode) {
        return Stream.of(new Recipient(true, providerNode));
    }

    /**
     * Builds the PushDownBooleanExpressionProposal.
     */
    private Optional<PushDownBooleanExpressionProposal> buildProposal(
            JoinOrFilterNode providerNode, ImmutableMultimap<Recipient, ImmutableExpression> recipientMap) {

        ImmutableCollection<Map.Entry<Recipient, ImmutableExpression>> recipientEntries = recipientMap.entries();

        ImmutableMultimap<JoinOrFilterNode, ImmutableExpression> directRecipients = recipientEntries.stream()
                .filter(e -> e.getKey().directRecipientNode.isPresent())
                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey().directRecipientNode.get(), e.getValue()))
                .filter(e -> e.getKey() != providerNode)
                .collect(ImmutableCollectors.toMultimap());

        ImmutableMultimap<QueryNode, ImmutableExpression> childOfFilterNodesToCreate = recipientEntries.stream()
                .filter(e -> e.getKey().indirectRecipientNode.isPresent())
                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey().indirectRecipientNode.get(), e.getValue()))
                .collect(ImmutableCollectors.toMultimap());

        if (directRecipients.isEmpty() && childOfFilterNodesToCreate.isEmpty()) {
            return Optional.empty();
        }
        else {
            ImmutableList<ImmutableExpression> expressionsToKeep = recipientEntries.stream()
                    .filter(e -> e.getKey().directRecipientNode.isPresent())
                    .filter(e -> e.getKey().directRecipientNode.get() == providerNode)
                    .map(Map.Entry::getValue)
                    .collect(ImmutableCollectors.toList());

            return Optional.of(new PushDownBooleanExpressionProposalImpl(providerNode, directRecipients,
                    childOfFilterNodesToCreate, expressionsToKeep));
        }
    }

}
