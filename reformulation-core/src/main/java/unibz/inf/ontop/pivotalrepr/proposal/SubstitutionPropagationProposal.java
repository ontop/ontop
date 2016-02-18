package unibz.inf.ontop.pivotalrepr.proposal;

import unibz.inf.ontop.model.VariableOrGroundTerm;
import unibz.inf.ontop.pivotalrepr.QueryNode;
import unibz.inf.ontop.model.ImmutableSubstitution;

/**
 * Proposal to apply a substitution to a focus node and to propagate it down and up.
 *
 */
public interface SubstitutionPropagationProposal<T extends QueryNode> extends NodeCentricOptimizationProposal<T> {

    ImmutableSubstitution<? extends VariableOrGroundTerm> getSubstitution();

}
