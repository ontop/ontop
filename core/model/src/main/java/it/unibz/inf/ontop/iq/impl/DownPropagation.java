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
import java.util.stream.Stream;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public interface DownPropagation {
    static DownPropagation of(Substitution<? extends VariableOrGroundTerm> descendingSubstitution, Optional<ImmutableExpression> constraint, ImmutableSet<Variable> projectedVariables, VariableGenerator variableGenerator) {
        if (!descendingSubstitution.isEmpty())
            return new FullDownPropagation(descendingSubstitution, constraint, projectedVariables, variableGenerator);

        if (constraint.isPresent())
            return new ConstraintOnlyDownPropagation(constraint.get(), projectedVariables, variableGenerator);

        return new EmptyDownPropagation(projectedVariables, variableGenerator);
    }

    static DownPropagation ofNormalized(Substitution<? extends VariableOrGroundTerm> descendingSubstitution, Optional<ImmutableExpression> constraint, ImmutableSet<Variable> projectedVariables, VariableGenerator variableGenerator, TermFactory termFactory, IntermediateQueryFactory iqFactory) {
        try {
            var normalizedSubstitution = normalizeDescendingSubstitution(descendingSubstitution, projectedVariables);

            Optional<ImmutableExpression> normalizedConstraint = constraint
                    .map(c -> normalizeConstraint(descendingSubstitution, c, projectedVariables))
                    .flatMap(termFactory::getConjunction);

            if (normalizedSubstitution.isPresent()) {
                var renaming = transformIntoFreshRenaming(normalizedSubstitution.get(), projectedVariables);
                if (renaming.isPresent())
                    return new RenamingDownPropagation(renaming.get(), normalizedConstraint, projectedVariables, variableGenerator);

                return new FullDownPropagation(normalizedSubstitution.get(), normalizedConstraint, projectedVariables, variableGenerator);
            }

            if (normalizedConstraint.isPresent())
                return new ConstraintOnlyDownPropagation(normalizedConstraint.get(), projectedVariables, variableGenerator);

            return new EmptyDownPropagation(projectedVariables, variableGenerator);
        }
        catch (UnsatisfiableDescendingSubstitutionException e) {
            return new InconsistentDownPropagation(iqFactory.createEmptyNode(computeProjectedVariables(descendingSubstitution, projectedVariables)));
        }
    }

    static DownPropagation ofNormalized(Substitution<? extends VariableOrGroundTerm> descendingSubstitution, ImmutableSet<Variable> projectedVariables, VariableGenerator variableGenerator) throws UnsatisfiableDescendingSubstitutionException {
        var normalizedSubstitution = normalizeDescendingSubstitution(descendingSubstitution, projectedVariables);
        return normalizedSubstitution.isEmpty()
                ? new EmptyDownPropagation(projectedVariables, variableGenerator)
                : new FullDownPropagation(normalizedSubstitution.get(), Optional.empty(), projectedVariables, variableGenerator);
    }


    static DownPropagation of(Substitution<? extends VariableOrGroundTerm> descendingSubstitution, ImmutableSet<Variable> projectedVariables, VariableGenerator variableGenerator) {
        return descendingSubstitution.isEmpty()
                ? new EmptyDownPropagation(projectedVariables, variableGenerator)
                : new FullDownPropagation(descendingSubstitution, Optional.empty(), projectedVariables, variableGenerator);
    }

    static DownPropagation of(ImmutableExpression constraint, ImmutableSet<Variable> variables, VariableGenerator variableGenerator) {
        return new ConstraintOnlyDownPropagation(constraint, variables, variableGenerator);
    }

    static DownPropagation of(Optional<ImmutableExpression> constraint, ImmutableSet<Variable> variables, VariableGenerator variableGenerator) {
        return constraint.isPresent()
                ? new ConstraintOnlyDownPropagation(constraint.get(), variables, variableGenerator)
                : new EmptyDownPropagation(variables, variableGenerator);
    }

    static DownPropagation of(ImmutableExpression constraint, VariableGenerator variableGenerator) {
        return new ConstraintOnlyDownPropagation(constraint, ImmutableSet.of(), variableGenerator);
    }

    static DownPropagation of(Optional<ImmutableExpression> optionalConstraint, VariableGenerator variableGenerator) {
        return of(optionalConstraint, ImmutableSet.of(), variableGenerator);
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

    DownPropagation withConstraint(Optional<ImmutableExpression> constraint);

    VariableNullability extendVariableNullability(VariableNullability variableNullability);

    IQTree propagate(IQTree child);

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
    private static Optional<Substitution<? extends VariableOrGroundTerm>> normalizeDescendingSubstitution(Substitution<? extends VariableOrGroundTerm> descendingSubstitution, ImmutableSet<Variable> projectedVariables)
            throws UnsatisfiableDescendingSubstitutionException {

        Substitution<? extends VariableOrGroundTerm> reducedSubstitution = descendingSubstitution.restrictDomainTo(projectedVariables);

        if (reducedSubstitution.isEmpty())
            return Optional.empty();

        if (reducedSubstitution.rangeAnyMatch(ImmutableTerm::isNull))
            throw new UnsatisfiableDescendingSubstitutionException();

        return Optional.of(reducedSubstitution);
    }


    private static Stream<ImmutableExpression> normalizeConstraint(Substitution<? extends VariableOrGroundTerm> descendingSubstitution, ImmutableExpression constraint, ImmutableSet<Variable> projectedVariables) {
        var newVariables = descendingSubstitution.restrictRangeTo(Variable.class).apply(projectedVariables);
        return constraint.flattenAND()
                .filter(e -> e.getVariableStream().anyMatch(newVariables::contains));
    }

    /**
     * If the substitution is a fresh renaming, returns it as an injective substitution
     */
    private static Optional<InjectiveSubstitution<Variable>> transformIntoFreshRenaming(Substitution<? extends VariableOrGroundTerm> descendingSubstitution, ImmutableSet<Variable> projectedVariables) {

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
    public DownPropagation withConstraint(Optional<ImmutableExpression> constraint) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VariableNullability extendVariableNullability(VariableNullability variableNullability) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IQTree propagate(IQTree child) {
        return emptyNode;
    }
}

abstract class AbstractDownPropagation implements DownPropagation {
    protected final VariableGenerator variableGenerator;
    protected final ImmutableSet<Variable> variables;

    AbstractDownPropagation(ImmutableSet<Variable> variables, VariableGenerator variableGenerator) {
        this.variableGenerator = Objects.requireNonNull(variableGenerator);
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
    public DownPropagation withConstraint(Optional<ImmutableExpression> constraint) {
        return DownPropagation.of(constraint, variables, variableGenerator);
    }

    @Override
    public VariableNullability extendVariableNullability(VariableNullability variableNullability) {
        return variableNullability;
    }

    @Override
    public IQTree propagate(IQTree child) {
        return child;
    }
}

class ConstraintOnlyDownPropagation extends AbstractDownPropagation implements DownPropagation {
    private final ImmutableExpression constraint;

    ConstraintOnlyDownPropagation(ImmutableExpression constraint, ImmutableSet<Variable> variables, VariableGenerator variableGenerator) {
        super(variables, variableGenerator);
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
    public DownPropagation withConstraint(Optional<ImmutableExpression> constraint) {
        return DownPropagation.of(constraint, variables, variableGenerator);
    }

    @Override
    public VariableNullability extendVariableNullability(VariableNullability variableNullability) {
         return variableNullability.extendToExternalVariables(constraint.getVariableStream());
    }

    @Override
    public IQTree propagate(IQTree child) {
        return child.propagateDownConstraint(constraint, variableGenerator);
    }
}


@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
class FullDownPropagation extends AbstractDownPropagation implements DownPropagation {
    private final Substitution<? extends VariableOrGroundTerm> substitution;
    private final Optional<ImmutableExpression> constraint;

    FullDownPropagation(Substitution<? extends VariableOrGroundTerm> substitution, Optional<ImmutableExpression> constraint, ImmutableSet<Variable> variables, VariableGenerator variableGenerator) {
        super(variables, variableGenerator);
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
    public DownPropagation withConstraint(Optional<ImmutableExpression> constraint) {
        return new FullDownPropagation(substitution, constraint, variables, variableGenerator);
    }

    @Override
    public VariableNullability extendVariableNullability(VariableNullability variableNullability) {
        return constraint.map(c -> variableNullability.extendToExternalVariables(c.getVariableStream()))
                .orElse(variableNullability);
    }

    @Override
    public IQTree propagate(IQTree child) {
        return child.applyDescendingSubstitution(substitution, constraint, variableGenerator);
    }
}

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
class RenamingDownPropagation extends AbstractDownPropagation implements DownPropagation {
    private final InjectiveSubstitution<Variable> substitution;
    private final Optional<ImmutableExpression> constraint;

    RenamingDownPropagation(InjectiveSubstitution<Variable> substitution, Optional<ImmutableExpression> constraint, ImmutableSet<Variable> variables, VariableGenerator variableGenerator) {
        super(variables, variableGenerator);
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
    public DownPropagation withConstraint(Optional<ImmutableExpression> constraint) {
        return new RenamingDownPropagation(substitution, constraint, variables, variableGenerator);
    }

    @Override
    public VariableNullability extendVariableNullability(VariableNullability variableNullability) {
        return constraint.map(c -> variableNullability.extendToExternalVariables(c.getVariableStream()))
                .orElse(variableNullability);
    }

    @Override
    public IQTree propagate(IQTree child) {
        IQTree r = child.applyFreshRenaming(substitution);
        return constraint.map(c -> r.propagateDownConstraint(c, this.variableGenerator))
                .orElse(r);
    }
}


