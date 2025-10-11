package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.DownPropagation;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.function.BiFunction;

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
    protected DownPropagation withConstraint(Optional<ImmutableExpression> optionalConstraint, ImmutableSet<Variable> variables) {
        var reducedSubstitution = reduceDescendingSubstitution(substitution, variables);
        var optionalNormalizedConstraint = normalizeConstraint(optionalConstraint, () -> variables, termFactory);
        if (!reducedSubstitution.isEmpty()) {
            return new RenamingDownPropagation(reducedSubstitution.injective(), optionalNormalizedConstraint, variables, variableGenerator, termFactory);
        }
        return new ConstraintOnlyDownPropagation(reducedSubstitution, optionalNormalizedConstraint, variables, variableGenerator, termFactory);
    }

    @Override
    public IQTree propagate(IQTree tree) {
        IQTree renamedTree = tree.applyFreshRenaming(substitution);
        return optionalConstraint.isPresent()
                ? renamedTree.propagateDownConstraint(new ConstraintOnlyDownPropagation(substitution.restrictDomainTo(ImmutableSet.of()),
                optionalConstraint, computeProjectedVariables(), variableGenerator, termFactory))
                : renamedTree;
    }
}
