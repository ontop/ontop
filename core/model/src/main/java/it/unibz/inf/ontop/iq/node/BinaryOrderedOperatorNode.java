package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.transform.IQTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

/**
 * The ordering of the operands is meaningful procedurally,
 * and therefore should be preserved.
 *
 * It may not be meaningful semantically though.
 * such that there may be instances of BinaryOrderedOperatorNode which are also instances of
 * CommutativeJoinOrFilterNode.
 */
public interface BinaryOrderedOperatorNode extends QueryNode {

    enum ArgumentPosition {
        LEFT,
        RIGHT
    }

    ImmutableSet<Variable> getNullableVariables(IQTree leftChild, IQTree rightChild);

    IQTree acceptTransformer(IQTree tree, IQTransformer transformer, IQTree leftChild, IQTree rightChild);

    IQTree normalizeForOptimization(IQTree leftChild, IQTree rightChild, VariableGenerator variableGenerator,
                                    IQProperties currentIQProperties);

    IQTree liftIncompatibleDefinitions(Variable variable, IQTree leftChild, IQTree rightChild);

    IQTree applyDescendingSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
            Optional<ImmutableExpression> constraint, IQTree leftChild, IQTree rightChild);

    boolean isConstructed(Variable variable, IQTree leftChild, IQTree rightChild);

    boolean isDistinct(IQTree leftChild, IQTree rightChild);

    IQTree propagateDownConstraint(ImmutableExpression constraint, IQTree leftChild, IQTree rightChild);

    /**
     * Only validates the node, not its children
     */
    void validateNode(IQTree leftChild, IQTree rightChild) throws InvalidIntermediateQueryException;

    IQTree removeDistincts(IQTree leftChild, IQTree rightChild, IQProperties properties);
}
