package org.semanticweb.ontop.pivotalrepr.impl;

import org.semanticweb.ontop.pivotalrepr.AbstractJoinNode;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;

public abstract class AbstractJoinNodeImpl extends QueryNodeImpl implements AbstractJoinNode {
    public AbstractJoinNodeImpl(IntermediateQuery query) {
        super(query);
    }
}
