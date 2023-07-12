package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.*;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.QueryNodeSubstitutionException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.NotRequiredVariableRemover;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.iq.request.VariableNonRequirement;
import it.unibz.inf.ontop.iq.transform.IQTreeExtendedTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.substitution.*;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class UnionNodeImpl extends CompositeQueryNodeImpl implements UnionNode {

    private static final String UNION_NODE_STR = "UNION";

    private final ImmutableSet<Variable> projectedVariables;

    private final IQTreeTools iqTreeTools;
    private final CoreUtilsFactory coreUtilsFactory;
    private final NotRequiredVariableRemover notRequiredVariableRemover;

    @AssistedInject
    private UnionNodeImpl(@Assisted ImmutableSet<Variable> projectedVariables,
                          IntermediateQueryFactory iqFactory,
                          SubstitutionFactory substitutionFactory, TermFactory termFactory,
                          CoreUtilsFactory coreUtilsFactory, IQTreeTools iqTreeTools,
                          NotRequiredVariableRemover notRequiredVariableRemover) {
        super(substitutionFactory, termFactory, iqFactory);
        this.projectedVariables = projectedVariables;
        this.iqTreeTools = iqTreeTools;
        this.coreUtilsFactory = coreUtilsFactory;
        this.notRequiredVariableRemover = notRequiredVariableRemover;
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public UnionNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public ImmutableSet<Substitution<NonVariableTerm>> getPossibleVariableDefinitions(ImmutableList<IQTree> children) {
        return children.stream()
                .flatMap(c -> c.getPossibleVariableDefinitions().stream())
                .map(s -> s.restrictDomainTo(projectedVariables))
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public boolean hasAChildWithLiftableDefinition(Variable variable, ImmutableList<IQTree> children) {
        return children.stream()
                .anyMatch(c -> (c.getRootNode() instanceof ConstructionNode)
                        && ((ConstructionNode) c.getRootNode()).getSubstitution().isDefining(variable));
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
                        e -> intersect(e.getValue())));

        ImmutableSet<ImmutableSet<Variable>> nullableGroups = preselectedGroupMap.keySet().stream()
                .map(v -> computeNullableGroup(v, preselectedGroupMap, variableNullabilities))
                .collect(ImmutableCollectors.toSet());

        ImmutableSet<Variable> scope = children.stream()
                .flatMap(c -> c.getVariables().stream())
                .collect(ImmutableCollectors.toSet());

        return coreUtilsFactory.createVariableNullability(nullableGroups, scope);
    }

    private ImmutableSet<Variable> computeNullableGroup(Variable mainVariable,
                                                        ImmutableMap<Variable,ImmutableSet<Variable>> preselectedGroupMap,
                                                        ImmutableSet<VariableNullability> variableNullabilities) {
        return preselectedGroupMap.get(mainVariable).stream()
                .filter(v -> mainVariable.equals(v)
                        || areInterdependent(mainVariable, v, preselectedGroupMap, variableNullabilities))
                .collect(ImmutableCollectors.toSet());
    }

    private boolean areInterdependent(Variable v1, Variable v2,
                                      ImmutableMap<Variable, ImmutableSet<Variable>> preselectedGroupMap,
                                      ImmutableSet<VariableNullability> variableNullabilities) {
        return preselectedGroupMap.get(v2).contains(v1)
                && variableNullabilities.stream()
                .allMatch(vn -> {
                    boolean v1Nullable = vn.isPossiblyNullable(v1);
                    boolean v2Nullable = vn.isPossiblyNullable(v2);

                    return (v1Nullable && v2Nullable) || ((!v1Nullable) && (!v2Nullable));
                });
    }

    private static ImmutableSet<Variable> intersect(Collection<ImmutableSet<Variable>> groups) {
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
                // We don't consider variables nullable on both side
                .filter(v -> !(variableNullability1.isPossiblyNullable(v) && variableNullability2.isPossiblyNullable(v)))
                .anyMatch(v -> areDisjointWhenNonNull(extractDefs(possibleDefs1, v), extractDefs(possibleDefs2, v), variableNullability1));
    }

    @Override
    public IQTree makeDistinct(ImmutableList<IQTree> children) {
        ImmutableMap<IQTree, ImmutableSet<IQTree>> compatibilityMap = extractCompatibilityMap(children);

        if (areGroupDisjoint(compatibilityMap)) {
            // NB: multiple occurrences of the same child are automatically eliminated
            return makeDistinctDisjointGroups(ImmutableSet.copyOf(compatibilityMap.values()));
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

    private IQTree makeDistinctDisjointGroups(ImmutableSet<ImmutableSet<IQTree>> disjointGroups) {
        ImmutableList<IQTree> newChildren = disjointGroups.stream()
                .map(this::makeDistinctGroup)
                .collect(ImmutableCollectors.toList());

        switch (newChildren.size()) {
            case 0:
                throw new MinorOntopInternalBugException("Was expecting to have at least one group of Union children");
            case 1:
                return newChildren.get(0);
            default:
                return iqFactory.createNaryIQTree(this, newChildren);
        }
    }

    private IQTree makeDistinctGroup(ImmutableSet<IQTree> childGroup) {
        switch (childGroup.size()) {
            case 0:
                throw new MinorOntopInternalBugException("Unexpected empty child group");
            case 1:
                return iqFactory.createUnaryIQTree(iqFactory.createDistinctNode(), childGroup.iterator().next());
            default:
                return iqFactory.createUnaryIQTree(
                        iqFactory.createDistinctNode(),
                        iqFactory.createNaryIQTree(this, ImmutableList.copyOf(childGroup)));
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
        switch(evaluation.getStatus()) {
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
        ImmutableList<IQTree> liftedChildren = children.stream()
                .map(c -> c.liftIncompatibleDefinitions(variable, variableGenerator))
                .collect(ImmutableCollectors.toList());
        
        return iqFactory.createNaryIQTree(this, liftedChildren);
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint, ImmutableList<IQTree> children,
                                          VariableGenerator variableGenerator) {
        return iqFactory.createNaryIQTree(this,
                children.stream()
                        .map(c -> c.propagateDownConstraint(constraint, variableGenerator))
                        .collect(ImmutableCollectors.toList()));
    }

    @Override
    public IQTree acceptTransformer(IQTree tree, IQTreeVisitingTransformer transformer, ImmutableList<IQTree> children) {
        return transformer.transformUnion(tree,this, children);
    }

    @Override
    public <T> IQTree acceptTransformer(IQTree tree, IQTreeExtendedTransformer<T> transformer,
                                    ImmutableList<IQTree> children, T context) {
        return transformer.transformUnion(tree,this, children, context);
    }

    @Override
    public <T> T acceptVisitor(IQVisitor<T> visitor, ImmutableList<IQTree> children) {
        return visitor.visitUnion(this, children);
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
        ImmutableList<IQTree> newChildren = children.stream()
                .map(IQTree::removeDistincts)
                .collect(ImmutableCollectors.toList());

        IQTreeCache newTreeCache = treeCache.declareDistinctRemoval(newChildren.equals(children));

        return iqFactory.createNaryIQTree(this, children, newTreeCache);
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

        if(ucsPartitionedByDisjointness.get(false).isEmpty())
            return ImmutableSet.copyOf(ucsPartitionedByDisjointness.get(true));

        // By definition not parts of the non-disjoint UCs
        var disjointVariables = firstChild.getVariables().stream()
                .filter(v -> areDisjoint(children, ImmutableSet.of(v)))
                .filter(v -> ucsPartitionedByDisjointness.get(true).stream().noneMatch(set -> set.size() == 1 && set.stream().findFirst().get().equals(v)))
                .collect(ImmutableCollectors.toSet());

        return Stream.concat(
                ucsPartitionedByDisjointness.get(true).stream(),
                ucsPartitionedByDisjointness.get(false).stream()
                        .flatMap(uc -> disjointVariables.stream()
                                        .map(v -> appendVariable(uc, v)))
                ).collect(ImmutableCollectors.toSet());
    }

    @Override
    public FunctionalDependencies inferFunctionalDependencies(ImmutableList<IQTree> children, ImmutableSet<ImmutableSet<Variable>> uniqueConstraints, ImmutableSet<Variable> variables) {
        int childrenCount = children.size();
        if (childrenCount < 2)
            throw new InvalidIntermediateQueryException("At least 2 children are expected for a union");

        IQTree firstChild = children.get(0);
        // Partitions the fds based on if they are fully disjoint or not. Guaranteed to have two entries: true and false (but they may be empty)
        ImmutableMap<Boolean, ImmutableList<Map.Entry<ImmutableSet<Variable>, ImmutableSet<Variable>>>> fdsPartitionedByDisjointness = firstChild.inferFunctionalDependencies().stream()
                .filter(fd -> children.stream()
                        .skip(1)
                        .allMatch(c -> c.inferFunctionalDependencies().contains(fd.getKey(), fd.getValue())))
                .collect(ImmutableCollectors.partitioningBy(uc -> areDisjoint(children, uc.getKey())));

        if(fdsPartitionedByDisjointness.get(false).isEmpty())
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

    private Map.Entry<ImmutableSet<Variable>, ImmutableSet<Variable>> appendDeterminant(Map.Entry<ImmutableSet<Variable>, ImmutableSet<Variable>> fd, Variable v) {
        return Maps.immutableEntry(
                Stream.concat(
                    fd.getKey().stream(),
                    Stream.of(v)).collect(ImmutableCollectors.toSet()
                ),
                Sets.difference(fd.getValue(), ImmutableSet.of(v)).immutableCopy()
        );
    }

    private ImmutableSet<Variable> appendVariable(ImmutableSet<Variable> uc, Variable v) {
        return Stream.concat(
                uc.stream(),
                Stream.of(v)).collect(ImmutableCollectors.toSet());
    }

    private boolean areDisjoint(ImmutableList<IQTree> children, ImmutableSet<Variable> uc) {
        int childrenCount = children.size();
        return IntStream.range(0, childrenCount)
                .allMatch(i -> IntStream.range(i + 1, childrenCount)
                        .allMatch(j -> areDisjoint(children.get(i), children.get(j), uc)));
    }

    /**
     * All the variables of a union could be projected out
     */
    @Override
    public VariableNonRequirement computeVariableNonRequirement(ImmutableList<IQTree> children) {
        return VariableNonRequirement.of(getVariables());
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        return projectedVariables;
    }

    @Override
    public ImmutableSet<Variable> getLocalVariables() {
        return projectedVariables;
    }

    @Override
    public String toString() {
        return UNION_NODE_STR + " " + projectedVariables;
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
        if (o == null || getClass() != o.getClass()) return false;
        UnionNodeImpl unionNode = (UnionNodeImpl) o;
        return projectedVariables.equals(unionNode.projectedVariables);
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
        ImmutableList<IQTree> liftedChildren = children.stream()
                .map(c -> c.normalizeForOptimization(variableGenerator))
                .filter(c -> !c.isDeclaredAsEmpty())
                .map(c -> notRequiredVariableRemover.optimize(c, projectedVariables, variableGenerator))
                .collect(ImmutableCollectors.toList());

        switch (liftedChildren.size()) {
            case 0:
                return iqFactory.createEmptyNode(projectedVariables);
            case 1:
                return liftedChildren.get(0);
            default:
                return tryToMergeSomeChildrenInAValuesNode(
                        liftBindingFromLiftedChildrenAndFlatten(liftedChildren, variableGenerator, treeCache),
                        variableGenerator, treeCache);
        }
    }

    @Override
    public IQTree applyDescendingSubstitution(Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                              Optional<ImmutableExpression> constraint, ImmutableList<IQTree> children,
                                              VariableGenerator variableGenerator) {
        ImmutableSet<Variable> updatedProjectedVariables = iqTreeTools.computeNewProjectedVariables(descendingSubstitution, projectedVariables);

        ImmutableList<IQTree> updatedChildren = children.stream()
                .map(c -> c.applyDescendingSubstitution(descendingSubstitution, constraint, variableGenerator))
                .filter(c -> !c.isDeclaredAsEmpty())
                .collect(ImmutableCollectors.toList());

        switch (updatedChildren.size()) {
            case 0:
                return iqFactory.createEmptyNode(updatedProjectedVariables);
            case 1:
                return updatedChildren.get(0);
            default:
                UnionNode newRootNode = iqFactory.createUnionNode(updatedProjectedVariables);
                return iqFactory.createNaryIQTree(newRootNode, updatedChildren);
        }
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(
            Substitution<? extends VariableOrGroundTerm> descendingSubstitution, ImmutableList<IQTree> children,
            VariableGenerator variableGenerator) {
        ImmutableSet<Variable> updatedProjectedVariables = iqTreeTools.computeNewProjectedVariables(descendingSubstitution, projectedVariables);

        ImmutableList<IQTree> updatedChildren = children.stream()
                .map(c -> c.applyDescendingSubstitutionWithoutOptimizing(descendingSubstitution, variableGenerator))
                .collect(ImmutableCollectors.toList());

        UnionNode newRootNode = iqFactory.createUnionNode(updatedProjectedVariables);
        return iqFactory.createNaryIQTree(newRootNode, updatedChildren);
    }

    @Override
    public IQTree applyFreshRenaming(InjectiveSubstitution<Variable> renamingSubstitution,
                                     ImmutableList<IQTree> children, IQTreeCache treeCache) {
        ImmutableList<IQTree> newChildren = children.stream()
                .map(c -> c.applyFreshRenaming(renamingSubstitution))
                .collect(ImmutableCollectors.toList());

        UnionNode newUnionNode = iqFactory.createUnionNode(substitutionFactory.apply(renamingSubstitution, getVariables()));

        IQTreeCache newTreeCache = treeCache.applyFreshRenaming(renamingSubstitution);

        return iqFactory.createNaryIQTree(newUnionNode, newChildren, newTreeCache);
    }


    /**
     * Has at least two children.
     * The returned tree may be opportunistically marked as "normalized" in case no further optimization is applied in this class.
     * Such a flag won't be taken seriously until leaving this class.
     *
     */
    private IQTree liftBindingFromLiftedChildrenAndFlatten(ImmutableList<IQTree> liftedChildren, VariableGenerator variableGenerator,
                                                           IQTreeCache treeCache) {
        /*
         * Cannot lift anything if some children do not have a construction node
         */
        if (liftedChildren.stream()
                .anyMatch(c -> !(c.getRootNode() instanceof ConstructionNode)))
            // Opportunistically flagged as normalized. May be discarded later on
            return iqFactory.createNaryIQTree(this, flattenChildren(liftedChildren), treeCache.declareAsNormalizedForOptimizationWithEffect());

        ImmutableList<Substitution<ImmutableTerm>> tmpNormalizedChildSubstitutions = liftedChildren.stream()
                .map(c -> (ConstructionNode) c.getRootNode())
                .map(ConstructionNode::getSubstitution)
                .map(s -> s.transform(this::normalizeNullAndRDFConstants))
                .collect(ImmutableCollectors.toList());

        Substitution<ImmutableTerm> mergedSubstitution = projectedVariables.stream()
                .map(v -> mergeDefinitions(v, tmpNormalizedChildSubstitutions, variableGenerator)
                        .map(d -> Maps.immutableEntry(v, d)))
                .flatMap(Optional::stream)
                .collect(substitutionFactory.toSubstitution());

        if (mergedSubstitution.isEmpty()) {
            // Opportunistically flagged as normalized. May be discarded later on
            return iqFactory.createNaryIQTree(this, flattenChildren(liftedChildren), treeCache.declareAsNormalizedForOptimizationWithEffect());
        }
        ConstructionNode newRootNode = iqFactory.createConstructionNode(projectedVariables,
                // Cleans up the temporary "normalization"
                mergedSubstitution.transform(ImmutableTerm::simplify));

        ImmutableSet<Variable> unionVariables = newRootNode.getChildVariables();
        UnionNode newUnionNode = iqFactory.createUnionNode(unionVariables);

        NaryIQTree unionIQ = iqFactory.createNaryIQTree(newUnionNode,
                IntStream.range(0, liftedChildren.size())
                        .mapToObj(i -> updateChild((UnaryIQTree) liftedChildren.get(i), mergedSubstitution,
                                tmpNormalizedChildSubstitutions.get(i), unionVariables, variableGenerator))
                        .flatMap(this::flattenChild)
                        .map(c -> iqTreeTools.createConstructionNodeTreeIfNontrivial(c, unionVariables))
                        .collect(ImmutableCollectors.toList()));

        return iqFactory.createUnaryIQTree(newRootNode, unionIQ)
                // TODO: see if needed or if we could opportunistically mark the tree as normalized
                .normalizeForOptimization(variableGenerator);
    }

    private ImmutableList<IQTree> flattenChildren(ImmutableList<IQTree> liftedChildren) {
        ImmutableList<IQTree> flattenedChildren = liftedChildren.stream()
                .flatMap(this::flattenChild)
                .collect(ImmutableCollectors.toList());
        return (liftedChildren.size() == flattenedChildren.size())
                ? liftedChildren
                : flattenedChildren;
    }

    private Stream<IQTree> flattenChild(IQTree child) {
        return (child.getRootNode() instanceof UnionNode)
                ? child.getChildren().stream()
                : Stream.of(child);
    }

    /**
     * RDF constants are transformed into RDF ground terms
     * Trick: NULL --> RDF(NULL,NULL)
     *
     * This "normalization" is temporary --> it will be "cleaned" but simplify the terms afterwards
     *
     */
    private ImmutableTerm normalizeNullAndRDFConstants(ImmutableTerm definition) {
        if (definition instanceof RDFConstant) {
            RDFConstant constant = (RDFConstant) definition;
            return termFactory.getRDFFunctionalTerm(
                    termFactory.getDBStringConstant(constant.getValue()),
                    termFactory.getRDFTermTypeConstant(constant.getType()));
        }
        else if (definition.isNull())
            return termFactory.getRDFFunctionalTerm(
                    termFactory.getNullConstant(), termFactory.getNullConstant());
        else
            return definition;
    }

    private Optional<ImmutableTerm> mergeDefinitions(
            Variable variable,
            ImmutableCollection<Substitution<ImmutableTerm>> childSubstitutions,
            VariableGenerator variableGenerator) {

        if (childSubstitutions.stream()
                .anyMatch(s -> !s.isDefining(variable)))
            return Optional.empty();

        return childSubstitutions.stream()
                .map(s -> s.get(variable))
                .map(this::normalizeNullAndRDFConstants)
                .map(Optional::of)
                .reduce((od1, od2) -> od1
                        .flatMap(d1 -> od2
                                .flatMap(d2 -> combineDefinitions(d1, d2, variableGenerator, true))))
                .flatMap(t -> t);
    }

    /**
     * Compare and combine the bindings, returning only the compatible (partial) values.
     *
     */
    private Optional<ImmutableTerm> combineDefinitions(ImmutableTerm d1, ImmutableTerm d2,
                                                       VariableGenerator variableGenerator,
                                                       boolean topLevel) {
        if (d1.equals(d2)) {
            return Optional.of(
                    // Top-level var-to-var must not be renamed since they are about projected variables
                    (d1.isGround() || (topLevel && (d1 instanceof Variable)))
                            ? d1
                            : replaceVariablesByFreshOnes((NonGroundTerm)d1, variableGenerator));
        }
        else if (d1 instanceof Variable)  {
            return topLevel
                    ? Optional.empty()
                    : Optional.of(variableGenerator.generateNewVariableFromVar((Variable) d1));
        }
        else if (d2 instanceof Variable)  {
            return topLevel
                    ? Optional.empty()
                    : Optional.of(variableGenerator.generateNewVariableFromVar((Variable) d2));
        }
        else if ((d1 instanceof ImmutableFunctionalTerm) && (d2 instanceof ImmutableFunctionalTerm)) {
            ImmutableFunctionalTerm functionalTerm1 = (ImmutableFunctionalTerm) d1;
            ImmutableFunctionalTerm functionalTerm2 = (ImmutableFunctionalTerm) d2;

            FunctionSymbol firstFunctionSymbol = functionalTerm1.getFunctionSymbol();

            /*
             * Different function symbols: stops the common part here.
             *
             * Same for functions that do not strongly type their arguments (unsafe to decompose). Example: STRICT_EQ
             */
            if ((!firstFunctionSymbol.equals(functionalTerm2.getFunctionSymbol()))
                    || (!firstFunctionSymbol.shouldBeDecomposedInUnion())) {
                return topLevel
                        ? Optional.empty()
                        : Optional.of(variableGenerator.generateNewVariable());
            }
            else {
                ImmutableList<? extends ImmutableTerm> arguments1 = functionalTerm1.getTerms();
                ImmutableList<? extends ImmutableTerm> arguments2 = functionalTerm2.getTerms();
                if (arguments1.size() != arguments2.size()) {
                    throw new IllegalStateException("Functions have different arities, they cannot be combined");
                }

                ImmutableList<ImmutableTerm> newArguments = IntStream.range(0, arguments1.size())
                        // RECURSIVE
                        .mapToObj(i -> combineDefinitions(arguments1.get(i), arguments2.get(i), variableGenerator, false)
                                .orElseGet(variableGenerator::generateNewVariable))
                        .collect(ImmutableCollectors.toList());

                return Optional.of(termFactory.getImmutableFunctionalTerm(firstFunctionSymbol, newArguments));
            }
        }
        else {
            return Optional.empty();
        }
    }

    private NonGroundTerm replaceVariablesByFreshOnes(NonGroundTerm term, VariableGenerator variableGenerator) {
        if (term instanceof Variable)
            return variableGenerator.generateNewVariableFromVar((Variable) term);

        NonGroundFunctionalTerm functionalTerm = (NonGroundFunctionalTerm) term;

        return termFactory.getNonGroundFunctionalTerm(functionalTerm.getFunctionSymbol(),
                    functionalTerm.getTerms().stream()
                        .map(a -> a instanceof NonGroundTerm
                                // RECURSIVE
                                ? replaceVariablesByFreshOnes((NonGroundTerm) a, variableGenerator)
                                : a)
                        .collect(ImmutableCollectors.toList()));
    }

    /**
     * TODO: find a better name
     */
    private IQTree updateChild(UnaryIQTree liftedChildTree, Substitution<ImmutableTerm> mergedSubstitution,
                               Substitution<ImmutableTerm> tmpNormalizedSubstitution,
                               ImmutableSet<Variable> projectedVariables, VariableGenerator variableGenerator) {

        ConstructionNode constructionNode = (ConstructionNode) liftedChildTree.getRootNode();

        ImmutableSet<Variable> formerV = constructionNode.getVariables();

        Substitution<ImmutableTerm> normalizedEta = substitutionFactory.onImmutableTerms().unifierBuilder(tmpNormalizedSubstitution)
                .unify(mergedSubstitution.stream(), Map.Entry::getKey, Map.Entry::getValue)
                .build()
                /*
                 * Normalizes eta so as to avoid projected variables to be substituted by non-projected variables.
                 *
                 * This normalization can be understood as a way to select a MGU (eta) among a set of equivalent MGUs.
                 * Such a "selection" is done a posteriori.
                 *
                 * Due to the current implementation of MGUS, the normalization should have no effect
                 * (already in a normal form). Here for safety.
                 */
                .map(eta -> substitutionFactory.getPrioritizingRenaming(eta, projectedVariables).compose(eta))
                .orElseThrow(() -> new QueryNodeSubstitutionException("The descending substitution " + mergedSubstitution
                        + " is incompatible with " + tmpNormalizedSubstitution));

        Substitution<ImmutableTerm> newTheta = normalizedEta.builder()
                .restrictDomainTo(projectedVariables)
                // Cleans up the temporary "normalization", in particular non-lifted RDF(NULL,NULL)
                .transform(ImmutableTerm::simplify)
                .build();

        Substitution<VariableOrGroundTerm> descendingSubstitution = normalizedEta.builder()
                .removeFromDomain(tmpNormalizedSubstitution.getDomain())
                .removeFromDomain(Sets.difference(newTheta.getDomain(), formerV))
                // NB: this is expected to be ok given that the expected compatibility of the merged substitution with
                // this construction node
                .transform(t -> (VariableOrGroundTerm)t)
                .build();


        IQTree newChild = liftedChildTree.getChild()
                .applyDescendingSubstitution(descendingSubstitution, Optional.empty(), variableGenerator);

        return iqTreeTools.createConstructionNodeTreeIfNontrivial(newChild, newTheta, () -> projectedVariables);
    }

    private IQTree tryToMergeSomeChildrenInAValuesNode(IQTree tree, VariableGenerator variableGenerator, IQTreeCache treeCache) {
        QueryNode rootNode = tree.getRootNode();

        if (rootNode instanceof ConstructionNode) {
            IQTree subTree = tree.getChildren().get(0);
            IQTree newSubTree = tryToMergeSomeChildrenInAValuesNode(subTree, variableGenerator, treeCache, false);
            return (subTree == newSubTree)
                    ? tree
                    : iqFactory.createUnaryIQTree((ConstructionNode) rootNode, newSubTree,
                    treeCache.declareAsNormalizedForOptimizationWithEffect());
        }
        else
            return tryToMergeSomeChildrenInAValuesNode(tree, variableGenerator, treeCache, true);
    }


    private IQTree tryToMergeSomeChildrenInAValuesNode(IQTree tree, VariableGenerator variableGenerator, IQTreeCache treeCache,
                                                       boolean isRoot) {
        QueryNode rootNode = tree.getRootNode();
        if (!(rootNode instanceof UnionNode))
            return tree;

        UnionNode unionNode = (UnionNode) rootNode;

        ImmutableList<IQTree> children = tree.getChildren();

        ImmutableList<IQTree> nonMergedChildren = children.stream()
                .filter(t -> !isMergeableInValuesNode(t))
                .collect(ImmutableCollectors.toList());

        // At least 2 mergeable children are needed
        if (nonMergedChildren.size() >= children.size() - 1)
            return tree;

        // Tries to reuse the ordered value list of a values node
        ImmutableList<Variable> valuesVariables = children.stream()
                .map(IQTree::getRootNode)
                .filter(n -> n instanceof ValuesNode)
                .map(n -> ((ValuesNode) n).getOrderedVariables())
                .findAny()
                // Otherwise creates an arbitrary order
                .orElseGet(() -> ImmutableList.copyOf(unionNode.getVariables()));

        ImmutableList<ImmutableList<Constant>> values = children.stream()
                .filter(this::isMergeableInValuesNode)
                .flatMap(c -> extractValues(c, valuesVariables))
                .collect(ImmutableCollectors.toList());

        IQTree mergedSubTree = iqFactory.createValuesNode(valuesVariables, values)
                // NB: some columns may be extracted and put into a construction node
                .normalizeForOptimization(variableGenerator);

        if (nonMergedChildren.isEmpty())
            return mergedSubTree;

        ImmutableList<IQTree> newChildren = Stream.concat(
                        Stream.of(mergedSubTree),
                        nonMergedChildren.stream())
                .collect(ImmutableCollectors.toList());

        return isRoot
                // Merging values nodes cannot trigger new binding lift opportunities
                ? iqFactory.createNaryIQTree(unionNode, newChildren,
                        treeCache.declareAsNormalizedForOptimizationWithEffect())
                : iqFactory.createNaryIQTree(unionNode, newChildren);
    }

    /**
     * TODO: relax these constraints once we are sure non-DB constants in values nodes
     * are lifted or transformed properly in the rest of the code
     */
    private boolean isMergeableInValuesNode(IQTree tree) {
        QueryNode rootNode = tree.getRootNode();
        if ((rootNode instanceof ValuesNode) || (rootNode instanceof TrueNode))
            return true;

        if (!(rootNode instanceof ConstructionNode))
            return false;

        IQTree child = tree.getChildren().get(0);

        if (!((child instanceof TrueNode) || (child instanceof ValuesNode)))
            return false;

        ConstructionNode constructionNode = (ConstructionNode) rootNode;

        //NB: RDF constants are already expected to be decomposed
        return constructionNode.getSubstitution().rangeAllMatch(v -> (v instanceof DBConstant) || v.isNull());
    }

    private Stream<ImmutableList<Constant>> extractValues(IQTree tree, ImmutableList<Variable> outputOrderedVariables) {
        if (tree instanceof ValuesNode)
            return extractValuesFromValuesNode((ValuesNode) tree, outputOrderedVariables);

        if (tree instanceof TrueNode)
            return Stream.of(ImmutableList.of());

        QueryNode rootNode = tree.getRootNode();
        if (!(rootNode instanceof ConstructionNode))
            throw new MinorOntopInternalBugException("Was expecting either a ValuesNode, a TrueNode or a ConstructionNode");

        Substitution<ImmutableTerm> substitution = ((ConstructionNode) rootNode).getSubstitution();
        IQTree child = tree.getChildren().get(0);

        if (child instanceof TrueNode)
            return Stream.of(
                    outputOrderedVariables.stream()
                        .map(substitution::get)
                        .map(t -> (Constant) t)
                        .collect(ImmutableCollectors.toList()));

        if (child instanceof ValuesNode)
            return extractValuesFromValuesNode((ValuesNode) child, outputOrderedVariables, substitution);

        throw new MinorOntopInternalBugException("Unexpected child: " + child);
    }

    private Stream<ImmutableList<Constant>> extractValuesFromValuesNode(ValuesNode valuesNode, ImmutableList<Variable> outputOrderedVariables) {
        ImmutableList<Variable> nodeOrderedVariables = valuesNode.getOrderedVariables();
        if (nodeOrderedVariables.equals(outputOrderedVariables))
            return valuesNode.getValues().stream();

        ImmutableList<Integer> indexes = outputOrderedVariables.stream()
                .map(nodeOrderedVariables::indexOf)
                .collect(ImmutableCollectors.toList());

        return valuesNode.getValues().stream()
                .map(vs -> indexes.stream()
                        .map(vs::get)
                        .collect(ImmutableCollectors.toList()));
    }

    private Stream<ImmutableList<Constant>> extractValuesFromValuesNode(ValuesNode valuesNode,
                                                                        ImmutableList<Variable> outputOrderedVariables,
                                                                        Substitution<ImmutableTerm> substitution) {
        ImmutableList<Variable> nodeOrderedVariables = valuesNode.getOrderedVariables();
        ImmutableMap<Variable, Integer> indexMap = outputOrderedVariables.stream()
                .collect(ImmutableCollectors.toMap(
                        v -> v,
                        nodeOrderedVariables::indexOf));

        return valuesNode.getValues().stream()
                .map(vs -> outputOrderedVariables.stream()
                        .map(v -> {
                            int index = indexMap.get(v);
                            return index == -1
                                    ? (Constant) substitution.get(v)
                                    : vs.get(index);
                        })
                        .collect(ImmutableCollectors.toList()));
    }
}
