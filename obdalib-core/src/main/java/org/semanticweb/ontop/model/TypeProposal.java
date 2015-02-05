package org.semanticweb.ontop.model;

import com.google.common.collect.ImmutableSet;
import fj.P2;
import fj.data.List;

/**
 * TODO: describe
 */
public interface TypeProposal {

    /**
     * Atom used for computing the unifier in order to propagate the types it contains.
     */
    public Function getUnifiableAtom();

    /**
     * TODO: explain
     */
    public Predicate getPredicate();

    /**
     * TODO: explain
     */
    public List<CQIE> applyType(List<CQIE> initialRules);

    /**
     * TODO: explain
     *
     * Returns the unifiable body atom and the new variable created.
     */
    P2<Function, java.util.Set<Variable>> convertIntoUnifiableAtom(Function bodyAtom, ImmutableSet<Variable> alreadyKnownRuleVariables);
}
