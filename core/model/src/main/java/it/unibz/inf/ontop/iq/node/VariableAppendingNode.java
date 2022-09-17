package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.Variable;

/**
 * Adds new variables to the tree, in addition of the ones provided by the children
 */
public interface VariableAppendingNode extends QueryNode {

    /**
     * Set of variables returned by a tree with this node as root, given the variables provided by the children
     */
    ImmutableSet<Variable> getVariables(ImmutableSet<Variable> childVariables);
}
