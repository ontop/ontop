package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.DownPropagation;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.iq.request.VariableNonRequirement;
import it.unibz.inf.ontop.iq.visit.IQTreeVisitor;
import it.unibz.inf.ontop.model.term.NonVariableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

/**
 * Has ONE child
 */
public interface UnaryOperatorNode extends QueryNode {

    IQTree normalizeForOptimization(IQTree child, VariableGenerator variableGenerator, IQTreeCache treeCache);

    UnaryOperatorNode applyFreshRenaming(InjectiveSubstitution<Variable> renamingSubstitution);

    IQTree propagateDownConstraint(DownPropagation dp, IQTree child);

    IQTree applyDescendingSubstitution(DownPropagation dp, IQTree child);

    VariableNullability getVariableNullability(IQTree child);

    boolean isConstructed(Variable variable, IQTree child);

    boolean isDistinct(IQTree tree, IQTree child);

    @Deprecated
    IQTree liftIncompatibleDefinitions(Variable variable, IQTree child, VariableGenerator variableGenerator);

    <T> T acceptVisitor(UnaryIQTree tree, IQTreeVisitor<T> visitor, IQTree child);

    /**
     * Only validates the node, not its child
     */
    void validateNode(IQTree child) throws InvalidIntermediateQueryException;

    ImmutableSet<Substitution<NonVariableTerm>> getPossibleVariableDefinitions(IQTree child);

    IQTree removeDistincts(IQTree child, IQTreeCache treeCache);

    ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints(IQTree child);

    FunctionalDependencies inferFunctionalDependencies(IQTree child, ImmutableSet<ImmutableSet<Variable>> uniqueConstraints, ImmutableSet<Variable> variables);

    VariableNonRequirement computeVariableNonRequirement(IQTree child);

    ImmutableSet<Variable> inferStrictDependents(UnaryIQTree tree, IQTree child);
}
