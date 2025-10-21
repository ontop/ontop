package it.unibz.inf.ontop.iq;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public interface DownPropagation {

    ImmutableSet<Variable> getVariables();

    VariableGenerator getVariableGenerator();

    ImmutableSet<Variable> computeProjectedVariables();

    /**
     * can be empty
     *
     * @return
     */
    Substitution<? extends VariableOrGroundTerm> getDescendingSubstitution();

    default Optional<ImmutableExpression> applyDescendingSubstitution(Optional<ImmutableExpression> optionalExpression) {
        return optionalExpression.map(getDescendingSubstitution()::apply);
    }

    Optional<ImmutableExpression> getConstraint();

    VariableNullability extendVariableNullability(VariableNullability variableNullability);

    IQTree propagate(IQTree tree);

    DownPropagation withNoConstraint();

    DownPropagation filterConstraint(Predicate<ImmutableExpression> filter);

    DownPropagation applySubstitutionToConstraint(Substitution<? extends ImmutableTerm> substitution, Supplier<VariableNullability> variableNullabilitySupplier) throws InconsistentDownPropagationException;

    DownPropagation extendToChildVariables(ImmutableSet<Variable> childVariables);

    DownPropagation restrictScope(ImmutableSet<Variable> variables);

    default IQTree propagateWithRestrictedScope(IQTree tree) {
        return restrictScope(tree.getVariables()).propagate(tree);
    }

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
