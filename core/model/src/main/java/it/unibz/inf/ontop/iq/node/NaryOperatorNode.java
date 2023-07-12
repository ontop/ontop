package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableList;
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
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

/**
 * Has at least two children
 */
public interface NaryOperatorNode extends QueryNode {

    IQTree normalizeForOptimization(ImmutableList<IQTree> children, VariableGenerator variableGenerator,
                                    IQTreeCache treeCache);

    IQTree applyDescendingSubstitution(Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                       Optional<ImmutableExpression> constraint, ImmutableList<IQTree> children,
                                       VariableGenerator variableGenerator);

    IQTree applyDescendingSubstitutionWithoutOptimizing(
            Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
            ImmutableList<IQTree> children, VariableGenerator variableGenerator);

    IQTree applyFreshRenaming(InjectiveSubstitution<Variable> renamingSubstitution, ImmutableList<IQTree> children,
                              IQTreeCache treeCache);

    VariableNullability getVariableNullability(ImmutableList<IQTree> children);

    boolean isConstructed(Variable variable, ImmutableList<IQTree> children);

    boolean isDistinct(IQTree tree, ImmutableList<IQTree> children);

    @Deprecated
    IQTree liftIncompatibleDefinitions(Variable variable, ImmutableList<IQTree> children, VariableGenerator variableGenerator);

    IQTree propagateDownConstraint(ImmutableExpression constraint, ImmutableList<IQTree> children, VariableGenerator variableGenerator);

    IQTree acceptTransformer(IQTree tree, IQTreeVisitingTransformer transformer, ImmutableList<IQTree> children);

    <T> IQTree acceptTransformer(IQTree tree, IQTreeExtendedTransformer<T> transformer, ImmutableList<IQTree> children,
                             T context);

    <T> T acceptVisitor(IQVisitor<T> visitor, ImmutableList<IQTree> children);

    /**
     * Only validates the node, not its children
     */
    void validateNode(ImmutableList<IQTree> children) throws InvalidIntermediateQueryException;

    NaryOperatorNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException;

    ImmutableSet<Substitution<NonVariableTerm>> getPossibleVariableDefinitions(ImmutableList<IQTree> children);

    IQTree removeDistincts(ImmutableList<IQTree> children, IQTreeCache treeCache);

    ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints(ImmutableList<IQTree> children);
    FunctionalDependencies inferFunctionalDependencies(ImmutableList<IQTree> children, ImmutableSet<ImmutableSet<Variable>> uniqueConstraints, ImmutableSet<Variable> variables);

    VariableNonRequirement computeVariableNonRequirement(ImmutableList<IQTree> children);
}
