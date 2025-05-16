package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;

public class DescendingSubstitution {
    private final Substitution<? extends VariableOrGroundTerm> descendingSubstitution;
    private final ImmutableSet<Variable> projectedVariables;


    public DescendingSubstitution(Substitution<? extends VariableOrGroundTerm> descendingSubstitution, ImmutableSet<Variable> projectedVariables) {
        this.descendingSubstitution = descendingSubstitution;
        this.projectedVariables = projectedVariables;
    }

    public Substitution<? extends VariableOrGroundTerm> getSubstitution() {
        return descendingSubstitution;
    }

    public ImmutableSet<Variable> getVariables() {
        return projectedVariables;
    }

    public ImmutableSet<Variable> computeProjectedVariables() {

        ImmutableSet<Variable> newVariables = descendingSubstitution.restrictDomainTo(projectedVariables).getRangeVariables();

        return Sets.union(newVariables, Sets.difference(projectedVariables, descendingSubstitution.getDomain())).immutableCopy();
    }

    /**
     * If the substitution is a fresh renaming, returns it as an injective substitution
     */
    public Optional<InjectiveSubstitution<Variable>> extractFreshRenaming() {

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

        Substitution<? extends VariableOrGroundTerm> reducedSubstitution = descendingSubstitution.restrictDomainTo(projectedVariables);

        if (reducedSubstitution.isEmpty())
            return Optional.empty();

        if (reducedSubstitution.rangeAnyMatch(ImmutableTerm::isNull))
            throw new UnsatisfiableDescendingSubstitutionException();

        return Optional.of(reducedSubstitution);
    }

    public ImmutableSet<Variable> getVariableImage() {
        return projectedVariables.stream()
                .map(descendingSubstitution::apply)
                .filter(t -> t instanceof Variable)
                .map(t -> (Variable)t)
                .collect(ImmutableCollectors.toSet());
    }

    /**
     * Typically thrown when a "null" variable is propagated down
     *
     */
    public static class UnsatisfiableDescendingSubstitutionException extends Exception {
    }

}
