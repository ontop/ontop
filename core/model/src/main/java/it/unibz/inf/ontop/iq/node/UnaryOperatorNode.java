package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.iq.request.VariableNonRequirement;
import it.unibz.inf.ontop.iq.transform.IQTreeExtendedTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.NonVariableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

/**
 * Has ONE child
 */
public interface UnaryOperatorNode extends QueryNode {

    IQTree normalizeForOptimization(IQTree child, VariableGenerator variableGenerator, IQTreeCache treeCache);

    IQTree applyDescendingSubstitution(Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                       Optional<ImmutableExpression> constraint, IQTree child, VariableGenerator variableGenerator);

    IQTree applyDescendingSubstitutionWithoutOptimizing(Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                                        IQTree child, VariableGenerator variableGenerator);

    IQTree applyFreshRenaming(InjectiveSubstitution<Variable> renamingSubstitution, IQTree child, IQTreeCache treeCache);

    VariableNullability getVariableNullability(IQTree child);

    boolean isConstructed(Variable variable, IQTree child);

    boolean isDistinct(IQTree tree, IQTree child);

    @Deprecated
    IQTree liftIncompatibleDefinitions(Variable variable, IQTree child, VariableGenerator variableGenerator);

    IQTree propagateDownConstraint(ImmutableExpression constraint, IQTree child, VariableGenerator variableGenerator);

    IQTree acceptTransformer(IQTree tree, IQTreeVisitingTransformer transformer, IQTree child);

    <T> IQTree acceptTransformer(IQTree tree, IQTreeExtendedTransformer<T> transformer, IQTree child, T context);

    <T> T acceptVisitor(IQVisitor<T> visitor, IQTree child);

    @Override
    UnaryOperatorNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException;

    /**
     * Only validates the node, not its child
     */
    void validateNode(IQTree child) throws InvalidIntermediateQueryException;

    ImmutableSet<Substitution<NonVariableTerm>> getPossibleVariableDefinitions(IQTree child);

    IQTree removeDistincts(IQTree child, IQTreeCache treeCache);

    ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints(IQTree child);

    FunctionalDependencies inferFunctionalDependencies(IQTree child, ImmutableSet<ImmutableSet<Variable>> uniqueConstraints, ImmutableSet<Variable> variables);

    VariableNonRequirement computeVariableNonRequirement(IQTree child);
}
