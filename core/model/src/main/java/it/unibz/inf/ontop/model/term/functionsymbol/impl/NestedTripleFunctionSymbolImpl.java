package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.NestedTripleFunctionSymbol;
import it.unibz.inf.ontop.model.type.*;

import java.util.Optional;

public class NestedTripleFunctionSymbolImpl extends FunctionSymbolImpl implements NestedTripleFunctionSymbol {

    private final NestedTripleTermType abstractNestedTripleTermType;

    public NestedTripleFunctionSymbolImpl(TypeFactory typeFactory,
                                          ImmutableList<TermType> termTypes) {
        super("NestedTriple", termTypes);
        abstractNestedTripleTermType = typeFactory.getAbstractNestedTripleTermType();
    }

    @Override   // TODO: should return true, must then implement methods to simplify, see RDFTermTypeFunctionSymbol
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(abstractNestedTripleTermType));
    }

    @Override
    protected boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return true;
    }

    @Override
    protected boolean tolerateNulls() {
        return false;
    }

    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return false;
    }
}
