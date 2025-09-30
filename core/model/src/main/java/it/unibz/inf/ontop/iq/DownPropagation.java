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

public interface DownPropagation {
    static ImmutableSet<Variable> computeProjectedVariables(Substitution<? extends VariableOrGroundTerm> substitution, ImmutableSet<Variable> projectedVariables) {
        ImmutableSet<Variable> newVariables = substitution.restrictDomainTo(projectedVariables).getRangeVariables();
        return Sets.union(newVariables, Sets.difference(projectedVariables, substitution.getDomain())).immutableCopy();
    }


    ImmutableSet<Variable> getVariables();

    VariableGenerator getVariableGenerator();

    ImmutableSet<Variable> computeProjectedVariables();

    Optional<Substitution<? extends VariableOrGroundTerm>> getOptionalDescendingSubstitution();

    Optional<ImmutableExpression> applySubstitution(Optional<ImmutableExpression> optionalExpression);

    Optional<ImmutableExpression> getConstraint();

    DownPropagation withConstraint(Optional<ImmutableExpression> constraint, ImmutableSet<Variable> variables);

    VariableNullability extendVariableNullability(VariableNullability variableNullability);

    IQTree propagate(IQTree tree);

    default IQTree propagateToChild(IQTree child) {
        return reduceScope(child.getVariables()).propagate(child);
    }

    default IQTree propagateToChildWithConstraint(Optional<ImmutableExpression> constraint, IQTree tree) {
        return withConstraint(constraint, tree.getVariables()).propagate(tree);
    }

    DownPropagation reduceScope(ImmutableSet<Variable> variables);

    /**
     * Thrown when a "null" variable is propagated down or when the constraint is inconsistent
     */
    class InconsistentDownPropagationException extends Exception {
    }

}
