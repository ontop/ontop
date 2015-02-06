package org.semanticweb.ontop.model;

import fj.data.List;
import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.TypeProposal;

/**
 * Top class in charge of generating a type proposal
 * and typing the definition rules of one query predicate.
 */
public interface PredicateLevelProposal {

    /**
     * Rules with typed heads and filter atoms.
     */
    List<CQIE> getTypedRules();

    /**
     * Type proposal for the current query predicate.
     */
    TypeProposal getTypeProposal();

    Predicate getPredicate();
}
