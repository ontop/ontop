package org.semanticweb.ontop.pivotalrepr.impl;

import org.semanticweb.ontop.model.impl.PredicateImpl;
import org.semanticweb.ontop.pivotalrepr.AtomPredicate;

/**
 * TODO: in the future, make it independent from PredicateImpl
 */
public class AtomPredicateImpl extends PredicateImpl implements AtomPredicate {
    private final boolean isExtensional;

    protected AtomPredicateImpl(String name, int arity, boolean isExtensional) {
        super(name, arity, null);
        this.isExtensional = isExtensional;
    }

    @Override
    public boolean isExtensional() {
        return isExtensional;
    }
}
