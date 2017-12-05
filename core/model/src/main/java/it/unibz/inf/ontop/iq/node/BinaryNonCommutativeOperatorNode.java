package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.VariableOrGroundTermSubstitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

/**
 * Operator QueryNode which are binary and whose operands ordering is semantically meaningful.
 *
 * For instance: Left Join.
 */
public interface BinaryNonCommutativeOperatorNode extends BinaryOrderedOperatorNode {

    IQTree liftBinding(IQTree leftChild, IQTree rightChild, VariableGenerator variableGenerator);

    IQTree applyDescendingSubstitution(
            VariableOrGroundTermSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
            Optional<ImmutableExpression> constraint, IQTree leftChild, IQTree rightChild);
}
