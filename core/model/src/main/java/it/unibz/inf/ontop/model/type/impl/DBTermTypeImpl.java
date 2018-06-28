package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;

public class DBTermTypeImpl extends TermTypeImpl implements DBTermType {

    protected DBTermTypeImpl(String name, TermTypeAncestry parentAncestry, boolean isAbstract) {
        super(name, parentAncestry, isAbstract);
    }
}
