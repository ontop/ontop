package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;

import java.util.Optional;

public class TemporaryCastToStringFunctionSymbolImpl extends AbstractCastFunctionSymbolImpl {

    protected TemporaryCastToStringFunctionSymbolImpl(TermType inputBaseType, DBTermType targetType) {
        super("TmpTo" + targetType.getName(), inputBaseType, targetType);
    }

    @Override
    public Optional<TermType> getInputType() {
        return Optional.empty();
    }

    @Override
    public boolean isTemporary() {
        return true;
    }
}
