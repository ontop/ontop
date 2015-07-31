package org.semanticweb.ontop.pivotalrepr.proposal.impl;

import com.google.common.base.Optional;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;

public class NodeCentricOptimizationResultsImpl extends ProposalResultsImpl
        implements NodeCentricOptimizationResults {

    private final Optional<QueryNode> optionalNextSibling;
    private final Optional<QueryNode> optionalNewNode;

    public NodeCentricOptimizationResultsImpl(IntermediateQuery query,
                                              QueryNode newNode) {
        super(query);
        this.optionalNextSibling = query.nextSibling(newNode);
        this.optionalNewNode = Optional.of(newNode);
    }

    public NodeCentricOptimizationResultsImpl(IntermediateQuery query,
                                              Optional<QueryNode> optionalNextSibling) {
        super(query);
        this.optionalNextSibling = optionalNextSibling;
        this.optionalNewNode = Optional.absent();
    }

    @Override
    public Optional<QueryNode> getOptionalNewNode() {
        return optionalNewNode;
    }

    @Override
    public Optional<QueryNode> getOptionalNextSibling() {
        return optionalNextSibling;
    }
}
