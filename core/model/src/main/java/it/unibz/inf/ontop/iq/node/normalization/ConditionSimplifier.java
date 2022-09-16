package it.unibz.inf.ontop.iq.node.normalization;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.node.impl.UnsatisfiableConditionException;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

import java.util.Optional;

public interface ConditionSimplifier {

    ExpressionAndSubstitution simplifyCondition(ImmutableExpression expression, ImmutableList<IQTree> children,
                                                VariableNullability variableNullability)
            throws UnsatisfiableConditionException;

    ExpressionAndSubstitution simplifyCondition(Optional<ImmutableExpression> nonOptimizedExpression,
                                                ImmutableSet<Variable> nonLiftableVariables, ImmutableList<IQTree> children,
                                                VariableNullability variableNullability)
                    throws UnsatisfiableConditionException;

    Optional<ImmutableExpression> computeDownConstraint(Optional<ImmutableExpression> optionalConstraint,
                                                        ExpressionAndSubstitution conditionSimplificationResults,
                                                        VariableNullability childVariableNullability)
                            throws UnsatisfiableConditionException;


    interface ExpressionAndSubstitution {
        ImmutableSubstitution<VariableOrGroundTerm> getSubstitution();

        Optional<ImmutableExpression> getOptionalExpression();
    }
}
