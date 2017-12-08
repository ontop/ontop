package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.VariableOrGroundTermSubstitution;
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

    IQTree liftBinding(IQTree leftChild, IQTree rightChild, VariableGenerator variableGenerator);

    IQTree applyDescendingSubstitution(
            VariableOrGroundTermSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
            Optional<ImmutableExpression> constraint, IQTree leftChild, IQTree rightChild);
}
