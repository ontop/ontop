package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

/**
 * Has at least two children
 */
public interface NaryOperatorNode extends QueryNode {

    IQTree liftBinding(ImmutableList<IQTree> children, VariableGenerator variableGenerator,
                       IQProperties currentIQProperties);

    IQTree applyDescendingSubstitution(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                       Optional<ImmutableExpression> constraint, ImmutableList<IQTree> children);

    IQTree applyDescendingSubstitutionWithoutOptimizing(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
            ImmutableList<IQTree> children);

    VariableNullability getVariableNullability(ImmutableList<IQTree> children);

    boolean isConstructed(Variable variable, ImmutableList<IQTree> children);

    IQTree liftIncompatibleDefinitions(Variable variable, ImmutableList<IQTree> children);

    IQTree propagateDownConstraint(ImmutableExpression constraint, ImmutableList<IQTree> children);

    IQTree acceptTransformer(IQTree tree, IQTreeVisitingTransformer transformer, ImmutableList<IQTree> children);

    /**
     * Only validates the node, not its children
     */
    void validateNode(ImmutableList<IQTree> children) throws InvalidIntermediateQueryException;

    NaryOperatorNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException;

    ImmutableSet<ImmutableSubstitution<NonVariableTerm>> getPossibleVariableDefinitions(ImmutableList<IQTree> children);
}
