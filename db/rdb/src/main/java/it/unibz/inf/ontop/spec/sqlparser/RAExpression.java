package it.unibz.inf.ontop.spec.sqlparser;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.dbschema.QuotedID;

public class RAExpression {

    private final IQTree iqTree;
    private final RAExpressionAttributes attributes;

    /**
     * constructs a relation expression
     * @param iqTree          a {@link IQTree}
     * @param attributes      an {@link RAExpressionAttributes}
     */
    RAExpression(IQTree iqTree, RAExpressionAttributes attributes) {
        this.iqTree = iqTree;
        this.attributes = attributes;
    }

    public IQTree getIQTree() { return iqTree; }

    public RAExpressionAttributes getAttributes() { return attributes; }

    public ImmutableMap<QuotedID, ImmutableTerm> getUnqualifiedAttributesMap() {
        return attributes.getUnqualifiedAttributesMap();
    }

    @Override
    public String toString() {
        return "RAExpression : " + iqTree + " with " + attributes;
    }
}
