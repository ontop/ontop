package it.unibz.inf.ontop.substitution.impl;

import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

import java.util.Optional;

public class ImmutableTermsSubstitutionOperations extends AbstractSubstitutionOperations<ImmutableTerm> {

    ImmutableTermsSubstitutionOperations(TermFactory termFactory) {
        super(termFactory);
    }

    @Override
    public ImmutableTerm apply(ImmutableSubstitution<? extends ImmutableTerm> substitution, Variable variable) {
        return Optional.<ImmutableTerm>ofNullable(substitution.get(variable)).orElse(variable);
    }

    @Override
    public ImmutableTerm applyToTerm(ImmutableSubstitution<? extends ImmutableTerm> substitution, ImmutableTerm t) {
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
    public ImmutableUnificationTools.UnifierBuilder<ImmutableTerm, ?> unifierBuilder(ImmutableSubstitution<ImmutableTerm> substitution) {
        return new ImmutableUnificationTools.ImmutableUnifierBuilder(termFactory, this, substitution);
    }
}


