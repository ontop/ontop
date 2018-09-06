package it.unibz.inf.ontop.model.term.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.NonFunctionalTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.type.TermType;

import javax.annotation.Nonnull;

public abstract class FunctionSymbolImpl extends PredicateImpl implements FunctionSymbol {
    protected FunctionSymbolImpl(@Nonnull String name,
                                 @Nonnull ImmutableList<TermType> expectedBaseTypes) {
        super(name, expectedBaseTypes);
    }

    /**
     * TODO: REMOVE IT (TEMPORARY)
     */
    @Override
    public FunctionalTermNullability evaluateNullability(ImmutableList<? extends NonFunctionalTerm> arguments,
                                                         VariableNullability childNullability) {
        // TODO: implement it seriously
        boolean isNullable = arguments.stream()
                .filter(a -> a instanceof Variable)
                .anyMatch(a -> childNullability.isPossiblyNullable((Variable) a));
        return new FunctionalTermNullabilityImpl(isNullable);
    }
}
