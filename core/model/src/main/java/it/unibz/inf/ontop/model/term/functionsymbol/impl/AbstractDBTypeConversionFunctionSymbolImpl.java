package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.FatalTypingException;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.functionsymbol.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.term.impl.FunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import java.util.Optional;

public abstract class AbstractDBTypeConversionFunctionSymbolImpl extends FunctionSymbolImpl implements DBTypeConversionFunctionSymbol {

    private final DBTermType targetType;

    protected AbstractDBTypeConversionFunctionSymbolImpl(String name, DBTermType inputBaseType, DBTermType targetType) {
        super(name, ImmutableList.of(inputBaseType));
        this.targetType = targetType;
    }

    @Override
    public DBTermType getTargetType() {
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
