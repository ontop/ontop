package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.iq.DownPropagation;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.node.normalization.impl.ConditionSimplifierImpl;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ConstraintOnlyDownPropagation extends AbstractDownPropagation implements DownPropagation {

    private final Substitution<? extends VariableOrGroundTerm> emptySubstitution;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    ConstraintOnlyDownPropagation(Optional<ImmutableExpression> optionalConstraint, ImmutableSet<Variable> variables, VariableGenerator variableGenerator, TermFactory termFactory) {
        super(optionalConstraint, variables, variableGenerator, termFactory);
        this.emptySubstitution = termFactory.getSubstitution(ImmutableMap.of());
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
        return new ConstraintOnlyDownPropagation(
                normalizeConstraint(optionalConstraint, () -> variables, termFactory),
                variables, variableGenerator, termFactory);
    }

    @Override
    public IQTree propagate(IQTree tree) {
        return optionalConstraint.isPresent()
                ? checkScope(tree).propagateDownConstraint(this)
                : checkScope(tree);
    }

    @Override
    public DownPropagation filterConstraint(Predicate<ImmutableExpression> filter) {
        return new ConstraintOnlyDownPropagation(getFilteredConstraint(filter), variables, variableGenerator, termFactory);
    }

    @Override
    public DownPropagation applySubstitutionToConstraint(Substitution<? extends ImmutableTerm> substitution, Supplier<VariableNullability> variableNullabilitySupplier) throws InconsistentDownPropagationException {
        Optional<ImmutableExpression> optionalSubstitutedConstraint = optionalConstraint.map(substitution::apply);

        Optional<ImmutableExpression> newConstraint = optionalSubstitutedConstraint.isPresent() && !optionalSubstitutedConstraint.equals(optionalConstraint)
                ? ConditionSimplifierImpl.evaluateCondition(optionalSubstitutedConstraint.get(), extendVariableNullability(variableNullabilitySupplier.get()))
                : optionalSubstitutedConstraint;

        return new ConstraintOnlyDownPropagation(
                newConstraint,
                Sets.union(Sets.difference(variables, substitution.getDomain()), substitution.getRangeVariables()).immutableCopy(),
                variableGenerator,
                termFactory);
    }

    @Override
    public DownPropagation extendToChildVariables(ImmutableSet<Variable> childVariables) {
        if (!childVariables.containsAll(variables))
            throw new IllegalArgumentException("Child variables must contain all of the variables in the same constraint");

        return new ConstraintOnlyDownPropagation(optionalConstraint, childVariables, variableGenerator, termFactory);
    }
}
