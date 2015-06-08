package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.base.Optional;
import org.semanticweb.ontop.model.BooleanExpression;
import org.semanticweb.ontop.pivotalrepr.AbstractJoinNode;

public abstract class AbstractJoinNodeImpl extends FilterNodeImpl implements AbstractJoinNode {

    protected AbstractJoinNodeImpl(Optional<BooleanExpression> optionalJoinCondition) {
        super(optionalJoinCondition);
    }

}
