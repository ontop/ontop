package it.unibz.inf.ontop.iq.node;

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
 * Has ONE child
 */
public interface UnaryOperatorNode extends QueryNode {

    IQTree normalizeForOptimization(IQTree child, VariableGenerator variableGenerator, IQProperties currentIQProperties);

    IQTree applyDescendingSubstitution(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                       Optional<ImmutableExpression> constraint, IQTree child);

    ImmutableSet<Variable> getNullableVariables(IQTree child);

    boolean isConstructed(Variable variable, IQTree child);

    @Deprecated
    IQTree liftIncompatibleDefinitions(Variable variable, IQTree child);

    IQTree propagateDownConstraint(ImmutableExpression constraint, IQTree child);

    IQTree acceptTransformer(IQTree tree, IQTransformer transformer, IQTree child);

    /**
     * Only validates the node, not its child
     */
    void validateNode(IQTree child) throws InvalidIntermediateQueryException;
}
