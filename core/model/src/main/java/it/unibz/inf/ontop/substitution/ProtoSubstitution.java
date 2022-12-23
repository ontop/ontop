package it.unibz.inf.ontop.substitution;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

/**
 * TODO: find a better name
 *
 * Minimal interface for a substitution
 *
 * May not depend on SubstitutionFactory
 *
 */
public interface ProtoSubstitution<T extends ImmutableTerm> {

    ImmutableMap<Variable, T> getImmutableMap();

    default boolean isDefining(Variable variable) { return getImmutableMap().containsKey(variable); }

    default ImmutableSet<Variable> getDomain() { return getImmutableMap().keySet(); }

    default ImmutableCollection<T> getRange() { return getImmutableMap().values(); }

    default T get(Variable variable) { return getImmutableMap().get(variable); }

    default boolean isEmpty() { return getImmutableMap().isEmpty(); }

    /**
     * Applies the substitution to an immutable term.
     */
    default ImmutableTerm apply(ImmutableTerm term) {
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

    /**
     * This method can be applied to simple variables
     */
    default ImmutableTerm applyToVariable(Variable variable) {
        T r = get(variable);
        return r == null ? variable : r;
    }

    ImmutableFunctionalTerm applyToFunctionalTerm(ImmutableFunctionalTerm functionalTerm);

    ImmutableExpression applyToBooleanExpression(ImmutableExpression booleanExpression);

    default ImmutableList<ImmutableTerm> apply(ImmutableList<? extends ImmutableTerm> terms) {
        return terms.stream()
                .map(this::apply)
                .collect(ImmutableCollectors.toList());
    }
}
