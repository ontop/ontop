package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.transform.IQTreeExtendedTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;

/**
 * All its children are expected to project its projected variables
 *
 * See IntermediateQueryFactory for creating a new instance.
 */
public interface UnionNode extends ExplicitVariableProjectionNode, NaryOperatorNode {

    /**
     * Returns true if it has, as a child, a construction node defining the variable.
     *
     * To be called on an already lifted tree.
     */
    boolean hasAChildWithLiftableDefinition(Variable variable, ImmutableList<IQTree> children);

    /**
     * Makes the tree be distinct
     */
    IQTree makeDistinct(ImmutableList<IQTree> children);

    @Override
    default IQTree acceptTransformer(IQTree tree, IQTreeVisitingTransformer transformer, ImmutableList<IQTree> children) {
        return transformer.transformUnion(tree,this, children);
    }

    @Override
    default <T> IQTree acceptTransformer(IQTree tree, IQTreeExtendedTransformer<T> transformer,
                                        ImmutableList<IQTree> children, T context) {
        return transformer.transformUnion(tree,this, children, context);
    }

    @Override
    default <T> T acceptVisitor(IQVisitor<T> visitor, ImmutableList<IQTree> children) {
        return visitor.visitUnion(this, children);
    }

    @Override
    default UnionNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException {
        return transformer.transform(this);
    }
}
