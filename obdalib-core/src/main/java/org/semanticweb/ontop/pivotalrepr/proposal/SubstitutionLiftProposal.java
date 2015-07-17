package org.semanticweb.ontop.pivotalrepr.proposal;

import com.google.common.collect.ImmutableList;

/**
 * TODO: explain
 */
public interface SubstitutionLiftProposal extends LocalOptimizationProposal {
    ConstructionNodeUpdate getTopNodeUpdate();

    ImmutableList<ConstructionNodeUpdate> getBottomNodeUpdates();
}
