package it.unibz.inf.ontop.pivotalrepr.proposal;

import it.unibz.inf.ontop.model.AtomPredicate;

/**
 * TODO: explain
 *
 * Not really an "optimization", but a reshaping.
 *
 */
public interface PredicateRenamingProposal extends QueryOptimizationProposal<ProposalResults> {

    AtomPredicate getFormerPredicate();

    AtomPredicate getNewPredicate();
}
