package unibz.inf.ontop.model;

import java.util.Optional;

import unibz.inf.ontop.pivotalrepr.ImmutableQueryModifiers;

/**
 * Substitution where variables are only mapped to variables
 */
public interface Var2VarSubstitution extends ImmutableSubstitution<Variable> {

    @Override
    Variable applyToVariable(Variable variable);

    VariableOrGroundTerm applyToVariableOrGroundTerm(VariableOrGroundTerm term);

    NonGroundTerm applyToNonGroundTerm(NonGroundTerm term);

    Optional<ImmutableQueryModifiers> applyToQueryModifiers(ImmutableQueryModifiers immutableQueryModifiers);
}
