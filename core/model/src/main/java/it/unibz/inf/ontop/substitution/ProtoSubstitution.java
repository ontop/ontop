package it.unibz.inf.ontop.substitution;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;

/**
 * TODO: find a better name
 *
 * Minimal interface for a substitution
 *
 * May not depend on the AtomFactory and SubstitutionFactory
 *
 */
public interface ProtoSubstitution<T extends ImmutableTerm> {

    ImmutableMap<Variable, T> getImmutableMap();

    boolean isDefining(Variable variable);

    ImmutableSet<Variable> getDomain();

    T get(Variable variable);

    boolean isEmpty();

    /**
     * Applies the substitution to an immutable term.
     */
    ImmutableTerm apply(ImmutableTerm term);

    /**
     * This method can be applied to simple variables
     */
    ImmutableTerm applyToVariable(Variable variable);

    ImmutableFunctionalTerm applyToFunctionalTerm(ImmutableFunctionalTerm functionalTerm);

    ImmutableExpression applyToBooleanExpression(ImmutableExpression booleanExpression);

    ImmutableList<ImmutableTerm> apply(ImmutableList<? extends ImmutableTerm> terms);

}
