package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.Optional;

public class AbstractBooleanNormFunctionSymbol extends AbstractDBTypeConversionFunctionSymbolImpl {

    private final DBTermType booleanType;

    protected AbstractBooleanNormFunctionSymbol(DBTermType booleanType, DBTermType stringType) {
        super("booleanLexicalNorm", booleanType, stringType);
        this.booleanType = booleanType;
    }

    @Override
    public Optional<DBTermType> getInputType() {
        return Optional.of(booleanType);
    }

    @Override
    public boolean isTemporary() {
        return false;
    }

    @Override
    public boolean canBePostProcessed() {
        return true;
    }
}
