package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.COL_TYPE;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;

/**
 * Ancestor of all the term types.
 *
 * Singleton
 */
public class TermTypeOrigin implements TermType {

    private static final TermType INSTANCE = new TermTypeOrigin();
    private final TermTypeAncestry ancestry;

    private TermTypeOrigin() {
        ancestry = new TermTypeAncestryImpl(this);
    }

    public static TermType getInstance() {
        return INSTANCE;
    }

    @Override
    public COL_TYPE getColType() {
        throw new UnsupportedOperationException("The RootTermType does not have a COL_TYPE");
    }

    @Override
    public boolean isAbstract() {
        return true;
    }

    @Override
    public boolean isA(TermType otherTermType) {
        return true;
    }

    @Override
    public TermType getCommonDenominator(TermType otherTermType) {
        return this;
    }

    @Override
    public TermTypeAncestry getAncestry() {
        return ancestry;
    }
}
