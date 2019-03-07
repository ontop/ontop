package it.unibz.inf.ontop.iq.transformer;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.transformer.impl.DefPushDownRequestImpl;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

/**
 * Pushes down in the tree a new definition (for a new variable) until:
 *   1. the condition can be evaluated to true/false/null
 *   2. or the definition is blocked by a (inner/left) join (its variables are defined in different children)
 */
public interface DefinitionPushDownTransformer extends IQTreeTransformer {


    interface DefPushDownRequest {

        Variable getNewVariable();

        /**
         * Only when the condition is satisfied!
         */
        ImmutableTerm getDefinitionWhenConditionSatisfied();

        ImmutableExpression getCondition();

        ImmutableSet<Variable> getDefinitionAndConditionVariables();

        DefPushDownRequest newRequest(ImmutableSubstitution<? extends ImmutableTerm> substitution);

        static DefPushDownRequest create(Variable newVariable, ImmutableTerm definition, ImmutableExpression condition) {
            return new DefPushDownRequestImpl(newVariable, definition, condition);
        }
    }

}
