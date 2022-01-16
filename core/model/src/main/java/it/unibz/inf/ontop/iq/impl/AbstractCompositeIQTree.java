package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.node.ExplicitVariableProjectionNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class AbstractCompositeIQTree<N extends QueryNode> implements CompositeIQTree<N> {

    private final N rootNode;
    private final ImmutableList<IQTree> children;
    private final ConcreteIQTreeCache treeCache;
    private static final String TAB_STR = "   ";

    /*
     * LAZY
     */
    @Nullable
    private ImmutableSet<Variable> knownVariables;

    /*
     * LAZY
     */
    @Nullable
    private String string;

    // Non final
    private boolean hasBeenSuccessfullyValidate;

    protected final IQTreeTools iqTreeTools;
    protected final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;

    protected AbstractCompositeIQTree(N rootNode, ImmutableList<IQTree> children,
                                      IQTreeCache treeCache, IQTreeTools iqTreeTools,
                                      IntermediateQueryFactory iqFactory, TermFactory termFactory) {
        this.iqTreeTools = iqTreeTools;
        this.iqFactory = iqFactory;
        this.termFactory = termFactory;
        if (children.isEmpty())
            throw new IllegalArgumentException("A composite IQ must have at least one child");
        this.rootNode = rootNode;
        this.children = children;
        if (!(treeCache instanceof ConcreteIQTreeCache))
            throw new IllegalArgumentException("Was expecting the tree cache to be instance of ConcreteIQTreeCache");
        this.treeCache = (ConcreteIQTreeCache) treeCache;
        // To be computed on-demand
        knownVariables = null;
        hasBeenSuccessfullyValidate = false;
    }

    @Override
    public N getRootNode() {
        return rootNode;
    }

    @Override
    public ImmutableList<IQTree> getChildren() {
        return children;
    }

    @Override
    public synchronized ImmutableSet<Variable> getVariables() {
        // Non-final
        ImmutableSet<Variable> variables = treeCache.getVariables();
        if (variables != null)
            return variables;
        variables = computeVariables();
        treeCache.setVariables(variables);
        return variables;
    }

    protected ImmutableSet<Variable> computeVariables() {
        if (rootNode instanceof ExplicitVariableProjectionNode)
            return ((ExplicitVariableProjectionNode) rootNode).getVariables();
        else
            return children.stream()
                    .flatMap(c -> c.getVariables().stream())
                    .collect(ImmutableCollectors.toSet());
    }

    @Override
    public ImmutableSet<Variable> getKnownVariables() {
        if (knownVariables == null)
            knownVariables = Stream.concat(
                    getRootNode().getLocalVariables().stream(),
                    getChildren().stream()
                            .flatMap(c -> c.getKnownVariables().stream()))
                    .collect(ImmutableCollectors.toSet());
        return knownVariables;
    }

    @Override
    public String toString() {
        if (string == null)
            string = printSubtree(this, "");
        return string;
    }

    /**
     * Recursive
     */
    private static String printSubtree(IQTree subTree, String offset) {
        String childOffset = offset + TAB_STR;

        return offset + subTree.getRootNode() + "\n"
                + subTree.getChildren().stream()
                    .map(c -> printSubtree(c, childOffset))
                    .reduce("", (c, a) -> c + a);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractCompositeIQTree<N> other = (AbstractCompositeIQTree<N>) o;
        return rootNode.equals(other.rootNode) && children.equals(other.children);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    protected Optional<ImmutableSubstitution<? extends VariableOrGroundTerm>> normalizeDescendingSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution)
            throws IQTreeTools.UnsatisfiableDescendingSubstitutionException {
        return iqTreeTools.normalizeDescendingSubstitution(this, descendingSubstitution);
    }

    @Override
    public IQTree applyDescendingSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
            Optional<ImmutableExpression> constraint) {
        try {
            Optional<ImmutableSubstitution<? extends VariableOrGroundTerm>> normalizedSubstitution =
                    normalizeDescendingSubstitution(descendingSubstitution);

            Optional<ImmutableExpression> newConstraint = normalizeConstraint(constraint, descendingSubstitution);

            return normalizedSubstitution
                    .flatMap(this::extractFreshRenaming)
                    // Fresh renaming
                    .map(s -> applyFreshRenaming(s, true))
                    .map(t -> newConstraint
                            .map(t::propagateDownConstraint)
                            .orElse(t))
                    // Regular substitution
                    .orElseGet(() -> normalizedSubstitution
                            .map(s -> applyRegularDescendingSubstitution(s, newConstraint))
                            .orElseGet(() -> newConstraint
                                    .map(this::propagateDownConstraint)
                                    .orElse(this)));

        } catch (IQTreeTools.UnsatisfiableDescendingSubstitutionException e) {
            return iqFactory.createEmptyNode(iqTreeTools.computeNewProjectedVariables(descendingSubstitution, getVariables()));
        }
    }

    @Override
    public IQTree applyFreshRenaming(InjectiveVar2VarSubstitution freshRenamingSubstitution) {
        return applyFreshRenaming(freshRenamingSubstitution, false);
    }

    @Override
    public IQTree applyFreshRenamingToAllVariables(InjectiveVar2VarSubstitution freshRenamingSubstitution) {
        return applyFreshRenaming(freshRenamingSubstitution, true);
    }

    private Optional<ImmutableExpression> normalizeConstraint(Optional<ImmutableExpression> constraint,
                                                              ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution) {
        if (!constraint.isPresent())
            return constraint;

        ImmutableSet<Variable> newVariables = getVariables().stream()
                .map(descendingSubstitution::applyToVariable)
                .filter(t -> t instanceof Variable)
                .map(t -> (Variable)t)
                .collect(ImmutableCollectors.toSet());

        return termFactory.getConjunction(constraint.get().flattenAND()
                .filter(e -> e.getVariableStream().anyMatch(newVariables::contains)));
    }

    protected abstract IQTree applyFreshRenaming(InjectiveVar2VarSubstitution freshRenamingSubstitution, boolean alreadyNormalized);

    protected abstract IQTree applyRegularDescendingSubstitution(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                                                 Optional<ImmutableExpression> constraint);

    private Optional<InjectiveVar2VarSubstitution> extractFreshRenaming(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution) {
        return iqTreeTools.extractFreshRenaming(descendingSubstitution, getVariables());
    }

    @Override
    public final void validate() throws InvalidIntermediateQueryException {
        if (!hasBeenSuccessfullyValidate) {
            validateNode();
            // (Indirectly) recursive
            children.forEach(IQTree::validate);

            hasBeenSuccessfullyValidate = true;
        }
    }

    /**
     * Only validates the node, not its children
     */
    protected abstract void validateNode() throws InvalidIntermediateQueryException;

    @Override
    public synchronized VariableNullability getVariableNullability() {
        // Non-final
        VariableNullability variableNullability = treeCache.getVariableNullability();
        if (variableNullability != null)
            return variableNullability;

        variableNullability = computeVariableNullability();
        treeCache.setVariableNullability(variableNullability);
        return variableNullability;
    }

    protected abstract VariableNullability computeVariableNullability();

    protected IQTreeCache getTreeCache() {
        return treeCache;
    }

    @Override
    public synchronized ImmutableSet<ImmutableSubstitution<NonVariableTerm>> getPossibleVariableDefinitions() {
        // Non-final
        ImmutableSet<ImmutableSubstitution<NonVariableTerm>> possibleVariableDefinitions = treeCache.getPossibleVariableDefinitions();
        if (possibleVariableDefinitions == null) {
            possibleVariableDefinitions = computePossibleVariableDefinitions();
            treeCache.setPossibleVariableDefinitions(possibleVariableDefinitions);
        }
        return possibleVariableDefinitions;
    }

    protected abstract ImmutableSet<ImmutableSubstitution<NonVariableTerm>> computePossibleVariableDefinitions();

    @Override
    public synchronized ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints() {
        // Non-final
        ImmutableSet<ImmutableSet<Variable>> uniqueConstraints = treeCache.getUniqueConstraints();
        if (uniqueConstraints == null) {
            uniqueConstraints = computeUniqueConstraints();
            treeCache.setUniqueConstraints(uniqueConstraints);
        }
        return uniqueConstraints;
    }

    protected abstract ImmutableSet<ImmutableSet<Variable>> computeUniqueConstraints();

    @Override
    public synchronized ImmutableSet<Variable> getNotInternallyRequiredVariables() {
        // Non-final
        ImmutableSet<Variable> notInternallyRequiredVariables = treeCache.getNotInternallyRequiredVariables();
        if (notInternallyRequiredVariables != null)
            return notInternallyRequiredVariables;

        notInternallyRequiredVariables = computeNotInternallyRequiredVariables();
        treeCache.setNotInternallyRequiredVariables(notInternallyRequiredVariables);
        return notInternallyRequiredVariables;
    }

    protected abstract ImmutableSet<Variable> computeNotInternallyRequiredVariables();


    @Override
    public boolean isDistinct() {
        // Non-final
        Boolean isDistinct = treeCache.isDistinct();
        if (isDistinct == null) {
            isDistinct = computeIsDistinct();
            treeCache.setIsDistinct(isDistinct);
        }
        return isDistinct;
    }

    protected abstract boolean computeIsDistinct();
}
