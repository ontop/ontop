package it.unibz.inf.ontop.pivotalrepr.impl;


import java.util.Optional;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.ImmutableBooleanExpression;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.model.VariableOrGroundTerm;
import it.unibz.inf.ontop.pivotalrepr.JoinOrFilterNode;
import it.unibz.inf.ontop.model.ImmutableSubstitution;

public abstract class JoinOrFilterNodeImpl extends QueryNodeImpl implements JoinOrFilterNode {

    private Optional<ImmutableBooleanExpression> optionalFilterCondition;

    protected JoinOrFilterNodeImpl(Optional<ImmutableBooleanExpression> optionalFilterCondition) {
        this.optionalFilterCondition = optionalFilterCondition;
    }

    @Override
    public Optional<ImmutableBooleanExpression> getOptionalFilterCondition() {
        return optionalFilterCondition;
    }

    protected String getOptionalFilterString() {
        if (optionalFilterCondition.isPresent()) {
            return " " + optionalFilterCondition.get().toString();
        }

        return "";
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        if (optionalFilterCondition.isPresent()) {
            return optionalFilterCondition.get().getVariables();
        }
        else {
            return ImmutableSet.of();
        }
    }

    protected ImmutableBooleanExpression transformBooleanExpression(
            ImmutableSubstitution<? extends VariableOrGroundTerm> substitution,
            ImmutableBooleanExpression booleanExpression) {
        return substitution.applyToBooleanExpression(booleanExpression);
    }
}
