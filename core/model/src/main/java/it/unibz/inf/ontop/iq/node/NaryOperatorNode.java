package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

/**
 * Has at least two children
 */
public interface NaryOperatorNode extends QueryNode {

    IQTree liftBinding(ImmutableList<IQTree> children, VariableGenerator variableGenerator,
                       IQProperties currentIQProperties);

    IQTree applyDescendingSubstitution(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                       Optional<ImmutableExpression> constraint, ImmutableList<IQTree> children);

    ImmutableSet<Variable> getNullableVariables(ImmutableList<IQTree> children);
}
