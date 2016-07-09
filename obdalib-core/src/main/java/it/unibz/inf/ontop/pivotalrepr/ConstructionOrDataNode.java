package it.unibz.inf.ontop.pivotalrepr;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.Variable;

/**
 * Common abstraction for ConstructionNodes and DataNodes.
 *
 * TODO: get rid of this interface
 */
public interface ConstructionOrDataNode extends QueryNode {

    ImmutableSet<Variable> getProjectedVariables();
}
