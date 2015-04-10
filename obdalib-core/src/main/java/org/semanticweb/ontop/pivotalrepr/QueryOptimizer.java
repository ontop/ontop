package org.semanticweb.ontop.pivotalrepr;

import com.google.common.base.Optional;

/**
 * TODO: complete it.
 */
public interface QueryOptimizer {

    public Optional<LocalOptimizationProposal> makeProposal(JoinNode node);
    //TODO: complete the list

}
