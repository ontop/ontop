package org.semanticweb.ontop.pivotalrepr.impl;


import com.google.common.base.Optional;
import org.semanticweb.ontop.pivotalrepr.LocalOptimizationProposal;
import org.semanticweb.ontop.pivotalrepr.QueryOptimizer;
import org.semanticweb.ontop.pivotalrepr.UnionNode;

public class UnionNodeImpl extends QueryNodeImpl implements UnionNode {
    @Override
    public Optional<LocalOptimizationProposal> acceptOptimizer(QueryOptimizer optimizer) {
        return optimizer.makeProposal(this);
    }

    @Override
    public UnionNode clone() {
        return new UnionNodeImpl();
    }
}
