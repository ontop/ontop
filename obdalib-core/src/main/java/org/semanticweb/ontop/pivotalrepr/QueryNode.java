package org.semanticweb.ontop.pivotalrepr;

import com.google.common.collect.ImmutableList;

/**
 * Almost immutable: except isRejected (can be set to true)?
 *
 * Or should allow some modifier update and filter expression?
 *
 * Or fully immutable?
 *
 *
 * TODO: complete it
 */
public interface QueryNode {

    public QueryModifiers getModifiers();
    public boolean hasModifiers();

    /**
     * Apply optimization locally (no "navigation with side-effects"??).
     *
     * To be implemented by leaf classes ("visitor" pattern).
     */
    public LocalOptimizationProposal proposeOptimization(QueryOptimizer optimizer);

    /**
     * "Visitor"-like method.
     * TODO: look if there is not a better pattern name
     *
     * Should called ONLY by the optimization proposal.
     *
     * TODO: replace OptimizationProposal by a more specific one.
     *
     *
     */
    public void applyOptimizationProposal(LocalOptimizationProposal proposal);

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
