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

    public List<CQIE> applyType(List<CQIE> initialRules);
    public List<CQIE> removeType(List<CQIE> initialRules);

}
