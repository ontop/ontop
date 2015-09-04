package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.NonGroundTerm;
import org.semanticweb.ontop.pivotalrepr.*;

public class GroupNodeImpl extends QueryNodeImpl implements GroupNode {

    private static final String GROUP_NODE_STR = "GROUP BY";
    private final ImmutableList<NonGroundTerm> groupingTerms;

    public GroupNodeImpl(ImmutableList<NonGroundTerm> groupingTerms) {
        if (groupingTerms.isEmpty()) {
            throw new IllegalArgumentException("At least one group condition must be given");
        }
        this.groupingTerms = groupingTerms;
    }

    @Override
    public ImmutableList<NonGroundTerm> getGroupingTerms() {
        return groupingTerms;
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public GroupNode clone() {
        return new GroupNodeImpl(groupingTerms);
    }

    @Override
    public GroupNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException,
            NotNeededNodeException {
        return transformer.transform(this);
    }

    @Override
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        return transformer.transform(this);
    }

    @Override
    public String toString() {
        return GROUP_NODE_STR + " " + groupingTerms;
    }
}
