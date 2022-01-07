package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.ConcreteIQTreeCache;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.NonVariableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

public class ConcreteIQTreeCacheImpl implements ConcreteIQTreeCache {

    private final boolean isNormalizedForOptimization;
    private final boolean areDistinctAlreadyRemoved;
    private final CoreSingletons coreSingletons;

    @Nullable
    private VariableNullability variableNullability;

    @Nullable
    private ImmutableSet<Variable> variables;

    @Nullable
    private ImmutableSet<Variable> notInternallyRequiredVariables;

    @Nullable
    private ImmutableSet<ImmutableSubstitution<NonVariableTerm>> possibleVariableDefinitions;

    @Nullable
    private ImmutableSet<ImmutableSet<Variable>> uniqueConstraints;

    @Nullable
    private Boolean isDistinct;

    @Inject
    @AssistedInject
    private ConcreteIQTreeCacheImpl(CoreSingletons coreSingletons) {
        this(false, coreSingletons);
    }

    @AssistedInject
    private ConcreteIQTreeCacheImpl(@Assisted boolean isNormalizedForOptimization, CoreSingletons coreSingletons) {
        this.coreSingletons = coreSingletons;
        this.isNormalizedForOptimization = isNormalizedForOptimization;
        this.areDistinctAlreadyRemoved = false;
    }

    /**
     * Internal constructor
     */
    protected ConcreteIQTreeCacheImpl(CoreSingletons coreSingletons, boolean isNormalizedForOptimization,
                                      boolean areDistinctAlreadyRemoved, @Nullable VariableNullability variableNullability,
                                      @Nullable ImmutableSet<Variable> variables,
                                      @Nullable ImmutableSet<Variable> notInternallyRequiredVariables,
                                      @Nullable ImmutableSet<ImmutableSubstitution<NonVariableTerm>> possibleVariableDefinitions,
                                      @Nullable ImmutableSet<ImmutableSet<Variable>> uniqueConstraints,
                                      @Nullable Boolean isDistinct) {
        this.isNormalizedForOptimization = isNormalizedForOptimization;
        this.areDistinctAlreadyRemoved = areDistinctAlreadyRemoved;
        this.coreSingletons = coreSingletons;
        this.variableNullability = variableNullability;
        this.variables = variables;
        this.notInternallyRequiredVariables = notInternallyRequiredVariables;
        this.possibleVariableDefinitions = possibleVariableDefinitions;
        this.uniqueConstraints = uniqueConstraints;
        this.isDistinct = isDistinct;
    }

    @Override
    public boolean isNormalizedForOptimization() {
        return isNormalizedForOptimization;
    }

    @Override
    public boolean areDistinctAlreadyRemoved() {
        return areDistinctAlreadyRemoved;
    }

    @Nullable
    @Override
    public ImmutableSet<Variable> getVariables() {
        return variables;
    }

    @Nullable
    @Override
    public ImmutableSet<Variable> getNotInternallyRequiredVariables() {
        return notInternallyRequiredVariables;
    }

    @Override
    public @Nullable VariableNullability getVariableNullability() {
        return variableNullability;
    }

    @Nullable
    @Override
    public ImmutableSet<ImmutableSubstitution<NonVariableTerm>> getPossibleVariableDefinitions() {
        return possibleVariableDefinitions;
    }

    @Nullable
    @Override
    public ImmutableSet<ImmutableSet<Variable>> getUniqueConstraints() {
        return uniqueConstraints;
    }

    @Nullable
    @Override
    public Boolean isDistinct() {
        return isDistinct;
    }

    @Override
    public void setVariables(@Nonnull ImmutableSet<Variable> variables) {
        this.variables = variables;
    }

    @Override
    public void setNotInternallyRequiredVariables(@Nonnull ImmutableSet<Variable> notInternallyRequiredVariables) {
        this.notInternallyRequiredVariables = notInternallyRequiredVariables;
    }

    @Override
    public IQTreeCache declareAsNormalizedForOptimizationWithoutEffect() {
        return new ConcreteIQTreeCacheImpl(coreSingletons, true, areDistinctAlreadyRemoved,
                variableNullability, variables, notInternallyRequiredVariables, possibleVariableDefinitions, uniqueConstraints, isDistinct);
    }

    /**
     * TODO: explicit assumptions about the effects of normalization
     */
    @Override
    public IQTreeCache declareAsNormalizedForOptimizationWithEffect() {
        return new ConcreteIQTreeCacheImpl(coreSingletons, true, areDistinctAlreadyRemoved,
                null, null, null, null, null, null);
    }

    /**
     * TODO: explicit assumptions about the effects
     */
    @Override
    public IQTreeCache declareConstraintPushedDownWithEffect() {
        return new ConcreteIQTreeCacheImpl(coreSingletons, false, areDistinctAlreadyRemoved,
                null, variables, null, null, null, null);
    }

    /**
     * TODO: explicit assumptions about the effects
     */

    @Override
    public IQTreeCache declareDistinctRemoval(boolean noEffect) {
        return noEffect
                ? new ConcreteIQTreeCacheImpl(coreSingletons, isNormalizedForOptimization, true,
                variableNullability, variables, notInternallyRequiredVariables, possibleVariableDefinitions, uniqueConstraints, isDistinct)
                : new ConcreteIQTreeCacheImpl(coreSingletons, false, true,
                variableNullability, variables, null, possibleVariableDefinitions, null, null);
    }

    @Override
    public synchronized void setVariableNullability(@Nonnull VariableNullability variableNullability) {
        if (this.variableNullability != null)
            throw new IllegalStateException("Variable nullability already present. Only call this method once");
        this.variableNullability = variableNullability;
    }

    @Override
    public synchronized void setPossibleVariableDefinitions(@Nonnull ImmutableSet<ImmutableSubstitution<NonVariableTerm>> possibleVariableDefinitions) {
        if (this.possibleVariableDefinitions != null)
            throw new IllegalStateException("Possible variable definitions already present. Only call this method once");
        this.possibleVariableDefinitions = possibleVariableDefinitions;
    }

    @Override
    public void setUniqueConstraints(@Nonnull ImmutableSet<ImmutableSet<Variable>> uniqueConstraints) {
        if (this.uniqueConstraints != null)
            throw new IllegalStateException("Unique constraints already present. Only call this method once");
        this.uniqueConstraints = uniqueConstraints;
    }

    @Override
    public void setIsDistinct(@Nonnull Boolean isDistinct) {
        if (this.isDistinct != null)
            throw new IllegalStateException("Distinct information is already present. Only call this method once");
        this.isDistinct = isDistinct;
    }

    /**
     * TODO: explicit assumptions about the effects
     */
    @Override
    public IQTreeCache applyFreshRenaming(InjectiveVar2VarSubstitution renamingSubstitution) {
        VariableNullability newVariableNullability = variableNullability == null
                ? null
                : variableNullability.applyFreshRenaming(renamingSubstitution);

        ImmutableSet<Variable> newVariables = variables == null
                ? null
                : variables.stream()
                .map(renamingSubstitution::applyToVariable)
                .collect(ImmutableCollectors.toSet());

        ImmutableSet<Variable> newNotInternallyRequiredVariables = notInternallyRequiredVariables == null
                ? null
                : notInternallyRequiredVariables.stream()
                .map(renamingSubstitution::applyToVariable)
                .collect(ImmutableCollectors.toSet());

        ImmutableSet<ImmutableSubstitution<NonVariableTerm>> newPossibleDefinitions = possibleVariableDefinitions == null
                ? null
                : possibleVariableDefinitions.stream()
                .map(renamingSubstitution::applyRenaming)
                .collect(ImmutableCollectors.toSet());

        ImmutableSet<ImmutableSet<Variable>> newUniqueConstraints = uniqueConstraints == null
                ? null
                : uniqueConstraints.stream()
                .map(vs -> vs.stream()
                        .map(renamingSubstitution::applyToVariable)
                        .collect(ImmutableCollectors.toSet()))
                .collect(ImmutableCollectors.toSet());

        return new ConcreteIQTreeCacheImpl(coreSingletons, isNormalizedForOptimization, areDistinctAlreadyRemoved,
                newVariableNullability, newVariables, newNotInternallyRequiredVariables, newPossibleDefinitions,
                newUniqueConstraints, isDistinct);
    }
}
