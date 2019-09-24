package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;

public abstract class DBTermTypeImpl extends TermTypeImpl implements DBTermType {

    private final String name;
    private final boolean areLexicalTermsUnique;

    protected DBTermTypeImpl(String name, TermTypeAncestry parentAncestry, boolean isAbstract, boolean areLexicalTermsUnique) {
        super(name, parentAncestry, isAbstract);
        this.name = name;
        this.areLexicalTermsUnique = areLexicalTermsUnique;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Default implementation, can be overridden
     */
    @Override
    public String getCastName() {
        return name;
    }

    @Override
    public boolean areLexicalTermsUnique() {
        return areLexicalTermsUnique;
    }
}
