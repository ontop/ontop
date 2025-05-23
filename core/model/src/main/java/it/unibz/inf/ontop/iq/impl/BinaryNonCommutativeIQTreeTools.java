package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.LeftJoinNode;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;

public class BinaryNonCommutativeIQTreeTools {

    public static class LeftJoinDecomposition extends IQTreeTools.IQTreeDecomposition<LeftJoinNode, BinaryNonCommutativeIQTree>  {

        protected final IQTree leftChild;
        protected final IQTree rightChild;

        private LeftJoinDecomposition(LeftJoinNode node, BinaryNonCommutativeIQTree tree) {
            super(node, tree);
            this.leftChild = tree.getLeftChild();
            this.rightChild = tree.getRightChild();
        }

        private LeftJoinDecomposition(LeftJoinNode node, IQTree leftChild, IQTree rightChild) {
            super(node, null);
            this.leftChild = leftChild;
            this.rightChild = rightChild;
        }

        public Optional<ImmutableExpression> joinCondition() {
            return node.getOptionalFilterCondition();
        }

        public static LeftJoinDecomposition of(IQTree tree) {
            return tree.getRootNode() instanceof LeftJoinNode
                    ? new LeftJoinDecomposition((LeftJoinNode)tree.getRootNode(), ((BinaryNonCommutativeIQTree)tree))
                    : new LeftJoinDecomposition(null, null, null);
        }

        public static LeftJoinDecomposition of(LeftJoinNode node, IQTree leftChild, IQTree rightChild) {
            return new LeftJoinDecomposition(node, leftChild, rightChild);
        }

        @Nonnull
        public IQTree leftChild() {
            return Objects.requireNonNull(leftChild);
        }

        @Nonnull
        public IQTree rightChild() {
            return Objects.requireNonNull(rightChild);
        }


        public ImmutableSet<Variable> commonVariables() {
            return BinaryNonCommutativeIQTreeTools.commonVariables(leftChild, rightChild).immutableCopy();
        }

        public ImmutableSet<Variable> projectedVariables() {
            return BinaryNonCommutativeIQTreeTools.projectedVariables(leftChild, rightChild).immutableCopy();
        }

        public ImmutableSet<Variable> rightSpecificVariables() {
            return BinaryNonCommutativeIQTreeTools.rightSpecificVariables(leftChild, rightChild).immutableCopy();
        }

        public ImmutableSet<Variable> rightVariables() {
            return rightChild.getVariables();
        }

        public ImmutableSet<Variable> leftVariables() {
            return leftChild.getVariables();
        }

        /**
         * A LJ condition can be handled if it can safely be lifting, which requires that the LJ operates over a
         * unique constraint on the right side
         */
        public boolean tolerateLJConditionLifting() {
            return joinCondition().isEmpty()
                    || rightChild.inferUniqueConstraints().stream()
                        .anyMatch(uc -> leftChild.getVariables().containsAll(uc));
        }
    }

    public static Sets.SetView<Variable> projectedVariables(IQTree leftChild, IQTree rightChild) {
        return Sets.union(leftChild.getVariables(), rightChild.getVariables());
    }

    public static Sets.SetView<Variable> rightSpecificVariables(IQTree leftChild, IQTree rightChild) {
        return Sets.difference(rightChild.getVariables(), leftChild.getVariables());
    }

    public static Sets.SetView<Variable> commonVariables(IQTree leftChild, IQTree rightChild) {
        return Sets.intersection(leftChild.getVariables(), rightChild.getVariables());
    }
}
