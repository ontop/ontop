package it.unibz.inf.ontop.iq;

import it.unibz.inf.ontop.iq.node.VariableNullability;

import java.util.Optional;

/**
 * TODO: find a better name
 *
 * Sub-interface for the methods reserved to the IQTree implementation
 *
 */
public interface ConcreteIQTreeCache extends IQTreeCache {

    Optional<VariableNullability> getVariableNullability();

    /**
     * Can only be set ONCE!
     */
    void setVariableNullability(VariableNullability variableNullability);

    /**
     * Temporary
     */
    @Deprecated
    IQProperties convertIntoIQProperties();

}
