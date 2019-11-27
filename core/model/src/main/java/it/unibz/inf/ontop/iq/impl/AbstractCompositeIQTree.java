package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.CompositeIQTree;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.node.ExplicitVariableProjectionNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class AbstractCompositeIQTree<N extends QueryNode> implements CompositeIQTree<N> {

    private final N rootNode;
    private final ImmutableList<IQTree> children;
    private final IQProperties iqProperties;
    private static final String TAB_STR = "   ";

    /**
     * LAZY
     */
    @Nullable
    private ImmutableSet<Variable> knownVariables;

    // Non final
    private boolean hasBeenSuccessfullyValidate;

    protected final IQTreeTools iqTreeTools;
    protected final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;

    protected AbstractCompositeIQTree(N rootNode, ImmutableList<IQTree> children,
                                      IQProperties iqProperties, IQTreeTools iqTreeTools,
                                      IntermediateQueryFactory iqFactory, TermFactory termFactory) {
        this.iqTreeTools = iqTreeTools;
        this.iqFactory = iqFactory;
        this.termFactory = termFactory;
        if (children.isEmpty())
            throw new IllegalArgumentException("A composite IQ must have at least one child");
        this.rootNode = rootNode;
        this.children = children;
        this.iqProperties = iqProperties;
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
    public ImmutableSet<Variable> getVariables() {
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
        return printSubtree(this, "");
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
        return (o instanceof CompositeIQTree)
                && isEquivalentTo((CompositeIQTree) o);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean isEquivalentTo(IQTree tree) {
        if (!getRootNode().isEquivalentTo(tree.getRootNode()))
            return false;

        ImmutableList<IQTree> otherChildren = tree.getChildren();
        return (children.size() == otherChildren.size())
                && IntStream.range(0, children.size())
                    .allMatch(i -> children.get(i).isEquivalentTo(otherChildren.get(i)));
    }

    protected IQProperties getProperties() {
        return iqProperties;
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
                    .filter(s -> !newConstraint.isPresent())
                    .flatMap(this::extractFreshRenaming)
                    .map(s -> applyFreshRenaming(s, true))
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

    private Optional<ImmutableExpression> normalizeConstraint(Optional<ImmutableExpression> constraint,
                                                              ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution) {
        if (!constraint.isPresent())
            return constraint;

        ImmutableSet<Variable> newVariables = getVariables().stream()
                .map(descendingSubstitution::apply)
                .filter(t -> t instanceof Variable)
                .map(t -> (Variable)t)
                .collect(ImmutableCollectors.toSet());

        return termFactory.getConjunction(constraint.get().flattenAND()
                .filter(e -> e.getVariableStream()
                        .anyMatch(newVariables::contains)));
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
}
