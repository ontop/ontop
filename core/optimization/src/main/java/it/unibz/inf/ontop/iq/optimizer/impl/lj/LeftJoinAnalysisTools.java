package it.unibz.inf.ontop.iq.optimizer.impl.lj;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.model.term.ImmutableExpression;

import java.util.Optional;

public class LeftJoinAnalysisTools {

    /**
     * A LJ condition can be handled if it can safely be lifting, which requires that the LJ operates over a
     * unique constraint on the right side
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static boolean tolerateLJConditionLifting(Optional<ImmutableExpression> optionalLJCondition, IQTree leftChild, IQTree rightChild) {
        return optionalLJCondition.isEmpty() || rightChild.inferUniqueConstraints().stream()
                .anyMatch(uc -> leftChild.getVariables().containsAll(uc));
    }
}
