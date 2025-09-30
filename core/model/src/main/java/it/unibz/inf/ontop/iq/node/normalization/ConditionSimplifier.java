package it.unibz.inf.ontop.iq.node.normalization;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.DownPropagation;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.Substitution;

import java.util.Optional;

public interface ConditionSimplifier {

    ExpressionAndSubstitution simplifyCondition(Optional<ImmutableExpression> expression,
                                                ImmutableSet<Variable> nonLiftableVariables, ImmutableList<IQTree> children,
                                                VariableNullability variableNullability)
                    throws DownPropagation.InconsistentDownPropagationException;


    ExpressionAndSubstitutionAndChildren simplifyAndPropagate(DownPropagation downPropagation, Optional<ImmutableExpression> expression, ImmutableList<IQTree> children,
                                                              VariableNullability variableNullability)
            throws DownPropagation.InconsistentDownPropagationException;


    interface ExpressionAndSubstitution {
        Substitution<VariableOrGroundTerm> getSubstitution();
        Optional<ImmutableExpression> getOptionalExpression();
    }

    interface ExpressionAndSubstitutionAndChildren {
        Optional<ConstructionNode> getConstructionNode();
        Optional<ImmutableExpression> getOptionalExpression();
        ImmutableList<IQTree> getChildren();
    }
}
