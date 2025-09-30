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
    public Optional<Substitution<? extends VariableOrGroundTerm>> getOptionalDescendingSubstitution() {
        return Optional.of(substitution);
    }

    @Override
    public Optional<ImmutableExpression> applySubstitution(Optional<ImmutableExpression> optionalExpression) {
        return optionalExpression.map(substitution::apply);
    }

    @Override
    public DownPropagation withConstraint(Optional<ImmutableExpression> optionalConstraint, ImmutableSet<Variable> variables) {
        var reducedSubstitution = AbstractDownPropagation.reduceDescendingSubstitution(substitution, variables);
        if (reducedSubstitution.isPresent()) {
            var normalizedConstraint = AbstractDownPropagation.normalizeConstraint(optionalConstraint, () -> variables, termFactory);
            return new RenamingDownPropagation(reducedSubstitution.get().injective(), normalizedConstraint, variables, variableGenerator, termFactory);
        }
        return new ConstraintOnlyDownPropagation(optionalConstraint, variables, variableGenerator, termFactory);
    }

    @Override
    public IQTree propagate(IQTree tree) {
        IQTree renamedTree = tree.applyFreshRenaming(substitution);
        return optionalConstraint.isPresent()
                ? renamedTree.propagateDownConstraint(new ConstraintOnlyDownPropagation(
                optionalConstraint, computeProjectedVariables(), variableGenerator, termFactory))
                : renamedTree;
    }
}
