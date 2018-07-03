package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.TermTypeAncestry;

public class StringDBTermType extends DBTermTypeImpl {

    protected StringDBTermType(String name, TermTypeAncestry parentAncestry) {
        super(name, parentAncestry, false);
    }

    @Override
    public boolean isString() {
        return true;
    }
}
