package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.impl.DownPropagation;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.iq.request.VariableNonRequirement;
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

    IQTree applyDescendingSubstitution(DownPropagation dp, IQTree child);

    IQTree applyDescendingSubstitutionWithoutOptimizing(Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                                        IQTree child, VariableGenerator variableGenerator);

    UnaryOperatorNode applyFreshRenaming(InjectiveSubstitution<Variable> renamingSubstitution);

    VariableNullability getVariableNullability(IQTree child);

    boolean isConstructed(Variable variable, IQTree child);

    boolean isDistinct(IQTree tree, IQTree child);

    @Deprecated
    IQTree liftIncompatibleDefinitions(Variable variable, IQTree child, VariableGenerator variableGenerator);

    IQTree propagateDownConstraint(DownPropagation dp, IQTree child);

    <T> T acceptVisitor(UnaryIQTree tree, IQVisitor<T> visitor, IQTree child);

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
