package org.semanticweb.ontop.model;

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
     * Only executed if no type should appear the given rules.
     *
     */
    public List<CQIE> removeHeadTypes(List<CQIE> initialRules);

    /**
     * TODO: remove
     */
    @Deprecated
    public List<CQIE> propagateChildArityChangeToBodies(List<CQIE> initialRules);

    /**
     * TODO: explain
     */
    Function prepareBodyAtomForUnification(Function bodyAtom, java.util.Set<Variable> alreadyKnownRuleVariables);
}
