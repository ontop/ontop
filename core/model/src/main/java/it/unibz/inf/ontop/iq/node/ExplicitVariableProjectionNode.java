package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.Variable;

/**
 * Node that explicitly declares the variables returned by its sub-tree.
 *
 * Common abstraction for ConstructionNodes, UnionNodes, DataNodes and EmptyNodes.
 *
 */
public interface ExplicitVariableProjectionNode extends QueryNode {

    /**
     * Set of variables returned by any sub-tree where this node is the root.
     */
    ImmutableSet<Variable> getVariables();
}
