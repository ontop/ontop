package it.unibz.inf.ontop.iq;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.NonVariableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

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
    ImmutableSet<Variable> getNotInternallyRequiredVariables();

    @Nullable
    VariableNullability getVariableNullability();

    @Nullable
    ImmutableSet<ImmutableSubstitution<NonVariableTerm>> getPossibleVariableDefinitions();

    @Nullable
    ImmutableSet<ImmutableSet<Variable>> getUniqueConstraints();

    @Nullable
    Boolean isDistinct();

    /**
     * Can only be set ONCE!
     */
    void setVariables(@Nonnull ImmutableSet<Variable> variables);

    /**
     * Can only be set ONCE!
     */
    void setNotInternallyRequiredVariables(@Nonnull ImmutableSet<Variable> notInternallyRequiredVariables);

    /**
     * Can only be set ONCE!
     */
    void setVariableNullability(@Nonnull VariableNullability variableNullability);

    /**
     * Can only be set ONCE!
     */
    void setPossibleVariableDefinitions(@Nonnull ImmutableSet<ImmutableSubstitution<NonVariableTerm>> possibleVariableDefinitions);

    /**
     * Can only be set ONCE!
     */
    void setUniqueConstraints(@Nonnull ImmutableSet<ImmutableSet<Variable>> uniqueConstraints);

    /**
     * Can only be set ONCE!
     */
    void setIsDistinct(@Nonnull Boolean isDistinct);
}
