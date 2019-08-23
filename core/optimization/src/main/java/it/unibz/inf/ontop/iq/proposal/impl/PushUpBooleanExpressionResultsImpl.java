package it.unibz.inf.ontop.iq.proposal.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.proposal.PushUpBooleanExpressionResults;

public class PushUpBooleanExpressionResultsImpl implements PushUpBooleanExpressionResults {

    private final ImmutableSet<QueryNode> expressionProviderReplacingNodes;
    private final IntermediateQuery resultingQuery;

    public PushUpBooleanExpressionResultsImpl(ImmutableSet<QueryNode> expressionProviderReplacingNodes, IntermediateQuery resultingQuery) {

        this.resultingQuery = resultingQuery;
        if(expressionProviderReplacingNodes.isEmpty()){
            throw new IllegalStateException("At least one replacing node must be provided");
        }
        this.expressionProviderReplacingNodes = expressionProviderReplacingNodes;
    }

    @Override
    public ImmutableSet<QueryNode> getExpressionProviderReplacingNodes() {
        return expressionProviderReplacingNodes;
    }

    @Override
    public IntermediateQuery getResultingQuery() {
        return resultingQuery;
    }
}
