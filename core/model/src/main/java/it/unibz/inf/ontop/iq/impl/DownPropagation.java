package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.EmptyNode;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public interface DownPropagation {

    static DownPropagation of(Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
                              Optional<ImmutableExpression> constraint,
                              ImmutableSet<Variable> projectedVariables,
                              VariableGenerator variableGenerator,
                              TermFactory termFactory,
                              IntermediateQueryFactory iqFactory) {
        try {
            var optionalNormalizedSubstitution = normalizeDescendingSubstitution(descendingSubstitution, projectedVariables);
            var optionalNormalizedConstraint = normalizeConstraint(constraint, () -> getVariables(descendingSubstitution, projectedVariables), termFactory);

            if (optionalNormalizedSubstitution.isPresent()) {
                var renaming = transformIntoFreshRenaming(optionalNormalizedSubstitution.get(), projectedVariables);
                if (renaming.isPresent())
                    return new RenamingDownPropagation(renaming.get(), optionalNormalizedConstraint, projectedVariables, variableGenerator, termFactory);

                return new FullDownPropagation(optionalNormalizedSubstitution.get(), optionalNormalizedConstraint, projectedVariables, variableGenerator, termFactory);
            }

            if (optionalNormalizedConstraint.isPresent())
                return new ConstraintOnlyDownPropagation(optionalNormalizedConstraint.get(), projectedVariables, variableGenerator, termFactory);

            return new EmptyDownPropagation(projectedVariables, variableGenerator);
        }
        catch (UnsatisfiableDescendingSubstitutionException e) {
            return new InconsistentDownPropagation(iqFactory.createEmptyNode(computeProjectedVariables(descendingSubstitution, projectedVariables)));
        }
    }

    static DownPropagation ofNormalized(Substitution<? extends VariableOrGroundTerm> descendingSubstitution, ImmutableSet<Variable> projectedVariables, VariableGenerator variableGenerator, IntermediateQueryFactory iqFactory) throws UnsatisfiableDescendingSubstitutionException {
        var r = of(descendingSubstitution, Optional.empty(), projectedVariables, variableGenerator, null, iqFactory); // null are not going to be used
        if (r instanceof InconsistentDownPropagation)
            throw  new UnsatisfiableDescendingSubstitutionException();
        return r;
    }

    static DownPropagation of(Optional<ImmutableExpression> optionalConstraint, ImmutableSet<Variable> variables, VariableGenerator variableGenerator, TermFactory termFactory) {
        var optionalNormalizedConstraint = normalizeConstraint(optionalConstraint, () -> variables, termFactory);
        return optionalNormalizedConstraint.isPresent()
                ? new ConstraintOnlyDownPropagation(optionalNormalizedConstraint.get(), variables, variableGenerator, termFactory)
                : new EmptyDownPropagation(variables, variableGenerator);
    }

    static DownPropagation of(InjectiveSubstitution<Variable> substitution, ImmutableSet<Variable> variables) {
        InjectiveSubstitution<Variable> restriction = substitution.restrictDomainTo(variables);
        return restriction.isEmpty()
                ? new EmptyDownPropagation(variables, null)
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

    DownPropagation withConstraint(Optional<ImmutableExpression> constraint, ImmutableSet<Variable> variables, TermFactory termFactory);

    VariableNullability extendVariableNullability(VariableNullability variableNullability);

    IQTree propagate(IQTree tree);

    default IQTree propagateToChild(IQTree child) {
        return reduceScope(child.getVariables()).propagate(child);
    }

    DownPropagation reduceScope(ImmutableSet<Variable> variables);

    /**
     * Typically thrown when a "null" variable is propagated down
     *
     */
    class UnsatisfiableDescendingSubstitutionException extends Exception {
    }

    /**
     * Excludes the variables that are not projected by the IQTree
     *
     * If a "null" variable is propagated down, throws an UnsatisfiableDescendingSubstitutionException.
     *
     */
    private static <T extends VariableOrGroundTerm> Optional<Substitution<T>> normalizeDescendingSubstitution(Substitution<T> descendingSubstitution, ImmutableSet<Variable> projectedVariables)
            throws UnsatisfiableDescendingSubstitutionException {

        Optional<Substitution<T>> reducedSubstitution = reduceDescendingSubstitution(descendingSubstitution, projectedVariables);

        if (reducedSubstitution.isPresent() && reducedSubstitution.get().rangeAnyMatch(ImmutableTerm::isNull))
            throw new UnsatisfiableDescendingSubstitutionException();

        return reducedSubstitution;
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

class InconsistentDownPropagation implements DownPropagation {
    private final EmptyNode emptyNode;

    InconsistentDownPropagation(EmptyNode emptyNode) {
        this.emptyNode = emptyNode;
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        return emptyNode.getVariables();
    }

    @Override
    public VariableGenerator getVariableGenerator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ImmutableSet<Variable> computeProjectedVariables() {
        return emptyNode.getVariables();
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
    public DownPropagation withConstraint(Optional<ImmutableExpression> constraint, ImmutableSet<Variable> variables, TermFactory termFactory) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VariableNullability extendVariableNullability(VariableNullability variableNullability) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IQTree propagate(IQTree tree) {
        return emptyNode;
    }

    @Override
    public DownPropagation reduceScope(ImmutableSet<Variable> variables) {
        throw new UnsupportedOperationException();
    }
}

abstract class AbstractDownPropagation implements DownPropagation {
    protected final VariableGenerator variableGenerator;
    protected final ImmutableSet<Variable> variables;

    AbstractDownPropagation(ImmutableSet<Variable> variables, VariableGenerator variableGenerator) {
        this.variableGenerator = variableGenerator;
        this.variables = variables;
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

    EmptyDownPropagation(ImmutableSet<Variable> variables, VariableGenerator variableGenerator) {
        super(variables, variableGenerator);
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
    public DownPropagation withConstraint(Optional<ImmutableExpression> constraint, ImmutableSet<Variable> variables, TermFactory termFactory) {
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
        return new EmptyDownPropagation(variables, variableGenerator);
    }
}

class ConstraintOnlyDownPropagation extends AbstractDownPropagation implements DownPropagation {
    private final ImmutableExpression constraint;
    private final TermFactory termFactory;

    ConstraintOnlyDownPropagation(ImmutableExpression constraint, ImmutableSet<Variable> variables, VariableGenerator variableGenerator, TermFactory termFactory) {
        super(variables, variableGenerator);
        this.constraint = constraint;
        this.termFactory = termFactory;
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
    public DownPropagation withConstraint(Optional<ImmutableExpression> constraint,  ImmutableSet<Variable> variables, TermFactory termFactory) {
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
        return DownPropagation.of(Optional.of(constraint), variables, variableGenerator, termFactory);
    }
}


@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
class FullDownPropagation extends AbstractDownPropagation implements DownPropagation {
    private final Substitution<? extends VariableOrGroundTerm> substitution;
    private final Optional<ImmutableExpression> constraint;
    private final TermFactory termFactory;

    FullDownPropagation(Substitution<? extends VariableOrGroundTerm> substitution, Optional<ImmutableExpression> constraint, ImmutableSet<Variable> variables, VariableGenerator variableGenerator, TermFactory termFactory) {
        super(variables, variableGenerator);
        this.substitution = substitution;
        this.constraint = constraint;
        this.termFactory = termFactory;
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
    public DownPropagation withConstraint(Optional<ImmutableExpression> constraint,  ImmutableSet<Variable> variables, TermFactory termFactory) {
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
        return tree.applyDescendingSubstitution(substitution, constraint, variableGenerator);
    }

    @Override
    public DownPropagation reduceScope(ImmutableSet<Variable> variables) {
        if (!this.variables.containsAll(variables))
            throw new IllegalArgumentException("Variables " +  variables + " are not included in " + this.variables);

        return withConstraint(constraint, variables, termFactory);
    }
}

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
class RenamingDownPropagation extends AbstractDownPropagation implements DownPropagation {
    private final InjectiveSubstitution<Variable> substitution;
    private final Optional<ImmutableExpression> constraint;
    private final TermFactory termFactory;

    RenamingDownPropagation(InjectiveSubstitution<Variable> substitution, Optional<ImmutableExpression> constraint, ImmutableSet<Variable> variables, VariableGenerator variableGenerator, TermFactory termFactory) {
        super(variables, variableGenerator);
        this.substitution = substitution;
        this.constraint = constraint;
        this.termFactory = termFactory;
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
    public DownPropagation withConstraint(Optional<ImmutableExpression> constraint, ImmutableSet<Variable> variables, TermFactory termFactory) {
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

        return withConstraint(constraint, variables, termFactory);
    }
}


