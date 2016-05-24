package it.unibz.inf.ontop.owlrefplatform.core.optimization;

import com.google.common.collect.*;
import it.unibz.inf.ontop.model.ImmutableExpression;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.PushDownBooleanExpressionProposal;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import it.unibz.inf.ontop.owlrefplatform.core.optimization.QueryNodeNavigationTools.*;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.PushDownBooleanExpressionProposalImpl;
import it.unibz.inf.ontop.pivotalrepr.unfolding.ProjectedVariableExtractionTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import static it.unibz.inf.ontop.owlrefplatform.core.optimization.QueryNodeNavigationTools.getDepthFirstNextNode;
import static it.unibz.inf.ontop.owlrefplatform.core.optimization.QueryNodeNavigationTools.getNextNodeAndQuery;
import static it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition.RIGHT;

/**
 * Extensible through hooks
 */
public class PushDownBooleanExpressionOptimizerImpl implements PushDownBooleanExpressionOptimizer {

    private enum ChildRequirement {
        /**
         * The projected variables of the child are independent of the expression
         */
        NO_NEED,
        /**
         * The child sub-tree can apply the condition to itself
         */
        CAN_TAKE,
        /**
         * The child requires the parent to apply the condition only if
         * no siblings does it.
         */
        KEEP_IT_IF_NO_SIBLING_TAKES_IT
    }

    /**
     * TODO: explain
     */
    private static class Recipient {
        public final Optional<QueryNode> indirectRecipientNode;
        public final Optional<JoinOrFilterNode> directRecipientNode;

        private Recipient(boolean isDirect, QueryNode recipientNode) {
            if (isDirect) {
                if (recipientNode instanceof JoinOrFilterNode) {
                    directRecipientNode = Optional.of((JoinOrFilterNode) recipientNode);
                    indirectRecipientNode = Optional.empty();
                }
                else {
                    throw new IllegalArgumentException("A direct recipient mode must be a JoinOrFilterNode");
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

    private Stream<Recipient> selectRecipients(IntermediateQuery query, JoinOrFilterNode providerNode,
                                                     ImmutableList<QueryNode> preSelectedChildren,
                                                     ImmutableExpression expression) {
        // Roots of the sub-trees
        ImmutableList.Builder<QueryNode> recipientChildSubTreeBuilder = ImmutableList.builder();
        // Non-final
        boolean requireASiblingToTakeIt = false;

        for(QueryNode child : preSelectedChildren) {
            switch(getChildRequirement(query, child, expression)) {
                case NO_NEED:
                    break;
                case CAN_TAKE:
                    recipientChildSubTreeBuilder.add(child);
                    break;
                case KEEP_IT_IF_NO_SIBLING_TAKES_IT:
                    requireASiblingToTakeIt = true;
                    break;
            }
        }
        ImmutableList<QueryNode> recipientChildSubTrees = recipientChildSubTreeBuilder.build();

        final boolean providerMustKeepExpression =
                (requireASiblingToTakeIt && recipientChildSubTrees.isEmpty());

        Stream<Recipient> childRecipientStream = recipientChildSubTrees.stream()
                .flatMap(child -> findRecipients(query, child, providerNode, expression));

        Stream<Recipient> providerRecipientStream = providerMustKeepExpression
                ? Stream.of(new Recipient(true, providerNode))
                : Stream.empty();

        return Stream.concat(providerRecipientStream, childRecipientStream)
                .distinct();
    }

    /**
     * TODO: explain
     */
    private ChildRequirement getChildRequirement(IntermediateQuery query, QueryNode child,
                                                 ImmutableExpression expression) {
        ImmutableSet<Variable> expressionVariables = expression.getVariables();

        ImmutableSet<Variable> projectedVariables = ProjectedVariableExtractionTools.extractProjectedVariables(
                query, child);

        /**
         * All the expression variables are projected: the current node is the recipient
         */
        if (projectedVariables.containsAll(expressionVariables)) {
            return ChildRequirement.CAN_TAKE;
        }
        /**
         * One part of the expression variables are projected: the expression must remain at the provider level
         * if no other child takes it
         */
        else if (expressionVariables.stream().anyMatch(projectedVariables::contains)) {
            return ChildRequirement.KEEP_IT_IF_NO_SIBLING_TAKES_IT;
        }
        /**
         * This data node is independent of the expression.
         */
        else {
            return ChildRequirement.NO_NEED;
        }
    }

    /**
     * TODO: explain
     */
    private Stream<Recipient> findRecipients(IntermediateQuery query, QueryNode currentNode, JoinOrFilterNode providerNode,
                                             ImmutableExpression expression) {

        if (currentNode instanceof CommutativeJoinOrFilterNode) {
            return findRecipientsInCommutativeJoinOrFilter(query, expression, providerNode,
                    (CommutativeJoinOrFilterNode) currentNode);
        }
        /**
         * May be recursive
         */
        else if (currentNode instanceof LeftJoinNode) {
            return findRecipientsInLeftJoin(query, expression, providerNode, (LeftJoinNode) currentNode);
        }
        else if (currentNode instanceof DataNode) {
            return findRecipientsInDataNode(query, expression, providerNode, (DataNode) currentNode);
        }
        /**
         * Recursive: find the recipients in the children
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
         * Default
         */
        else {
            return selectRecipientsUnexceptedNode(query, expression, providerNode, currentNode);
        }
    }

    private Stream<Recipient> findRecipientsInCommutativeJoinOrFilter(IntermediateQuery query,
                                                                      ImmutableExpression expression,
                                                                      JoinOrFilterNode providerNode,
                                                                      CommutativeJoinOrFilterNode currentNode) {
        return Stream.of(new Recipient(true, currentNode));
    }

    /**
     * TODO: should we also propagate the expression on the right for PERFORMANCE reasons only?
     * Relevant cases are probably very rare.
     */
    private Stream<Recipient> findRecipientsInLeftJoin(IntermediateQuery query,
                                                       ImmutableExpression expression,
                                                       JoinOrFilterNode providerNode,
                                                       LeftJoinNode currentNode) {
        QueryNode leftChild = query.getChild(currentNode, LEFT)
                .orElseThrow(() -> new IllegalStateException("The LJ node was expected to have a left child"));

        ImmutableSet<Variable> projectedVariablesOnTheLeft = ProjectedVariableExtractionTools.extractProjectedVariables(
                query, leftChild);

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
                    .map(p -> p == providerNode)
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


    private Stream<Recipient> findRecipientsInDataNode(IntermediateQuery query, ImmutableExpression expression,
                                                       JoinOrFilterNode providerNode, DataNode currentNode) {
        if (query.getParent(currentNode)
                .map(p -> p == providerNode)
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
