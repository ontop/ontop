package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.iq.DownPropagation;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

public class RenamingDownPropagation extends AbstractDownPropagation implements DownPropagation {
    private final InjectiveSubstitution<Variable> substitution;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    RenamingDownPropagation(InjectiveSubstitution<Variable> substitution, Optional<ImmutableExpression> optionalConstraint, ImmutableSet<Variable> variables, VariableGenerator variableGenerator, TermFactory termFactory) {
        super(optionalConstraint, variables, variableGenerator, termFactory);
        this.substitution = substitution;
    }

    @Override
    public ImmutableSet<Variable> getResultingProjectedVariables() {
        return DownPropagation.getProjectedVariablesAfterDescendingSubstitution(substitution, variables);
    }

    @Override
    public Substitution<? extends VariableOrGroundTerm> getDescendingSubstitution() {
        return substitution;
    }

    @Override
    protected DownPropagation withReducedScope(ImmutableSet<Variable> variables) {
        var reducedSubstitution = reduceDescendingSubstitution(substitution, variables);
        var optionalNormalizedConstraint = normalizeConstraint(optionalConstraint, variables, substitution, termFactory);
        if (!reducedSubstitution.isEmpty())
            return new RenamingDownPropagation(reducedSubstitution.injective(), optionalNormalizedConstraint, variables, variableGenerator, termFactory);

        return new ConstraintOnlyDownPropagation(optionalNormalizedConstraint, variables, variableGenerator, termFactory);
    }

    @Override
    public IQTree propagate(IQTree tree) {
        IQTree renamedTree = checkScope(tree).applyFreshRenaming(substitution);
        return optionalConstraint.isPresent()
                ? renamedTree.propagateDownConstraint(new ConstraintOnlyDownPropagation(optionalConstraint, getResultingProjectedVariables(), variableGenerator, termFactory))
                : renamedTree;
    }

    @Override
    protected DownPropagation updateConstraint(Optional<ImmutableExpression> constraint) {
        return new RenamingDownPropagation(substitution, constraint, variables, variableGenerator, termFactory);
    }

    @Override
    public DownPropagation extendToVariables(ImmutableSet<Variable> additionalVariables) {
        return new RenamingDownPropagation(substitution, optionalConstraint, Sets.union(variables, additionalVariables).immutableCopy(), variableGenerator, termFactory);
    }
}
