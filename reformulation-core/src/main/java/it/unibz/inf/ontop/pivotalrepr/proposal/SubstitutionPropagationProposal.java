package it.unibz.inf.ontop.pivotalrepr.proposal;

import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.VariableOrGroundTerm;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.model.ImmutableSubstitution;

/**
 * Proposal to apply a substitution to a focus node and to propagate it down and up.
 *
 */
public interface SubstitutionPropagationProposal<T extends QueryNode> extends SimpleNodeCentricOptimizationProposal<T> {

    ImmutableSubstitution<? extends ImmutableTerm> getSubstitution();

}
