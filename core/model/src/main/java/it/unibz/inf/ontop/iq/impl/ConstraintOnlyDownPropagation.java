package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.DownPropagation;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.function.BiFunction;

public class ConstraintOnlyDownPropagation extends AbstractDownPropagation implements DownPropagation {

    private final Substitution<? extends VariableOrGroundTerm> emptySubstitution;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    ConstraintOnlyDownPropagation(Substitution<? extends VariableOrGroundTerm> emptySubstitution, Optional<ImmutableExpression> optionalConstraint, ImmutableSet<Variable> variables, VariableGenerator variableGenerator, TermFactory termFactory) {
        super(optionalConstraint, variables, variableGenerator, termFactory);
        this.emptySubstitution = emptySubstitution;
    }

    @Override
    public ImmutableSet<Variable> computeProjectedVariables() {
        return variables;
    }

    @Override
    public Substitution<? extends VariableOrGroundTerm> getDescendingSubstitution() {
        return emptySubstitution;
    }

    @Override
    protected DownPropagation withConstraint(Optional<ImmutableExpression> optionalConstraint,  ImmutableSet<Variable> variables) {
        return new ConstraintOnlyDownPropagation(emptySubstitution,
                normalizeConstraint(optionalConstraint, () -> variables, termFactory),
                variables, variableGenerator, termFactory);
    }

    @Override
    public IQTree propagate(IQTree tree) {
        return optionalConstraint.isPresent()
                ? tree.propagateDownConstraint(this)
                : tree;
    }
}
