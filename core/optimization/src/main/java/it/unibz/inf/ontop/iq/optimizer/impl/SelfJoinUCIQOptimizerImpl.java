package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.dbschema.UniqueConstraint;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OptimizationSingletons;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.optimizer.SelfJoinUCIQOptimizer;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools.ArgumentMapUnification;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

@Singleton
public class SelfJoinUCIQOptimizerImpl implements SelfJoinUCIQOptimizer {

    private final IQTreeTransformer selfJoinUCTransformer;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    public SelfJoinUCIQOptimizerImpl(IntermediateQueryFactory iqFactory,
                                     OptimizationSingletons optimizationSingletons) {
        this.iqFactory = iqFactory;
        this.selfJoinUCTransformer = new SelfJoinUCTransformer(optimizationSingletons);
    }

    @Override
    public IQ optimize(IQ query) {
        IQTree initialTree = query.getTree();
        IQTree newTree = selfJoinUCTransformer.transform(initialTree);
        return (newTree.equals(initialTree))
                ? query
                : iqFactory.createIQ(query.getProjectionAtom(), newTree)
                .normalizeForOptimization();
    }


    private static class SelfJoinUCTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        private final OptimizationState noSolutionState;
        protected final TermFactory termFactory;
        protected final ImmutableUnificationTools unificationTools;
        protected final SubstitutionFactory substitutionFactory;

        protected SelfJoinUCTransformer(OptimizationSingletons optimizationSingletons) {
            super(optimizationSingletons.getCoreSingletons());
            CoreSingletons coreSingletons = optimizationSingletons.getCoreSingletons();
            this.termFactory = coreSingletons.getTermFactory();
            this.unificationTools = coreSingletons.getUnificationTools();
            this.substitutionFactory = coreSingletons.getSubstitutionFactory();
            this.noSolutionState = new OptimizationState(ImmutableSet.of(), ImmutableList.of(), substitutionFactory.getSubstitution());
        }

        @Override
        public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            // Recursive
            ImmutableList<IQTree> liftedChildren = children.stream()
                    .map(t -> t.acceptTransformer(this))
                    .collect(ImmutableCollectors.toList());

            ImmutableMap<Boolean, ImmutableList<IQTree>> childPartitions = liftedChildren.stream()
                    .collect(ImmutableCollectors.partitioningBy(n -> n instanceof ExtensionalDataNode));

            Optional<ImmutableList<IQTree>> nonExtensionalChildren = Optional.ofNullable(childPartitions.get(false));
            Optional<ImmutableList<IQTree>> extensionalChildren = Optional.ofNullable(childPartitions.get(true));

            Optional<ImmutableMultimap<RelationDefinition, ExtensionalDataNode>> optionalDataNodeMap = extensionalChildren
                    .map(ns -> ns.stream()
                            .map(n -> (ExtensionalDataNode) n)
                            .collect(ImmutableCollectors.toMultimap(
                                    ExtensionalDataNode::getRelationDefinition,
                                    n -> n)));

            ImmutableList<OptimizationState> optimizationStates = optionalDataNodeMap
                    .map(m -> m.asMap().entrySet().stream()
                            .map(e -> optimizeExtensionalDataNodes(e.getKey(), e.getValue()))
                            .collect(ImmutableCollectors.toList()))
                    .orElseGet(ImmutableList::of);

            /*
             * First empty case: no solution for one relation
             */
            if (optimizationStates.stream().anyMatch(s -> s.extensionalDataNodes.isEmpty())) {
                return iqFactory.createEmptyNode(tree.getVariables());
            }

            Optional<ImmutableSubstitution<VariableOrGroundTerm>> optionalUnifier = unifySubstitutions(
                    optimizationStates.stream()
                    .map(s -> s.substitution));

            /*
             * Second empty case: incompatible unifications between relations
             */
            if (!optionalUnifier.isPresent()) {
                return iqFactory.createEmptyNode(tree.getVariables());
            }
            ImmutableSubstitution<VariableOrGroundTerm> unifier = optionalUnifier.get();

            ImmutableList<IQTree> optimizedExtensionalDataNodes = optimizationStates.stream()
                    .flatMap(s -> s.extensionalDataNodes.stream())
                    .collect(ImmutableCollectors.toList());

            if (optimizedExtensionalDataNodes.size() == extensionalChildren.map(AbstractCollection::size)
                    .orElse(0)) {
                return liftedChildren.equals(children)
                        ? tree
                        : iqFactory.createNaryIQTree(rootNode, liftedChildren);
            }

            ImmutableList<IQTree> newChildren = nonExtensionalChildren
                    .map(nes -> Stream.concat(optimizedExtensionalDataNodes.stream(), nes.stream())
                            .collect(ImmutableCollectors.toList()))
                    .orElse(optimizedExtensionalDataNodes);

            Optional<ImmutableExpression> newExpression = termFactory.getConjunction(
                    Stream.concat(
                            rootNode.getOptionalFilterCondition()
                                    .map(ImmutableExpression::flattenAND)
                                    .orElseGet(Stream::empty),
                            optimizationStates.stream()
                                    .flatMap(s -> s.newExpressions.stream())));

            IQTree newJoinTree = iqFactory.createNaryIQTree(iqFactory.createInnerJoinNode(newExpression), newChildren)
                    .applyDescendingSubstitution(unifier, Optional.empty());

            return unifier.isEmpty()
                    ? newJoinTree
                    : iqFactory.createUnaryIQTree(
                            iqFactory.createConstructionNode(tree.getVariables(),
                                    (ImmutableSubstitution<ImmutableTerm>)(ImmutableSubstitution<?>)unifier),
                            newJoinTree);
        }

        protected OptimizationState optimizeExtensionalDataNodes(RelationDefinition relationDefinition,
                                                                 Collection<ExtensionalDataNode> dataNodes) {

            OptimizationState initialState = new OptimizationState(ImmutableSet.of(), dataNodes,
                    substitutionFactory.getSubstitution());

            if ((dataNodes.size() < 2))
                return initialState;

            return relationDefinition.getUniqueConstraints().stream()
                    .reduce(initialState,
                            (s,c) -> simplifyUsingUC(c, s),
                            (ns1, ns2) -> {
                        throw new MinorOntopInternalBugException("Not expected to be executed in //");
                    });
        }

        private OptimizationState simplifyUsingUC(UniqueConstraint uniqueConstraint, OptimizationState state) {
            if ((state.extensionalDataNodes.size() < 2))
                return state;

            ImmutableList<Integer> ucIndexes = uniqueConstraint.getDeterminants().stream()
                    .map(a -> a.getIndex() - 1)
                    .collect(ImmutableCollectors.toList());

            ImmutableMap<ImmutableList<Optional<VariableOrGroundTerm>>, Collection<ExtensionalDataNode>> map = state.extensionalDataNodes.stream()
                    .collect(ImmutableCollectors.toMultimap(
                            n -> ucIndexes.stream()
                                    .map(i -> Optional.ofNullable(n.getArgumentMap().get(i))
                                            .map(t -> (VariableOrGroundTerm) t))
                                    .collect(ImmutableCollectors.toList()),
                            n -> n)).asMap();

            ImmutableList<Optional<Simplification>> simplifications = map.entrySet().stream()
                    .filter(e -> e.getValue().size() > 1)
                    .map(e -> simplify(e.getKey().stream()
                                    .map(Optional::get)
                                    .collect(ImmutableCollectors.toList()),
                            e.getValue()))
                    .collect(ImmutableCollectors.toList());

            /*
             * No solution --> return no data nodes
             */
            if (simplifications.stream().anyMatch(s -> !s.isPresent()))
                return noSolutionState;

            Optional<ImmutableSubstitution<VariableOrGroundTerm>> optionalUnifier = unifySubstitutions(simplifications.stream()
                            .map(Optional::get)
                            .map(s -> s.substitution));

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
                            .map(s -> s.dataNode),
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

        private Optional<Simplification> simplify(ImmutableList<VariableOrGroundTerm> determinants,
                                        Collection<ExtensionalDataNode> dataNodes) {
            if (dataNodes.size() < 2)
                throw new IllegalArgumentException("At least two nodes");

            ImmutableSet<ImmutableExpression> expressions = determinants.stream()
                    .filter(d -> d instanceof Variable)
                    .map(d -> (Variable) d)
                    .map(termFactory::getDBIsNotNull)
                    .collect(ImmutableCollectors.toSet());

            Optional<ArgumentMapUnification> unification = dataNodes.stream()
                    .reduce(null,
                            (o, n) -> o == null
                                    ? Optional.of(new ArgumentMapUnification(
                                            n.getArgumentMap(),
                                            substitutionFactory.getSubstitution()))
                                    : o.flatMap(u -> unify(u, n.getArgumentMap())),
                            (m1, m2) -> {
                                throw new MinorOntopInternalBugException("Was not expected to be runned in //");
                            });
            return unification
                    .map(u -> new Simplification(
                            expressions,
                            iqFactory.createExtensionalDataNode(
                                    dataNodes.iterator().next().getRelationDefinition(), u.argumentMap),
                            u.substitution));
        }

        private Optional<ArgumentMapUnification> unify(
                ArgumentMapUnification previousUnification,
                ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap) {

            ImmutableMap<Integer, ? extends VariableOrGroundTerm> updatedArgumentMap =
                    previousUnification.substitution.applyToArgumentMap(argumentMap);

            return unificationTools.computeArgumentMapMGU(previousUnification.argumentMap, updatedArgumentMap)
                    .flatMap(u -> previousUnification.substitution.isEmpty()
                            ? Optional.of(u)
                            : unificationTools.computeAtomMGUS(previousUnification.substitution, u.substitution)
                                .map(s -> new ArgumentMapUnification(u.argumentMap, s)));
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

    }

    /**
     * If extensional data nodes are empty, insert an EmptyNode
     */
    private static class OptimizationState {
        public final ImmutableSet<ImmutableExpression> newExpressions;
        public final Collection<ExtensionalDataNode> extensionalDataNodes;
        public final ImmutableSubstitution<VariableOrGroundTerm> substitution;

        private OptimizationState(ImmutableSet<ImmutableExpression> newExpressions,
                                  Collection<ExtensionalDataNode> extensionalDataNodes,
                                  ImmutableSubstitution<VariableOrGroundTerm> substitution) {
            this.newExpressions = newExpressions;
            this.extensionalDataNodes = extensionalDataNodes;
            this.substitution = substitution;
        }
    }

    private static class Simplification {
        public final ImmutableSet<ImmutableExpression> expressions;
        public final ExtensionalDataNode dataNode;
        public final ImmutableSubstitution<VariableOrGroundTerm> substitution;

        private Simplification(ImmutableSet<ImmutableExpression> expressions, ExtensionalDataNode dataNode,
                               ImmutableSubstitution<VariableOrGroundTerm> substitution) {
            this.expressions = expressions;
            this.dataNode = dataNode;
            this.substitution = substitution;
        }
    }

}
