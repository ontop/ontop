package org.semanticweb.ontop.model;

import fj.data.List;

/**
 * TODO: describe
 */
public interface TypeProposal {

    /**
     * TODO: explain
     *
     */
    @Deprecated
    public Function getProposedHead();

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
    public List<CQIE> removeType(List<CQIE> initialRules);

    /**
     * TODO: explain
     */
    public List<CQIE> propagateChildArityChangeToBodies(List<CQIE> initialRules);

}
