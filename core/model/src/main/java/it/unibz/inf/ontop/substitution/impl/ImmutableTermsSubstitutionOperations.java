package it.unibz.inf.ontop.substitution.impl;

import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.UnifierBuilder;

public class ImmutableTermsSubstitutionOperations extends AbstractSubstitutionOperations<ImmutableTerm> {

    ImmutableTermsSubstitutionOperations(TermFactory termFactory) {
        super(termFactory, v -> v, s -> s);
    }

    @Override
    public ImmutableTerm applyToTerm(Substitution<? extends ImmutableTerm> substitution, ImmutableTerm t) {
        if (t instanceof Variable) {
            return apply(substitution, (Variable) t);
        }
        if (t instanceof Constant) {
            return t;
        }
        if (t instanceof ImmutableFunctionalTerm) {
            return apply(substitution, (ImmutableFunctionalTerm) t);
        }
        throw new IllegalArgumentException("Unexpected kind of term: " + t.getClass());
    }

    @Override
    public AbstractUnifierBuilder<ImmutableTerm> unifierBuilder(Substitution<ImmutableTerm> substitution) {
        return new AbstractUnifierBuilder<>(termFactory, this, substitution) {
            @Override
            protected UnifierBuilder<ImmutableTerm> unifyUnequalTerms(ImmutableTerm term1, ImmutableTerm term2) {
                // Special case: unification of two functional terms (possibly recursive)
                if ((term1 instanceof ImmutableFunctionalTerm) && (term2 instanceof ImmutableFunctionalTerm)) {
                    ImmutableFunctionalTerm f1 = (ImmutableFunctionalTerm) term1;
                    ImmutableFunctionalTerm f2 = (ImmutableFunctionalTerm) term2;
                    if (f1.getFunctionSymbol().equals(f2.getFunctionSymbol()))
                        return unify(f1.getTerms(), f2.getTerms());

                    return empty();
                }
                else {
                    return attemptUnifying(term1, term2)
                            .or(() -> attemptUnifying(term2, term1))
                            .orElseGet(this::empty);
                }
            }

            @Override
            protected boolean doesNotContainVariable(Variable variable, ImmutableTerm term) {
                return term.getVariableStream().noneMatch(variable::equals);
            }
        };
    }
}


