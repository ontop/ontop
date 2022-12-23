package it.unibz.inf.ontop.substitution;

import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;

/**
 * Substitution where variables are only mapped to variables
 */
public interface Var2VarSubstitution extends ImmutableSubstitution<Variable> {

    @Override // more specific return type
    default Variable applyToVariable(Variable variable) {
        Variable r = get(variable);
        return r == null ? variable : r;
    }

    /**
     * Guarantees that the term type is preserved
     */
    <T extends ImmutableTerm> T applyToTerm(T term);

}
