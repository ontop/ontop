package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.Variable;

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
    default <T> T acceptVisitor(NaryIQTree tree, IQVisitor<T> visitor, ImmutableList<IQTree> children) {
        return visitor.transformUnion(tree, this, children);
    }
}
