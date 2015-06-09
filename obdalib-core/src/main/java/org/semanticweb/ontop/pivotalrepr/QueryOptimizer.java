package org.semanticweb.ontop.pivotalrepr;

import com.google.common.base.Optional;
import org.semanticweb.ontop.pivotalrepr.impl.FilterNodeImpl;
import org.semanticweb.ontop.pivotalrepr.impl.ProjectionNodeImpl;

/**
 * TODO: complete it.
 */
public interface QueryOptimizer {

    Optional<LocalOptimizationProposal> makeProposal(InnerJoinNode node);

    Optional<LocalOptimizationProposal> makeProposal(SimpleFilterNode filterNode);

    Optional<LocalOptimizationProposal> makeProposal(ProjectionNode projectionNode);

    //TODO: complete the list

}
