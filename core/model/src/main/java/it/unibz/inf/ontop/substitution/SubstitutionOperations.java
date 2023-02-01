package it.unibz.inf.ontop.substitution;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.impl.AbstractSubstitutionOperations;

import java.util.Optional;

public interface SubstitutionOperations<T extends ImmutableTerm>  {


    T apply(ImmutableSubstitution<? extends T> substitution, Variable variable);

    T applyToTerm(ImmutableSubstitution<? extends T> substitution, T t);


    ImmutableFunctionalTerm apply(ImmutableSubstitution<? extends T> substitution, ImmutableFunctionalTerm term);

    ImmutableExpression apply(ImmutableSubstitution<? extends T> substitution, ImmutableExpression expression);

    ImmutableList<T> apply(ImmutableSubstitution<? extends T> substitution, ImmutableList<? extends Variable> variables);

    ImmutableSet<T> apply(ImmutableSubstitution<? extends T> substitution, ImmutableSet<? extends Variable> terms);

    ImmutableList<T> applyToTerms(ImmutableSubstitution<? extends T> substitution, ImmutableList<? extends T> terms);

    ImmutableMap<Integer, T> applyToTerms(ImmutableSubstitution<? extends T> substitution, ImmutableMap<Integer, ? extends T> argumentMap);

/*
    default ImmutableSubstitution<T> compose(ImmutableSubstitution<? extends T> g, ImmutableSubstitution<? extends T> f) {
        if (g.isEmpty())
            return (ImmutableSubstitution) f;

        if (f.isEmpty())
            return (ImmutableSubstitution) g;

        ImmutableMap<Variable, T> map = Stream.concat(
                        f.entrySet().stream()
                                .map(e -> Maps.immutableEntry(e.getKey(), applyToTerm(g, e.getValue()))),
                        g.entrySet().stream())
                .filter(e -> !e.getKey().equals(e.getValue()))
                .collect(ImmutableCollectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (fValue, gValue) -> fValue));

        return getSubstitution(map);
    }
*/

    static SubstitutionOperations<ImmutableTerm> onImmutableTerms() {
        return new AbstractSubstitutionOperations<>() {
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
        };
    }




}
