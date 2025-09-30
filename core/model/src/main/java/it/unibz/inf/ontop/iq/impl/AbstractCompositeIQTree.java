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
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractCompositeIQTree<N extends QueryNode> extends AbstractIQTree implements CompositeIQTree<N> {

    private final N rootNode;
    private final ImmutableList<IQTree> children;
    private final ConcreteIQTreeCache treeCache;
    private static final String TAB_STR = "   ";


    // LAZY
    @Nullable
    private ImmutableSet<Variable> knownVariables;

    // LAZY
    @Nullable
    private String string;

    // Non final
    private boolean hasBeenSuccessfullyValidate;

    private final TermFactory termFactory;

    protected AbstractCompositeIQTree(N rootNode, ImmutableList<IQTree> children,
                                      IQTreeCache treeCache, IQTreeTools iqTreeTools,
                                      IntermediateQueryFactory iqFactory, TermFactory termFactory,
                                      SubstitutionFactory substitutionFactory) {
        super(iqTreeTools, iqFactory);
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

    private ImmutableSet<Variable> computeVariables() {
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
        // TODO: why not synchronized?
        return getCachedValue(treeCache::isDistinct, this::computeIsDistinct, treeCache::setIsDistinct);
    }

    protected abstract boolean computeIsDistinct();


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
            DownPropagation ds = iqTreeTools.createDownPropagation(descendingSubstitution, Optional.empty(), getVariables(), variableGenerator);
            return ds.getOptionalDescendingSubstitution()
                    .map(s -> doApplyDescendingSubstitutionWithoutOptimizing(s, variableGenerator))
                    .orElse(this);
        }
        catch (DownPropagation.InconsistentDownPropagationException e) {
            return iqTreeTools.createEmptyNode(DownPropagation.computeProjectedVariables(descendingSubstitution, getVariables()));
        }
    }

    protected abstract IQTree doApplyDescendingSubstitutionWithoutOptimizing(Substitution<? extends VariableOrGroundTerm> descendingSubstitution, VariableGenerator variableGenerator);

    @Override
    public boolean isDeclaredAsEmpty() {
        return false;
    }
}
