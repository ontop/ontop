package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;

public abstract class DBTermTypeImpl extends TermTypeImpl implements DBTermType {

    private final String name;

    protected DBTermTypeImpl(String name, TermTypeAncestry parentAncestry, boolean isAbstract) {
        super(name, parentAncestry, isAbstract);
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Default implementation, can be overridden
     */
    @Override
    public String getCompleteName() {
        return name;
    }
}
