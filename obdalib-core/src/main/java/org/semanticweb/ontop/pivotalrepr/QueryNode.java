package org.semanticweb.ontop.pivotalrepr;

import com.google.common.collect.ImmutableList;

/**
 * Mutable BUT ONLY WHEN APPLYING LocalOptimizationProposal forwarded by the IntermediateQuery.
 *
 * --> Mutations under control.
 *
 */
public interface QueryNode {

    public QueryModifiers getModifiers();
    public boolean hasModifiers();

    /**
     * "Accept" method for the "Visitor" pattern.
     *
     * To be implemented by leaf classes.
     *
     * TODO: check if visitor is the proper name.
     */
    public LocalOptimizationProposal acceptOptimizer(QueryOptimizer optimizer);

    public BooleanExpression getFilterExpression();
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
