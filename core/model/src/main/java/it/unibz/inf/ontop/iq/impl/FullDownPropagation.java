package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.iq.DownPropagation;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

public class FullDownPropagation extends AbstractDownPropagation implements DownPropagation {
    private final Substitution<? extends VariableOrGroundTerm> substitution;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    FullDownPropagation(Substitution<? extends VariableOrGroundTerm> substitution, Optional<ImmutableExpression> optionalConstraint, ImmutableSet<Variable> variables, VariableGenerator variableGenerator, TermFactory termFactory) {
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
            return createNonEmptySubstitutionDownPropagation(reducedSubstitution, optionalNormalizedConstraint, variables, variableGenerator, termFactory);

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
    public DownPropagation extendToVariables(ImmutableSet<Variable> additionalVariables) {
        return new FullDownPropagation(substitution, optionalConstraint, Sets.union(variables, additionalVariables).immutableCopy(), variableGenerator, termFactory);
    }
}
