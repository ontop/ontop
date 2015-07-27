package org.semanticweb.ontop.pivotalrepr.proposal;

import org.semanticweb.ontop.model.AtomPredicate;

/**
 * TODO: explain
 *
 * Not really an "optimization", but a reshaping.
 *
 */
public interface PredicateRenamingProposal extends QueryOptimizationProposal {

    AtomPredicate getFormerPredicate();

    AtomPredicate getNewPredicate();
}
