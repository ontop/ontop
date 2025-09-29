package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.function.Supplier;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public interface DownPropagation {

    /**
     * Excludes the variables that are not projected by the IQTree
     *
     * If a "null" variable is propagated down, throws an UnsatisfiableDescendingSubstitutionException.
     *
     */

    static DownPropagation of(Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
                              Optional<ImmutableExpression> constraint,
                              ImmutableSet<Variable> projectedVariables,
                              VariableGenerator variableGenerator,
                              TermFactory termFactory) throws InconsistentDownPropagationException {

        var optionalReducedSubstitution = reduceDescendingSubstitution(descendingSubstitution, projectedVariables);

        if (optionalReducedSubstitution.isPresent()) {
            var reducedSubstitution = optionalReducedSubstitution.get();
            if (reducedSubstitution.rangeAnyMatch(ImmutableTerm::isNull))
                throw new InconsistentDownPropagationException();

            var optionalNormalizedConstraint = normalizeConstraint(constraint, () -> getVariables(descendingSubstitution, projectedVariables), termFactory);
            var renaming = transformIntoFreshRenaming(reducedSubstitution, projectedVariables);
            return renaming.isPresent()
                    ? new RenamingDownPropagation(renaming.get(), optionalNormalizedConstraint, projectedVariables, variableGenerator, termFactory)
                    : new FullDownPropagation(reducedSubstitution, optionalNormalizedConstraint, projectedVariables, variableGenerator, termFactory);
        }

        return of(constraint, getVariables(descendingSubstitution, projectedVariables), variableGenerator, termFactory);
    }

    static DownPropagation of(Optional<ImmutableExpression> optionalConstraint, ImmutableSet<Variable> variables, VariableGenerator variableGenerator, TermFactory termFactory) {
        var optionalNormalizedConstraint = normalizeConstraint(optionalConstraint, () -> variables, termFactory);
        return optionalNormalizedConstraint.isEmpty()
                ? new EmptyDownPropagation(variables, variableGenerator, termFactory)
                : new ConstraintOnlyDownPropagation(optionalNormalizedConstraint.get(), variables, variableGenerator, termFactory);
    }

    static DownPropagation of(InjectiveSubstitution<Variable> substitution, ImmutableSet<Variable> variables) {
        InjectiveSubstitution<Variable> restriction = substitution.restrictDomainTo(variables);
        return restriction.isEmpty()
                ? new EmptyDownPropagation(variables, null, null)
                : new RenamingDownPropagation(substitution, Optional.empty(), variables, null, null);
    }


    static ImmutableSet<Variable> computeProjectedVariables(Substitution<? extends VariableOrGroundTerm> substitution, ImmutableSet<Variable> projectedVariables) {
        ImmutableSet<Variable> newVariables = substitution.restrictDomainTo(projectedVariables).getRangeVariables();
        return Sets.union(newVariables, Sets.difference(projectedVariables, substitution.getDomain())).immutableCopy();
    }


    ImmutableSet<Variable> getVariables();

    VariableGenerator getVariableGenerator();

    ImmutableSet<Variable> computeProjectedVariables();

    Optional<Substitution<? extends VariableOrGroundTerm>> getOptionalDescendingSubstitution();

    Optional<ImmutableExpression> applySubstitution(Optional<ImmutableExpression> optionalExpression);

    Optional<ImmutableExpression> getConstraint();

    DownPropagation withConstraint(Optional<ImmutableExpression> constraint, ImmutableSet<Variable> variables);

    VariableNullability extendVariableNullability(VariableNullability variableNullability);

    IQTree propagate(IQTree tree);

    default IQTree propagateToChild(IQTree child) {
        return reduceScope(child.getVariables()).propagate(child);
    }

    default IQTree propagateToChildWithConstraint(Optional<ImmutableExpression> constraint, IQTree tree) {
        return withConstraint(constraint, tree.getVariables()).propagate(tree);
    }

    DownPropagation reduceScope(ImmutableSet<Variable> variables);

    /**
     * Thrown when a "null" variable is propagated down or when the constraint is inconsistent
     */
    class InconsistentDownPropagationException extends Exception {
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

    private static ImmutableSet<Variable> getVariables(Substitution<? extends VariableOrGroundTerm> descendingSubstitution, ImmutableSet<Variable> projectedVariables) {
        return (ImmutableSet)descendingSubstitution.restrictRangeTo(Variable.class).apply(projectedVariables);
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

}


abstract class AbstractDownPropagation implements DownPropagation {
    protected final VariableGenerator variableGenerator;
    protected final ImmutableSet<Variable> variables;
    protected final TermFactory termFactory;

    AbstractDownPropagation(ImmutableSet<Variable> variables, VariableGenerator variableGenerator, TermFactory termFactory) {
        this.variableGenerator = variableGenerator;
        this.variables = variables;
        this.termFactory = termFactory;
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        return variables;
    }

    @Override
    public VariableGenerator getVariableGenerator() {
        return variableGenerator;
    }
}

class EmptyDownPropagation extends AbstractDownPropagation implements DownPropagation {

    EmptyDownPropagation(ImmutableSet<Variable> variables, VariableGenerator variableGenerator, TermFactory termFactory) {
        super(variables, variableGenerator, termFactory);
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
    public Optional<ImmutableExpression> getConstraint() {
        return Optional.empty();
    }

    @Override
    public DownPropagation withConstraint(Optional<ImmutableExpression> constraint, ImmutableSet<Variable> variables) {
        return DownPropagation.of(constraint, variables, variableGenerator, termFactory);
    }

    @Override
    public VariableNullability extendVariableNullability(VariableNullability variableNullability) {
        return variableNullability;
    }

    @Override
    public IQTree propagate(IQTree tree) {
        return tree;
    }

    @Override
    public DownPropagation reduceScope(ImmutableSet<Variable> variables) {
        if (!this.variables.containsAll(variables))
            throw new IllegalArgumentException("Variables " +  variables + " are not included in " + this.variables);

        if (this.variables.size() == variables.size())
            return this;

        return new EmptyDownPropagation(variables, variableGenerator, termFactory);
    }
}

class ConstraintOnlyDownPropagation extends AbstractDownPropagation implements DownPropagation {
    private final ImmutableExpression constraint;

    ConstraintOnlyDownPropagation(ImmutableExpression constraint, ImmutableSet<Variable> variables, VariableGenerator variableGenerator, TermFactory termFactory) {
        super(variables, variableGenerator, termFactory);
        this.constraint = constraint;
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
    public Optional<ImmutableExpression> getConstraint() {
        return Optional.of(constraint);
    }

    @Override
    public DownPropagation withConstraint(Optional<ImmutableExpression> constraint,  ImmutableSet<Variable> variables) {
        return DownPropagation.of(constraint, variables, variableGenerator, termFactory);
    }

    @Override
    public VariableNullability extendVariableNullability(VariableNullability variableNullability) {
         return variableNullability.extendToExternalVariables(constraint.getVariableStream());
    }

    @Override
    public IQTree propagate(IQTree tree) {
        return tree.propagateDownConstraint(this);
    }

    @Override
    public DownPropagation reduceScope(ImmutableSet<Variable> variables) {
        if (!this.variables.containsAll(variables))
            throw new IllegalArgumentException("Variables " +  variables + " are not included in " + this.variables);

        if (this.variables.size() == variables.size())
            return this;

        return DownPropagation.of(Optional.of(constraint), variables, variableGenerator, termFactory);
    }
}


@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
class FullDownPropagation extends AbstractDownPropagation implements DownPropagation {
    private final Substitution<? extends VariableOrGroundTerm> substitution;
    private final Optional<ImmutableExpression> constraint;

    FullDownPropagation(Substitution<? extends VariableOrGroundTerm> substitution, Optional<ImmutableExpression> constraint, ImmutableSet<Variable> variables, VariableGenerator variableGenerator, TermFactory termFactory) {
        super(variables, variableGenerator, termFactory);
        this.substitution = substitution;
        this.constraint = constraint;
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
    public Optional<ImmutableExpression> getConstraint() {
        return constraint;
    }

    @Override
    public DownPropagation withConstraint(Optional<ImmutableExpression> constraint,  ImmutableSet<Variable> variables) {
        var reducedSubstitution = DownPropagation.reduceDescendingSubstitution(substitution, variables);
        if (reducedSubstitution.isPresent()) {
            var normalizedConstraint = DownPropagation.normalizeConstraint(constraint, () -> variables, termFactory);
            var renaming = DownPropagation.transformIntoFreshRenaming(reducedSubstitution.get(), variables);
            return renaming.isPresent()
                    ? new RenamingDownPropagation(renaming.get(), normalizedConstraint, variables, variableGenerator, termFactory)
                     :new FullDownPropagation(substitution, normalizedConstraint, variables, variableGenerator, termFactory);
        }
        return DownPropagation.of(constraint, variables, variableGenerator, termFactory);
    }

    @Override
    public VariableNullability extendVariableNullability(VariableNullability variableNullability) {
        return constraint.map(c -> variableNullability.extendToExternalVariables(c.getVariableStream()))
                .orElse(variableNullability);
    }

    @Override
    public IQTree propagate(IQTree tree) {
        return tree.applyDescendingSubstitution(this);
    }

    @Override
    public DownPropagation reduceScope(ImmutableSet<Variable> variables) {
        if (!this.variables.containsAll(variables))
            throw new IllegalArgumentException("Variables " +  variables + " are not included in " + this.variables);

        if (this.variables.size() == variables.size())
            return this;

        return withConstraint(constraint, variables);
    }
}

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
class RenamingDownPropagation extends AbstractDownPropagation implements DownPropagation {
    private final InjectiveSubstitution<Variable> substitution;
    private final Optional<ImmutableExpression> constraint;

    RenamingDownPropagation(InjectiveSubstitution<Variable> substitution, Optional<ImmutableExpression> constraint, ImmutableSet<Variable> variables, VariableGenerator variableGenerator, TermFactory termFactory) {
        super(variables, variableGenerator, termFactory);
        this.substitution = substitution;
        this.constraint = constraint;
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
    public Optional<ImmutableExpression> getConstraint() {
        return constraint;
    }

    @Override
    public DownPropagation withConstraint(Optional<ImmutableExpression> constraint, ImmutableSet<Variable> variables) {
        var reducedSubstitution = DownPropagation.reduceDescendingSubstitution(substitution, variables);
        if (reducedSubstitution.isPresent()) {
            var normalizedConstraint = DownPropagation.normalizeConstraint(constraint, () -> variables, termFactory);
            return new RenamingDownPropagation(reducedSubstitution.get().injective(), normalizedConstraint, variables, variableGenerator, termFactory);
        }
        return DownPropagation.of(constraint, variables, variableGenerator, termFactory);
    }

    @Override
    public VariableNullability extendVariableNullability(VariableNullability variableNullability) {
        return constraint.map(c -> variableNullability.extendToExternalVariables(c.getVariableStream()))
                .orElse(variableNullability);
    }

    @Override
    public IQTree propagate(IQTree tree) {
        IQTree r = tree.applyFreshRenaming(substitution);
        return constraint.map(c -> r.propagateDownConstraint(
                new ConstraintOnlyDownPropagation(
                        c,
                        DownPropagation.computeProjectedVariables(substitution, variables),
                        variableGenerator, termFactory)))
                .orElse(r);
    }

    @Override
    public DownPropagation reduceScope(ImmutableSet<Variable> variables) {
        if (!this.variables.containsAll(variables))
            throw new IllegalArgumentException("Variables " +  variables + " are not included in " + this.variables);

        if (this.variables.size() == variables.size())
            return this;

        return withConstraint(constraint, variables);
    }
}


