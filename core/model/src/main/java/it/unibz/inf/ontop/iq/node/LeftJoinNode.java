package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;

/**
 * See IntermediateQueryFactory for creating a new instance.
 */
public interface LeftJoinNode extends JoinLikeNode, BinaryNonCommutativeOperatorNode {

    @Override
    LeftJoinNode applyFreshRenaming(InjectiveSubstitution<Variable> renamingSubstitution);

    @Override
    default <T> T acceptVisitor(BinaryNonCommutativeIQTree tree, IQVisitor<T> visitor, IQTree leftChild, IQTree rightChild) {
        return visitor.transformLeftJoin(tree, this, leftChild, rightChild);
    }
}
