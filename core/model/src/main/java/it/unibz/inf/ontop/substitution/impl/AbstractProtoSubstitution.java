package it.unibz.inf.ontop.substitution.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.substitution.ProtoSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

public abstract class AbstractProtoSubstitution<T extends ImmutableTerm> implements ProtoSubstitution<T> {

    protected final TermFactory termFactory;

    protected AbstractProtoSubstitution(TermFactory termFactory) {
        this.termFactory = termFactory;
    }

    @Override
    public ImmutableTerm apply(ImmutableTerm term) {
        if (term instanceof Constant) {
            return term;
        }
        if (term instanceof Variable) {
            return applyToVariable((Variable) term);
        }
        if (term instanceof ImmutableFunctionalTerm) {
            return applyToFunctionalTerm((ImmutableFunctionalTerm) term);
        }
        throw new IllegalArgumentException("Unexpected kind of term: " + term.getClass());
    }

    @Override
    public ImmutableFunctionalTerm applyToFunctionalTerm(ImmutableFunctionalTerm functionalTerm) {
        if (isEmpty())
            return functionalTerm;

        ImmutableList<ImmutableTerm> subTerms = apply(functionalTerm.getTerms());

        FunctionSymbol functionSymbol = functionalTerm.getFunctionSymbol();
        // Distinguishes the BooleanExpression from the other functional terms.
        return (functionSymbol instanceof BooleanFunctionSymbol)
            ? termFactory.getImmutableExpression((BooleanFunctionSymbol) functionSymbol, subTerms)
            : termFactory.getImmutableFunctionalTerm(functionSymbol, subTerms);
    }

    @Override
    public ImmutableExpression applyToBooleanExpression(ImmutableExpression booleanExpression) {
        if (isEmpty())
            return booleanExpression;

        return termFactory.getImmutableExpression(booleanExpression.getFunctionSymbol(),
                apply(booleanExpression.getTerms()));
    }

    @Override
    public ImmutableList<ImmutableTerm> apply(ImmutableList<? extends ImmutableTerm> terms) {
        return terms.stream()
                .map(this::apply)
                .collect(ImmutableCollectors.toList());
    }

}
