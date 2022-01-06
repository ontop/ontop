package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;

/**
 * All its children are expected to project its projected variables
 *
 * See IntermediateQueryFactory for creating a new instance.
 */
public interface UnionNode extends ExplicitVariableProjectionNode, NaryOperatorNode {

    @Override
    UnionNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException;

    /**
     * Returns true if its has, as a child, a construction node defining the variable.
     *
     * To be called on already lifted tree.
     */
    boolean hasAChildWithLiftableDefinition(Variable variable, ImmutableList<IQTree> children);

    /**
     * Makes the tree be distinct
     */
    IQTree makeDistinct(ImmutableList<IQTree> children);
}
