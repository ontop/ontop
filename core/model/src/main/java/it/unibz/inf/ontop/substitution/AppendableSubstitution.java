package it.unibz.inf.ontop.substitution;

import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.Variable;

/**
 * Mutable substitution that accepts new entries.
 *
 * Goal: make explicit the fact that this substitution is mutable. Will be useful
 * in the future where normal substitutions will become immutable.
 *
 * Restricted form of mutation.
 *
 * TODO: find a better adjective than appendable.
 *
 */
public interface AppendableSubstitution extends Substitution {

    void put(Variable var, Term term);

}
