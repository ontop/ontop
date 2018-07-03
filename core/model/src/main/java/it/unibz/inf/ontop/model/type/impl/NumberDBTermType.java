package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.TermTypeAncestry;

public class NumberDBTermType extends DBTermTypeImpl {

    protected NumberDBTermType(String name, TermTypeAncestry parentAncestry, boolean isAbstract) {
        super(name, parentAncestry, isAbstract);
    }

    @Override
    public boolean isString() {
        return false;
    }
}
