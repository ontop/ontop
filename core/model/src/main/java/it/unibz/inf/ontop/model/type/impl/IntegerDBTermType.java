package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;

public class IntegerDBTermType extends DBTermTypeImpl implements DBTermType {

    public IntegerDBTermType(TermTypeAncestry parentAncestry) {
        super("INTEGER", parentAncestry, false);
    }
}
