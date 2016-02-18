package it.unibz.inf.ontop.owlrefplatform.core.basicoperations;

import it.unibz.inf.ontop.model.Substitution;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.model.Term;

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
