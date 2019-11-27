package it.unibz.inf.ontop.iq;

import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;

import java.util.Optional;

/**
 * TODO: explain
 *
 *
 * IMPORTANT: An IQTreeCache instance CAN ONLY be attached to a SINGLE IQTree!
 */
public interface IQTreeCache {

    boolean isNormalizedForOptimization();

    boolean areDistinctAlreadyRemoved();

    Optional<VariableNullability> getVariableNullability();


    IQTreeCache declareAsNormalizedForOptimizationWithoutEffect();
    IQTreeCache declareAsNormalizedForOptimizationWithEffect();

    /**
     * TODO: consider if we should keep track of the constraint
     */
    IQTreeCache declareConstraintPushedDownWithEffect();

    IQTreeCache declareDistinctRemovalWithoutEffect();
    IQTreeCache declareDistinctRemovalWithEffect();

    /**
     * Can only be set ONCE!
     */
    void setVariableNullability(VariableNullability variableNullability);

    /**
     * Temporary
     */
    @Deprecated
    IQProperties convertIntoIQProperties();

    IQTreeCache applyFreshRenaming(InjectiveVar2VarSubstitution renamingSubstitution);
}
