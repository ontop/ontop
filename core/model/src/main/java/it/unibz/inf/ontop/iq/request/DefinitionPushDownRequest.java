package it.unibz.inf.ontop.iq.request;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.request.impl.DefPushDownRequestImpl;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

/**
 * Request for pushing down in the IQTree the definition of a new variable
 */
public interface DefinitionPushDownRequest {

    Variable getNewVariable();

    /**
     * Only when the condition is satisfied!
     */
    ImmutableTerm getDefinitionWhenConditionSatisfied();

    ImmutableExpression getCondition();

    ImmutableSet<Variable> getDefinitionAndConditionVariables();

    DefinitionPushDownRequest newRequest(ImmutableSubstitution<? extends ImmutableTerm> substitution);

    static DefinitionPushDownRequest create(Variable newVariable, ImmutableTerm definition, ImmutableExpression condition) {
        return new DefPushDownRequestImpl(newVariable, definition, condition);
    }
}
