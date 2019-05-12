package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;

/**
 * Head node an IntermediateQuery
 *
 * TODO: further explain
 *
 * See IntermediateQueryFactory for creating a new instance.
 *
 */
public interface ConstructionNode extends ExplicitVariableProjectionNode, UnaryOperatorNode {

    /**
     * (Some) projected variable --> transformed variable
     */
    ImmutableSubstitution<ImmutableTerm> getSubstitution();

    @Override
    ConstructionNode clone();

    @Override
    ConstructionNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException;

    /**
     * Variables that have to be provided by the child
     */
    ImmutableSet<Variable> getChildVariables();
}
