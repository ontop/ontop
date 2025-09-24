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
        this(Optional.empty(), Optional.of(descendingSubstitution), projectedVariables);
    }

    public DownPropagation(Optional<ImmutableExpression> constraint, Substitution<? extends VariableOrGroundTerm> descendingSubstitution, ImmutableSet<Variable> projectedVariables) {
        this(constraint, Optional.of(descendingSubstitution), projectedVariables);
    }

    public DownPropagation(Optional<ImmutableExpression> constraint, Optional<Substitution<? extends VariableOrGroundTerm>> optionalDescendingSubstitution, ImmutableSet<Variable> projectedVariables) {
        this.constraint = constraint;
        this.optionalDescendingSubstitution = optionalDescendingSubstitution;
        this.projectedVariables = projectedVariables;
    }

    public DownPropagation(Optional<ImmutableExpression> constraint, ImmutableSet<Variable> projectedVariables) {
        this(constraint, Optional.empty(), projectedVariables);
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

    public ImmutableExpression applySubstitution(ImmutableExpression expression) {
        return optionalDescendingSubstitution.map(s -> s.apply(expression)).get();
    }

    public Optional<ImmutableExpression> applySubstitution(Optional<ImmutableExpression> optionalExpression) {
        return optionalDescendingSubstitution
                .map(s -> optionalExpression.map(s::apply))
                .orElse(optionalExpression);
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

    public DownPropagation normalize(TermFactory termFactory) throws UnsatisfiableDescendingSubstitutionException {
        var normalizedSubstitution = normalizeDescendingSubstitution();

        if (constraint.isEmpty())
            return new DownPropagation(constraint, normalizedSubstitution, projectedVariables);

        ImmutableSet<?> newVariables = getVar2VarFragment().apply(projectedVariables);

        return new DownPropagation(
                termFactory.getConjunction(constraint.get().flattenAND()
                        .filter(e -> e.getVariableStream().anyMatch(newVariables::contains))),
                normalizedSubstitution, projectedVariables);
    }

    private Substitution<Variable> getVar2VarFragment() {
        var descendingSubstitution = optionalDescendingSubstitution.get();
        return descendingSubstitution.restrictRangeTo(Variable.class);
    }

    /**
     * If the substitution is a fresh renaming, returns it as an injective substitution
     */
    public Optional<InjectiveSubstitution<Variable>> extractFreshRenaming() {

        Substitution<Variable> var2VarFragment = getVar2VarFragment();

        var descendingSubstitution = optionalDescendingSubstitution.get();
        int size = descendingSubstitution.getDomain().size();

        if (var2VarFragment.getDomain().size() != size
                || Sets.difference(var2VarFragment.getRangeSet(), projectedVariables).size() != size)
            return Optional.empty();

        return Optional.of(var2VarFragment.injective());
    }

}
