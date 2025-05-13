package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.iq.request.VariableNonRequirement;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
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
    private final SubstitutionFactory substitutionFactory;

    protected AbstractCompositeIQTree(N rootNode, ImmutableList<IQTree> children,
                                      IQTreeCache treeCache, IQTreeTools iqTreeTools,
                                      IntermediateQueryFactory iqFactory, TermFactory termFactory,
                                      SubstitutionFactory substitutionFactory) {
        this.iqTreeTools = iqTreeTools;
        this.iqFactory = iqFactory;
        this.termFactory = termFactory;
        this.substitutionFactory = substitutionFactory;
        if (children.isEmpty())
            throw new IllegalArgumentException("A composite IQ must have at least one child");
        this.rootNode = rootNode;
        this.children = children;
        if (!(treeCache instanceof ConcreteIQTreeCache))
            throw new IllegalArgumentException("Was expecting the tree cache to be instance of ConcreteIQTreeCache");
        this.treeCache = (ConcreteIQTreeCache) treeCache;
        // To be computed on-demand
        knownVariables = null;
        string = null;
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
        return getCachedValue(treeCache::getVariables, this::computeVariables, treeCache::setVariables);
    }

    protected ImmutableSet<Variable> computeVariables() {
        if (rootNode instanceof ExplicitVariableProjectionNode)
            return ((ExplicitVariableProjectionNode) rootNode).getVariables();
        ImmutableSet<Variable> childVariables = children.stream()
                .flatMap(c -> c.getVariables().stream())
                .collect(ImmutableCollectors.toSet());
        if (rootNode instanceof FlattenNode)
            return ((FlattenNode) rootNode).getVariables(childVariables);
        return  childVariables;
    }

    @Override
    public ImmutableSet<Variable> getKnownVariables() {
        return getCachedValue(() -> knownVariables, this::computeKnownVariables, v -> knownVariables = v);
    }

    private ImmutableSet<Variable> computeKnownVariables() {
        return Stream.concat(
                        getRootNode().getLocalVariables().stream(),
                        getChildren().stream()
                                .flatMap(c -> c.getKnownVariables().stream()))
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public String toString() {
        return getCachedValue(() -> string, () -> printSubtree(this, ""), s -> string = s);
    }

    /**
     * Recursive
     */
    private static String printSubtree(IQTree subTree, String offset) {
        String childOffset = offset + TAB_STR;

        return offset + subTree.getRootNode() + "\n"
                + subTree.getChildren().stream()
                    .map(c -> printSubtree(c, childOffset))
                    .collect(Collectors.joining(""));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractCompositeIQTree<?> other = (AbstractCompositeIQTree<?>) o;
        return rootNode.equals(other.rootNode) && children.equals(other.children);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public IQTree applyDescendingSubstitution(
            Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
            Optional<ImmutableExpression> constraint, VariableGenerator variableGenerator) {

        ImmutableSet<Variable> variables = getVariables();

        try {
            Optional<Substitution<? extends VariableOrGroundTerm>> normalizedSubstitution =
                    iqTreeTools.normalizeDescendingSubstitution(this, descendingSubstitution);

            Optional<ImmutableExpression> newConstraint = normalizeConstraint(constraint, descendingSubstitution);

            return normalizedSubstitution
                    .flatMap(s -> iqTreeTools.extractFreshRenaming(s, variables))
                    // Fresh renaming
                    .map(this::applyRestrictedFreshRenaming)
                    .map(t -> iqTreeTools.propagateDownOptionalConstraint(t, newConstraint, variableGenerator))
                    // Regular substitution
                    .or(() -> normalizedSubstitution
                            .map(s -> applyRegularDescendingSubstitution(s, newConstraint, variableGenerator)))
                    .orElseGet(() -> iqTreeTools.propagateDownOptionalConstraint(this, newConstraint, variableGenerator));
        }
        catch (IQTreeTools.UnsatisfiableDescendingSubstitutionException e) {
            return iqFactory.createEmptyNode(iqTreeTools.computeNewProjectedVariables(descendingSubstitution, variables));
        }
    }

    @Override
    public IQTree applyFreshRenaming(InjectiveSubstitution<Variable> freshRenamingSubstitution) {
        return applyRestrictedFreshRenaming(freshRenamingSubstitution.restrictDomainTo(getVariables()));
    }

    protected IQTree applyRestrictedFreshRenaming(InjectiveSubstitution<Variable> selectedSubstitution) {
        return selectedSubstitution.isEmpty()
                ? this
                : applyNonEmptyFreshRenaming(selectedSubstitution);
    }

    protected abstract IQTree applyNonEmptyFreshRenaming(InjectiveSubstitution<Variable> freshRenamingSubstitution);

    private Optional<ImmutableExpression> normalizeConstraint(Optional<ImmutableExpression> constraint,
                                                              Substitution<? extends VariableOrGroundTerm> descendingSubstitution) {
        if (!constraint.isPresent())
            return constraint;

        ImmutableSet<Variable> newVariables = getVariables().stream()
                .map(v -> substitutionFactory.onVariableOrGroundTerms().apply(descendingSubstitution, v))
                .filter(t -> t instanceof Variable)
                .map(t -> (Variable)t)
                .collect(ImmutableCollectors.toSet());

        return termFactory.getConjunction(constraint.get().flattenAND()
                .filter(e -> e.getVariableStream().anyMatch(newVariables::contains)));
    }


    protected abstract IQTree applyRegularDescendingSubstitution(Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                                                 Optional<ImmutableExpression> constraint, VariableGenerator variableGenerator);

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
        return getCachedValue(treeCache::getVariableNullability, this::computeVariableNullability, treeCache::setVariableNullability);
    }

    protected abstract VariableNullability computeVariableNullability();

    protected IQTreeCache getTreeCache() {
        return treeCache;
    }


    @Override
    public synchronized ImmutableSet<Substitution<NonVariableTerm>> getPossibleVariableDefinitions() {
        return getCachedValue(treeCache::getPossibleVariableDefinitions, this::computePossibleVariableDefinitions, treeCache::setPossibleVariableDefinitions);
    }

    protected abstract ImmutableSet<Substitution<NonVariableTerm>> computePossibleVariableDefinitions();

    @Override
    public synchronized ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints() {
        return getCachedValue(treeCache::getUniqueConstraints, this::computeUniqueConstraints, treeCache::setUniqueConstraints);
    }

    protected abstract ImmutableSet<ImmutableSet<Variable>> computeUniqueConstraints();

    @Override
    public synchronized FunctionalDependencies inferFunctionalDependencies() {
        return getCachedValue(treeCache::getFunctionalDependencies, this::computeFunctionalDependencies, treeCache::setFunctionalDependencies);
    }

    protected abstract FunctionalDependencies computeFunctionalDependencies();


    @Override
    public synchronized ImmutableSet<Variable> inferStrictDependents() {
        return getCachedValue(treeCache::getStrictDependents, this::computeStrictDependents, treeCache::setStrictDependents);
    }

    protected abstract ImmutableSet<Variable> computeStrictDependents();


    @Override
    public synchronized VariableNonRequirement getVariableNonRequirement() {
        return getCachedValue(treeCache::getVariableNonRequirement, this::computeVariableNonRequirement, treeCache::setVariableNonRequirement);
    }

    protected abstract VariableNonRequirement computeVariableNonRequirement();


    @Override
    public boolean isDistinct() {
        // why not synchronized?
        return getCachedValue(treeCache::isDistinct, this::computeIsDistinct, treeCache::setIsDistinct);
    }

    protected abstract boolean computeIsDistinct();


    private <T> T getCachedValue(Supplier<T> supplier, Supplier<T> constructor, Consumer<T> storer) {
        // Non-final
        T value = supplier.get();
        if (value == null) {
            value = constructor.get();
            storer.accept(value);
        }
        return value;
    }

    @Override
    public IQTree removeDistincts() {
        IQTreeCache treeCache = getTreeCache();
        return treeCache.areDistinctAlreadyRemoved()
                ? this
                : doRemoveDistincts(treeCache);
    }

    protected abstract IQTree doRemoveDistincts(IQTreeCache treeCache);

    @Override
    public IQTree normalizeForOptimization(VariableGenerator variableGenerator) {
        IQTreeCache treeCache = getTreeCache();
        return treeCache.isNormalizedForOptimization()
                ? this
                : doNormalizeForOptimization(variableGenerator, treeCache);
    }

    protected abstract IQTree doNormalizeForOptimization(VariableGenerator variableGenerator, IQTreeCache treeCache);

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(
            Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
            VariableGenerator variableGenerator) {
        try {
            return iqTreeTools.normalizeDescendingSubstitution(this, descendingSubstitution)
                    .map(s -> doApplyDescendingSubstitutionWithoutOptimizing(s, variableGenerator))
                    .orElse(this);
        }
        catch (IQTreeTools.UnsatisfiableDescendingSubstitutionException e) {
            return iqFactory.createEmptyNode(iqTreeTools.computeNewProjectedVariables(descendingSubstitution, getVariables()));
        }
    }

    protected abstract IQTree doApplyDescendingSubstitutionWithoutOptimizing(Substitution<? extends VariableOrGroundTerm> descendingSubstitution, VariableGenerator variableGenerator);

    @Override
    public IQTree replaceSubTree(IQTree subTreeToReplace, IQTree newSubTree) {
        if (equals(subTreeToReplace))
            return newSubTree;

        ImmutableList<IQTree> newChildren = getChildren().stream()
                .map(c -> c.replaceSubTree(subTreeToReplace, newSubTree))
                .collect(ImmutableCollectors.toList());

        return createIQTree(newChildren);
    }

    protected abstract IQTree createIQTree(ImmutableList<IQTree> newChildren);

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint, VariableGenerator variableGenerator) {
        IQTree newTree = doPropagateDownConstraint(constraint, variableGenerator);
        return equals(newTree)
                ? this
                : newTree;
    }

    protected abstract IQTree doPropagateDownConstraint(ImmutableExpression constraint, VariableGenerator variableGenerator);

    @Override
    public boolean isDeclaredAsEmpty() {
        return false;
    }

}
