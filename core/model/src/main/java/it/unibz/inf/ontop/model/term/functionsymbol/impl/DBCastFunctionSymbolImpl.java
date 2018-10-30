package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import it.unibz.inf.ontop.model.type.DBTermType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class DBCastFunctionSymbolImpl extends AbstractDBCastFunctionSymbolImpl {

    @Nullable
    private final DBTermType inputType;

    protected DBCastFunctionSymbolImpl(@Nonnull DBTermType inputBaseType,
                                       DBTermType targetType) {
        super(inputBaseType.isAbstract()
                ? "to" + targetType
                : inputBaseType + "To" + targetType,
                inputBaseType, targetType);
        this.inputType = inputBaseType.isAbstract() ? null : inputBaseType;
    }

    @Override
    public Optional<DBTermType> getInputType() {
        return Optional.ofNullable(inputType);
    }

    @Override
    public boolean isTemporary() {
        return false;
    }
}
