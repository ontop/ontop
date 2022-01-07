package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.transform.IQTreeExtendedTransformer;
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

    VariableNullability getVariableNullability(IQTree leftChild, IQTree rightChild);

    ImmutableSet<ImmutableSubstitution<NonVariableTerm>> getPossibleVariableDefinitions(IQTree leftChild, IQTree rightChild);

    IQTree acceptTransformer(IQTree tree, IQTreeVisitingTransformer transformer, IQTree leftChild, IQTree rightChild);

    <T> IQTree acceptTransformer(IQTree tree, IQTreeExtendedTransformer<T> transformer, IQTree leftChild, IQTree rightChild,
                                 T context);

    <T> T acceptVisitor(IQVisitor<T> visitor, IQTree leftChild, IQTree rightChild);

    IQTree normalizeForOptimization(IQTree leftChild, IQTree rightChild, VariableGenerator variableGenerator,
                                    IQTreeCache treeCache);

    IQTree liftIncompatibleDefinitions(Variable variable, IQTree leftChild, IQTree rightChild, VariableGenerator variableGenerator);

    IQTree applyDescendingSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
            Optional<ImmutableExpression> constraint, IQTree leftChild, IQTree rightChild);

    IQTree applyDescendingSubstitutionWithoutOptimizing(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
            IQTree leftChild, IQTree rightChild);

    IQTree applyFreshRenaming(InjectiveVar2VarSubstitution renamingSubstitution, IQTree leftChild, IQTree rightChild,
                              IQTreeCache treeCache);

    boolean isConstructed(Variable variable, IQTree leftChild, IQTree rightChild);

    boolean isDistinct(IQTree tree, IQTree leftChild, IQTree rightChild);

    IQTree propagateDownConstraint(ImmutableExpression constraint, IQTree leftChild, IQTree rightChild);

    /**
     * Only validates the node, not its children
     */
    void validateNode(IQTree leftChild, IQTree rightChild) throws InvalidIntermediateQueryException;

    IQTree removeDistincts(IQTree leftChild, IQTree rightChild, IQTreeCache treeCache);

    ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints(IQTree leftChild, IQTree rightChild);

    ImmutableSet<Variable> computeNotInternallyRequiredVariables(IQTree leftChild, IQTree rightChild);
}
