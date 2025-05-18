package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;


@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class DownPropagation {
    private final Optional<ImmutableExpression> constraint;
    private final Optional<Substitution<? extends VariableOrGroundTerm>> optionalDescendingSubstitution;
    private final ImmutableSet<Variable> projectedVariables;


    public DownPropagation(Substitution<? extends VariableOrGroundTerm> descendingSubstitution, ImmutableSet<Variable> projectedVariables) {
        this.constraint = Optional.empty();
        this.optionalDescendingSubstitution = Optional.of(descendingSubstitution);
        this.projectedVariables = projectedVariables;
    }

    public DownPropagation(Optional<ImmutableExpression> constraint, Substitution<? extends VariableOrGroundTerm> descendingSubstitution, ImmutableSet<Variable> projectedVariables) {
        this.constraint = constraint;
        this.optionalDescendingSubstitution = Optional.of(descendingSubstitution);
        this.projectedVariables = projectedVariables;
    }

    public DownPropagation(Optional<ImmutableExpression> constraint, ImmutableSet<Variable> projectedVariables) {
        this.constraint = constraint;
        this.optionalDescendingSubstitution = Optional.empty();
        this.projectedVariables = projectedVariables;
    }

    public DownPropagation(ImmutableSet<Variable> projectedVariables) {
        this(Optional.empty(), projectedVariables);
    }

    public ImmutableSet<Variable> getVariables() {
        return projectedVariables;
    }

    public ImmutableSet<Variable> computeProjectedVariables() {
        if (optionalDescendingSubstitution.isEmpty())
            return projectedVariables;

        ImmutableSet<Variable> newVariables = optionalDescendingSubstitution.get().restrictDomainTo(projectedVariables).getRangeVariables();

        return Sets.union(newVariables, Sets.difference(projectedVariables, optionalDescendingSubstitution.get().getDomain())).immutableCopy();
    }


    public Optional<Substitution<? extends VariableOrGroundTerm>> getOptionalDescendingSubstitution() {
        return optionalDescendingSubstitution;
    }

    public Substitution<? extends VariableOrGroundTerm> getSubstitution() {
        return optionalDescendingSubstitution.get();
    }

    public Optional<ImmutableExpression> getConstraint() {
        return constraint;
    }


    public VariableNullability extendVariableNullability(VariableNullability variableNullability) {
        return constraint
                .map(c -> variableNullability.extendToExternalVariables(c.getVariableStream()))
                .orElse(variableNullability);
    }

    public IQTree propagateDownOptionalConstraint(IQTree child, VariableGenerator variableGenerator) {
        return constraint.map(c -> child.propagateDownConstraint(c, variableGenerator))
                .orElse(child);
    }

    public IQTree propagate(IQTree child, VariableGenerator variableGenerator) {
        return optionalDescendingSubstitution
                .filter(s -> !s.isEmpty())
                .map(s -> child.applyDescendingSubstitution(s, constraint, variableGenerator))
                .orElseGet(() -> propagateDownOptionalConstraint(child, variableGenerator));
    }

    public ImmutableList<IQTree> propagate(ImmutableList<IQTree> children, VariableGenerator variableGenerator) {
        return optionalDescendingSubstitution
                .filter(s -> !s.isEmpty())
                .map(s -> children.stream()
                        .map(c -> c.applyDescendingSubstitution(s, constraint, variableGenerator))
                        .collect(ImmutableCollectors.toList()))
                .or(() -> constraint
                        .map(cs -> children.stream()
                                .map(c -> c.propagateDownConstraint(cs, variableGenerator))
                                .collect(ImmutableCollectors.toList())))
                .orElse(children);
    }


    /**
     * If the substitution is a fresh renaming, returns it as an injective substitution
     */
    public Optional<InjectiveSubstitution<Variable>> extractFreshRenaming() {

        var descendingSubstitution = optionalDescendingSubstitution.get();
        Substitution<Variable> var2VarFragment = descendingSubstitution.restrictRangeTo(Variable.class);
        int size = descendingSubstitution.getDomain().size();

        if (var2VarFragment.getDomain().size() != size
                || Sets.difference(var2VarFragment.getRangeSet(), projectedVariables).size() != size)
            return Optional.empty();

        return Optional.of(var2VarFragment.injective());
    }

    /**
     * Excludes the variables that are not projected by the IQTree
     *
     * If a "null" variable is propagated down, throws an UnsatisfiableDescendingSubstitutionException.
     *
     */
    public Optional<Substitution<? extends VariableOrGroundTerm>> normalizeDescendingSubstitution()
            throws UnsatisfiableDescendingSubstitutionException {

        var descendingSubstitution = optionalDescendingSubstitution.get();
        Substitution<? extends VariableOrGroundTerm> reducedSubstitution = descendingSubstitution.restrictDomainTo(projectedVariables);

        if (reducedSubstitution.isEmpty())
            return Optional.empty();

        if (reducedSubstitution.rangeAnyMatch(ImmutableTerm::isNull))
            throw new UnsatisfiableDescendingSubstitutionException();

        return Optional.of(reducedSubstitution);
    }


    /**
     * Typically thrown when a "null" variable is propagated down
     *
     */
    public static class UnsatisfiableDescendingSubstitutionException extends Exception {
    }

    public DownPropagation normalizeConstraint(TermFactory termFactory) {
        if (constraint.isEmpty())
            return this;

        var descendingSubstitution = optionalDescendingSubstitution.get();
        ImmutableSet<Variable> newVariables = projectedVariables.stream()
                .map(descendingSubstitution::apply)
                .filter(t -> t instanceof Variable)
                .map(t -> (Variable)t)
                .collect(ImmutableCollectors.toSet());

        return new DownPropagation(
                termFactory.getConjunction(constraint.get().flattenAND()
                        .filter(e -> e.getVariableStream().anyMatch(newVariables::contains))),
                descendingSubstitution, getVariables());
    }
}
