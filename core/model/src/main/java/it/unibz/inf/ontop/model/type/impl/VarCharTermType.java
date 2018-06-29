package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.TermTypeAncestry;

public class VarCharTermType extends DBTermTypeImpl {

    protected VarCharTermType(TermTypeAncestry parentAncestry) {
        super("VARCHAR", parentAncestry, false);
    }
}
