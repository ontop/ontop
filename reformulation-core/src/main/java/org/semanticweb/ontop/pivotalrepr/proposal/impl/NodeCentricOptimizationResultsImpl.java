package org.semanticweb.ontop.pivotalrepr.proposal.impl;

import com.google.common.base.Optional;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;

public class NodeCentricOptimizationResultsImpl extends ProposalResultsImpl
        implements NodeCentricOptimizationResults {

    private final Optional<QueryNode> optionalNextSibling;
    private final Optional<QueryNode> optionalNewNode;
    private final Optional<QueryNode> optionalNewParent;

    public NodeCentricOptimizationResultsImpl(IntermediateQuery query,
                                              QueryNode newNode) {
        super(query);
        this.optionalNextSibling = query.nextSibling(newNode);
        this.optionalNewNode = Optional.of(newNode);
        this.optionalNewParent = query.getParent(newNode);
    }

    public NodeCentricOptimizationResultsImpl(IntermediateQuery query,
                                              Optional<QueryNode> optionalNextSibling,
                                              Optional<QueryNode> optionalParent) {
        super(query);
        this.optionalNextSibling = optionalNextSibling;
        this.optionalNewNode = Optional.absent();
        this.optionalNewParent = optionalParent;
    }

    @Override
    public Optional<QueryNode> getOptionalNewNode() {
        return optionalNewNode;
    }

    @Override
    public Optional<QueryNode> getOptionalNextSibling() {
        return optionalNextSibling;
    }

    @Override
    public Optional<QueryNode> getOptionalParentNode() {
        return optionalNewParent;
    }
}
