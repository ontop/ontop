package it.unibz.inf.ontop.pivotalrepr.proposal;

import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;

/**
 * Propagates up a substitution from the focus node
*/
public interface SubstitutionUpPropagationProposal<N extends QueryNode> extends NodeCentricOptimizationProposal<N> {

    ImmutableSubstitution<? extends ImmutableTerm> getAscendingSubstitution();
}
