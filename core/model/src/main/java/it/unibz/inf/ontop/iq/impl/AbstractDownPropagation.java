package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.iq.DownPropagation;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;


@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public abstract class AbstractDownPropagation implements DownPropagation {
    protected final Optional<ImmutableExpression> optionalConstraint;
    protected final ImmutableSet<Variable> variables;
    protected final VariableGenerator variableGenerator;
    protected final TermFactory termFactory;

    AbstractDownPropagation(Optional<ImmutableExpression> optionalConstraint, ImmutableSet<Variable> variables, VariableGenerator variableGenerator, TermFactory termFactory) {
        this.optionalConstraint = optionalConstraint;
        this.variableGenerator = variableGenerator;
        this.variables = variables;
        this.termFactory = termFactory;
    }

    @Override
    public Optional<ImmutableExpression> getConstraint() {
        return optionalConstraint;
    }

    @Override
    public VariableNullability extendVariableNullability(VariableNullability variableNullability) {
        return optionalConstraint.map(c -> variableNullability.extendToExternalVariables(c.getVariableStream()))
                .orElse(variableNullability);
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        return variables;
    }

    @Override
    public VariableGenerator getVariableGenerator() {
        return variableGenerator;
    }


    protected final IQTree checkScope(IQTree tree) {
        if (!this.variables.equals(tree.getVariables()))
            throw new IllegalArgumentException("Variables " + variables + " do not match " + tree);
        return tree;
    }

    protected abstract DownPropagation withReducedScope(ImmutableSet<Variable> variables);

    protected final Optional<ImmutableExpression> getFilteredConstraint(Predicate<ImmutableExpression> filter) {
        return optionalConstraint.flatMap(
                constraint -> termFactory.getConjunction(constraint.flattenAND().filter(filter)));
    }

    @Override
    public DownPropagation restrictScope(ImmutableSet<Variable> newVariables) {
        if (!variables.containsAll(newVariables))
            throw new IllegalArgumentException("Variables " + newVariables + " are not included in " + this.variables);

        if (variables.size() == newVariables.size())
            return this;

        return withReducedScope(newVariables);
    }

    /**
     * If the substitution is a fresh renaming, returns it as an injective substitution
     */
    static Optional<InjectiveSubstitution<Variable>> transformIntoFreshRenaming(Substitution<? extends VariableOrGroundTerm> descendingSubstitution, ImmutableSet<Variable> projectedVariables) {

        Substitution<Variable> var2VarFragment = descendingSubstitution.restrictRangeTo(Variable.class);

        int size = descendingSubstitution.getDomain().size();

        if (var2VarFragment.getDomain().size() != size
                || Sets.difference(var2VarFragment.getRangeSet(), projectedVariables).size() != size)
            return Optional.empty();

        return Optional.of(var2VarFragment.injective());
    }

    @Override
    public final DownPropagation withNoConstraint() {
        return updateConstraint(Optional.empty());
    }

    @Override
    public final DownPropagation filterConstraint(Predicate<ImmutableExpression> filter) {
        return updateConstraint(getFilteredConstraint(filter));
    }

    protected abstract DownPropagation updateConstraint(Optional<ImmutableExpression> constraint);

    static <T extends VariableOrGroundTerm> Substitution<T> reduceDescendingSubstitution(Substitution<T> descendingSubstitution, ImmutableSet<Variable> projectedVariables) {
        return descendingSubstitution.restrictDomainTo(projectedVariables);
    }

    static Optional<ImmutableExpression> normalizeConstraint(Optional<ImmutableExpression> optionalConstraint, Supplier<ImmutableSet<Variable>> projectedVariablesSupplier, TermFactory termFactory) {
        if (optionalConstraint.isPresent()) {
            var projectedVariables = projectedVariablesSupplier.get();
            return termFactory.getConjunction(optionalConstraint.get().flattenAND()
                    .filter(e -> e.getVariableStream().anyMatch(projectedVariables::contains)));
        }
        return Optional.empty();
    }
}






