package it.unibz.inf.ontop.model;

import java.util.Optional;

import it.unibz.inf.ontop.pivotalrepr.ImmutableQueryModifiers;

/**
 * Substitution where variables are only mapped to variables
 */
public interface Var2VarSubstitution extends ImmutableSubstitution<Variable> {

    @Override
    Variable applyToVariable(Variable variable);

    VariableOrGroundTerm applyToVariableOrGroundTerm(VariableOrGroundTerm term);

    NonGroundTerm applyToNonGroundTerm(NonGroundTerm term);

    Optional<ImmutableQueryModifiers> applyToQueryModifiers(ImmutableQueryModifiers immutableQueryModifiers);

    /**
     * Applies the substitution to the domain and co-domain terns
     *
     * Returns Optional.empty() if the results is not a substitution (incompatible entries)
     */
    Optional<ImmutableSubstitution<ImmutableTerm>> applyToSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution);
}
