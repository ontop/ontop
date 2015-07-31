package org.semanticweb.ontop.pivotalrepr.proposal.impl;

import com.google.common.base.Optional;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;

public class NodeCentricOptimizationResultsImpl extends ProposalResultsImpl
        implements NodeCentricOptimizationResults {

    private final Optional<QueryNode> optionalNewParent;
    private final Optional<QueryNode> optionalNewNode;

    public NodeCentricOptimizationResultsImpl(IntermediateQuery query,
                                              QueryNode newNode) {
        super(query);
        this.optionalNewParent = query.getParent(newNode);
        this.optionalNewNode = Optional.of(newNode);
    }

    public NodeCentricOptimizationResultsImpl(IntermediateQuery query,
                                              Optional<QueryNode> optionalNewParent) {
        super(query);
        this.optionalNewParent = optionalNewParent;
        this.optionalNewNode = Optional.absent();
    }

    @Override
    public Optional<QueryNode> getOptionalNewParentNode() {
        return optionalNewParent;
    }

    @Override
    public Optional<QueryNode> getOptionalNewNode() {
        return optionalNewNode;
    }
}
