package it.unibz.inf.ontop.pivotalrepr.impl;


import java.util.Optional;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.owlrefplatform.core.unfolding.ExpressionEvaluator;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.JoinOrFilterNode;

public abstract class JoinOrFilterNodeImpl extends QueryNodeImpl implements JoinOrFilterNode {

    private Optional<ImmutableExpression> optionalFilterCondition;

    protected JoinOrFilterNodeImpl(Optional<ImmutableExpression> optionalFilterCondition) {
        this.optionalFilterCondition = optionalFilterCondition;
    }

    @Override
    public Optional<ImmutableExpression> getOptionalFilterCondition() {
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

    protected ExpressionEvaluator.Evaluation transformBooleanExpression(
            IntermediateQuery query,
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            ImmutableExpression booleanExpression) {

        ImmutableExpression substitutedExpression = substitution.applyToBooleanExpression(booleanExpression);

        return new ExpressionEvaluator(query.getMetadata().getUriTemplateMatcher()).evaluateExpression(
                substitutedExpression);
    }
}
