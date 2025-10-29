package it.unibz.inf.ontop.iq.node.normalization;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.DownPropagation;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.Substitution;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public interface ConditionSimplifier {

    ExpressionAndSubstitution simplifyCondition(Optional<ImmutableExpression> expression,
                                                ImmutableSet<Variable> nonLiftableVariables,
                                                ImmutableList<IQTree> children,
                                                VariableNullability variableNullability)
                    throws DownPropagation.InconsistentDownPropagationException;

    ExpressionAndSubstitution simplifyConditionForLeftJoin(Optional<ImmutableExpression> nonOptimizedExpression,
                                                Function<ImmutableExpression, VariableNullability> variableNullability,
                                                ImmutableSet<Variable> leftVariables, ImmutableSet<Variable> rightVariables)
            throws DownPropagation.InconsistentDownPropagationException;

    DownPropagation getCombinedDownPropagation(DownPropagation dp, ExpressionAndSubstitution simplification, VariableNullability variableNullability) throws DownPropagation.InconsistentDownPropagationException;

    interface ExpressionAndSubstitution {
        Substitution<VariableOrGroundTerm> getSubstitution();
        Optional<ImmutableExpression> getOptionalExpression();
    }

}
