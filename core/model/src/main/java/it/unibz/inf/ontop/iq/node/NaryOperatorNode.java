package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.transform.IQTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

/**
 * Has at least two children
 */
public interface NaryOperatorNode extends QueryNode {

    IQTree normalizeForOptimization(ImmutableList<IQTree> children, VariableGenerator variableGenerator,
                                    IQProperties currentIQProperties);

    IQTree applyDescendingSubstitution(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                       Optional<ImmutableExpression> constraint, ImmutableList<IQTree> children);

    ImmutableSet<Variable> getNullableVariables(ImmutableList<IQTree> children);

    boolean isConstructed(Variable variable, ImmutableList<IQTree> children);

    boolean isDistinct(ImmutableList<IQTree> children);

    @Deprecated
    IQTree liftIncompatibleDefinitions(Variable variable, ImmutableList<IQTree> children);

    IQTree propagateDownConstraint(ImmutableExpression constraint, ImmutableList<IQTree> children);

    IQTree acceptTransformer(IQTree tree, IQTransformer transformer, ImmutableList<IQTree> children);

    /**
     * Only validates the node, not its children
     */
    void validateNode(ImmutableList<IQTree> children) throws InvalidIntermediateQueryException;

    IQTree removeDistincts(ImmutableList<IQTree> children, IQProperties properties);
}
