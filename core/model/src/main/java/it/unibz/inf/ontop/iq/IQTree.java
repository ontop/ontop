package it.unibz.inf.ontop.iq;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;


public interface IQTree {

    QueryNode getRootNode();

    ImmutableList<IQTree> getChildren();

    ImmutableSet<Variable> getVariables();

    IQTree liftBinding(VariableGenerator variableGenerator);

    default boolean isLeaf() {
        return getChildren().isEmpty();
    }

    IQTree applyDescendingSubstitution(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                       @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
                                   Optional<ImmutableExpression> constraint);

    ImmutableSet<Variable> getKnownVariables();
}
