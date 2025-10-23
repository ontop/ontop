package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.iq.DownPropagation;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

public class ConstraintOnlyDownPropagation extends AbstractDownPropagation implements DownPropagation {

    private final Substitution<? extends VariableOrGroundTerm> emptySubstitution;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    ConstraintOnlyDownPropagation(Optional<ImmutableExpression> optionalConstraint, ImmutableSet<Variable> variables, VariableGenerator variableGenerator, TermFactory termFactory) {
        super(optionalConstraint, variables, variableGenerator, termFactory);
        this.emptySubstitution = termFactory.getSubstitution(ImmutableMap.of());
    }

    @Override
    public ImmutableSet<Variable> getResultingProjectedVariables() {
        return variables;
    }

    @Override
    public Substitution<? extends VariableOrGroundTerm> getDescendingSubstitution() {
        return emptySubstitution;
    }

    @Override
    protected DownPropagation withReducedScope(ImmutableSet<Variable> variables) {
        return new ConstraintOnlyDownPropagation(
                normalizeConstraint(optionalConstraint, variables, emptySubstitution, termFactory),
                variables, variableGenerator, termFactory);
    }

    @Override
    public IQTree propagate(IQTree tree) {
        return optionalConstraint.isPresent()
                ? checkScope(tree).propagateDownConstraint(this)
                : checkScope(tree);
    }

    @Override
    protected DownPropagation updateConstraint(Optional<ImmutableExpression> constraint) {
        return new ConstraintOnlyDownPropagation(constraint, variables, variableGenerator, termFactory);
    }

    @Override
    public DownPropagation extendToVariables(ImmutableSet<Variable> additionalVariables) {
        return new ConstraintOnlyDownPropagation(optionalConstraint, Sets.union(variables, additionalVariables).immutableCopy(), variableGenerator, termFactory);
    }
}
