package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.FatalTypingException;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.functionsymbol.CastFunctionSymbol;
import it.unibz.inf.ontop.model.term.impl.FunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TypeInference;

import javax.annotation.Nonnull;

public abstract class CastFunctionSymbolImpl extends FunctionSymbolImpl implements CastFunctionSymbol {

    private final TermType targetType;

    protected CastFunctionSymbolImpl(@Nonnull String name, @Nonnull TermType expectedBaseType,
                                     TermType targetType) {
        super(name, 1, ImmutableList.of(expectedBaseType));
        this.targetType = targetType;
    }

    @Override
    public TermType getTargetType() {
        return targetType;
    }

    /**
     * TODO: implement it seriously
     */
    @Override
    public TypeInference inferType(ImmutableList<? extends ImmutableTerm> terms) throws FatalTypingException {
        // TODO: check the types of the terms
        return TypeInference.declareTermType(targetType);
    }
}
