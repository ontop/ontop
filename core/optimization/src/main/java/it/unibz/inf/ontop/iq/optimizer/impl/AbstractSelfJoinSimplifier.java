package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.FunctionalDependency;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.impl.DownPropagation;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.node.normalization.ConstructionSubstitutionNormalizer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.*;
import java.util.stream.Stream;

public abstract class AbstractSelfJoinSimplifier<C extends FunctionalDependency> implements InnerJoinTransformer {

    protected final IntermediateQueryFactory iqFactory;
    protected final TermFactory termFactory;
    protected final SubstitutionFactory substitutionFactory;
    protected final IQTreeTools iqTreeTools;

    private final ConstructionSubstitutionNormalizer substitutionNormalizer;

    protected final VariableGenerator variableGenerator;

    public AbstractSelfJoinSimplifier(CoreSingletons coreSingletons, VariableGenerator variableGenerator) {
        this.iqFactory = coreSingletons.getIQFactory();
        this.termFactory = coreSingletons.getTermFactory();
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
        this.substitutionNormalizer = coreSingletons.getConstructionSubstitutionNormalizer();
        this.iqTreeTools = coreSingletons.getIQTreeTools();
        this.variableGenerator = variableGenerator;
    }

    /**
     * Returns an empty result to indicate that no optimization has been applied
     */
    @Override
    public Optional<IQTree> transformInnerJoin(NaryIQTree tree, InnerJoinNode innerJoinNode, ImmutableList<IQTree> transformedChildren) {
        ImmutableMap<Boolean, ImmutableList<IQTree>> childPartitions = transformedChildren.stream()
                .collect(ImmutableCollectors.partitioningBy(n -> (n instanceof ExtensionalDataNode)
                        && hasConstraint((ExtensionalDataNode) n)));

        var extensionalChildrenWithConstraint = childPartitions.get(true);
        assert extensionalChildrenWithConstraint != null;

        ImmutableMap<RelationDefinition, Collection<ExtensionalDataNode>> dataNodeMap = extensionalChildrenWithConstraint.stream()
                        .map(n -> (ExtensionalDataNode) n)
                        .collect(ImmutableCollectors.toMultimap(
                                ExtensionalDataNode::getRelationDefinition,
                                n -> n)).asMap();

        // No room for optimization (no self-join involving a constraint of the desired type) -> returns fast
        if (dataNodeMap.values().stream()
                        .noneMatch(c -> c.size() > 1))
            return Optional.empty();

        ImmutableList<OptimizationState> optimizationStates = dataNodeMap.entrySet().stream()
                        .map(e -> optimizeExtensionalDataNodes(e.getKey(), e.getValue()))
                        .collect(ImmutableCollectors.toList());

        /*
         * First empty case: no solution for one relation
         */
        if (optimizationStates.stream().anyMatch(s -> s.extensionalDataNodes.isEmpty())) {
            return Optional.of(iqFactory.createEmptyNode(tree.getVariables()));
        }

        // NB: may return a unifier with no entry
        Optional<Substitution<VariableOrGroundTerm>> optionalUnifier = optimizationStates.stream()
                        .map(s -> s.substitution)
                        .collect(substitutionFactory.onVariableOrGroundTerms().toUnifier());

        /*
         * Second empty case: incompatible unifications between relations
         */
        if (!optionalUnifier.isPresent()) {
            return Optional.of(iqFactory.createEmptyNode(tree.getVariables()));
        }
        Substitution<VariableOrGroundTerm> unifier = optionalUnifier.get();

        ImmutableList<IQTree> optimizedExtensionalDataNodes = optimizationStates.stream()
                .flatMap(s -> s.extensionalDataNodes.stream())
                .collect(ImmutableCollectors.toList());

        if (canEliminateNodes() && optimizedExtensionalDataNodes.size() == extensionalChildrenWithConstraint.size()) {
            return Optional.empty();
        }

        ImmutableList<IQTree> nonExtensionalChildrenWithConstraint = childPartitions.get(false);
        assert nonExtensionalChildrenWithConstraint != null;

        ImmutableList<IQTree> newChildren = Stream.concat(
                        optimizedExtensionalDataNodes.stream(),
                        nonExtensionalChildrenWithConstraint.stream())
                .collect(ImmutableCollectors.toList());

        DownPropagation dp = DownPropagation.of(unifier, NaryIQTreeTools.projectedVariables(newChildren), variableGenerator);
        ImmutableList<IQTree> newChildrenPropagated = NaryIQTreeTools.transformChildren(newChildren, dp::propagate);

        Optional<ImmutableExpression> newExpression = termFactory.getConjunction(
                innerJoinNode.getOptionalFilterCondition(),
                optimizationStates.stream()
                        .flatMap(s -> s.newExpressions.stream()));

        Optional<ImmutableExpression> newExpressionWithUnifier = newExpression.map(unifier::apply);

        IQTree newTree = iqTreeTools.createOptionalInnerJoinTree(newExpressionWithUnifier, newChildrenPropagated)
                .orElseThrow(() -> new MinorOntopInternalBugException("Should have been detected before"));

        ConstructionSubstitutionNormalizer.ConstructionSubstitutionNormalization normalization =
                substitutionNormalizer.normalizeSubstitution(unifier, tree.getVariables());
        IQTree normalizedNewTree = normalization.updateChild(newTree, variableGenerator);

        Substitution<ImmutableTerm> normalizedTopSubstitution = normalization.getNormalizedSubstitution();

        return Optional.of(iqTreeTools.unaryIQTreeBuilder()
                .append(iqTreeTools.createOptionalConstructionNode(tree.getVariables(), normalizedTopSubstitution, normalizedNewTree))
                .build(normalizedNewTree));
    }

    protected abstract boolean canEliminateNodes();

    protected abstract boolean hasConstraint(ExtensionalDataNode node);

    protected abstract Stream<C> extractConstraints(RelationDefinition relationDefinition);


    private OptimizationState optimizeExtensionalDataNodes(RelationDefinition relationDefinition,
                                                             Collection<ExtensionalDataNode> dataNodes) {

        OptimizationState initialState = new OptimizationState(ImmutableSet.of(), dataNodes, substitutionFactory.getSubstitution());

        if (dataNodes.size() < 2)
            return initialState;

        return extractConstraints(relationDefinition)
                .reduce(initialState,
                        (s,c) -> simplifyUsingConstraint(c, s),
                        (ns1, ns2) -> {
                            throw new MinorOntopInternalBugException("Not expected to be executed in //");
                        });
    }

    private OptimizationState simplifyUsingConstraint(C constraint, OptimizationState state) {
        if (state.extensionalDataNodes.size() < 2)
            return state;

        ImmutableList<Integer> ucIndexes = constraint.getDeterminants().stream()
                .map(a -> a.getIndex() - 1)
                .collect(ImmutableCollectors.toList());

        ImmutableMap<ImmutableList<Optional<VariableOrGroundTerm>>, Collection<ExtensionalDataNode>> map = state.extensionalDataNodes.stream()
                .collect(ImmutableCollectors.toMultimap(
                        n -> ucIndexes.stream()
                                .map(i -> Optional.<VariableOrGroundTerm>ofNullable(n.getArgumentMap().get(i)))
                                .collect(ImmutableCollectors.toList()),
                        n -> n)).asMap();

        ImmutableList<Optional<DeterminantGroupEvaluation>> simplifications = map.entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .filter(e -> e.getKey().stream().allMatch(Optional::isPresent))
                .map(e -> evaluateDeterminantGroup(
                        e.getKey().stream()
                                .map(Optional::get)
                                .collect(ImmutableCollectors.toList()),
                        e.getValue(),
                        constraint))
                .collect(ImmutableCollectors.toList());

        /*
         * No solution --> return no data nodes
         */
        if (simplifications.stream().anyMatch(s -> !s.isPresent()))
            return new OptimizationState(ImmutableSet.of(), ImmutableList.of(), substitutionFactory.getSubstitution());

        Optional<Substitution<VariableOrGroundTerm>> optionalUnifier = Stream.concat(
                        simplifications.stream()
                                .map(Optional::get)
                                .map(s -> s.substitution),
                        Stream.of(state.substitution))
                .collect(substitutionFactory.onVariableOrGroundTerms().toUnifier());

        if (!optionalUnifier.isPresent())
            return new OptimizationState(ImmutableSet.of(), ImmutableList.of(), substitutionFactory.getSubstitution());

        Substitution<VariableOrGroundTerm> unifier = optionalUnifier.get();

        ImmutableSet<ImmutableExpression> newExpressions = Stream.concat(
                state.newExpressions.stream(),
                simplifications.stream()
                        .map(Optional::get)
                        .flatMap(s -> s.expressions.stream()))
                .collect(ImmutableCollectors.toSet());

        ImmutableList<ExtensionalDataNode> newDataNodes = Stream.concat(
                        simplifications.stream()
                                .map(Optional::get)
                                .flatMap(s -> s.dataNodes.stream()),
                        map.entrySet().stream()
                                .filter(e -> e.getKey().stream().anyMatch(o -> !o.isPresent()) || e.getValue().size() < 2)
                                .flatMap(e -> e.getValue().stream()))
                .map(n -> iqFactory.createExtensionalDataNode(
                        n.getRelationDefinition(),
                        substitutionFactory.onVariableOrGroundTerms().applyToTerms(unifier, n.getArgumentMap())))
                .collect(ImmutableCollectors.toList());

        return new OptimizationState(newExpressions, newDataNodes, unifier);
    }

    protected abstract Optional<DeterminantGroupEvaluation> evaluateDeterminantGroup(ImmutableList<VariableOrGroundTerm> determinants,
                                                                                     Collection<ExtensionalDataNode> dataNodes,
                                                                                     C constraint);


    /**
     * TODO: we don't need to remove the functional terms for the determinants
     */
    protected NormalizationBeforeUnification normalizeDataNodes(
            Collection<ExtensionalDataNode> dataNodes, C constraint) {
        ImmutableSet<GroundFunctionalTerm> groundFunctionalTerms = dataNodes.stream()
                .flatMap(n -> n.getArgumentMap().values().stream())
                .filter(t -> t instanceof GroundFunctionalTerm)
                .map(t -> (GroundFunctionalTerm)t)
                .collect(ImmutableCollectors.toSet());

        if (groundFunctionalTerms.isEmpty())
            return new NormalizationBeforeUnification(dataNodes, ImmutableSet.of());

        ImmutableMap<GroundFunctionalTerm, Variable> groundFunctionalTermMap = groundFunctionalTerms.stream()
                .collect(ImmutableCollectors.toMap(
                        t -> t,
                        t -> termFactory.getVariable("v" + UUID.randomUUID())));

        ImmutableList<ExtensionalDataNode> newDataNodes = dataNodes.stream()
                .map(d -> normalizeDataNode(d, groundFunctionalTermMap))
                .collect(ImmutableCollectors.toList());

        ImmutableSet<ImmutableExpression> equalities = groundFunctionalTermMap.entrySet().stream()
                .map(e -> termFactory.getStrictEquality(e.getKey(), e.getValue()))
                .collect(ImmutableCollectors.toSet());

        return new NormalizationBeforeUnification(newDataNodes, equalities);
    }

    /**
     * Basic implementation, as expected to be rarely used in practice.
     */
    private ExtensionalDataNode normalizeDataNode(ExtensionalDataNode dataNode,
                                                  ImmutableMap<GroundFunctionalTerm, Variable> groundFunctionalTermMap) {
        ImmutableMap<Integer, VariableOrGroundTerm> newArgumentMap = dataNode.getArgumentMap().entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> Optional.<VariableOrGroundTerm>ofNullable(groundFunctionalTermMap.get(e.getValue()))
                                .orElseGet(e::getValue)));
        return iqFactory.createExtensionalDataNode(dataNode.getRelationDefinition(), newArgumentMap);
    }

    /**
     * If extensional data nodes are empty, insert an EmptyNode
     */
    protected static class OptimizationState {
        public final ImmutableSet<ImmutableExpression> newExpressions;
        public final Collection<ExtensionalDataNode> extensionalDataNodes;
        public final Substitution<VariableOrGroundTerm> substitution;

        protected OptimizationState(ImmutableSet<ImmutableExpression> newExpressions,
                                  Collection<ExtensionalDataNode> extensionalDataNodes,
                                  Substitution<VariableOrGroundTerm> substitution) {
            this.newExpressions = newExpressions;
            this.extensionalDataNodes = extensionalDataNodes;
            this.substitution = substitution;
        }
    }

    protected static class DeterminantGroupEvaluation {
        public final ImmutableSet<ImmutableExpression> expressions;
        public final ImmutableList<ExtensionalDataNode> dataNodes;
        public final Substitution<VariableOrGroundTerm> substitution;

        protected DeterminantGroupEvaluation(ImmutableSet<ImmutableExpression> expressions, ImmutableList<ExtensionalDataNode> dataNodes,
                                             Substitution<VariableOrGroundTerm> substitution) {
            this.expressions = expressions;
            this.dataNodes = dataNodes;
            this.substitution = substitution;
        }
    }

    protected static class NormalizationBeforeUnification {
        public final Collection<ExtensionalDataNode> dataNodes;
        public final ImmutableSet<ImmutableExpression> equalities;

        protected NormalizationBeforeUnification(Collection<ExtensionalDataNode> dataNodes,
                                               ImmutableSet<ImmutableExpression> equalities) {
            this.dataNodes = dataNodes;
            this.equalities = equalities;
        }
    }
}

