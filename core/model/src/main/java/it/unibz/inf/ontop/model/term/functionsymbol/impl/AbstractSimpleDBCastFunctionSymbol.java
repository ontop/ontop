package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public abstract class AbstractSimpleDBCastFunctionSymbol extends AbstractDBTypeConversionFunctionSymbolImpl {

    @Nullable
    private final DBTermType inputType;

    protected AbstractSimpleDBCastFunctionSymbol(@Nonnull DBTermType inputBaseType,
                                                 DBTermType targetType) {
        super(inputBaseType.isAbstract()
                ? "to" + targetType
                : inputBaseType + "To" + targetType,
                inputBaseType, targetType);
        this.inputType = inputBaseType.isAbstract() ? null : inputBaseType;
    }

    @Override
    protected DBConstant convertDBConstant(DBConstant constant, TermFactory termFactory) {
        return termFactory.getDBConstant((constant).getValue(), getTargetType());
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
