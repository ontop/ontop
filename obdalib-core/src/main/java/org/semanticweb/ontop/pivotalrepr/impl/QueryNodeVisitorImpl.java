package org.semanticweb.ontop.pivotalrepr.impl;

import org.semanticweb.ontop.pivotalrepr.NonStandardNode;
import org.semanticweb.ontop.pivotalrepr.QueryNodeVisitor;

public abstract class QueryNodeVisitorImpl implements QueryNodeVisitor {

    /**
     * To be overwritten by "non-standard" sub-classes.
     * However standard sub-classes do not need to override it.
     */
    @Override
    public void visit(NonStandardNode nonStandardNode) {
        throw new UnsupportedOperationException("Non-standard nodes are not supported by this class. " +
                "Please create a dedicated sub-class that overrides this method.");
    }
}
