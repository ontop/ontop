package it.unibz.inf.ontop.iq;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.iq.request.VariableNonRequirement;
import it.unibz.inf.ontop.model.term.NonVariableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.Substitution;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * TODO: find a better name
 *
 * These methods are RESERVED to the IQTree implementations
 *
 */
public interface ConcreteIQTreeCache extends IQTreeCache {

    @Nullable
    ImmutableSet<Variable> getVariables();

    @Nullable
    VariableNonRequirement getVariableNonRequirement();

    @Nullable
    VariableNullability getVariableNullability();

    @Nullable
    ImmutableSet<Substitution<NonVariableTerm>> getPossibleVariableDefinitions();

    @Nullable
    ImmutableSet<ImmutableSet<Variable>> getUniqueConstraints();

    @Nullable
    FunctionalDependencies getFunctionalDependencies();

    @Nullable
    Boolean isDistinct();

    /**
     * Can only be set ONCE!
     */
    void setVariables(@Nonnull ImmutableSet<Variable> variables);

    /**
     * Can only be set ONCE!
     */
    void setVariableNonRequirement(@Nonnull VariableNonRequirement notInternallyRequiredVariables);

    /**
     * Can only be set ONCE!
     */
    void setVariableNullability(@Nonnull VariableNullability variableNullability);

    /**
     * Can only be set ONCE!
     */
    void setPossibleVariableDefinitions(@Nonnull ImmutableSet<Substitution<NonVariableTerm>> possibleVariableDefinitions);

    /**
     * Can only be set ONCE!
     */
    void setUniqueConstraints(@Nonnull ImmutableSet<ImmutableSet<Variable>> uniqueConstraints);

    /**
     * Can only be set ONCE!
     */
    void setFunctionalDependencies(@Nonnull FunctionalDependencies functionalDependencies);

    /**
     * Can only be set ONCE!
     */
    void setIsDistinct(@Nonnull Boolean isDistinct);
}
