package it.unibz.inf.ontop.pivotalrepr;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.Variable;

/**
 * Node that explicitly projects some variables.
 *
 * Common abstraction for ConstructionNodes, UnionNodes, DataNodes and EmptyNodes.
 *
 */
public interface ExplicitVariableProjectionNode extends QueryNode {

    ImmutableSet<Variable> getProjectedVariables();
}
