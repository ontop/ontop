package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.Substitution;

/**
 * Common abstraction for {@link ConstructionNode} and  {@link AggregationNode}
 */
public interface ExtendedProjectionNode extends ExplicitVariableProjectionNode, UnaryOperatorNode {

    /**
     * {@code (Some) projected variable --> transformed variable}
     */
    Substitution<? extends ImmutableTerm> getSubstitution();

    /**
     * Variables that have to be provided by the child
     */
    ImmutableSet<Variable> getChildVariables();
}
