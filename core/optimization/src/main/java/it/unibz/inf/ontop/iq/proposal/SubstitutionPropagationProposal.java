package it.unibz.inf.ontop.iq.proposal;

import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.iq.node.QueryNode;

/**
 * Proposal to apply a substitution to a focus node and to propagate it down and up.
 *
 * Assumption: the substitution must be directly applicable to the focus node
 * (i.e. the focus node should not reject it).
 *
 *
 */
public interface SubstitutionPropagationProposal<T extends QueryNode> extends SimpleNodeCentricOptimizationProposal<T> {

    ImmutableSubstitution<? extends ImmutableTerm> getSubstitution();

}
