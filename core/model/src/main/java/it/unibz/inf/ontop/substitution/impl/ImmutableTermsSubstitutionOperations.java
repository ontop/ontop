package it.unibz.inf.ontop.substitution.impl;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.UnifierBuilder;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collector;

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
    public AbstractUnifierBuilder<ImmutableTerm> unifierBuilder(ImmutableSubstitution<ImmutableTerm> substitution) {
        return new AbstractUnifierBuilder<>(termFactory, this, substitution) {
            @Override
            protected UnifierBuilder<ImmutableTerm> unifyUnequalTerms(ImmutableTerm term1, ImmutableTerm term2) {
                // Special case: unification of two functional terms (possibly recursive)
                if ((term1 instanceof ImmutableFunctionalTerm) && (term2 instanceof ImmutableFunctionalTerm)) {
                    ImmutableFunctionalTerm f1 = (ImmutableFunctionalTerm) term1;
                    ImmutableFunctionalTerm f2 = (ImmutableFunctionalTerm) term2;
                    if (f1.getFunctionSymbol().equals(f2.getFunctionSymbol()))
                        return unifyTermLists(f1.getTerms(), f2.getTerms());

                    return emptySelf();
                }
                else {
                    // avoid unifying x with f(g(x))
                    if (term1 instanceof Variable && term2.getVariableStream().noneMatch(term1::equals))
                        return extendSubstitution((Variable) term1, term2);
                    if (term2 instanceof Variable && term1.getVariableStream().noneMatch(term2::equals))
                        return extendSubstitution((Variable) term2, term1);

                    return emptySelf(); // neither is a variable, impossible to unify distinct terms
                }
            }
        };
    }
    @Override
    public Collector<ImmutableSubstitution<ImmutableTerm>, ?, Optional<ImmutableSubstitution<ImmutableTerm>>> toUnifier() {
        return Collector.of(
                () -> unifierBuilder(termFactory.getSubstitution(ImmutableMap.of())),
                (a, s) -> a.unifyTermStreams(s.entrySet().stream(), Map.Entry::getKey, Map.Entry::getValue),
                AbstractUnifierBuilder::merge,
                UnifierBuilder::build);
    }
}


