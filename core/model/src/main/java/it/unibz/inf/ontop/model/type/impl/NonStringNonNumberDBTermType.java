package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.TermTypeAncestry;

public class NonStringNonNumberDBTermType extends DBTermTypeImpl {

    protected NonStringNonNumberDBTermType(String name, TermTypeAncestry parentAncestry, boolean isAbstract) {
        super(name, parentAncestry, isAbstract);
    }

    @Override
    public boolean isString() {
        return false;
    }
}
