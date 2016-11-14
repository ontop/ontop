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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.owlrefplatform.core.optimization.QueryNodeNavigationTools.getDepthFirstNextNode;
import static it.unibz.inf.ontop.owlrefplatform.core.optimization.QueryNodeNavigationTools.getNextNodeAndQuery;
import static it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition.RIGHT;

/**
 * Extensible through hooks
 */
public class PushDownBooleanExpressionOptimizerImpl implements PushDownBooleanExpressionOptimizer {

    /**
     * A recipient node n receives a boolean expression e being propagated down,
     * only if all variables of e are projected out by the subtree rooted in n.
     * n may be either:
     * - a direct recipient of e, if it natively supports boolean expressions (JoinOrFilterNode)
     * _ an indirect recipient of e,
     * and a parent filter node for n will be created as the direct recipient of e.
     *
     * If no recipient node (direct or indirect) is provided,
     * then the recipient node is also the provider of the expression
     *
     */
    private static class Recipient {
        public final Optional<JoinOrFilterNode> directRecipientNode;
        public final Optional<QueryNode> indirectRecipientNode;

        private Recipient(QueryNode root) {
            if(root instanceof  CommutativeJoinOrFilterNode){
                directRecipientNode = Optional.of((JoinOrFilterNode) root);
                indirectRecipientNode = Optional.empty();
            }else{
                indirectRecipientNode = Optional.of(root);
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

    private static class SubtreeRequirements {
        public final boolean acceptsExpression;
        public final boolean keepExpressionAtProviderLevel;

        private SubtreeRequirements(boolean acceptsExpression, boolean keepExpressionAtProviderLevel) {
            this.acceptsExpression = acceptsExpression;
            this.keepExpressionAtProviderLevel=keepExpressionAtProviderLevel;
        }
    }

    /**
     * Takes a node p providing an expression e,
     * and a child n of p.
     * Determines:
     * - whether the subtree rooted in n can propagate e
     * - whether n (by itself) enforces that and p must keep e
     * (note that this does not prevent e to be also duplicated down)
     */
    private SubtreeRequirements getSubtreeRequirements(IntermediateQuery query, QueryNode root, ImmutableSet<Variable> expressionVariables, ImmutableSet<Variable> projectedVariables){
       /**
        * If all of e's variables are projected out by the subtree rooted in n,
        * then it is a propagating subtree
        */
       if(projectedVariables.containsAll(expressionVariables)){
           /**
            * Specific case of a LeftJoinNode rooted subtree
            */
           if(root instanceof LeftJoinNode){
               return getLeftJoinRootedSubtreeRequirements((LeftJoinNode) root, query, expressionVariables, projectedVariables);
           }
           /**
            * By default, no requirement on p
            */
           return new SubtreeRequirements(true, false);
       }
       /**
        * If only some of e's variables are projected out by the subtree rooted in n,
        * then it is not a propagating subtree, and there is no requirement on p
        */
        if(expressionVariables.stream().anyMatch(projectedVariables::contains)){
                return new SubtreeRequirements(false, false);
        }
        /**
         * Otherwise, the subtree rooted in n is not propagating,
         * and there is no requirement on p
         */
        return new SubtreeRequirements(false,false);
    }


    /**
     * Takes a node p providing an expression e,
     * and a leftJoin child n of p,
     * such that the subtree rooted in n projects out al variables of e.
     * Determines whether n (by itself) enforces that and p must keep e
     * (note that this does not prevent e to be also duplicated down)
     */
    private SubtreeRequirements getLeftJoinRootedSubtreeRequirements(LeftJoinNode root, IntermediateQuery query, ImmutableSet<Variable> expressionVariables, ImmutableSet<Variable> projectedVariables) {
        /**
         * If the right child of n projects out all variables of e,
         * then p must keep the expression,
         */
        QueryNode rightChild = query.getChild(root, RIGHT)
                .orElseThrow(() -> new IllegalTreeException("a LeftJoinNode is expected to have a right child"));
        if (query.getVariables(rightChild).containsAll(expressionVariables)){
            return new SubtreeRequirements(true, true);
        }
        /**
         * Otherwise, no requirement on p
         */
        return new SubtreeRequirements(true, false);

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
         * For each boolean expression, looks for recipients.
         */
        ImmutableMultimap<Recipient, ImmutableExpression> recipientMap = booleanExpressions.stream()
                .flatMap(ex -> selectRecipients(currentQuery, providerNode, preSelectedChildren, ex)
                        .map(recipient -> new SimpleEntry<>(recipient, ex)))
                .collect(ImmutableCollectors.toMultimap());

        return buildProposal(providerNode, recipientMap);
    }


    /**
     * For an expression e, a (filter or join) node p providing e,
     * and a list S of preselected candidate subtrees for propagation,
     * decides whether e should remain attached to p,
     * and/or which subtree in S e should be propagated to.
     * Also selects the recipient for e in each of these selected subtrees.
     *
     * If e should remain attached to p,
     * then p is simply returned among the recipients (not that p may or may not be the only recipient)
     *
     */
    private Stream<Recipient> selectRecipients(IntermediateQuery query, JoinOrFilterNode providerNode,
                                               ImmutableList<QueryNode> candidateSubtreeRoots,
                                               ImmutableExpression expression) {
       ImmutableList.Builder<QueryNode> selectedSubtreeRootsBuilder = ImmutableList.builder();
        // Non-final
        boolean mustKeepAtProviderLevel = false;

        for(QueryNode candidateSubtreeRoot : candidateSubtreeRoots) {
            ImmutableSet<Variable> expressionVariables = expression.getVariables();
            ImmutableSet<Variable> projectedVariables = query.getVariables(candidateSubtreeRoot);

            SubtreeRequirements subtreeRequirements = getSubtreeRequirements(query, candidateSubtreeRoot,
                    expressionVariables, projectedVariables);
            /**
             * If at least one subtree requires it,
             * the boolean expression must remain at the provider's level
             * (note that this does not prevent the expression to be also propagated down)
             */
            if (subtreeRequirements.keepExpressionAtProviderLevel){
                mustKeepAtProviderLevel = true;
            }
            /**
             * If the subtree accepts the expression,
             * propagate it down
             */
            if(subtreeRequirements.acceptsExpression){
                selectedSubtreeRootsBuilder.add(candidateSubtreeRoot);
            }
        }

        ImmutableList<QueryNode> selectedSubtreeRoots = selectedSubtreeRootsBuilder.build();
        /**
         * If no candidate subtree has been selected,
         * keep the expression at the provider's level
         */
        if(selectedSubtreeRoots.isEmpty()){
            mustKeepAtProviderLevel = true;
        }

        Stream<Recipient> childRecipients = selectedSubtreeRoots.stream()
                .flatMap(subtreeRoot -> findRecipientsInSelectedSubtree(query, subtreeRoot, providerNode, expression));


        Stream<Recipient> recipients =  mustKeepAtProviderLevel
                ? Stream.of(new Recipient(providerNode))
                : Stream.empty();

        return Stream.concat(recipients, childRecipients).distinct();
    }



    /**
     * TODO: explain
     */
    private Stream<Recipient> findRecipientsInSelectedSubtree(IntermediateQuery query, QueryNode subtreeRoot, JoinOrFilterNode providerNode,
                                                              ImmutableExpression expression) {

        if (subtreeRoot instanceof CommutativeJoinOrFilterNode) {
            return findRecipientsInCommutativeJoinOrFilterRootedSubtree((CommutativeJoinOrFilterNode) subtreeRoot);
        }

        if (subtreeRoot instanceof DataNode) {
            return findRecipientsInDataNodeRootedSubtree(query, expression, providerNode, (DataNode) subtreeRoot);
        }

        /**
         * Possible (indirect) recursion
         */
        if (subtreeRoot instanceof LeftJoinNode) {
            return findRecipientsInLeftJoinRootedSubtree(query, expression, providerNode, (LeftJoinNode) subtreeRoot);
        }

        /**
         * Possible (indirect) recursion
         */
        if (subtreeRoot instanceof UnionNode) {
            return findRecipientsInUnionNodeRootedSubtree(query, expression, providerNode, (UnionNode) subtreeRoot);
        }

        /**
         * Possible (indirect) recursion
         */
        if (subtreeRoot instanceof ConstructionNode){
            return findRecipientsInConstructionNodeRootedSubtree(query, expression, providerNode, (ConstructionNode) subtreeRoot);
        }

        if (subtreeRoot instanceof TrueNode) {
            /**
             * Limit case (boolean expressions without variables)
             */
            if(expression.getVariables().isEmpty()){
                return findRecipientsInTrueNodeRootedSubtree(query, expression, providerNode, (TrueNode) subtreeRoot);
            }
            throw new IllegalTreeUpdateException("a TrueNode does not project out variables");
        }
        else if(subtreeRoot instanceof EmptyNode) {
            throw new IllegalTreeException("This query should not contain an EmptyNode");
        }
        /**
         * for GroupNodes only (not supported yet) ?
         */
        else{
            return findRecipientsInUnexpectedNodeRootedSubtree(query, expression, providerNode, subtreeRoot);
        }
    }

    private Stream<Recipient> findRecipientsInUnionNodeRootedSubtree(IntermediateQuery query, ImmutableExpression expression, JoinOrFilterNode providerNode, UnionNode subtreeRoot) {
            ImmutableList<QueryNode> children = query.getChildren(subtreeRoot);
            if (children.isEmpty()) {
                throw new IllegalStateException("Children expected for " + subtreeRoot);
            }
            /**
             * Possible (indirect) recursion
             */
            return selectRecipients(query, providerNode, children, expression);
    }

    private Stream<Recipient> findRecipientsInConstructionNodeRootedSubtree(IntermediateQuery query, ImmutableExpression expression, JoinOrFilterNode providerNode, ConstructionNode subtreeRoot) {
        ImmutableList<QueryNode> children = query.getChildren(subtreeRoot);
        if (children.size() != 1) {
            throw new IllegalStateException("Exactly one child expected for " + subtreeRoot);
        }
        /** Possible (indirect) recursion */
        return selectRecipients(query, providerNode, children, expression);
    }

    private Stream<Recipient> findRecipientsInCommutativeJoinOrFilterRootedSubtree(CommutativeJoinOrFilterNode currentNode) {
        return Stream.of(new Recipient(currentNode));
    }

    /**
     * This methods only propagates down an expression e coming from a provider parent p of a LJ node n,
     * and not the joining condition of n.
     *
     * e is not added to the joining condition of n,
     * but may be propagated down directly to the children subtrees of n
     *
     */
    private Stream<Recipient> findRecipientsInLeftJoinRootedSubtree(IntermediateQuery query,
                                                                    ImmutableExpression expression,
                                                                    JoinOrFilterNode providerNode,
                                                                    LeftJoinNode currentNode) {
        Stream<Recipient> recipients;
        /**
         * If the provider is the parent of the left join node
         */
        if (query.getParent(currentNode)
                .filter(p -> p == providerNode)
                .isPresent()) {
            /**
            *  Look for recipients in right and left subtrees (note that if there is none,
             *  the provider node will be returned as the only recipient)
            */
            recipients = selectRecipients(query, providerNode, query.getChildren(currentNode), expression);
        }
        else {
            /**
             * Ask for a filter node to be created above the leftJoinNode
             */
            recipients = Stream.of(new Recipient(currentNode));
        }
        return  recipients;
    }


    private Stream<Recipient> findRecipientsInDataNodeRootedSubtree(IntermediateQuery query, ImmutableExpression expression,
                                                                    JoinOrFilterNode providerNode, DataNode currentNode) {
        if (query.getParent(currentNode)
                .filter(p -> p == providerNode)
                .isPresent()) {
            /**
             * Keep the expression in the provider node
             */
            return Stream.of(new Recipient(providerNode));
        }
        else {
            /**
             * Ask for a filter node to be created above this data node
             */
            return Stream.of(new Recipient(currentNode));
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
            return Stream.of(new Recipient(providerNode));
        }
        else {
            /**
             * Ask for a filter node to be created above this data node
             */
            return Stream.of(new Recipient(currentNode));
        }
    }

    /**
     * Hook
     *
     * By default, does not push down (no optimization)
     */
    protected Stream<Recipient> findRecipientsInUnexpectedNodeRootedSubtree(IntermediateQuery currentQuery,
                                                                            ImmutableExpression expression,
                                                                            JoinOrFilterNode providerNode, QueryNode currentNode) {
        return Stream.of(new Recipient(providerNode));
    }

    /**
     * Builds the PushDownBooleanExpressionProposal.
     */
    private Optional<PushDownBooleanExpressionProposal> buildProposal(
            JoinOrFilterNode providerNode, ImmutableMultimap<Recipient, ImmutableExpression> recipientMap) {

        ImmutableCollection<Map.Entry<Recipient, ImmutableExpression>> recipientEntries = recipientMap.entries();
        /**
         * Collect new direct recipients nodes for each expression,
         * filtering out provider nodes, and therefore also LeftJoinNodes.
         */
        ImmutableMultimap<CommutativeJoinOrFilterNode, ImmutableExpression> directRecipientNodes = recipientEntries.stream()
                .filter(e -> e.getKey().directRecipientNode.isPresent())
                .filter(e -> e.getKey().directRecipientNode.get() != providerNode)
                .map(e -> new AbstractMap.SimpleEntry<>(
                        (CommutativeJoinOrFilterNode)e.getKey().directRecipientNode.get(),  e.getValue()))
                .collect(ImmutableCollectors.toMultimap());
        /**
         * Collect indirect recipient nodes
         */
        ImmutableMultimap<QueryNode, ImmutableExpression> indirectRecipientNodes = recipientEntries.stream()
                .filter(e -> e.getKey().indirectRecipientNode.isPresent())
                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey().indirectRecipientNode.get(), e.getValue()))
                .collect(ImmutableCollectors.toMultimap());

        if (directRecipientNodes.isEmpty() && indirectRecipientNodes.isEmpty()) {
            return Optional.empty();
        }
        else {
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
