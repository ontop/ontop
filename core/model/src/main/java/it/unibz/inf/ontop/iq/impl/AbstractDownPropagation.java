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

    @Override
    public DownPropagation reduceScope(ImmutableSet<Variable> variables) {
        if (!this.variables.containsAll(variables))
            throw new IllegalArgumentException("Variables " +  variables + " are not included in " + this.variables);

        if (this.variables.size() == variables.size())
            return this;

        return withConstraint(optionalConstraint, variables);
    }

    @Override
    public IQTree propagateToChild(IQTree child) {
        return reduceScope(child.getVariables()).propagate(child);
    }

    @Override
    public IQTree propagateWithConstraint(Optional<ImmutableExpression> constraint, IQTree tree) {
        return withConstraint(constraint, tree.getVariables()).propagate(tree);
    }

    protected abstract DownPropagation withConstraint(Optional<ImmutableExpression> constraint, ImmutableSet<Variable> variables);


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



    static <T extends VariableOrGroundTerm> Optional<Substitution<T>> reduceDescendingSubstitution(Substitution<T> descendingSubstitution, ImmutableSet<Variable> projectedVariables) {
        Substitution<T> reducedSubstitution = descendingSubstitution.restrictDomainTo(projectedVariables);

        if (reducedSubstitution.isEmpty())
            return Optional.empty();

        return Optional.of(reducedSubstitution);
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






