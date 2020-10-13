package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermTypeAncestry;
import it.unibz.inf.ontop.model.type.lexical.DefaultLexicalSpaces;
import it.unibz.inf.ontop.model.type.lexical.LexicalSpace;

import java.util.Optional;

public abstract class DBTermTypeImpl extends TermTypeImpl implements DBTermType {

    private final String name;
    private final LexicalSpace lexicalSpace;
    private final Category category;

    protected DBTermTypeImpl(String name, TermTypeAncestry parentAncestry, boolean isAbstract,
                             Category category) {
        super(name, parentAncestry, isAbstract);
        this.name = name;
        this.category = category;
        this.lexicalSpace = DefaultLexicalSpaces.getDefaultSpace(category);
    }

    @Override
    public Category getCategory() {
        return category;
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
    public Optional<Boolean> isValidLexicalValue(String lexicalValue) {
        return lexicalSpace.includes(lexicalValue);
    }
}
