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

    /**
     * Applies the substitution to the domain and co-domain terns
     *
     * Returns Optional.empty() if the results is not a substitution (incompatible entries)
     */
    <T extends ImmutableTerm>
    Optional<ImmutableSubstitution<T>> applyToSubstitution(ImmutableSubstitution<T> substitution);

    Var2VarSubstitution composeWithVar2Var(Var2VarSubstitution g);

    NonGroundTerm applyToNonGroundTerm(NonGroundTerm term);
}
