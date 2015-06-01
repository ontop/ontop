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
     * TODO: keep??
     */
    public QueryModifiers getModifiers();

    /**
     * TODO: keep??
     */
    public boolean hasModifiers();

    /**
     * "Accept" method for the "Visitor" pattern.
     *
     * To be implemented by leaf classes.
     *
     * TODO: check if visitor is the proper name.
     */
    public Optional<LocalOptimizationProposal> acceptOptimizer(QueryOptimizer optimizer);

    /**
     * TODO: should it be here?
     */
    public BooleanExpression getFilterExpression();

    /**
     * TODO: should it be here?
     */
    public boolean hasFilterExpression();

    public boolean isRejected();

    /**
     * TODO: throw an exception if the node is not in the DAG anymore.
     *
     * Follows the evolution of the DAG ("dynamic").
     *
     */
    public ImmutableList<QueryNode> getCurrentSubNodes();
}
