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
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.node.normalization.ConstructionSubstitutionNormalizer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class AbstractSelfJoinSimplifier<C extends FunctionalDependency> {

    private final OptimizationState noSolutionState;
    protected final IntermediateQueryFactory iqFactory;
    protected final TermFactory termFactory;
    protected final ImmutableUnificationTools unificationTools;
    protected final SubstitutionFactory substitutionFactory;
    private final ConstructionSubstitutionNormalizer substitutionNormalizer;

    public AbstractSelfJoinSimplifier(CoreSingletons coreSingletons) {
        this.iqFactory = coreSingletons.getIQFactory();
        this.termFactory = coreSingletons.getTermFactory();
        this.unificationTools = coreSingletons.getUnificationTools();
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
        this.substitutionNormalizer = coreSingletons.getConstructionSubstitutionNormalizer();
        this.noSolutionState = new OptimizationState(ImmutableSet.of(), ImmutableList.of(), substitutionFactory.getSubstitution());
    }

    /**
     * Returns an empty result to indicate that no optimization has been applied
     */
    public Optional<IQTree> transformInnerJoin(InnerJoinNode rootNode, ImmutableList<IQTree> children,
                                     ImmutableSet<Variable> projectedVariables) {
        ImmutableMap<Boolean, ImmutableList<IQTree>> childPartitions = children.stream()
                .collect(ImmutableCollectors.partitioningBy(n -> (n instanceof ExtensionalDataNode)
                        && hasConstraint((ExtensionalDataNode) n)));

        Optional<ImmutableList<IQTree>> extensionalChildrenWithConstraint = Optional.ofNullable(childPartitions.get(true));

        Optional<ImmutableMap<RelationDefinition, Collection<ExtensionalDataNode>>> optionalDataNodeMap = extensionalChildrenWithConstraint
                .map(ns -> ns.stream()
                        .map(n -> (ExtensionalDataNode) n)
                        .collect(ImmutableCollectors.toMultimap(
                                ExtensionalDataNode::getRelationDefinition,
                                n -> n)).asMap());

        // No room for optimization (no self-join involving a constraint of the desired type) -> returns fast
        if (!optionalDataNodeMap
                .filter(m -> m.values().stream()
                        .anyMatch(c -> c.size() > 1))
                .isPresent())
            return Optional.empty();

        ImmutableList<OptimizationState> optimizationStates = optionalDataNodeMap
                .map(m -> m.entrySet().stream()
                        .map(e -> optimizeExtensionalDataNodes(e.getKey(), e.getValue()))
                        .collect(ImmutableCollectors.toList()))
                .orElseGet(ImmutableList::of);

        /*
         * First empty case: no solution for one relation
         */
        if (optimizationStates.stream().anyMatch(s -> s.extensionalDataNodes.isEmpty())) {
            return Optional.of(iqFactory.createEmptyNode(projectedVariables));
        }

        // NB: may return an unifier with no entry
        Optional<ImmutableSubstitution<VariableOrGroundTerm>> optionalUnifier = unifySubstitutions(
                optimizationStates.stream()
                        .map(s -> s.substitution));

        /*
         * Second empty case: incompatible unifications between relations
         */
        if (!optionalUnifier.isPresent()) {
            return Optional.of(iqFactory.createEmptyNode(projectedVariables));
        }
        ImmutableSubstitution<VariableOrGroundTerm> unifier = optionalUnifier.get();

        ImmutableList<IQTree> optimizedExtensionalDataNodes = optimizationStates.stream()
                .flatMap(s -> s.extensionalDataNodes.stream())
                .collect(ImmutableCollectors.toList());

        if (canEliminateNodes() && optimizedExtensionalDataNodes.size() == extensionalChildrenWithConstraint.map(AbstractCollection::size)
                .orElse(0)) {
            return Optional.empty();
        }

        Optional<ImmutableList<IQTree>> nonExtensionalChildrenWithConstraint = Optional.ofNullable(childPartitions.get(false));

        ImmutableList<IQTree> newChildren = nonExtensionalChildrenWithConstraint
                .map(nes -> Stream.concat(optimizedExtensionalDataNodes.stream(), nes.stream())
                        .collect(ImmutableCollectors.toList()))
                .orElse(optimizedExtensionalDataNodes);

        Optional<ImmutableExpression> newExpression = termFactory.getConjunction(
                rootNode.getOptionalFilterCondition(),
                optimizationStates.stream()
                        .flatMap(s -> s.newExpressions.stream()));

        return Optional.of(buildNewTree(newChildren, newExpression, unifier, projectedVariables));
    }

    protected abstract boolean canEliminateNodes();

    protected abstract boolean hasConstraint(ExtensionalDataNode node);

    private IQTree buildNewTree(ImmutableList<IQTree> children, Optional<ImmutableExpression> expression,
                                ImmutableSubstitution<VariableOrGroundTerm> unifier,
                                ImmutableSet<Variable> projectedVariables) {

        ImmutableList<IQTree> newChildren = unifier.isEmpty()
                ? children
                : children.stream()
                .map(t -> t.applyDescendingSubstitution(unifier, Optional.empty()))
                .collect(ImmutableCollectors.toList());

        Optional<ImmutableExpression> newExpression = expression
                .map(unifier::applyToBooleanExpression);

        IQTree newTree;
        switch (newChildren.size()) {
            case 0:
                throw new MinorOntopInternalBugException("Should have been detected before");
            case 1:
                IQTree child = newChildren.iterator().next();
                newTree = newExpression
                        .map(e -> (IQTree) iqFactory.createUnaryIQTree(iqFactory.createFilterNode(e), child))
                        .orElse(child);
                break;
            default:
                newTree = iqFactory.createNaryIQTree(iqFactory.createInnerJoinNode(newExpression), newChildren);
        }

        ConstructionSubstitutionNormalizer.ConstructionSubstitutionNormalization normalization = substitutionNormalizer
                .normalizeSubstitution((ImmutableSubstitution<ImmutableTerm>)(ImmutableSubstitution<?>)unifier,
                        projectedVariables);
        IQTree normalizedNewTree = normalization.updateChild(newTree);

        ImmutableSubstitution<ImmutableTerm> normalizedTopSubstitution = normalization.getNormalizedSubstitution();

        return normalizedTopSubstitution.isEmpty()
                ? normalizedNewTree.getVariables().size() == projectedVariables.size()
                    ? normalizedNewTree
                    // Makes sure no additional variables is projected
                    : iqFactory.createUnaryIQTree(iqFactory.createConstructionNode(projectedVariables), normalizedNewTree)
                : iqFactory.createUnaryIQTree(
                iqFactory.createConstructionNode(projectedVariables, normalizedTopSubstitution),
                normalizedNewTree);
    }


    protected OptimizationState optimizeExtensionalDataNodes(RelationDefinition relationDefinition,
                                                             Collection<ExtensionalDataNode> dataNodes) {

        OptimizationState initialState = new OptimizationState(ImmutableSet.of(), dataNodes,
                substitutionFactory.getSubstitution());

        if ((dataNodes.size() < 2))
            return initialState;

        return extractConstraints(relationDefinition)
                .reduce(initialState,
                        (s,c) -> simplifyUsingConstraint(c, s),
                        (ns1, ns2) -> {
                            throw new MinorOntopInternalBugException("Not expected to be executed in //");
                        });
    }

    protected abstract Stream<C> extractConstraints(RelationDefinition relationDefinition);

    private OptimizationState simplifyUsingConstraint(C constraint, OptimizationState state) {
        if ((state.extensionalDataNodes.size() < 2))
            return state;

        ImmutableList<Integer> ucIndexes = constraint.getDeterminants().stream()
                .map(a -> a.getIndex() - 1)
                .collect(ImmutableCollectors.toList());

        ImmutableMap<ImmutableList<Optional<VariableOrGroundTerm>>, Collection<ExtensionalDataNode>> map = state.extensionalDataNodes.stream()
                .collect(ImmutableCollectors.toMultimap(
                        n -> ucIndexes.stream()
                                .map(i -> Optional.ofNullable(n.getArgumentMap().get(i))
                                        .map(t -> (VariableOrGroundTerm) t))
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
            return noSolutionState;

        Optional<ImmutableSubstitution<VariableOrGroundTerm>> optionalUnifier = unifySubstitutions(
                Stream.concat(
                        simplifications.stream()
                                .map(Optional::get)
                                .map(s -> s.substitution),
                        Stream.of(state.substitution)));

        if (!optionalUnifier.isPresent()) {
            return noSolutionState;
        }
        ImmutableSubstitution<VariableOrGroundTerm> unifier = optionalUnifier.get();

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
                .map(n -> applySubstitution(unifier, n))
                .collect(ImmutableCollectors.toList());

        return new OptimizationState(newExpressions, newDataNodes, unifier);
    }

    private ExtensionalDataNode applySubstitution(ImmutableSubstitution<VariableOrGroundTerm> substitution,
                                                  ExtensionalDataNode dataNode) {
        return iqFactory.createExtensionalDataNode(
                dataNode.getRelationDefinition(),
                substitution.applyToArgumentMap(dataNode.getArgumentMap()));
    }

    protected abstract Optional<DeterminantGroupEvaluation> evaluateDeterminantGroup(ImmutableList<VariableOrGroundTerm> determinants,
                                                                                     Collection<ExtensionalDataNode> dataNodes,
                                                                                     C constraint);

    protected Optional<ImmutableUnificationTools.ArgumentMapUnification> unifyDataNodes(
            Stream<ExtensionalDataNode> dataNodes,
            Function<ExtensionalDataNode, ImmutableMap<Integer, ? extends VariableOrGroundTerm>> argumentMapSelector) {
         return dataNodes
                .reduce(null,
                        (o, n) -> o == null
                                ? Optional.of(new ImmutableUnificationTools.ArgumentMapUnification(
                                argumentMapSelector.apply(n),
                                substitutionFactory.getSubstitution()))
                                : o.flatMap(u -> unify(u, argumentMapSelector.apply(n))),
                        (m1, m2) -> {
                            throw new MinorOntopInternalBugException("Was not expected to be runned in //");
                        });
    }

    private Optional<ImmutableUnificationTools.ArgumentMapUnification> unify(
            ImmutableUnificationTools.ArgumentMapUnification previousUnification,
            ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap) {

            ImmutableMap<Integer, ? extends VariableOrGroundTerm> updatedArgumentMap =
                previousUnification.substitution.applyToArgumentMap(argumentMap);

        return unificationTools.computeArgumentMapMGU(previousUnification.argumentMap, updatedArgumentMap)
                .flatMap(u -> previousUnification.substitution.isEmpty()
                        ? Optional.of(u)
                        : unificationTools.computeAtomMGUS(previousUnification.substitution, u.substitution)
                        .map(s -> new ImmutableUnificationTools.ArgumentMapUnification(u.argumentMap, s)));
    }


    private Optional<ImmutableSubstitution<VariableOrGroundTerm>> unifySubstitutions(
            Stream<ImmutableSubstitution<VariableOrGroundTerm>> substitutions) {
        return substitutions
                .reduce(Optional.of(substitutionFactory.getSubstitution()),
                        (o, s) -> o.flatMap(s1 -> unificationTools.computeAtomMGUS(s1, s)),
                        (s1, s2) -> {
                            throw new MinorOntopInternalBugException("No //");
                        });
    }

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
        else {
            ImmutableMap<GroundFunctionalTerm, Variable> groundFunctionalTermMap = groundFunctionalTerms.stream()
                    .collect(ImmutableCollectors.toMap(
                            t -> t,
                            t -> termFactory.getVariable("v" + UUID.randomUUID().toString())
                    ));

            ImmutableList<ExtensionalDataNode> newDataNodes = dataNodes.stream()
                    .map(d -> normalizeDataNode(d, groundFunctionalTermMap))
                    .collect(ImmutableCollectors.toList());

            ImmutableSet<ImmutableExpression> equalities = groundFunctionalTermMap.entrySet().stream()
                    .map(e -> termFactory.getStrictEquality(e.getKey(), e.getValue()))
                    .collect(ImmutableCollectors.toSet());

            return new NormalizationBeforeUnification(newDataNodes, equalities);
        }
    }

    /**
     * Basic implementation, as expected to be rarely used in practice.
     */
    private ExtensionalDataNode normalizeDataNode(ExtensionalDataNode dataNode,
                                                  ImmutableMap<GroundFunctionalTerm, Variable> groundFunctionalTermMap) {
        ImmutableMap<Integer, VariableOrGroundTerm> newArgumentMap = dataNode.getArgumentMap().entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> Optional.ofNullable((VariableOrGroundTerm) groundFunctionalTermMap.get(e.getValue()))
                                .orElseGet(e::getValue)));
        return iqFactory.createExtensionalDataNode(dataNode.getRelationDefinition(), newArgumentMap);
    }

    /**
     * If extensional data nodes are empty, insert an EmptyNode
     */
    protected static class OptimizationState {
        public final ImmutableSet<ImmutableExpression> newExpressions;
        public final Collection<ExtensionalDataNode> extensionalDataNodes;
        public final ImmutableSubstitution<VariableOrGroundTerm> substitution;

        protected OptimizationState(ImmutableSet<ImmutableExpression> newExpressions,
                                  Collection<ExtensionalDataNode> extensionalDataNodes,
                                  ImmutableSubstitution<VariableOrGroundTerm> substitution) {
            this.newExpressions = newExpressions;
            this.extensionalDataNodes = extensionalDataNodes;
            this.substitution = substitution;
        }
    }

    protected static class DeterminantGroupEvaluation {
        public final ImmutableSet<ImmutableExpression> expressions;
        public final ImmutableList<ExtensionalDataNode> dataNodes;
        public final ImmutableSubstitution<VariableOrGroundTerm> substitution;

        protected DeterminantGroupEvaluation(ImmutableSet<ImmutableExpression> expressions, ImmutableList<ExtensionalDataNode> dataNodes,
                                             ImmutableSubstitution<VariableOrGroundTerm> substitution) {
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

