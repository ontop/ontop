package org.semanticweb.ontop.model;

import java.util.Optional;
import org.semanticweb.ontop.model.impl.VariableImpl;
import org.semanticweb.ontop.pivotalrepr.ImmutableQueryModifiers;

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
