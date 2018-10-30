package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import it.unibz.inf.ontop.model.type.TermType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class CastFunctionSymbolImpl extends AbstractCastFunctionSymbolImpl {

    @Nullable
    private final TermType inputType;

    /**
     * Abstract input type
     */
    protected CastFunctionSymbolImpl(@Nonnull String name, @Nonnull TermType expectedBaseType,
                                     TermType targetType) {
        super(name, expectedBaseType, targetType);
        this.inputType = null;
    }

    /**
     * Concrete input type
     */
    protected CastFunctionSymbolImpl(@Nonnull TermType inputType, @Nonnull String name,
                                     TermType targetType) {
        super(name, inputType, targetType);
        this.inputType = inputType;
    }

    @Override
    public Optional<TermType> getInputType() {
        return Optional.ofNullable(inputType);
    }

    @Override
    public boolean isTemporary() {
        return false;
    }
}
