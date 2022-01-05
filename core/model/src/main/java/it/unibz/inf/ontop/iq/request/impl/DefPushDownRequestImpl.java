package it.unibz.inf.ontop.iq.request.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.request.DefinitionPushDownRequest;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Objects;
import java.util.stream.Stream;

public class DefPushDownRequestImpl implements DefinitionPushDownRequest {

    private final Variable newVariable;
    private final ImmutableTerm definition;
    private final ImmutableExpression condition;

    public DefPushDownRequestImpl(Variable newVariable, ImmutableTerm definition, ImmutableExpression condition) {
        this.newVariable = newVariable;
        this.definition = definition;
        this.condition = condition;
    }

    @Override
    public Variable getNewVariable() {
        return newVariable;
    }

    @Override
    public ImmutableTerm getDefinitionWhenConditionSatisfied() {
        return definition;
    }

    @Override
    public ImmutableExpression getCondition() {
        return condition;
    }

    @Override
    public ImmutableSet<Variable> getDefinitionAndConditionVariables() {
        return Stream.concat(
                    definition.getVariableStream(),
                    condition.getVariableStream())
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public DefinitionPushDownRequest newRequest(
            ImmutableSubstitution<? extends ImmutableTerm> substitution) {
        return new DefPushDownRequestImpl(newVariable,
                substitution.apply(definition),
                substitution.applyToBooleanExpression(condition));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefinitionPushDownRequest)) return false;
        DefinitionPushDownRequest that = (DefinitionPushDownRequest) o;
        return Objects.equals(newVariable, that.getNewVariable()) &&
                Objects.equals(definition, that.getDefinitionWhenConditionSatisfied()) &&
                Objects.equals(condition, that.getCondition());
    }

    @Override
    public int hashCode() {
        return Objects.hash(newVariable, definition, condition);
    }
}
