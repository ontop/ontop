package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import org.semanticweb.ontop.model.Substitution;
import org.semanticweb.ontop.model.Term;
import org.semanticweb.ontop.model.impl.VariableImpl;

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

    void put(VariableImpl var, Term term);

}
