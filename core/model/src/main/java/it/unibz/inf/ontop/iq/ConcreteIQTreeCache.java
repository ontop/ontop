package it.unibz.inf.ontop.iq;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.Variable;

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
    VariableNullability getVariableNullability();

    /**
     * Can only be set ONCE!
     */
    void setVariables(@Nonnull ImmutableSet<Variable> variables);

    /**
     * Can only be set ONCE!
     */
    void setVariableNullability(@Nonnull VariableNullability variableNullability);

    /**
     * Temporary
     */
    @Deprecated
    IQProperties convertIntoIQProperties();

}
