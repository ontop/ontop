package org.semanticweb.ontop.pivotalrepr.proposal;

import org.semanticweb.ontop.model.ImmutableSubstitution;
import org.semanticweb.ontop.model.VariableOrGroundTerm;
import org.semanticweb.ontop.pivotalrepr.QueryNode;

/**
 * TODO: explain
 */
public interface SubstitutionPropagationProposal extends NodeCentricOptimizationProposal<QueryNode> {

    ImmutableSubstitution<VariableOrGroundTerm> getSubstitution();

}
