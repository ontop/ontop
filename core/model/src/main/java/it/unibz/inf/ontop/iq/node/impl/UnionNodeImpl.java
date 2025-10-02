package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.*;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.DownPropagation;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.UnionNormalizer;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.iq.request.VariableNonRequirement;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.*;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;

public class UnionNodeImpl extends CompositeQueryNodeImpl implements UnionNode {

    private static final String UNION_NODE_STR = "UNION";

    private final ImmutableSet<Variable> projectedVariables;

    private final IQTreeTools iqTreeTools;
    private final CoreUtilsFactory coreUtilsFactory;
    private final UnionNormalizer unionNormalizer;

    @AssistedInject
    private UnionNodeImpl(@Assisted ImmutableSet<Variable> projectedVariables,
                          IntermediateQueryFactory iqFactory,
                          SubstitutionFactory substitutionFactory, TermFactory termFactory,
                          CoreUtilsFactory coreUtilsFactory, IQTreeTools iqTreeTools,
                          UnionNormalizer unionNormalizer) {
        super(substitutionFactory, termFactory, iqFactory, iqTreeTools);
        this.projectedVariables = projectedVariables;
        this.iqTreeTools = iqTreeTools;
        this.coreUtilsFactory = coreUtilsFactory;
        this.unionNormalizer = unionNormalizer;
    }

    @Override
    public ImmutableSet<Substitution<NonVariableTerm>> getPossibleVariableDefinitions(ImmutableList<IQTree> children) {
        return children.stream()
                .flatMap(c -> c.getPossibleVariableDefinitions().stream())
                // Preventive: in principle the children should only project the union variables
                .map(s -> s.restrictDomainTo(projectedVariables))
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public boolean hasAChildWithLiftableDefinition(Variable variable, ImmutableList<IQTree> children) {
        return children.stream()
                .anyMatch(c -> UnaryIQTreeDecomposition.of(c, ConstructionNode.class)
                        .getOptionalNode()
                        .map(cn -> cn.getSubstitution().isDefining(variable)).isPresent()
);
    }

    @Override
    public VariableNullability getVariableNullability(ImmutableList<IQTree> children) {
        ImmutableSet<VariableNullability> variableNullabilities = children.stream()
                .map(IQTree::getVariableNullability)
                .collect(ImmutableCollectors.toSet());

        ImmutableMultimap<Variable, ImmutableSet<Variable>> multimap = variableNullabilities.stream()
                .flatMap(vn -> vn.getNullableGroups().stream())
                .flatMap(g -> g.stream()
                        .map(v -> Maps.immutableEntry(v, g)))
                .collect(ImmutableCollectors.toMultimap());

        ImmutableMap<Variable, ImmutableSet<Variable>> preselectedGroupMap = multimap.asMap().entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> intersectionOfAll(e.getValue())));

        ImmutableSet<ImmutableSet<Variable>> nullableGroups = preselectedGroupMap.keySet().stream()
                .map(v -> computeNullableGroup(v, preselectedGroupMap, variableNullabilities))
                .collect(ImmutableCollectors.toSet());

        ImmutableSet<Variable> scope = NaryIQTreeTools.projectedVariables(children);
        return coreUtilsFactory.createVariableNullability(nullableGroups, scope);
    }

    private ImmutableSet<Variable> computeNullableGroup(Variable mainVariable,
                                                        ImmutableMap<Variable,ImmutableSet<Variable>> preselectedGroupMap,
                                                        ImmutableSet<VariableNullability> variableNullabilities) {
        return preselectedGroupMap.get(mainVariable).stream()
                .filter(v -> mainVariable.equals(v)
                        || preselectedGroupMap.get(v).contains(mainVariable)
                            && variableNullabilities.stream().allMatch(vn -> vn.isPossiblyNullable(mainVariable) == vn.isPossiblyNullable(v)))
                .collect(ImmutableCollectors.toSet());
    }

    private static ImmutableSet<Variable> intersectionOfAll(Collection<ImmutableSet<Variable>> groups) {
        return groups.stream()
                .reduce((g1, g2) -> Sets.intersection(g1, g2).immutableCopy())
                .orElseThrow(() -> new IllegalArgumentException("groups must not be empty"));
    }

    @Override
    public boolean isConstructed(Variable variable, ImmutableList<IQTree> children) {
        return children.stream()
                .anyMatch(c -> c.isConstructed(variable));
    }

    @Override
    public boolean isDistinct(IQTree tree, ImmutableList<IQTree> children) {
        if (children.stream().anyMatch(c -> !c.isDistinct()))
            return false;

        return IntStream.range(0, children.size())
                .allMatch(i -> children.subList(i+1, children.size()).stream()
                        .allMatch(o -> areDisjoint(children.get(i), o)));
    }

    /**
     * Returns true if we are sure the two children can only return different tuples
     */
    private boolean areDisjoint(IQTree child1, IQTree child2) {
        return areDisjoint(child1, child2, projectedVariables);
    }

    private boolean areDisjoint(IQTree child1, IQTree child2, ImmutableSet<Variable> variables) {
        VariableNullability variableNullability1 = child1.getVariableNullability();
        VariableNullability variableNullability2 = child2.getVariableNullability();

        ImmutableSet<Substitution<NonVariableTerm>> possibleDefs1 = child1.getPossibleVariableDefinitions();
        ImmutableSet<Substitution<NonVariableTerm>> possibleDefs2 = child2.getPossibleVariableDefinitions();

        return variables.stream()
                // We don't consider variables nullable on both sides
                .filter(v -> !(variableNullability1.isPossiblyNullable(v) && variableNullability2.isPossiblyNullable(v)))
                .anyMatch(v -> areDisjointWhenNonNull(extractDefs(possibleDefs1, v), extractDefs(possibleDefs2, v), variableNullability1));
    }

    @Override
    public IQTree makeDistinct(ImmutableList<IQTree> children) {
        ImmutableMap<IQTree, ImmutableSet<IQTree>> compatibilityMap = extractCompatibilityMap(children);

        if (areGroupDisjoint(compatibilityMap)) {
            // NB: multiple occurrences of the same child are automatically eliminated
            ImmutableList<IQTree> newChildren = ImmutableSet.copyOf(compatibilityMap.values()).stream()
                    .map(this::makeDistinctGroup)
                    .collect(ImmutableCollectors.toList());

            return makeDistinctGroupTree(newChildren);
        }
        /*
         * Fail-back: in the presence of non-disjoint groups of children,
         * puts the DISTINCT above.
         *
         * TODO: could be improved
         */
        else {
            return makeDistinctGroup(ImmutableSet.copyOf(children));
        }
    }

    private ImmutableMap<IQTree, ImmutableSet<IQTree>> extractCompatibilityMap(ImmutableList<IQTree> children) {
        return IntStream.range(0, children.size())
                .boxed()
                // Compare to itself
                .flatMap(i -> IntStream.range(i, children.size())
                        .boxed()
                        .flatMap(j -> (i.equals(j) || (!areDisjoint(children.get(i), children.get(j))))
                                ? Stream.of(
                                Maps.immutableEntry(children.get(i), children.get(j)),
                                Maps.immutableEntry(children.get(j), children.get(i)))
                                : Stream.empty()))
                .collect(ImmutableCollectors.toMultimap())
                .asMap().entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> ImmutableSet.copyOf(e.getValue())));
    }

    private IQTree makeDistinctGroup(ImmutableSet<IQTree> childGroup) {
        return iqFactory.createUnaryIQTree(iqFactory.createDistinctNode(),
                makeDistinctGroupTree(ImmutableList.copyOf(childGroup)));
    }

    private IQTree makeDistinctGroupTree(ImmutableList<IQTree> list) {
        switch (list.size()) {
            case 0:
                throw new MinorOntopInternalBugException("Unexpected empty child group");
            case 1:
                return list.get(0);
            default:
                return iqFactory.createNaryIQTree(this, list);
        }
    }

    private boolean areGroupDisjoint(ImmutableMap<IQTree, ImmutableSet<IQTree>> compatibilityMap) {
        return compatibilityMap.values().stream()
                .allMatch(g -> g.stream()
                        .allMatch(t -> compatibilityMap.get(t).equals(g)));
    }

    private ImmutableSet<ImmutableTerm> extractDefs(ImmutableSet<Substitution<NonVariableTerm>> possibleDefs,
                                                           Variable v) {
        if (possibleDefs.isEmpty())
            return ImmutableSet.of(v);

        return possibleDefs.stream()
                .map(s -> s.apply(v))
                .collect(ImmutableCollectors.toSet());
    }

    private boolean areDisjointWhenNonNull(ImmutableSet<ImmutableTerm> defs1, ImmutableSet<ImmutableTerm> defs2,
                                           VariableNullability variableNullability) {
        return defs1.stream()
                .allMatch(d1 -> defs2.stream()
                        .allMatch(d2 -> areDisjointWhenNonNull(d1, d2, variableNullability)));
    }

    private boolean areDisjointWhenNonNull(ImmutableTerm t1, ImmutableTerm t2, VariableNullability variableNullability) {
        IncrementalEvaluation evaluation = t1.evaluateStrictEq(t2, variableNullability);
        switch (evaluation.getStatus()) {
            case SIMPLIFIED_EXPRESSION:
                return evaluation.getNewExpression()
                        .orElseThrow(() -> new MinorOntopInternalBugException("An expression was expected"))
                        .evaluate2VL(variableNullability)
                        .isEffectiveFalse();
            case IS_NULL:
            case IS_FALSE:
                return true;
            case SAME_EXPRESSION:
            case IS_TRUE:
            default:
                return false;
        }
    }


    /**
     * TODO: make it compatible definitions together (requires a VariableGenerator so as to lift bindings)
     */
    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, ImmutableList<IQTree> children, VariableGenerator variableGenerator) {
        ImmutableList<IQTree> liftedChildren = NaryIQTreeTools.transformChildren(
                children,
                c -> c.liftIncompatibleDefinitions(variable, variableGenerator));

        return iqFactory.createNaryIQTree(this, liftedChildren);
    }

    @Override
    public void validateNode(ImmutableList<IQTree> children) throws InvalidIntermediateQueryException {
        if (children.size() < 2) {
            throw new InvalidIntermediateQueryException("UNION node " + this
                    +" does not have at least 2 children node.");
        }

        ImmutableSet<Variable> unionVariables = getVariables();
        for (IQTree child : children) {
            if (!child.getVariables().equals(unionVariables)) {
                throw new InvalidIntermediateQueryException("This child " + child
                        + " does not project exactly all the variables " +
                        "of the UNION node (" + unionVariables + ")\n" + this);
            }
        }
    }

    @Override
    public IQTree removeDistincts(ImmutableList<IQTree> children, IQTreeCache treeCache) {
        ImmutableList<IQTree> newChildren = NaryIQTreeTools.transformChildren(children,
                IQTree::removeDistincts);

        IQTreeCache newTreeCache = treeCache.declareDistinctRemoval(newChildren.equals(children));
        return iqFactory.createNaryIQTree(this, newChildren, newTreeCache);
    }

    /**
     * TODO: generalize it and merge it with the isDistinct() method implementation
     *
     * We could infer more constraints by not only checking for equal unique constraints, but also checking if
     * some unique constraint is a superset of all unique constraints.
     */
    @Override
    public ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints(ImmutableList<IQTree> children) {
        int childrenCount = children.size();
        if (childrenCount < 2)
            throw new InvalidIntermediateQueryException("At least 2 children are expected for a union");

        IQTree firstChild = children.get(0);
        // Partitions the unique constraints based on if they are fully disjoint or not. Guaranteed to have two entries: true and false (but they may be empty)
        ImmutableMap<Boolean, ImmutableList<ImmutableSet<Variable>>> ucsPartitionedByDisjointness = firstChild.inferUniqueConstraints().stream()
                .filter(uc -> children.stream()
                        .skip(1)
                        .allMatch(c -> c.inferUniqueConstraints().contains(uc)))
                .collect(ImmutableCollectors.partitioningBy(uc -> areDisjoint(children, uc)));

        var nonDisjointUcs = ucsPartitionedByDisjointness.get(false);
        assert nonDisjointUcs != null;
        var disjointUcs = ucsPartitionedByDisjointness.get(true);
        assert disjointUcs != null;

        if (nonDisjointUcs.isEmpty())
            return ImmutableSet.copyOf(disjointUcs);

        var singleVariableDisjointUcs = disjointUcs.stream()
                .filter(uc -> uc.size() == 1)
                .flatMap(Collection::stream)
                .collect(ImmutableCollectors.toSet());

        var additionalVariablesToConsider = Sets.difference(getVariables(), singleVariableDisjointUcs);

        // At the moment, we are only considering one extra variable
        var extendedUcStream = nonDisjointUcs.stream()
                .flatMap(uc -> additionalVariablesToConsider.stream()
                        .filter(v -> !uc.contains(v))
                        .map(v -> Sets.union(uc, ImmutableSet.of(v)).immutableCopy())
                        .filter(extendedUc -> areDisjoint(children, extendedUc)));

        return Stream.concat(disjointUcs.stream(), extendedUcStream)
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public FunctionalDependencies inferFunctionalDependencies(ImmutableList<IQTree> children, ImmutableSet<ImmutableSet<Variable>> uniqueConstraints, ImmutableSet<Variable> variables) {
        int childrenCount = children.size();
        if (childrenCount < 2)
            throw new InvalidIntermediateQueryException("At least 2 children are expected for a union");

        IQTree firstChild = children.get(0);

        var mergedDependencies = children.stream()
                .skip(1)
                .reduce(firstChild.inferFunctionalDependencies(), (fd, c) -> fd.merge(c.inferFunctionalDependencies()), (fd1, fd2) -> fd1.merge(fd2));

        // Partitions the fds based on if they are fully disjoint or not. Guaranteed to have two entries: true and false (but they may be empty)
        ImmutableMap<Boolean, ImmutableList<Map.Entry<ImmutableSet<Variable>, ImmutableSet<Variable>>>> fdsPartitionedByDisjointness = mergedDependencies.stream()
                .collect(ImmutableCollectors.partitioningBy(fd -> areDisjoint(children, fd.getKey())));

        if (fdsPartitionedByDisjointness.get(false).isEmpty())
            return fdsPartitionedByDisjointness.get(true)
                    .stream()
                    .collect(FunctionalDependencies.toFunctionalDependencies());

        // By definition not parts of the non-disjoint UCs
        var disjointVariables = firstChild.getVariables().stream()
                .filter(v -> areDisjoint(children, ImmutableSet.of(v)))
                .filter(v -> fdsPartitionedByDisjointness.get(true).stream().noneMatch(entry -> entry.getKey().size() == 1 && entry.getKey().stream().findFirst().get().equals(v)))
                .collect(ImmutableCollectors.toSet());

        return Stream.concat(
                fdsPartitionedByDisjointness.get(true).stream(),
                fdsPartitionedByDisjointness.get(false).stream()
                        .flatMap(fd -> disjointVariables.stream()
                                .map(v -> appendDeterminant(fd, v)))
        ).collect(FunctionalDependencies.toFunctionalDependencies());
    }

    private static Map.Entry<ImmutableSet<Variable>, ImmutableSet<Variable>> appendDeterminant(Map.Entry<ImmutableSet<Variable>, ImmutableSet<Variable>> fd, Variable v) {
        ImmutableSet<Variable> vSet = ImmutableSet.of(v);
        return Maps.immutableEntry(
                Sets.union(fd.getKey(), vSet).immutableCopy(),
                Sets.difference(fd.getValue(), vSet).immutableCopy());
    }

    @Override
    public ImmutableSet<Variable> inferStrictDependents(NaryIQTree tree, ImmutableList<IQTree> children) {
        return children.stream()
                .<Set<Variable>>map(IQTree::inferStrictDependents)
                .reduce(Sets::intersection)
                .map(ImmutableSet::copyOf)
                .orElseThrow(() -> new InvalidIntermediateQueryException("At least 2 children are expected for a union"));
    }


    private boolean areDisjoint(ImmutableList<IQTree> children, ImmutableSet<Variable> vars) {
        int childrenCount = children.size();
        return IntStream.range(0, childrenCount)
                .allMatch(i -> IntStream.range(i + 1, childrenCount)
                        .allMatch(j -> areDisjoint(children.get(i), children.get(j), vars)));
    }

    /**
     * All the variables of a union could be projected out
     */
    @Override
    public VariableNonRequirement computeVariableNonRequirement(ImmutableList<IQTree> children) {
        return VariableNonRequirement.of(getVariables());
    }

    @Override
    public String toString() {
        return UNION_NODE_STR + " " + projectedVariables;
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        return projectedVariables;
    }

    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        return projectedVariables;
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        return ImmutableSet.of();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof UnionNodeImpl) {
            UnionNodeImpl that = (UnionNodeImpl) o;
            return projectedVariables.equals(that.projectedVariables);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectedVariables);
    }

    /**
     * TODO: refactor
     */
    @Override
    public IQTree normalizeForOptimization(ImmutableList<IQTree> children, VariableGenerator variableGenerator, IQTreeCache treeCache) {
        return unionNormalizer.normalizeForOptimization(this, children, variableGenerator, treeCache);
    }

    @Override
    public IQTree propagateDownConstraint(DownPropagation dp, ImmutableList<IQTree> children) {
        return propagateDown(dp, children);
    }

    @Override
    public IQTree applyDescendingSubstitution(DownPropagation dp, ImmutableList<IQTree> children) {
        return propagateDown(dp, children);
    }

    private IQTree propagateDown(DownPropagation dp, ImmutableList<IQTree> children) {
        ImmutableList<IQTree> updatedChildren = children.stream()
                .map(dp::propagateToChild)
                .filter(c -> !c.isDeclaredAsEmpty())
                .collect(ImmutableCollectors.toList());

        switch (updatedChildren.size()) {
            case 0:
                return iqFactory.createEmptyNode(dp.computeProjectedVariables());
            case 1:
                return updatedChildren.get(0);
            default:
                return iqTreeTools.createUnionTree(dp.computeProjectedVariables(), updatedChildren);
        }
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                                               ImmutableList<IQTree> children, VariableGenerator variableGenerator) {
        return iqFactory.createNaryIQTree(applyDescendingSubstitution(descendingSubstitution),
                NaryIQTreeTools.transformChildren(children,
                        c -> iqTreeTools.applyDownPropagationWithoutOptimization(c, descendingSubstitution, variableGenerator)));
    }

    @Override
    public UnionNode applyFreshRenaming(InjectiveSubstitution<Variable> renamingSubstitution) {
        return applyDescendingSubstitution(renamingSubstitution);
    }

    private UnionNode applyDescendingSubstitution(Substitution<? extends VariableOrGroundTerm> descendingSubstitution) {
        return iqFactory.createUnionNode(DownPropagation.computeProjectedVariables(descendingSubstitution, projectedVariables));
    }
}
