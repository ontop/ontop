package it.unibz.inf.ontop.iq.node.impl;


import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.datalog.impl.DatalogTools;
import it.unibz.inf.ontop.evaluator.TermNullabilityEvaluator;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.evaluator.ExpressionEvaluator;
import it.unibz.inf.ontop.iq.node.JoinOrFilterNode;

import java.util.Optional;

public abstract class JoinOrFilterNodeImpl extends QueryNodeImpl implements JoinOrFilterNode {

    private Optional<ImmutableExpression> optionalFilterCondition;
    private final TermNullabilityEvaluator nullabilityEvaluator;
    protected final TermFactory termFactory;
    protected final TypeFactory typeFactory;
    protected final DatalogTools datalogTools;
    private final ExpressionEvaluator defaultExpressionEvaluator;

    protected JoinOrFilterNodeImpl(Optional<ImmutableExpression> optionalFilterCondition,
                                   TermNullabilityEvaluator nullabilityEvaluator, TermFactory termFactory,
                                   TypeFactory typeFactory, DatalogTools datalogTools,
                                   ExpressionEvaluator defaultExpressionEvaluator) {
        this.optionalFilterCondition = optionalFilterCondition;
        this.nullabilityEvaluator = nullabilityEvaluator;
        this.termFactory = termFactory;
        this.typeFactory = typeFactory;
        this.datalogTools = datalogTools;
        this.defaultExpressionEvaluator = defaultExpressionEvaluator;
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
    public ImmutableSet<Variable> getLocalVariables() {
        if (optionalFilterCondition.isPresent()) {
            return optionalFilterCondition.get().getVariables();
        }
        else {
            return ImmutableSet.of();
        }
    }

    protected ExpressionEvaluator.EvaluationResult transformBooleanExpression(
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            ImmutableExpression booleanExpression) {

        ImmutableExpression substitutedExpression = substitution.applyToBooleanExpression(booleanExpression);

        return createExpressionEvaluator().evaluateExpression(substitutedExpression);
    }

    protected ExpressionEvaluator createExpressionEvaluator() {
        return defaultExpressionEvaluator.clone();
    }

    protected boolean isFilteringNullValue(Variable variable) {
        return getOptionalFilterCondition()
                .filter(e -> nullabilityEvaluator.isFilteringNullValue(e, variable))
                .isPresent();
    }

    protected TermNullabilityEvaluator getNullabilityEvaluator() {
        return nullabilityEvaluator;
    }

    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        return getLocalVariables();
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        return ImmutableSet.of();
    }
}
