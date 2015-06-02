package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.base.Optional;
import org.semanticweb.ontop.pivotalrepr.*;

public class InnerJoinNodeImpl extends AbstractJoinNodeImpl implements InnerJoinNode {

    @Override
    public Optional<LocalOptimizationProposal> acceptOptimizer(QueryOptimizer optimizer) {
        return optimizer.makeProposal(this);
    }
}
