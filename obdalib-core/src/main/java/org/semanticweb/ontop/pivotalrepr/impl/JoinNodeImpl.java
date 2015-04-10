package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.base.Optional;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.JoinNode;
import org.semanticweb.ontop.pivotalrepr.LocalOptimizationProposal;
import org.semanticweb.ontop.pivotalrepr.QueryOptimizer;

public class JoinNodeImpl extends QueryNodeImpl implements JoinNode {

    public JoinNodeImpl(IntermediateQuery query) {
        super(query);
    }

    @Override
    public Optional<LocalOptimizationProposal> acceptOptimizer(QueryOptimizer optimizer) {
        return optimizer.makeProposal(this);
    }
}
