package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.ConcreteIQTreeCache;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.Variable;
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

    /**
     * Initial constructor
     */
    @Inject
    protected ConcreteIQTreeCacheImpl(CoreSingletons coreSingletons) {
        this.coreSingletons = coreSingletons;
        this.isNormalizedForOptimization = false;
        this.areDistinctAlreadyRemoved = false;
    }

    /**
     * Internal constructor
     */
    protected ConcreteIQTreeCacheImpl(CoreSingletons coreSingletons, boolean isNormalizedForOptimization,
                                      boolean areDistinctAlreadyRemoved, @Nullable VariableNullability variableNullability,
                                      @Nullable ImmutableSet<Variable> variables) {
        this.isNormalizedForOptimization = isNormalizedForOptimization;
        this.areDistinctAlreadyRemoved = areDistinctAlreadyRemoved;
        this.coreSingletons = coreSingletons;
        this.variableNullability = variableNullability;
        this.variables = variables;
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

    @Override
    public @Nullable VariableNullability getVariableNullability() {
        return variableNullability;
    }

    @Override
    public void setVariables(@Nonnull ImmutableSet<Variable> variables) {
        this.variables = variables;
    }

    @Override
    public IQTreeCache declareAsNormalizedForOptimizationWithoutEffect() {
        return new ConcreteIQTreeCacheImpl(coreSingletons, true, areDistinctAlreadyRemoved,
                variableNullability, variables);
    }

    /**
     * TODO: explicit assumptions about the effects of normalization
     */
    @Override
    public IQTreeCache declareAsNormalizedForOptimizationWithEffect() {
        return new ConcreteIQTreeCacheImpl(coreSingletons, true, areDistinctAlreadyRemoved,
                variableNullability, variables);
    }

    /**
     * TODO: explicit assumptions about the effects
     */
    @Override
    public IQTreeCache declareConstraintPushedDownWithEffect() {
        return new ConcreteIQTreeCacheImpl(coreSingletons, false, areDistinctAlreadyRemoved,
                variableNullability, variables);
    }

    @Override
    public IQTreeCache declareDistinctRemovalWithoutEffect() {
        return new ConcreteIQTreeCacheImpl(coreSingletons, isNormalizedForOptimization, true,
                variableNullability, variables);
    }

    /**
     * TODO: explicit assumptions about the effects
     */
    @Override
    public IQTreeCache declareDistinctRemovalWithEffect() {
        return new ConcreteIQTreeCacheImpl(coreSingletons, false, true,
                variableNullability, variables);
    }

    @Override
    public  synchronized void setVariableNullability(VariableNullability variableNullability) {
        if (this.variableNullability != null)
            throw new IllegalStateException("Variable nullability already present. Only call this method once");
        this.variableNullability = variableNullability;
    }

    @Override
    public IQProperties convertIntoIQProperties() {
        // Non-final
        IQProperties properties = coreSingletons.getIQFactory().createIQProperties();
        if (areDistinctAlreadyRemoved)
            properties = properties.declareDistinctRemovalWithoutEffect();
        if (isNormalizedForOptimization)
            properties = properties.declareNormalizedForOptimization();

        return properties;
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

        return new ConcreteIQTreeCacheImpl(coreSingletons, isNormalizedForOptimization, areDistinctAlreadyRemoved,
                newVariableNullability, newVariables);
    }
}
