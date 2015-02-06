package org.semanticweb.ontop.model;

import com.google.common.collect.ImmutableSet;
import fj.P2;

/**
 * Type proposal made by a query predicate.
 *
 * Is able to convert body atoms (necessary for dealing with URI templates)
 * so that one can compute substitution out of them.
 */
public interface TypeProposal {

    /**
     * Atom used for computing the unifier in order to propagate the types it contains.
     */
    public Function getTypedAtom();

    /**
     * Predicate of the atom.
     */
    public Predicate getPredicate();

    /**
     * Converts a body atom (necessary for dealing with URI templates)
     * so that one can compute substitution out of it.
     *
     * When dealing with URI templates, new variables are created.
     *
     * Returns the unifiable body atom and the new variable created.
     */
    P2<Function, java.util.Set<Variable>> convertIntoUnifiableAtom(Function bodyAtom, ImmutableSet<Variable> alreadyKnownRuleVariables);
}
