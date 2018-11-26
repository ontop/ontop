package it.unibz.inf.ontop.iq.node.normalization;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.impl.UnsatisfiableConditionException;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.NonFunctionalTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

import java.util.Optional;

public interface ConditionSimplifier {

    ExpressionAndSubstitution simplifyCondition(ImmutableExpression expression)
            throws UnsatisfiableConditionException;

    ExpressionAndSubstitution simplifyCondition(Optional<ImmutableExpression> nonOptimizedExpression,
                                                ImmutableSet<Variable> nonLiftableVariables)
                    throws UnsatisfiableConditionException;

    Optional<ImmutableExpression> computeDownConstraint(Optional<ImmutableExpression> optionalConstraint,
                                                        ExpressionAndSubstitution conditionSimplificationResults)
                            throws UnsatisfiableConditionException;


    interface ExpressionAndSubstitution {
        ImmutableSubstitution<NonFunctionalTerm> getSubstitution();

        Optional<ImmutableExpression> getOptionalExpression();
    }
}
