package org.semanticweb.ontop.pivotalrepr.impl;

import org.semanticweb.ontop.pivotalrepr.JoinNode;
import org.semanticweb.ontop.pivotalrepr.LocalOptimizationProposal;
import org.semanticweb.ontop.pivotalrepr.QueryOptimizer;

public class JoinNodeImpl extends QueryNodeImpl implements JoinNode {

    @Override
    public LocalOptimizationProposal acceptOptimizer(QueryOptimizer optimizer) {
        return optimizer.makeProposal(this);
    }
}
