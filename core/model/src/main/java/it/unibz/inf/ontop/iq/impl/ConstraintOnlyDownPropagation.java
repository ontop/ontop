package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.DownPropagation;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

public class ConstraintOnlyDownPropagation extends AbstractDownPropagation implements DownPropagation {

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    ConstraintOnlyDownPropagation(Optional<ImmutableExpression> optionalConstraint, ImmutableSet<Variable> variables, VariableGenerator variableGenerator, TermFactory termFactory) {
        super(optionalConstraint, variables, variableGenerator, termFactory);
    }

    @Override
    public ImmutableSet<Variable> computeProjectedVariables() {
        return variables;
    }

    @Override
    public Optional<Substitution<? extends VariableOrGroundTerm>> getOptionalDescendingSubstitution() {
        return Optional.empty();
    }

    @Override
    public Optional<ImmutableExpression> applySubstitution(Optional<ImmutableExpression> optionalExpression) {
        return optionalExpression;
    }

    @Override
    public DownPropagation withConstraint(Optional<ImmutableExpression> optionalConstraint,  ImmutableSet<Variable> variables) {
        return new ConstraintOnlyDownPropagation(optionalConstraint, variables, variableGenerator, termFactory);
    }

    @Override
    public IQTree propagate(IQTree tree) {
        return optionalConstraint.isPresent()
                ? tree.propagateDownConstraint(this)
                : tree;
    }
}
