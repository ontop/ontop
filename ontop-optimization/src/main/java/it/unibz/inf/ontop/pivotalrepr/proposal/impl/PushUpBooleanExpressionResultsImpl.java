package it.unibz.inf.ontop.pivotalrepr.proposal.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.PushUpBooleanExpressionResults;

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
