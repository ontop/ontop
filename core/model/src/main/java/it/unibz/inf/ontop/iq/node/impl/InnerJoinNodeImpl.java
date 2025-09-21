package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.*;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.evaluator.TermNullabilityEvaluator;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.impl.DownPropagation;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier.ExpressionAndSubstitution;
import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier;
import it.unibz.inf.ontop.iq.node.normalization.InnerJoinNormalizer;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.iq.request.VariableNonRequirement;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.impl.NaryIQTreeTools.replaceChild;


@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class InnerJoinNodeImpl extends JoinLikeNodeImpl implements InnerJoinNode {

    private static final String JOIN_NODE_STR = "JOIN";
    private final InnerJoinNormalizer normalizer;

    @AssistedInject
    protected InnerJoinNodeImpl(@Assisted Optional<ImmutableExpression> optionalFilterCondition,
                                TermNullabilityEvaluator nullabilityEvaluator,
                                TermFactory termFactory, TypeFactory typeFactory,
                                IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory,
                                IQTreeTools iqTreeTools,
                                JoinOrFilterVariableNullabilityTools variableNullabilityTools, ConditionSimplifier conditionSimplifier,
                                InnerJoinNormalizer normalizer) {
        super(optionalFilterCondition, nullabilityEvaluator, termFactory, iqFactory, typeFactory,
                substitutionFactory, variableNullabilityTools, conditionSimplifier, iqTreeTools);
        this.normalizer = normalizer;
    }

    @AssistedInject
    private InnerJoinNodeImpl(@Assisted ImmutableExpression joiningCondition,
                              TermNullabilityEvaluator nullabilityEvaluator,
                              TermFactory termFactory, TypeFactory typeFactory,
                              IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory,
                              IQTreeTools iqTreeTools,
                              JoinOrFilterVariableNullabilityTools variableNullabilityTools, ConditionSimplifier conditionSimplifier, InnerJoinNormalizer normalizer) {
        this(Optional.of(joiningCondition), nullabilityEvaluator, termFactory, typeFactory, iqFactory,
                substitutionFactory, iqTreeTools, variableNullabilityTools, conditionSimplifier, normalizer);
    }

    @AssistedInject
    private InnerJoinNodeImpl(TermNullabilityEvaluator nullabilityEvaluator, TermFactory termFactory,
                              TypeFactory typeFactory, IntermediateQueryFactory iqFactory,
                              SubstitutionFactory substitutionFactory, IQTreeTools iqTreeTools,
                              JoinOrFilterVariableNullabilityTools variableNullabilityTools, ConditionSimplifier conditionSimplifier, InnerJoinNormalizer normalizer) {
        this(Optional.empty(), nullabilityEvaluator, termFactory, typeFactory, iqFactory,
                substitutionFactory, iqTreeTools, variableNullabilityTools, conditionSimplifier, normalizer);
    }

    @Override
    public ImmutableSet<Substitution<NonVariableTerm>> getPossibleVariableDefinitions(ImmutableList<IQTree> children) {
        return children.stream()
                .map(IQTree::getPossibleVariableDefinitions)
                .reduce(ImmutableSet.of(), this::combineVarDefs);
    }

    private ImmutableSet<Substitution<NonVariableTerm>> combineVarDefs(
            ImmutableSet<Substitution<NonVariableTerm>> s1,
            ImmutableSet<Substitution<NonVariableTerm>> s2) {

         // substitutionFactory.compose takes the first definition of a common variable.
         // It behaves like a union except that is robust to "non-identical" definitions.
         // If normalized, two definitions for the same variables are expected to be compatible.
         //
         // If not normalized, the definitions may be incompatible, but that's fine
         // since they will not produce any result.

        return s1.isEmpty()
                ? s2
                : s2.isEmpty()
                    ? s1
                    : s1.stream()
                        .flatMap(d1 -> s2.stream()
                            .map(d2 -> substitutionFactory.onNonVariableTerms().compose(d2, d1)))
                        .collect(ImmutableCollectors.toSet());
    }


    @Override
    public int hashCode() {
        return getOptionalFilterCondition().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof InnerJoinNodeImpl) {
            InnerJoinNodeImpl that = (InnerJoinNodeImpl) o;
            return getOptionalFilterCondition().equals(that.getOptionalFilterCondition());
        }
        return false;
    }

    @Override
    public String toString() {
        return JOIN_NODE_STR + getOptionalFilterString();
    }

    /**
     * TODO: refactor
     */
    @Override
    public IQTree normalizeForOptimization(ImmutableList<IQTree> children, VariableGenerator variableGenerator, IQTreeCache treeCache) {
        return normalizer.normalizeForOptimization(this, children, variableGenerator, treeCache);
    }

    @Override
    public IQTree applyDescendingSubstitution(Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                              Optional<ImmutableExpression> constraint, ImmutableList<IQTree> children,
                                              VariableGenerator variableGenerator) {

        DownPropagation downPropagation = new DownPropagation(constraint, descendingSubstitution, NaryIQTreeTools.projectedVariables(children));

        var unoptimizedExpression = downPropagation.applySubstitution(getOptionalFilterCondition());

        VariableNullability simplifiedChildFutureVariableNullability = variableNullabilityTools.getSimplifiedVariableNullability(
                downPropagation.computeProjectedVariables());

        try {
            ExpressionAndSubstitution simplifiedJoinCondition = conditionSimplifier.simplifyCondition(
                    unoptimizedExpression, ImmutableSet.of(), children, simplifiedChildFutureVariableNullability);

            var extendedDownConstraint = conditionSimplifier.extendAndSimplifyDownConstraint(
                    downPropagation, simplifiedJoinCondition, downPropagation.extendVariableNullability(simplifiedChildFutureVariableNullability));

            ImmutableList<IQTree> newChildren = extendedDownConstraint.propagate(children, variableGenerator);

            IQTree joinTree = iqTreeTools.createInnerJoinTree(
                    simplifiedJoinCondition.getOptionalExpression(),
                    newChildren);

            return iqTreeTools.unaryIQTreeBuilder()
                    .append(iqTreeTools.createOptionalConstructionNode(
                            downPropagation::computeProjectedVariables,
                            simplifiedJoinCondition.getSubstitution()))
                    .build(joinTree);
        }
        catch (UnsatisfiableConditionException e) {
            return iqTreeTools.createEmptyNode(downPropagation);
        }
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(
            Substitution<? extends VariableOrGroundTerm> descendingSubstitution, ImmutableList<IQTree> children,
            VariableGenerator variableGenerator) {

        ImmutableList<IQTree> newChildren = NaryIQTreeTools.transformChildren(
                children,
                c -> c.applyDescendingSubstitutionWithoutOptimizing(descendingSubstitution, variableGenerator));

        return iqTreeTools.createInnerJoinTree(
                getOptionalFilterCondition().map(descendingSubstitution::apply),
                newChildren);
    }

    @Override
    public IQTree applyFreshRenaming(InjectiveSubstitution<Variable> renamingSubstitution, ImmutableList<IQTree> children,
                                     IQTreeCache treeCache) {
        ImmutableList<IQTree> newChildren = NaryIQTreeTools.transformChildren(children,
                c -> c.applyFreshRenaming(renamingSubstitution));

        Optional<ImmutableExpression> newCondition = getOptionalFilterCondition()
                .map(renamingSubstitution::apply);

        InnerJoinNode newJoinNode = createInnerJoinNode(newCondition);

        IQTreeCache newTreeCache = treeCache.applyFreshRenaming(renamingSubstitution);
        return iqFactory.createNaryIQTree(newJoinNode, newChildren, newTreeCache);
    }

    @Override
    public VariableNullability getVariableNullability(ImmutableList<IQTree> children) {
        return variableNullabilityTools.getVariableNullability(children, getOptionalFilterCondition());
    }

    @Override
    public boolean isConstructed(Variable variable, ImmutableList<IQTree> children) {
        return children.stream()
                .anyMatch(c -> c.isConstructed(variable));
    }

    @Override
    public boolean isDistinct(IQTree tree, ImmutableList<IQTree> children) {
        return super.isDistinct(tree, children);
    }

    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, ImmutableList<IQTree> children, VariableGenerator variableGenerator) {
        return IntStream.range(0, children.size())
                .mapToObj(i -> liftUnionChild(i, children, variable, variableGenerator))
                .flatMap(Optional::stream)
                .findFirst()
                .map(t -> t.normalizeForOptimization(variableGenerator))
                .orElseGet(() -> iqFactory.createNaryIQTree(this, children));
    }

    private Optional<IQTree> liftUnionChild(int childIndex, ImmutableList<IQTree> children, Variable variable, VariableGenerator variableGenerator) {
        IQTree child = children.get(childIndex);
        if (!child.isConstructed(variable))
            return Optional.empty();

        IQTree liftedChild = child.liftIncompatibleDefinitions(variable, variableGenerator);
        var union = NaryIQTreeTools.UnionDecomposition.of(liftedChild)
                .filter(d -> d.getNode().hasAChildWithLiftableDefinition(variable, d.getChildren()));
        if (!union.isPresent())
            return Optional.empty();

        return Optional.of(iqTreeTools.createUnionTree(NaryIQTreeTools.projectedVariables(children),
                union.transformChildren(c ->
                        iqFactory.createNaryIQTree(this,
                                replaceChild(children, childIndex, c)))));
    }


    @Override
    public void validateNode(ImmutableList<IQTree> children) throws InvalidIntermediateQueryException {
        if (children.size() < 2) {
            throw new InvalidIntermediateQueryException("JOIN node " + this
                    +" does not have at least 2 children.\n" + children);
        }

        getOptionalFilterCondition()
                .ifPresent(e -> checkExpression(e, children));

        checkNonProjectedVariables(children);
    }

    @Override
    public IQTree removeDistincts(ImmutableList<IQTree> children, IQTreeCache treeCache) {
        ImmutableList<IQTree> newChildren = NaryIQTreeTools.transformChildren(children,
                IQTree::removeDistincts);

        IQTreeCache newTreeCache = treeCache.declareDistinctRemoval(newChildren.equals(children));
        return iqFactory.createNaryIQTree(this, newChildren, newTreeCache);
    }

    /**
     * For unique constraints to emerge from an inner join, children must provide unique constraints.
     */
    @Override
    public ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints(ImmutableList<IQTree> children) {

        ImmutableSet<IQTree> childrenSet = ImmutableSet.copyOf(children);

        ImmutableMap<IQTree, ImmutableSet<ImmutableSet<Variable>>> constraintMap = childrenSet.stream()
                .collect(ImmutableCollectors.toMap(
                        c -> c,
                        IQTree::inferUniqueConstraints));

        /*
         * Pre-condition: all the children must have at least one unique constraint
         */
        if (constraintMap.values().stream().anyMatch(AbstractCollection::isEmpty))
            return ImmutableSet.of();

        ImmutableSet<ImmutableSet<Variable>> naturalJoinConstraints = extractConstraintsOverNaturalJoins(children,
                childrenSet, constraintMap);

        ImmutableSet<ImmutableSet<Variable>> combinedConstraints = extractCombinedConstraints(constraintMap.values(),
                getVariableNullability(children));

        return removeRedundantConstraints(Sets.union(naturalJoinConstraints, combinedConstraints));

    }

    /**
     * Naturally joined over some of children constraints.
     * TODO: see if still needed
     */
    private ImmutableSet<ImmutableSet<Variable>> extractConstraintsOverNaturalJoins(ImmutableList<IQTree> children,
                                                                                    ImmutableSet<IQTree> childrenSet,
                                                                                    ImmutableMap<IQTree, ImmutableSet<ImmutableSet<Variable>>> childConstraintMap) {
        // Non-saturated
        ImmutableMultimap<IQTree, IQTree> directDependencyMap = IntStream.range(0, children.size() - 1)
                .boxed()
                .flatMap(i -> IntStream.range(i +1, children.size())
                        .boxed()
                        .flatMap(j -> extractFunctionalDependencies(children.get(i), children.get(j), childConstraintMap)))
                .collect(ImmutableCollectors.toMultimap());

        Multimap<IQTree, IQTree> saturatedDependencyMap = saturateDependencies(directDependencyMap);

        return saturatedDependencyMap.asMap().entrySet().stream()
                .filter(e -> e.getValue().containsAll(Sets.difference(childrenSet, ImmutableSet.of(e.getKey()))))
                .map(Map.Entry::getKey)
                .flatMap(child -> childConstraintMap.get(child).stream())
                .collect(ImmutableCollectors.toSet());
    }

    private ImmutableSet<ImmutableSet<Variable>> extractCombinedConstraints(
            ImmutableCollection<ImmutableSet<ImmutableSet<Variable>>> childConstraints,
            VariableNullability variableNullability) {
        ImmutableList<ImmutableSet<ImmutableSet<Variable>>> nonNullableConstraints = childConstraints.stream()
                .map(cs -> cs.stream()
                        .filter(c -> c.stream().noneMatch(variableNullability::isPossiblyNullable))
                        .collect(ImmutableCollectors.toSet()))
                .collect(ImmutableCollectors.toList());

        if (nonNullableConstraints.isEmpty() || nonNullableConstraints.stream().anyMatch(AbstractCollection::isEmpty))
            return ImmutableSet.of();
        
        return computeCartesianProduct(nonNullableConstraints, 0);
    }

    private ImmutableSet<ImmutableSet<Variable>> computeCartesianProduct(ImmutableList<ImmutableSet<ImmutableSet<Variable>>> nonNullableConstraints,
                                                             int index) {
        int arity = nonNullableConstraints.size();
        if (index == (arity -1))
            return nonNullableConstraints.get(index);

        ImmutableSet<ImmutableSet<Variable>> followingCartesianProduct = computeCartesianProduct(nonNullableConstraints, index + 1);

        return nonNullableConstraints.get(index).stream()
                .flatMap(c -> followingCartesianProduct.stream()
                        .map(c1 -> Sets.union(c, c1).immutableCopy()))
                .collect(ImmutableCollectors.toSet());
    }

    private ImmutableSet<ImmutableSet<Variable>> removeRedundantConstraints(Set<ImmutableSet<Variable>> allConstraints) {
        Set<ImmutableSet<Variable>> mergedConstraints = allConstraints.stream()
                .sorted(Comparator.comparingInt(AbstractCollection::size))
                .reduce(Sets.newHashSet(),
                        (cs, c1) -> {
                            if (cs.stream().noneMatch(c1::containsAll))
                                cs.add(c1);
                            return cs;
                        }
                        ,
                        (c1, c2) -> {
                            throw new MinorOntopInternalBugException("No merging");
                        });

        return ImmutableSet.copyOf(mergedConstraints);
    }

    /*
    We can simply collect all FDs from the children.
     */
    @Override
    public FunctionalDependencies inferFunctionalDependencies(ImmutableList<IQTree> children, ImmutableSet<ImmutableSet<Variable>> uniqueConstraints, ImmutableSet<Variable> variables) {
        return children.stream()
                .flatMap(child -> child.inferFunctionalDependencies().stream())
                .collect(FunctionalDependencies.toFunctionalDependencies());
    }


    @Override
    public VariableNonRequirement computeVariableNonRequirement(ImmutableList<IQTree> children) {
        var nonRequirementBeforeFilter = computeVariableNonRequirementForChildren(children);
        return nonRequirementBeforeFilter.withRequiredVariables(getLocallyRequiredVariables());
    }

    @Override
    public ImmutableSet<Variable> inferStrictDependents(NaryIQTree tree, ImmutableList<IQTree> children) {
        // Default implementation
        return IQTreeTools.computeStrictDependentsFromFunctionalDependencies(tree);
    }

    private Stream<Map.Entry<IQTree, IQTree>> extractFunctionalDependencies(
            IQTree t1, IQTree t2, ImmutableMap<IQTree, ImmutableSet<ImmutableSet<Variable>>> constraintMap) {

        Set<Variable> commonVariables = Sets.intersection(t1.getVariables(), t2.getVariables());
        if (commonVariables.isEmpty())
            return Stream.empty();

        return Stream.of(
                Optional.of(Maps.immutableEntry(t1, t2))
                        .filter(e -> constraintMap.get(e.getValue()).stream()
                                .anyMatch(commonVariables::containsAll)),
                Optional.of(Maps.immutableEntry(t2, t1))
                        .filter(e -> constraintMap.get(e.getValue()).stream()
                                .anyMatch(commonVariables::containsAll)))
                .flatMap(Optional::stream);
    }

    private Multimap<IQTree, IQTree> saturateDependencies(ImmutableMultimap<IQTree, IQTree> directDependencyMap) {
        Multimap<IQTree, IQTree> mutableMultimap = HashMultimap.create(directDependencyMap);

        boolean hasConverged;
        do {
            hasConverged = true;

            for (IQTree determinant : directDependencyMap.keys()) {
                ImmutableSet<IQTree> dependents = ImmutableSet.copyOf(mutableMultimap.get(determinant));
                for (IQTree dependent : dependents) {
                    if (mutableMultimap.putAll(determinant, mutableMultimap.get(dependent)))
                        hasConverged = false;
                }
            }
        } while (!hasConverged);
        return mutableMultimap;
    }


    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint, ImmutableList<IQTree> children,
                                          VariableGenerator variableGenerator) {

        DownPropagation downPropagation = new DownPropagation(Optional.of(constraint), NaryIQTreeTools.projectedVariables(children));

        VariableNullability extendedChildrenVariableNullability = downPropagation.extendVariableNullability(variableNullabilityTools.getChildrenVariableNullability(children));

        try {
            var simplifiedJoinCondition = conditionSimplifier.simplifyCondition(
                    getOptionalFilterCondition(), ImmutableSet.of(), children, extendedChildrenVariableNullability);

            var extendedDownConstraint = conditionSimplifier.extendAndSimplifyDownConstraint(
                    downPropagation, simplifiedJoinCondition, extendedChildrenVariableNullability);

            //TODO: propagate different constraints to different children
            ImmutableList<IQTree> newChildren = extendedDownConstraint.propagate(
                    children, variableGenerator);

            InnerJoinNode newJoin = createInnerJoinNode(simplifiedJoinCondition.getOptionalExpression());

            NaryIQTree joinTree = iqFactory.createNaryIQTree(newJoin, newChildren);

            return iqTreeTools.unaryIQTreeBuilder()
                    .append(iqTreeTools.createOptionalConstructionNode(
                            downPropagation::computeProjectedVariables,
                            simplifiedJoinCondition.getSubstitution()))
                    .build(joinTree);
        }
        catch (UnsatisfiableConditionException e) {
            return iqTreeTools.createEmptyNode(downPropagation);
        }
    }

    private InnerJoinNode createInnerJoinNode(Optional<ImmutableExpression> optionalExpression) {
         return optionalExpression.equals(getOptionalFilterCondition())
                ? this
                : iqFactory.createInnerJoinNode(optionalExpression);
    }


}
