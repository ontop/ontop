package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.Optional;

public class TemporaryDBTypeConversionToStringFunctionSymbolImpl extends AbstractDBTypeConversionFunctionSymbolImpl {

    protected TemporaryDBTypeConversionToStringFunctionSymbolImpl(DBTermType inputBaseType, DBTermType targetType) {
        super("TmpTo" + targetType.getName(), inputBaseType, targetType);
    }

    @Override
    public Optional<DBTermType> getInputType() {
        return Optional.empty();
    }

    @Override
    public boolean isTemporary() {
        return true;
    }
}
