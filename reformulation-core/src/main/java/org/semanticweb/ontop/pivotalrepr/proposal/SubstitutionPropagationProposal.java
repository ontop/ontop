package org.semanticweb.ontop.pivotalrepr.proposal;

import org.semanticweb.ontop.model.ImmutableSubstitution;
import org.semanticweb.ontop.model.VariableOrGroundTerm;
import org.semanticweb.ontop.pivotalrepr.QueryNode;

/**
 * Proposal to apply a substitution to a focus node and to propagate it down and up.
 *
 */
public interface SubstitutionPropagationProposal<T extends QueryNode> extends NodeCentricOptimizationProposal<T> {

    ImmutableSubstitution<? extends VariableOrGroundTerm> getSubstitution();

}
