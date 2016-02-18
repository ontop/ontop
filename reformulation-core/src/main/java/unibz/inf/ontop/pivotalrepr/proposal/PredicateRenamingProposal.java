package unibz.inf.ontop.pivotalrepr.proposal;

import unibz.inf.ontop.model.AtomPredicate;

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
