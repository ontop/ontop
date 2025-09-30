package it.unibz.inf.ontop.iq;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.function.BiFunction;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public interface DownPropagation {

    ImmutableSet<Variable> getVariables();

    VariableGenerator getVariableGenerator();

    ImmutableSet<Variable> computeProjectedVariables();

    Optional<Substitution<? extends VariableOrGroundTerm>> getOptionalDescendingSubstitution();

    <R, T extends R> R withSubstitution(T value, BiFunction<Substitution<? extends VariableOrGroundTerm>, T, R> function);

    Optional<ImmutableExpression> getConstraint();

    VariableNullability extendVariableNullability(VariableNullability variableNullability);

    IQTree propagate(IQTree tree);

    IQTree propagateToChild(IQTree child);

    IQTree propagateWithConstraint(Optional<ImmutableExpression> constraint, IQTree tree);

    DownPropagation reduceScope(ImmutableSet<Variable> variables);

    /**
     * Thrown when a "null" variable is propagated down or when the constraint is inconsistent
     */
    class InconsistentDownPropagationException extends Exception {
    }

    static ImmutableSet<Variable> computeProjectedVariables(Substitution<? extends VariableOrGroundTerm> substitution, ImmutableSet<Variable> projectedVariables) {
        ImmutableSet<Variable> newVariables = substitution.restrictDomainTo(projectedVariables).getRangeVariables();
        return Sets.union(newVariables, Sets.difference(projectedVariables, substitution.getDomain())).immutableCopy();
    }
    
}
