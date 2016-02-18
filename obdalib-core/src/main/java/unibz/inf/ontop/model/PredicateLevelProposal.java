package unibz.inf.ontop.model;

import fj.data.List;

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
     * Rules after type removal.
     */
    List<CQIE> getDetypedRules();

    /**
     * Type proposal for the current query predicate.
     */
    TypeProposal getTypeProposal();

    Predicate getPredicate();
}
