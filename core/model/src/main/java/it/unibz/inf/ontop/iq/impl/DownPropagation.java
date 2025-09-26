package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public interface DownPropagation {
    static DownPropagation of(Optional<ImmutableExpression> constraint, Substitution<? extends VariableOrGroundTerm> descendingSubstitution, ImmutableSet<Variable> projectedVariables) {
        return new DownPropagationImpl(constraint, Optional.of(descendingSubstitution), projectedVariables);
    }

    // new

    static DownPropagation of(Substitution<? extends VariableOrGroundTerm> descendingSubstitution, ImmutableSet<Variable> projectedVariables, VariableGenerator variableGenerator) {
        return descendingSubstitution.isEmpty()
                ? new EmptyDownPropagation(projectedVariables, variableGenerator)
                : new SubstitutionOnlyDownPropagation(descendingSubstitution, projectedVariables, variableGenerator);
    }

    static DownPropagation ofNormalized(Substitution<? extends VariableOrGroundTerm> descendingSubstitution, ImmutableSet<Variable> projectedVariables, VariableGenerator variableGenerator) throws UnsatisfiableDescendingSubstitutionException {
        var normalized = DownPropagationImpl.normalizeDescendingSubstitution(descendingSubstitution, projectedVariables);
        return normalized.isEmpty()
                ? new EmptyDownPropagation(projectedVariables, variableGenerator)
                : new SubstitutionOnlyDownPropagation(normalized.get(), projectedVariables, variableGenerator);
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

    ImmutableSet<Variable> computeProjectedVariables();

    Optional<Substitution<? extends VariableOrGroundTerm>> getOptionalDescendingSubstitution();

    Optional<ImmutableExpression> applySubstitution(Optional<ImmutableExpression> optionalExpression);

    Optional<ImmutableExpression> getConstraint();

    DownPropagation withConstraint(Optional<ImmutableExpression> constraint);

    VariableNullability extendVariableNullability(VariableNullability variableNullability);

    IQTree propagate(IQTree child, VariableGenerator variableGenerator);

    default IQTree propagate(IQTree child) {
        throw new UnsupportedOperationException();
    }

    ImmutableList<IQTree> propagate(ImmutableList<IQTree> children, VariableGenerator variableGenerator);

    DownPropagation normalize(TermFactory termFactory) throws UnsatisfiableDescendingSubstitutionException;

    Optional<InjectiveSubstitution<Variable>> transformIntoFreshRenaming();

    /**
     * Typically thrown when a "null" variable is propagated down
     *
     */
    class UnsatisfiableDescendingSubstitutionException extends Exception {
    }

}

abstract class AbstractDownPropagation implements DownPropagation {
    protected final VariableGenerator variableGenerator;

    AbstractDownPropagation(VariableGenerator variableGenerator) {
        this.variableGenerator = variableGenerator;
    }
}

class EmptyDownPropagation extends AbstractDownPropagation implements DownPropagation {
    private final ImmutableSet<Variable> variables;

    EmptyDownPropagation(ImmutableSet<Variable> variables, VariableGenerator variableGenerator) {
        super(variableGenerator);
        this.variables = variables;
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        return variables;
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
    public IQTree propagate(IQTree child, VariableGenerator variableGenerator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IQTree propagate(IQTree child) {
        return child;
    }

    @Override
    public ImmutableList<IQTree> propagate(ImmutableList<IQTree> children, VariableGenerator variableGenerator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DownPropagation normalize(TermFactory termFactory) throws UnsatisfiableDescendingSubstitutionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<InjectiveSubstitution<Variable>> transformIntoFreshRenaming() {
        throw new UnsupportedOperationException();
    }
}

class ConstraintOnlyDownPropagation extends AbstractDownPropagation implements DownPropagation {
    private final ImmutableExpression constraint;
    private final ImmutableSet<Variable> variables;

    ConstraintOnlyDownPropagation(ImmutableExpression constraint, ImmutableSet<Variable> variables, VariableGenerator variableGenerator) {
        super(variableGenerator);
        this.constraint = constraint;
        this.variables = variables;
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        return variables;
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
    public IQTree propagate(IQTree child, VariableGenerator variableGenerator) {
        return child.propagateDownConstraint(constraint, this.variableGenerator);
    }

    @Override
    public IQTree propagate(IQTree child) {
        return child.propagateDownConstraint(constraint, variableGenerator);
    }

    @Override
    public ImmutableList<IQTree> propagate(ImmutableList<IQTree> children, VariableGenerator variableGenerator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DownPropagation normalize(TermFactory termFactory) throws UnsatisfiableDescendingSubstitutionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<InjectiveSubstitution<Variable>> transformIntoFreshRenaming() {
        throw new UnsupportedOperationException();
    }
}

class SubstitutionOnlyDownPropagation extends AbstractDownPropagation implements DownPropagation {
    private final Substitution<? extends VariableOrGroundTerm> substitution;
    private final ImmutableSet<Variable> variables;

    SubstitutionOnlyDownPropagation(Substitution<? extends VariableOrGroundTerm> substitution, ImmutableSet<Variable> variables, VariableGenerator variableGenerator) {
        super(variableGenerator);
        this.substitution = substitution;
        this.variables = variables;
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        return variables;
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
        return Optional.empty();
    }

    @Override
    public DownPropagation withConstraint(Optional<ImmutableExpression> constraint) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VariableNullability extendVariableNullability(VariableNullability variableNullability) {
        return variableNullability;
    }

    @Override
    public IQTree propagate(IQTree child, VariableGenerator variableGenerator) {
        return child.applyDescendingSubstitution(substitution, Optional.empty(), this.variableGenerator);
    }

    @Override
    public IQTree propagate(IQTree child) {
        return child.applyDescendingSubstitution(substitution, Optional.empty(), variableGenerator);
    }

    @Override
    public ImmutableList<IQTree> propagate(ImmutableList<IQTree> children, VariableGenerator variableGenerator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DownPropagation normalize(TermFactory termFactory) throws UnsatisfiableDescendingSubstitutionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<InjectiveSubstitution<Variable>> transformIntoFreshRenaming() {
        throw new UnsupportedOperationException();
    }
}


@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
class DownPropagationImpl implements DownPropagation {
    private final Optional<ImmutableExpression> constraint;
    private final Optional<Substitution<? extends VariableOrGroundTerm>> optionalDescendingSubstitution;
    private final ImmutableSet<Variable> projectedVariables;

    DownPropagationImpl(Optional<ImmutableExpression> constraint, Optional<Substitution<? extends VariableOrGroundTerm>> optionalDescendingSubstitution, ImmutableSet<Variable> projectedVariables) {
        this.constraint = constraint;
        this.optionalDescendingSubstitution = optionalDescendingSubstitution;
        this.projectedVariables = projectedVariables;
    }

    public ImmutableSet<Variable> getVariables() {
        return projectedVariables;
    }

    public ImmutableSet<Variable> computeProjectedVariables() {
        return optionalDescendingSubstitution
                .map(s -> DownPropagation.computeProjectedVariables(s, projectedVariables))
                .orElse(projectedVariables);
    }


    public Optional<Substitution<? extends VariableOrGroundTerm>> getOptionalDescendingSubstitution() {
        return optionalDescendingSubstitution;
    }

    public Optional<ImmutableExpression> applySubstitution(Optional<ImmutableExpression> optionalExpression) {
        return optionalDescendingSubstitution
                .map(s -> optionalExpression.map(s::apply))
                .orElse(optionalExpression);
    }

    public Optional<ImmutableExpression> getConstraint() {
        return constraint;
    }

    @Override
    public DownPropagation withConstraint(Optional<ImmutableExpression> constraint) {
        return new DownPropagationImpl(constraint, optionalDescendingSubstitution, projectedVariables);
    }


    public VariableNullability extendVariableNullability(VariableNullability variableNullability) {
        return constraint
                .map(c -> variableNullability.extendToExternalVariables(c.getVariableStream()))
                .orElse(variableNullability);
    }

    public IQTree propagate(IQTree child, VariableGenerator variableGenerator) {
        return optionalDescendingSubstitution
                .filter(s -> !s.isEmpty())
                .map(s -> child.applyDescendingSubstitution(s, constraint, variableGenerator))
                .orElseGet(() -> constraint.map(c -> child.propagateDownConstraint(c, variableGenerator))
                        .orElse(child));
    }

    public ImmutableList<IQTree> propagate(ImmutableList<IQTree> children, VariableGenerator variableGenerator) {
        return optionalDescendingSubstitution
                .filter(s -> !s.isEmpty())
                .map(s -> NaryIQTreeTools.transformChildren(
                        children,
                        c -> c.applyDescendingSubstitution(s, constraint, variableGenerator)))
                .or(() -> constraint
                        .map(cs -> NaryIQTreeTools.transformChildren(children,
                                c -> c.propagateDownConstraint(cs, variableGenerator))))
                .orElse(children);
    }



    /**
     * Excludes the variables that are not projected by the IQTree
     *
     * If a "null" variable is propagated down, throws an UnsatisfiableDescendingSubstitutionException.
     *
     */
    static Optional<Substitution<? extends VariableOrGroundTerm>> normalizeDescendingSubstitution(Substitution<? extends VariableOrGroundTerm> descendingSubstitution, ImmutableSet<Variable> projectedVariables)
            throws UnsatisfiableDescendingSubstitutionException {

        Substitution<? extends VariableOrGroundTerm> reducedSubstitution = descendingSubstitution.restrictDomainTo(projectedVariables);

        if (reducedSubstitution.isEmpty())
            return Optional.empty();

        if (reducedSubstitution.rangeAnyMatch(ImmutableTerm::isNull))
            throw new UnsatisfiableDescendingSubstitutionException();

        return Optional.of(reducedSubstitution);
    }


    public DownPropagation normalize(TermFactory termFactory) throws UnsatisfiableDescendingSubstitutionException {
        var descendingSubstitution = optionalDescendingSubstitution.get();
        var normalizedSubstitution = normalizeDescendingSubstitution(descendingSubstitution, projectedVariables);

        if (constraint.isEmpty())
            return new DownPropagationImpl(constraint, normalizedSubstitution, projectedVariables);

        ImmutableSet<?> newVariables = descendingSubstitution.restrictRangeTo(Variable.class).apply(projectedVariables);

        return new DownPropagationImpl(
                termFactory.getConjunction(constraint.get().flattenAND()
                        .filter(e -> e.getVariableStream().anyMatch(newVariables::contains))),
                normalizedSubstitution, projectedVariables);
    }

    /**
     * If the substitution is a fresh renaming, returns it as an injective substitution
     */
    public Optional<InjectiveSubstitution<Variable>> transformIntoFreshRenaming() {

        var descendingSubstitution = optionalDescendingSubstitution.get();
        Substitution<Variable> var2VarFragment = descendingSubstitution.restrictRangeTo(Variable.class);

        int size = descendingSubstitution.getDomain().size();

        if (var2VarFragment.getDomain().size() != size
                || Sets.difference(var2VarFragment.getRangeSet(), projectedVariables).size() != size)
            return Optional.empty();

        return Optional.of(var2VarFragment.injective());
    }

}
