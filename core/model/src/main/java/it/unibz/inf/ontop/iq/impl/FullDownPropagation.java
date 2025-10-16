package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.DownPropagation;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class FullDownPropagation extends AbstractDownPropagation implements DownPropagation {
    private final Substitution<? extends VariableOrGroundTerm> substitution;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    FullDownPropagation(Substitution<? extends VariableOrGroundTerm> substitution, Optional<ImmutableExpression> optionalConstraint, ImmutableSet<Variable> variables, VariableGenerator variableGenerator, TermFactory termFactory) {
        super(optionalConstraint, variables, variableGenerator, termFactory);
        this.substitution = substitution;
    }

    @Override
    public ImmutableSet<Variable> computeProjectedVariables() {
        return DownPropagation.computeProjectedVariables(substitution, variables);
    }

    @Override
    public Substitution<? extends VariableOrGroundTerm> getDescendingSubstitution() {
        return substitution;
    }

    @Override
    protected DownPropagation withConstraint(Optional<ImmutableExpression> optionalConstraint,  ImmutableSet<Variable> variables) {
        var reducedSubstitution = reduceDescendingSubstitution(substitution, variables);
        var optionalNormalizedConstraint = normalizeConstraint(optionalConstraint, () -> variables, termFactory);
        if (!reducedSubstitution.isEmpty()) {
            var optionalRenaming = transformIntoFreshRenaming(reducedSubstitution, variables);
            return optionalRenaming.isPresent()
                    ? new RenamingDownPropagation(optionalRenaming.get(), optionalNormalizedConstraint, variables, variableGenerator, termFactory)
                    : new FullDownPropagation(reducedSubstitution, optionalNormalizedConstraint, variables, variableGenerator, termFactory);
        }
        return new ConstraintOnlyDownPropagation(optionalNormalizedConstraint, variables, variableGenerator, termFactory);
    }

    @Override
    public IQTree propagate(IQTree tree) {
        return checkScope(tree).applyDescendingSubstitution(this);
    }

    @Override
    protected DownPropagation updateConstraint(Optional<ImmutableExpression> constraint) {
        return new FullDownPropagation(substitution, constraint, variables, variableGenerator, termFactory);
    }

    @Override
    public DownPropagation applySubstitutionToConstraint(Substitution<? extends ImmutableTerm> substitution, Supplier<VariableNullability> variableNullabilitySupplier) throws InconsistentDownPropagationException {
        throw new UnsupportedOperationException("FullDownPropagation does not support applySubstitutionToConstraint");
    }

    @Override
    public DownPropagation extendToChildVariables(ImmutableSet<Variable> childVariables) {
        if (!childVariables.containsAll(variables))
            throw new IllegalArgumentException("Child variables must contain all of the variables in the same constraint");

        return new FullDownPropagation(substitution, optionalConstraint, childVariables, variableGenerator, termFactory);
    }
}
