package org.semanticweb.ontop.pivotalrepr;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

/**
 * Mutable BUT ONLY WHEN APPLYING LocalOptimizationProposal forwarded by the IntermediateQuery.
 *
 * --> Mutations under control.
 *
 * Golden rule: after mutation, the node must be semantically equivalent (for instance, not specialized).
 *
 */
public interface QueryNode {

    /**
     * "Accept" method for the "Visitor" pattern.
     *
     * To be implemented by leaf classes.
     *
     * TODO: check if visitor is the proper name.
     */
    public Optional<LocalOptimizationProposal> acceptOptimizer(QueryOptimizer optimizer);

    public boolean isRejected();
}
