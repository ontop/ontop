package it.unibz.inf.ontop.model.term.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.FatalTypingException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nonnull;

public abstract class FunctionSymbolImpl extends PredicateImpl implements FunctionSymbol {

    protected FunctionSymbolImpl(@Nonnull String name, int arity,
                                 @Nonnull ImmutableList<TermType> expectedBaseTypes) {
        super(name, arity, expectedBaseTypes);
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

    @Override
    public ImmutableTerm simplify(ImmutableList<? extends ImmutableTerm> terms,
                                  boolean isInConstructionNodeInOptimizationPhase, TermFactory termFactory) {

        ImmutableList<ImmutableTerm> newTerms = terms.stream()
                .map(t -> (t instanceof ImmutableFunctionalTerm)
                        ? ((ImmutableFunctionalTerm) t).simplify(isInConstructionNodeInOptimizationPhase)
                        : t)
                .collect(ImmutableCollectors.toList());

        return buildTermAfterEvaluation(newTerms, termFactory);
    }

    /**
     * By default, just build a new functional term.
     *
     * To be extended for reacting to null values and so on.
     *
     */
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory) {
        return termFactory.getImmutableFunctionalTerm(this, newTerms);
    }

    protected void validateSubTermTypes(ImmutableList<? extends ImmutableTerm> terms) throws FatalTypingException {
        for(ImmutableTerm term : terms) {
            term.inferAndValidateType();
        }
    }
}
