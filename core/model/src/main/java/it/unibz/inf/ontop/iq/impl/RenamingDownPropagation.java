package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.DownPropagation;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class RenamingDownPropagation extends AbstractDownPropagation implements DownPropagation {
    private final InjectiveSubstitution<Variable> substitution;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    RenamingDownPropagation(InjectiveSubstitution<Variable> substitution, Optional<ImmutableExpression> optionalConstraint, ImmutableSet<Variable> variables, VariableGenerator variableGenerator, TermFactory termFactory) {
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
    protected DownPropagation withReducedScope(ImmutableSet<Variable> variables) {
        var reducedSubstitution = reduceDescendingSubstitution(substitution, variables);
        var optionalNormalizedConstraint = normalizeConstraint(optionalConstraint, () -> variables, termFactory);
        if (!reducedSubstitution.isEmpty()) {
            return new RenamingDownPropagation(reducedSubstitution.injective(), optionalNormalizedConstraint, variables, variableGenerator, termFactory);
        }
        return new ConstraintOnlyDownPropagation(optionalNormalizedConstraint, variables, variableGenerator, termFactory);
    }

    @Override
    public IQTree propagate(IQTree tree) {
        IQTree renamedTree = checkScope(tree).applyFreshRenaming(substitution);
        return optionalConstraint.isPresent()
                ? renamedTree.propagateDownConstraint(new ConstraintOnlyDownPropagation(optionalConstraint, computeProjectedVariables(), variableGenerator, termFactory))
                : renamedTree;
    }

    @Override
    protected DownPropagation updateConstraint(Optional<ImmutableExpression> constraint) {
        return new RenamingDownPropagation(substitution, constraint, variables, variableGenerator, termFactory);
    }

    @Override
    public DownPropagation extendToChildVariables(ImmutableSet<Variable> childVariables) {
        if (!childVariables.containsAll(variables))
            throw new IllegalArgumentException("Child variables must contain all of the variables in the same constraint");

        return new RenamingDownPropagation(substitution, optionalConstraint, childVariables, variableGenerator, termFactory);
    }

}
