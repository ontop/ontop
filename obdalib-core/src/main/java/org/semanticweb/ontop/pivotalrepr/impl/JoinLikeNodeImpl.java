package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.base.Optional;
import org.semanticweb.ontop.model.BooleanExpression;
import org.semanticweb.ontop.pivotalrepr.JoinLikeNode;

public abstract class JoinLikeNodeImpl extends JoinOrFilterNodeImpl implements JoinLikeNode {

    protected JoinLikeNodeImpl(Optional<BooleanExpression> optionalJoinCondition) {
        super(optionalJoinCondition);
    }

}
