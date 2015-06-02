package org.semanticweb.ontop.pivotalrepr;

import com.google.common.base.Optional;
import org.semanticweb.ontop.pivotalrepr.impl.FilterNodeImpl;

/**
 * TODO: complete it.
 */
public interface QueryOptimizer {

    Optional<LocalOptimizationProposal> makeProposal(InnerJoinNode node);

    Optional<LocalOptimizationProposal> makeProposal(SimpleFilterNode filterNode);

    //TODO: complete the list

}
