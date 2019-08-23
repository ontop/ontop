package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.iq.optimizer.PushDownBooleanExpressionOptimizer;
import it.unibz.inf.ontop.iq.optimizer.impl.QueryNodeNavigationTools.NextNodeAndQuery;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.iq.proposal.PushDownBooleanExpressionProposal;
import it.unibz.inf.ontop.iq.proposal.impl.PushDownBooleanExpressionProposalImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.inject.Inject;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.iq.optimizer.impl.QueryNodeNavigationTools.getDepthFirstNextNode;
import static it.unibz.inf.ontop.iq.optimizer.impl.QueryNodeNavigationTools.getNextNodeAndQuery;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.RIGHT;

/**
 * Extensible through hooks
 */
public class PushDownBooleanExpressionOptimizerImpl implements PushDownBooleanExpressionOptimizer {

    @Inject
    private PushDownBooleanExpressionOptimizerImpl() {
    }

    protected static class Push {
        public final Recipient recipient;
        public final ImmutableExpression expression;

        protected Push(Recipient recipient, ImmutableExpression expression) {
            this.recipient = recipient;
            this.expression = expression;
        }
    }

    /**
     * A recipient node n receives a boolean expression e being propagated down,
     * only if all variables of e are projected out by the subtree rooted in n.
     * n may be either:
     * - a direct recipient of e, if it natively supports boolean expressions (JoinOrFilterNode)
     * - an indirect recipient of e,
     * and a parent filter node for n will be created as the direct recipient of e.
     * <p>
     * Note that a lefJoinNode may be either a direct recipient node (if it is also the provider node),
     * or an indirect one
     */
    protected static class Recipient {
        public final Optional<JoinOrFilterNode> directRecipientNode;
        public final Optional<QueryNode> indirectRecipientNode;

        private Recipient(LeftJoinNode root, boolean isDirectRecipient) {
            if (isDirectRecipient) {
                directRecipientNode = Optional.of(root);
                indirectRecipientNode = Optional.empty();
            } else {
                directRecipientNode = Optional.empty();
                indirectRecipientNode = Optional.of(root);
            }
        }

        public Recipient(QueryNode root) {
            if (root instanceof CommutativeJoinOrFilterNode) {
                directRecipientNode = Optional.of((JoinOrFilterNode) root);
                indirectRecipientNode = Optional.empty();
            } else {
                directRecipientNode = Optional.empty();
                indirectRecipientNode = Optional.of(root);
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


    protected Push getProviderAsPush(QueryNode queryNode, ImmutableExpression expression) {
        if (queryNode instanceof CommutativeJoinOrFilterNode) {
            return new Push(new Recipient(queryNode), expression);
        }
        if (queryNode instanceof LeftJoinNode) {
            return new Push(new Recipient((LeftJoinNode) queryNode, true), expression);
        }
        throw new IllegalStateException("Only Join or Filter Nodes may provide a boolean expression");
    }


    /**
     * High-level method
     */
    @Override
    public IntermediateQuery optimize(final IntermediateQuery initialQuery) {
        return pushDownExpressions(initialQuery);
    }

    /**
     * Tries to optimize all the JoinOrFilterNodes, ONE BY ONE.
     * Navigates in a top-down fashion.
     */
    private IntermediateQuery pushDownExpressions(final IntermediateQuery initialQuery) {
        // Non-final
        Optional<QueryNode> optionalCurrentNode = initialQuery.getFirstChild(initialQuery.getRootNode());

        // Non-final
        IntermediateQuery currentQuery = initialQuery;

        while (optionalCurrentNode.isPresent()) {
            final QueryNode currentNode = optionalCurrentNode.get();

            /*
             * InnerJoinNode, LeftJoinNode, FilterNode or some extensions
             */
            if (currentNode instanceof JoinOrFilterNode) {
                NextNodeAndQuery nextNodeAndQuery = optimizeJoinOrFilter(currentQuery, (JoinOrFilterNode) currentNode);
                optionalCurrentNode = nextNodeAndQuery.getOptionalNextNode();
                currentQuery = nextNodeAndQuery.getNextQuery();
            } else {
                optionalCurrentNode = getDepthFirstNextNode(currentQuery, currentNode);
            }
        }
        return currentQuery;
    }


    /**
     * Tries to optimize one JoinOrFilterNode.
     * Returns information for the continuing the navigation in the possibly new IntermediateQuery.
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

            return getNextNodeAndQuery(currentQuery, results);
        } else {
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
            return makeProposalForUnexpectedFocusNode(currentQuery, focusNode);
        }
    }

    /**
     * Hook (useful for extensions)
     * <p>
     * By default, does not make a proposal
     */
    protected Optional<PushDownBooleanExpressionProposal> makeProposalForUnexpectedFocusNode(IntermediateQuery currentQuery,
                                                                                             JoinOrFilterNode focusNode) {
        return Optional.empty();
    }


    /**
     * providerNode: provides the boolean expressions
     */
    private Optional<PushDownBooleanExpressionProposal> makeProposalForJoinOrFilterNode(
            IntermediateQuery currentQuery, JoinOrFilterNode providerNode, ImmutableList<QueryNode> preSelectedChildren) {

        /*
         * If there is no boolean expression, no proposal
         */
        Optional<ImmutableExpression> optionalNestedExpression = providerNode.getOptionalFilterCondition();

        if (!optionalNestedExpression.isPresent()) {
            return Optional.empty();
        }

        /*
         * Decomposes the boolean expressions as much as possible (conjunction)
         */
        ImmutableSet<ImmutableExpression> booleanExpressions = optionalNestedExpression.get().flattenAND();

        /*
         * For each boolean expression, looks for recipients.
         */
        ImmutableMultimap<Recipient, ImmutableExpression> recipientMap = booleanExpressions.stream()
                .flatMap(ex -> selectPushes(currentQuery, providerNode, preSelectedChildren, ex))
                .collect(ImmutableCollectors.toMultimap(
                        p -> p.recipient,
                        p -> p.expression));

        return buildProposal(providerNode, recipientMap);
    }


    /**
     * For an expression e, a (filter or join) node p providing e,
     * and a list S of preselected candidate subtrees for propagation,
     * decides whether e should remain attached to p,
     * and/or which subtree in S e should be propagated to.
     * Also selects the recipient for e in each of these selected subtrees.
     * <p>
     * If e should remain attached to p,
     * then p is simply returned among the recipients (not that p may or may not be the only recipient)
     *
     * TODO: update the comment
     */
    private Stream<Push> selectPushes(IntermediateQuery query, JoinOrFilterNode providerNode,
                                           ImmutableList<QueryNode> candidateSubtreeRoots,
                                           ImmutableExpression expression) {
        ImmutableList.Builder<QueryNode> selectedSubtreeRootsBuilder = ImmutableList.builder();

        ImmutableSet<Variable> expressionVariables = expression.getVariables();
        for (QueryNode candidateSubtreeRoot : candidateSubtreeRoots) {
            ImmutableSet<Variable> projectedVariables = query.getVariables(candidateSubtreeRoot);
            if (projectedVariables.containsAll(expressionVariables)) {
                selectedSubtreeRootsBuilder.add(candidateSubtreeRoot);
            }
        }

        ImmutableList<QueryNode> selectedSubtreeRoots = selectedSubtreeRootsBuilder.build();
        /*
         * If no candidate subtree has been selected,
         * keep the expression at the provider's level (otherwise the expression will be dropped)
         */
        if (selectedSubtreeRoots.isEmpty()) {
            return Stream.of(getProviderAsPush(providerNode, expression));
        }

        return selectedSubtreeRoots.stream()
                .flatMap(subtreeRoot -> findPushesInSelectedSubtree(query, subtreeRoot, providerNode, expression))
                .distinct();
    }



    /**
     * TODO: explain
     */
    protected Stream<Push> findPushesInSelectedSubtree(IntermediateQuery query, QueryNode subtreeRoot,
                                                            JoinOrFilterNode providerNode,
                                                            ImmutableExpression expression) {

        if (subtreeRoot instanceof CommutativeJoinOrFilterNode) {
            return findPushesInCommutativeJoinOrFilterRootedSubtree((CommutativeJoinOrFilterNode) subtreeRoot, expression);
        }

        if (subtreeRoot instanceof LeafIQTree) {
            return Stream.of(getProviderAsPush(providerNode, expression));
        }
        if (subtreeRoot instanceof LeftJoinNode) {
            return Stream.of(findPushInLeftJoinRootedSubtree(query, expression, providerNode, (LeftJoinNode) subtreeRoot));
        }
        if (subtreeRoot instanceof UnionNode) {
            return findPushesInUnionNodeRootedSubtree(query, expression, (UnionNode) subtreeRoot);
        }
        if (subtreeRoot instanceof ConstructionNode) {
            return Stream.of(findPushInConstructionNodeRootedSubtree(query, expression, (ConstructionNode) subtreeRoot));
        }
        /*
         * for GroupNodes only (not supported yet) ?
         */
        else {
            return findPushesInUnexpectedNodeRootedSubtree(query, expression, providerNode, subtreeRoot);
        }
    }

    private Stream<Push> findPushesInUnionNodeRootedSubtree(IntermediateQuery query, ImmutableExpression expression, UnionNode subtreeRoot) {
        ImmutableList<QueryNode> children = query.getChildren(subtreeRoot);
        if (children.isEmpty()) {
            throw new IllegalStateException("Children expected for " + subtreeRoot);
        }
        return children.stream()
                .map(c -> new Push(new Recipient(c), expression));
    }

    private Push findPushInConstructionNodeRootedSubtree(IntermediateQuery query, ImmutableExpression expression, ConstructionNode subtreeRoot) {
        ImmutableList<QueryNode> children = query.getChildren(subtreeRoot);
        if (children.size() != 1) {
            throw new IllegalStateException("Exactly one child expected for " + subtreeRoot);
        }
        return new Push(
                new Recipient(children.get(0)),
                subtreeRoot.getSubstitution().applyToBooleanExpression(expression));
    }

    private Stream<Push> findPushesInCommutativeJoinOrFilterRootedSubtree(CommutativeJoinOrFilterNode currentNode,
                                                                          ImmutableExpression expression) {
        return Stream.of(new Push(new Recipient(currentNode), expression));
    }

    /**
     * This methods only propagates down an expression e coming from a provider parent p of a LJ node n,
     * and not the joining condition of n.
     * <p>
     * e is not added to the joining condition of n,
     * but may be propagated down d irectly to the left subtree of n
     */
    private Push findPushInLeftJoinRootedSubtree(IntermediateQuery query,
                                                 ImmutableExpression expression,
                                                 JoinOrFilterNode providerNode,
                                                 LeftJoinNode currentNode) {
        QueryNode leftChild = query.getChild(currentNode, LEFT)
                .orElseThrow(() -> new MinorOntopInternalBugException("Was expecting a left child"));

        return query.getVariables(leftChild).containsAll(expression.getVariables())
                ? new Push(new Recipient(leftChild), expression)
                : getProviderAsPush(providerNode, expression);
    }

    /**
     * Hook
     * <p>
     * By default, does not push down (no optimization)
     */
    protected Stream<Push> findPushesInUnexpectedNodeRootedSubtree(IntermediateQuery currentQuery,
                                                                        ImmutableExpression expression,
                                                                        JoinOrFilterNode providerNode, QueryNode currentNode) {
        return Stream.of(getProviderAsPush(providerNode, expression));
    }

    /**
     * Builds the PushDownBooleanExpressionProposal.
     */
    private Optional<PushDownBooleanExpressionProposal> buildProposal(
            JoinOrFilterNode providerNode, ImmutableMultimap<Recipient, ImmutableExpression> recipientMap) {

        ImmutableCollection<Map.Entry<Recipient, ImmutableExpression>> recipientEntries = recipientMap.entries();
        /*
         * Collect new direct recipients nodes for each expression,
         * filtering out provider nodes, and therefore also LeftJoinNodes.
         */
        ImmutableMultimap<CommutativeJoinOrFilterNode, ImmutableExpression> directRecipientNodes = recipientEntries.stream()
                .filter(e -> e.getKey().directRecipientNode.isPresent())
                .filter(e -> e.getKey().directRecipientNode.get() != providerNode)
                .map(e -> new AbstractMap.SimpleEntry<>(
                        (CommutativeJoinOrFilterNode) e.getKey().directRecipientNode.get(), e.getValue()))
                .collect(ImmutableCollectors.toMultimap());
        /*
         * Collect indirect recipient nodes
         */
        ImmutableMultimap<QueryNode, ImmutableExpression> indirectRecipientNodes = recipientEntries.stream()
                .filter(e -> e.getKey().indirectRecipientNode.isPresent())
                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey().indirectRecipientNode.get(), e.getValue()))
                .collect(ImmutableCollectors.toMultimap());

        if (directRecipientNodes.isEmpty() && indirectRecipientNodes.isEmpty()) {
            return Optional.empty();
        } else {
            ImmutableList<ImmutableExpression> expressionsToKeep = recipientEntries.stream()
                    .filter(e -> e.getKey().directRecipientNode.isPresent())
                    .filter(e -> e.getKey().directRecipientNode.get() == providerNode)
                    .map(Map.Entry::getValue)
                    .collect(ImmutableCollectors.toList());

            return Optional.of(new PushDownBooleanExpressionProposalImpl(providerNode, directRecipientNodes,
                    indirectRecipientNodes, expressionsToKeep));
        }
    }
}
