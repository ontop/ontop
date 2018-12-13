package it.unibz.inf.ontop.model.term.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nonnull;

public abstract class FunctionSymbolImpl extends PredicateImpl implements FunctionSymbol {

    private final ImmutableList<TermType> expectedBaseTypes;

    protected FunctionSymbolImpl(@Nonnull String name,
                                 @Nonnull ImmutableList<TermType> expectedBaseTypes) {
        super(name, expectedBaseTypes.size());
        this.expectedBaseTypes = expectedBaseTypes;
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
                        ? t.simplify(isInConstructionNodeInOptimizationPhase)
                        : t)
                .collect(ImmutableCollectors.toList());

        return buildTermAfterEvaluation(newTerms, isInConstructionNodeInOptimizationPhase, termFactory);
    }

    /**
     * By default, just build a new functional term.
     *
     * To be extended for reacting to null values and so on.
     *
     */
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     boolean isInConstructionNodeInOptimizationPhase,
                                                     TermFactory termFactory) {
        return termFactory.getImmutableFunctionalTerm(this, newTerms);
    }

    protected boolean isOneArgumentNull(ImmutableList<ImmutableTerm> subTerms) {
        return subTerms.stream()
                .filter(t -> t instanceof Constant)
                .anyMatch(t -> ((Constant) t).isNull());
    }

    protected ImmutableList<TermType> getExpectedBaseTypes() {
        return expectedBaseTypes;
    }

    @Override
    public TermType getExpectedBaseType(int index) {
        return expectedBaseTypes.get(index);
    }
}
