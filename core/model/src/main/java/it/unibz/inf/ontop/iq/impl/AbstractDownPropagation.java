package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableMap;
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

    @Override
    public IQTree propagateWithRestrictedScope(IQTree tree) {
        ImmutableSet<Variable> newVariables = tree.getVariables();
        if (!variables.containsAll(newVariables))
            throw new IllegalArgumentException("Variables " + newVariables + " are not included in " + this.variables);

        return variables.size() == newVariables.size()
                ? propagate(tree)
                : withReducedScope(newVariables).propagate(tree);
    }


    @Override
    public final DownPropagation withNoConstraint() {
        return updateConstraint(Optional.empty());
    }

    @Override
    public final DownPropagation withRestrictedConstraint(Predicate<ImmutableExpression> filter) {
        return updateConstraint(optionalConstraint.flatMap(
                c -> termFactory.getConjunction(c.flattenAND().filter(filter))));
    }

    @Override
    public DownPropagation withRestrictedSubstitution(ImmutableSet<Variable> variablesToRemove) {
        if (getDescendingSubstitution().isEmpty())
            return this;

        var newDescendingSubstitution = getDescendingSubstitution().removeFromDomain(variablesToRemove);
        return createNonEmptySubstitutionDownPropagation(newDescendingSubstitution, optionalConstraint, variables, variableGenerator, termFactory);
    }


    protected abstract DownPropagation updateConstraint(Optional<ImmutableExpression> constraint);

    protected static <T extends VariableOrGroundTerm> Substitution<T> reduceDescendingSubstitution(Substitution<T> descendingSubstitution, ImmutableSet<Variable> projectedVariables) {
        return descendingSubstitution.restrictDomainTo(projectedVariables);
    }

    protected static Optional<ImmutableExpression> normalizeConstraint(Optional<ImmutableExpression> optionalConstraint, ImmutableSet<Variable> variables, Substitution<? extends VariableOrGroundTerm> substitution, TermFactory termFactory) {
        if (optionalConstraint.isPresent()) {
            var projectedVariables = DownPropagation.getProjectedVariablesAfterDescendingSubstitution(substitution, variables);
            return termFactory.getConjunction(optionalConstraint.get().flattenAND()
                    .filter(e -> e.getVariableStream().anyMatch(projectedVariables::contains)));
        }
        return Optional.empty();
    }

    static DownPropagation createDownPropagation(Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                                 Optional<ImmutableExpression> constraint,
                                                 ImmutableSet<Variable> variables,
                                                 VariableGenerator variableGenerator,
                                                 TermFactory termFactory) throws DownPropagation.InconsistentDownPropagationException {

        var reducedSubstitution = reduceDescendingSubstitution(descendingSubstitution, variables);
        if (!reducedSubstitution.isEmpty()) {
            if (reducedSubstitution.rangeAnyMatch(ImmutableTerm::isNull))
                throw new DownPropagation.InconsistentDownPropagationException();

            var optionalNormalizedConstraint = normalizeConstraint(constraint, variables, descendingSubstitution, termFactory);
            return createNonEmptySubstitutionDownPropagation(reducedSubstitution, optionalNormalizedConstraint, variables, variableGenerator, termFactory);
        }

        return createDownPropagation(constraint, variables, variableGenerator, termFactory);
    }

    static DownPropagation createDownPropagation(Optional<ImmutableExpression> optionalConstraint, ImmutableSet<Variable> variables, VariableGenerator variableGenerator, TermFactory termFactory) {
        var optionalNormalizedConstraint = normalizeConstraint(optionalConstraint, variables, termFactory.getSubstitution(ImmutableMap.of()), termFactory);
        return new ConstraintOnlyDownPropagation(optionalNormalizedConstraint, variables, variableGenerator, termFactory);
    }

    protected static DownPropagation createNonEmptySubstitutionDownPropagation(Substitution<? extends VariableOrGroundTerm> reducedSubstitution,
                                                                               Optional<ImmutableExpression> optionalNormalizedConstraint,
                                                                               ImmutableSet<Variable> variables,
                                                                               VariableGenerator variableGenerator,
                                                                               TermFactory termFactory) {
        var optionalRenaming = transformIntoFreshRenaming(reducedSubstitution, variables);
        return optionalRenaming.isPresent()
                ? new RenamingDownPropagation(optionalRenaming.get(), optionalNormalizedConstraint, variables, variableGenerator, termFactory)
                : new FullDownPropagation(reducedSubstitution, optionalNormalizedConstraint, variables, variableGenerator, termFactory);
    }

    /**
     * If the substitution is a fresh renaming, returns it as an injective substitution
     */
    private static Optional<InjectiveSubstitution<Variable>> transformIntoFreshRenaming(Substitution<? extends VariableOrGroundTerm> descendingSubstitution, ImmutableSet<Variable> variables) {
        Substitution<Variable> var2VarFragment = descendingSubstitution.restrictRangeTo(Variable.class);
        int size = descendingSubstitution.getDomain().size();

        if (var2VarFragment.getDomain().size() != size
                || Sets.difference(var2VarFragment.getRangeSet(), variables).size() != size)
            return Optional.empty();

        return Optional.of(var2VarFragment.injective());
    }
}






