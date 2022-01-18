package it.unibz.inf.ontop.substitution;

import java.util.Optional;

import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.NonGroundTerm;
import it.unibz.inf.ontop.model.term.Variable;

/**
 * Substitution where variables are only mapped to variables
 */
public interface Var2VarSubstitution extends ImmutableSubstitution<Variable> {

    @Override
    Variable applyToVariable(Variable variable);

    /**
     * Guarantees that the term type is preserved
     */
    <T extends ImmutableTerm> T applyToTerm(T term);

}
