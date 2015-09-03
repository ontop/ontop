package org.semanticweb.ontop.pivotalrepr.proposal.impl;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;

public class NodeCentricOptimizationResultsImpl extends ProposalResultsImpl
        implements NodeCentricOptimizationResults {

    private final Optional<QueryNode> optionalNextSibling;
    private final Optional<QueryNode> optionalNewNode;
    private final Optional<QueryNode> optionalClosestAncestor;

    public NodeCentricOptimizationResultsImpl(IntermediateQuery query,
                                              QueryNode newNode) {
        super(query);
        this.optionalNextSibling = query.nextSibling(newNode);
        this.optionalNewNode = Optional.of(newNode);
        this.optionalClosestAncestor = query.getParent(newNode);
    }

    /**
     * When the focus node has been removed.
     *
     */
    public NodeCentricOptimizationResultsImpl(IntermediateQuery query,
                                              Optional<QueryNode> optionalNextSibling,
                                              Optional<QueryNode> optionalClosestAncestor) {
        super(query);
        this.optionalNextSibling = optionalNextSibling;
        this.optionalNewNode = Optional.absent();
        this.optionalClosestAncestor = optionalClosestAncestor;

        /**
         * Checks the closest ancestor is the parent of the next sibling
         * (if any of course).
         */
        if (optionalNextSibling.isPresent() && optionalClosestAncestor.isPresent()) {
            Optional<QueryNode> optionalSiblingParent = query.getParent(optionalNextSibling.get());
            if ((!optionalSiblingParent.isPresent()) || (optionalSiblingParent.get() != optionalClosestAncestor.get())) {
                throw new IllegalArgumentException("The closest ancestor is not the parent of the next sibling");
            }
        }

    }

    /**
     * If absent, it means that the focus node has been deleted.
     */
    @Override
    public Optional<QueryNode> getOptionalNewNode() {
        return optionalNewNode;
    }

    /**
     * Is or was (if the node has been deleted) a sibling.
     *
     * Note that in case of cascade deletion, the sibling may appear higher in the tree.
     */
    @Override
    public Optional<QueryNode> getOptionalNextSibling() {
        return optionalNextSibling;
    }

    /**
     * Is usually the parent but may be another ancestor in
     * case of delete cascading.
     */
    @Override
    public Optional<QueryNode> getOptionalClosestAncestor() {
        return optionalClosestAncestor;
    }
}
