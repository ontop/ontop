package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.FatalTypingException;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.functionsymbol.CastFunctionSymbol;
import it.unibz.inf.ontop.model.term.impl.FunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import java.util.Optional;

public abstract class AbstractCastFunctionSymbolImpl extends FunctionSymbolImpl implements CastFunctionSymbol {

    private final TermType targetType;

    protected AbstractCastFunctionSymbolImpl(String name, TermType inputBaseType, TermType targetType) {
        super(name, ImmutableList.of(inputBaseType));
        this.targetType = targetType;
    }

    @Override
    public TermType getTargetType() {
        return targetType;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(targetType));
    }

    @Override
    public Optional<TermTypeInference> inferAndValidateType(ImmutableList<? extends ImmutableTerm> terms) throws FatalTypingException {
        validateSubTermTypes(terms);
        return inferType(terms);
    }
}
