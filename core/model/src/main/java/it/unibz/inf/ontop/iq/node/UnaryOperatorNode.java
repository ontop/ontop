package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.VariableOrGroundTermSubstitution;

import java.util.Optional;

/**
 * Has ONE child
 */
public interface UnaryOperatorNode extends QueryNode {

    IQTree liftBinding(IQTree liftedChildIQTree);

    IQTree applyDescendingSubstitution(VariableOrGroundTermSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                       Optional<ImmutableExpression> constraint, IQTree child);
}
