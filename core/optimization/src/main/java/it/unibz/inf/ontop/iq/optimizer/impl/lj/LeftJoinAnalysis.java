package it.unibz.inf.ontop.iq.optimizer.impl.lj;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.LeftJoinNode;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class LeftJoinAnalysis {

    private final LeftJoinNode leftJoin;
    private final IQTree leftChild;
    private final IQTree rightChild;

    private LeftJoinAnalysis(LeftJoinNode leftJoin, IQTree leftChild, IQTree rightChild) {
        this.leftJoin = leftJoin;
        this.leftChild = leftChild;
        this.rightChild = rightChild;
    }

    public static LeftJoinAnalysis of(LeftJoinNode leftJoin, IQTree leftChild, IQTree rightChild) {
        return new LeftJoinAnalysis(leftJoin, leftChild, rightChild);
    }

    public static LeftJoinAnalysis of(IQTreeTools.BinaryNonCommutativeIQTreeDecomposition<LeftJoinNode> decomposition) {
        return new LeftJoinAnalysis(decomposition.getNode(), decomposition.getLeftChild(), decomposition.getRightChild());
    }

    public LeftJoinNode getNode() {
        return leftJoin;
    }

    public Optional<ImmutableExpression> joinCondition() {
        return leftJoin.getOptionalFilterCondition();
    }

    public IQTree leftChild() {
        return leftChild;
    }

    public ImmutableSet<Variable> leftVariables() {
        return leftChild.getVariables();
    }

    public ImmutableSet<Variable> rightVariables() {
        return rightChild.getVariables();
    }

    public IQTree rightChild() {
        return rightChild;
    }

    public ImmutableSet<Variable> rightSpecificVariables() {
        return Sets.difference(rightChild.getVariables(), leftChild.getVariables()).immutableCopy();
    }

    /**
     * A LJ condition can be handled if it can safely be lifting, which requires that the LJ operates over a
     * unique constraint on the right side
     */
    public boolean tolerateLJConditionLifting() {
        return leftJoin.getOptionalFilterCondition().isEmpty() || rightChild.inferUniqueConstraints().stream()
                .anyMatch(uc -> leftChild.getVariables().containsAll(uc));
    }
}
