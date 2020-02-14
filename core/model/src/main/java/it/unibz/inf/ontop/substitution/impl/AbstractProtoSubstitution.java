package it.unibz.inf.ontop.substitution.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.substitution.ProtoSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

public abstract class AbstractProtoSubstitution<T extends ImmutableTerm> implements ProtoSubstitution<T> {

    final TermFactory termFactory;

    protected AbstractProtoSubstitution(TermFactory termFactory) {
        this.termFactory = termFactory;
    }

    @Override
    public ImmutableTerm apply(ImmutableTerm term) {
        if (term instanceof Constant) {
            return term;
        }
        else if (term instanceof Variable) {
            return applyToVariable((Variable) term);
        }
        else if (term instanceof ImmutableFunctionalTerm) {
            return applyToFunctionalTerm((ImmutableFunctionalTerm) term);
        }
        else {
            throw new IllegalArgumentException("Unexpected kind of term: " + term.getClass());
        }
    }

    @Override
    public ImmutableFunctionalTerm applyToFunctionalTerm(ImmutableFunctionalTerm functionalTerm) {
        if (isEmpty())
            return functionalTerm;

        ImmutableList.Builder<ImmutableTerm> subTermsBuilder = ImmutableList.builder();

        for (ImmutableTerm subTerm : functionalTerm.getTerms()) {
            subTermsBuilder.add(apply(subTerm));
        }
        FunctionSymbol functionSymbol = functionalTerm.getFunctionSymbol();

        /*
         * Distinguishes the BooleanExpression from the other functional terms.
         */
        if (functionSymbol instanceof BooleanFunctionSymbol) {
            return termFactory.getImmutableExpression((BooleanFunctionSymbol) functionSymbol,
                    subTermsBuilder.build());
        }
        else {
            return termFactory.getImmutableFunctionalTerm(functionSymbol, subTermsBuilder.build());
        }
    }

    @Override
    public ImmutableExpression applyToBooleanExpression(ImmutableExpression booleanExpression) {
        return (ImmutableExpression) apply(booleanExpression);
    }

    @Override
    public ImmutableList<ImmutableTerm> apply(ImmutableList<? extends ImmutableTerm> terms) {
        return terms.stream()
                .map(this::apply)
                .collect(ImmutableCollectors.toList());
    }

}
