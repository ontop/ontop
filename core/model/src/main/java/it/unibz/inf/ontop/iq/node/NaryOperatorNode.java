package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.DownPropagation;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.iq.request.VariableNonRequirement;
import it.unibz.inf.ontop.iq.visit.IQTreeVisitor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

/**
 * Has at least two children
 */
public interface NaryOperatorNode extends QueryNode {

    IQTree normalizeForOptimization(ImmutableList<IQTree> children, VariableGenerator variableGenerator,
                                    IQTreeCache treeCache);

    NaryOperatorNode applyFreshRenaming(InjectiveSubstitution<Variable> renamingSubstitution);

    IQTree propagateDownConstraint(DownPropagation dp, ImmutableList<IQTree> children);

    IQTree applyDescendingSubstitution(DownPropagation dp, ImmutableList<IQTree> children);


    VariableNullability getVariableNullability(ImmutableList<IQTree> children);

    boolean isConstructed(Variable variable, ImmutableList<IQTree> children);

    boolean isDistinct(IQTree tree, ImmutableList<IQTree> children);

    @Deprecated
    IQTree liftIncompatibleDefinitions(Variable variable, ImmutableList<IQTree> children, VariableGenerator variableGenerator);

    <T> T acceptVisitor(NaryIQTree tree, IQTreeVisitor<T> visitor, ImmutableList<IQTree> children);

    /**
     * Only validates the node, not its children
     */
    void validateNode(ImmutableList<IQTree> children) throws InvalidIntermediateQueryException;

    ImmutableSet<Substitution<NonVariableTerm>> getPossibleVariableDefinitions(ImmutableList<IQTree> children);

    IQTree removeDistincts(ImmutableList<IQTree> children, IQTreeCache treeCache);

    ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints(ImmutableList<IQTree> children);
    FunctionalDependencies inferFunctionalDependencies(ImmutableList<IQTree> children, ImmutableSet<ImmutableSet<Variable>> uniqueConstraints, ImmutableSet<Variable> variables);

    VariableNonRequirement computeVariableNonRequirement(ImmutableList<IQTree> children);

    ImmutableSet<Variable> inferStrictDependents(NaryIQTree tree, ImmutableList<IQTree> children);
}
