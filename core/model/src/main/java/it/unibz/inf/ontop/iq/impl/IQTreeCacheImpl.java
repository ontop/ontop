package it.unibz.inf.ontop.iq.impl;

import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Optional;

public class IQTreeCacheImpl implements IQTreeCache {

    private final boolean isNormalizedForOptimization;
    private final boolean areDistinctAlreadyRemoved;
    private final CoreSingletons coreSingletons;

    @Nullable
    private VariableNullability variableNullability;

    /**
     * Initial constructor
     */
    @Inject
    protected IQTreeCacheImpl(CoreSingletons coreSingletons) {
        this.coreSingletons = coreSingletons;
        this.isNormalizedForOptimization = false;
        this.areDistinctAlreadyRemoved = false;
    }

    protected IQTreeCacheImpl(CoreSingletons coreSingletons, boolean isNormalizedForOptimization,
                              boolean areDistinctAlreadyRemoved, @Nullable VariableNullability variableNullability) {
        this.isNormalizedForOptimization = isNormalizedForOptimization;
        this.areDistinctAlreadyRemoved = areDistinctAlreadyRemoved;
        this.coreSingletons = coreSingletons;
        this.variableNullability = variableNullability;
    }

    @Override
    public boolean isNormalizedForOptimization() {
        return isNormalizedForOptimization;
    }

    @Override
    public boolean areDistinctAlreadyRemoved() {
        return areDistinctAlreadyRemoved;
    }

    @Override
    public Optional<VariableNullability> getVariableNullability() {
        return Optional.ofNullable(variableNullability);
    }

    @Override
    public IQTreeCache declareAsNormalizedForOptimizationWithoutEffect() {
        return new IQTreeCacheImpl(coreSingletons, true, areDistinctAlreadyRemoved, variableNullability);
    }

    /**
     * TODO: explicit assumptions about the effects of normalization
     */
    @Override
    public IQTreeCache declareAsNormalizedForOptimizationWithEffect() {
        return new IQTreeCacheImpl(coreSingletons, true, areDistinctAlreadyRemoved, variableNullability);
    }

    /**
     * TODO: explicit assumptions about the effects
     */
    @Override
    public IQTreeCache declareConstraintPushedDownWithEffect() {
        return new IQTreeCacheImpl(coreSingletons, false, areDistinctAlreadyRemoved, variableNullability);
    }

    @Override
    public IQTreeCache declareDistinctRemovalWithoutEffect() {
        return new IQTreeCacheImpl(coreSingletons, isNormalizedForOptimization, true, variableNullability);
    }

    /**
     * TODO: explicit assumptions about the effects
     */
    @Override
    public IQTreeCache declareDistinctRemovalWithEffect() {
        return new IQTreeCacheImpl(coreSingletons, false, true, variableNullability);
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

    @Override
    public IQTreeCache applyFreshRenaming(InjectiveVar2VarSubstitution renamingSubstitution) {
        VariableNullability newVariableNullability = variableNullability == null
                ? null
                : variableNullability.applyFreshRenaming(renamingSubstitution);
        return new IQTreeCacheImpl(coreSingletons, isNormalizedForOptimization, areDistinctAlreadyRemoved, newVariableNullability);
    }
}
