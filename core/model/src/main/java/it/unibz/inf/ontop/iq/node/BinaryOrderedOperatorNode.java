package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.NonVariableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

/**
 * The ordering of the operands is meaningful procedurally,
 * and therefore should be preserved.
 *
 * It may not be meaningful semantically though.
 * such that there may be instances of BinaryOrderedOperatorNode which are also instances of
 * CommutativeJoinOrFilterNode.
 */
public interface BinaryOrderedOperatorNode extends QueryNode {

    enum ArgumentPosition {
        LEFT,
        RIGHT
    }

    VariableNullability getVariableNullability(IQTree leftChild, IQTree rightChild);

    ImmutableSet<ImmutableSubstitution<NonVariableTerm>> getPossibleVariableDefinitions(IQTree leftChild, IQTree rightChild);

    IQTree acceptTransformer(IQTree tree, IQTreeVisitingTransformer transformer, IQTree leftChild, IQTree rightChild);

    <T> T acceptVisitor(IQVisitor<T> visitor, IQTree leftChild, IQTree rightChild);

    IQTree normalizeForOptimization(IQTree leftChild, IQTree rightChild, VariableGenerator variableGenerator,
                                    IQProperties currentIQProperties);

    IQTree liftIncompatibleDefinitions(Variable variable, IQTree leftChild, IQTree rightChild, VariableGenerator variableGenerator);

    IQTree applyDescendingSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
            Optional<ImmutableExpression> constraint, IQTree leftChild, IQTree rightChild);

    IQTree applyDescendingSubstitutionWithoutOptimizing(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
            IQTree leftChild, IQTree rightChild);

    IQTree applyFreshRenaming(InjectiveVar2VarSubstitution renamingSubstitution, IQTree leftChild, IQTree rightChild,
                              IQProperties properties, Optional<VariableNullability> variableNullability);

    boolean isConstructed(Variable variable, IQTree leftChild, IQTree rightChild);

    boolean isDistinct(IQTree leftChild, IQTree rightChild);

    IQTree propagateDownConstraint(ImmutableExpression constraint, IQTree leftChild, IQTree rightChild);

    /**
     * Only validates the node, not its children
     */
    void validateNode(IQTree leftChild, IQTree rightChild) throws InvalidIntermediateQueryException;

    IQTree removeDistincts(IQTree leftChild, IQTree rightChild, IQProperties properties);

    ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints(IQTree leftChild, IQTree rightChild);
}
