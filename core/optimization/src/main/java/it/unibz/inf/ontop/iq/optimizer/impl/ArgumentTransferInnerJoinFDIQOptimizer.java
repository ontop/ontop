package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.Attribute;
import it.unibz.inf.ontop.dbschema.FunctionalDependency;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.optimizer.InnerJoinIQOptimizer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * TODO: explain
 */
public class ArgumentTransferInnerJoinFDIQOptimizer implements InnerJoinIQOptimizer {

    private final ArgumentTransferJoinTransformer transformer;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    protected ArgumentTransferInnerJoinFDIQOptimizer(CoreSingletons coreSingletons) {
        this.transformer = new ArgumentTransferJoinTransformer(coreSingletons);
        this.iqFactory = coreSingletons.getIQFactory();
    }

    @Override
    public IQ optimize(IQ query) {
        IQTree initialTree = query.getTree();
        IQTree newTree = transformer.transform(initialTree);
        return (newTree.equals(initialTree))
                ? query
                : iqFactory.createIQ(query.getProjectionAtom(), newTree)
                .normalizeForOptimization();
    }


    protected static class ArgumentTransferJoinTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        protected ArgumentTransferJoinTransformer(CoreSingletons coreSingletons) {
            super(coreSingletons);
        }

        @Override
        public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            // Recursive
            ImmutableList<IQTree> newChildren = children.stream()
                    .map(t -> t.acceptTransformer(this))
                    .collect(ImmutableCollectors.toList());

            ImmutableSet<Integer> dataNodeWithFdIndexes = IntStream.range(0, newChildren.size())
                    .filter(i -> {
                        IQTree child = newChildren.get((i));
                        return (child instanceof ExtensionalDataNode)
                                && !((ExtensionalDataNode) child).getRelationDefinition()
                                .getOtherFunctionalDependencies().isEmpty();
                    })
                    .boxed()
                    .collect(ImmutableCollectors.toSet());

            Optional<ImmutableList<IQTree>> result = tryToTransfer(children, dataNodeWithFdIndexes);

            if (!result.isPresent())
                return children.equals(newChildren)
                        ? tree
                        : iqFactory.createNaryIQTree(rootNode, newChildren);

           throw new RuntimeException("TODO: continue");
        }

        protected Optional<ImmutableList<IQTree>> tryToTransfer(ImmutableList<IQTree> children,
                                                                 ImmutableSet<Integer> dataNodeWithFdIndexes) {
            if (dataNodeWithFdIndexes.size() < 2)
                return Optional.empty();

            ImmutableMap<RelationDefinition, Collection<ExtensionalDataNode>> nodeMap = dataNodeWithFdIndexes.stream()
                    .map(children::get)
                    .map(c -> (ExtensionalDataNode) c)
                    .collect(ImmutableCollectors.toMultimap(
                            ExtensionalDataNode::getRelationDefinition,
                            c -> c)).asMap();

            ImmutableMap<RelationDefinition, Optional<IQTree>> simplificationMap = nodeMap.entrySet().stream()
                    .collect(ImmutableCollectors.toMap(
                            Map.Entry::getKey,
                            e -> tryToTransfer(e.getKey(), e.getValue())));

            if (simplificationMap.values().stream()
                    .noneMatch(Optional::isPresent))
                return Optional.empty();

            return Optional.of(
                    simplificationMap.entrySet().stream()
                    .flatMap(e -> e.getValue()
                            .map(Stream::of)
                            .orElseGet(() -> nodeMap.get(e.getKey()).stream()
                                    .map(n -> (IQTree) n)))
                    .collect(ImmutableCollectors.toList()));

        }

        private Optional<IQTree> tryToTransfer(RelationDefinition relationDefinition, Collection<ExtensionalDataNode> nodes) {
            TransferState state = new TransferState(relationDefinition, ImmutableList.copyOf(nodes));
            TransferState finalState = relationDefinition.getOtherFunctionalDependencies().stream()
                    .reduce(state, TransferState::update, (s1, s2) -> {
                        throw new MinorOntopInternalBugException("Not expect to run in //");
                    });

            return finalState.convert(nodes);
        }

        protected class TransferState {
            protected final RelationDefinition relationDefinition;
            protected final ImmutableList<ExtensionalDataNode> nodes;

            protected TransferState(RelationDefinition relationDefinition, ImmutableList<ExtensionalDataNode> nodes) {
                this.relationDefinition = relationDefinition;
                this.nodes = nodes;
            }

            public TransferState update(FunctionalDependency functionalDependency) {
                if (nodes.size() < 2)
                    return this;

                ImmutableMap<Optional<ImmutableList<VariableOrGroundTerm>>, Collection<ExtensionalDataNode>>
                        nodeByDeterminantMap = nodes.stream()
                        .collect(ImmutableCollectors.toMultimap(
                                n -> extractDeterminantKey(n, functionalDependency.getDeterminants()),
                                n -> n
                        )).asMap();

                ImmutableMap<Optional<ImmutableList<VariableOrGroundTerm>>, Optional<TransferState>> groupsAfterTransfer = nodeByDeterminantMap.entrySet().stream()
                        .collect(ImmutableCollectors.toMap(
                                Map.Entry::getKey,
                                e -> tryToTransfer(ImmutableList.copyOf(e.getValue()), functionalDependency)));

                Optional<TransferState> mergedSubTransferState = groupsAfterTransfer.values().stream()
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .reduce(TransferState::merge);

                return mergedSubTransferState
                        // Appends nodes from other groups
                        .map(s -> s.appendNodes(groupsAfterTransfer.entrySet().stream()
                                .filter(e -> !e.getValue().isPresent())
                                .flatMap(e -> nodeByDeterminantMap.get(e.getKey()).stream())))
                        // Merges with the local construction node
                        .map(this::mergeWithChild)
                        .orElse(this);
            }


            private Optional<ImmutableList<VariableOrGroundTerm>> extractDeterminantKey(ExtensionalDataNode node,
                                                                                        ImmutableSet<Attribute> determinants) {
                ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap = node.getArgumentMap();

                ImmutableList<Optional<? extends VariableOrGroundTerm>> arguments = determinants.stream()
                        .map(a -> a.getIndex() - 1)
                        .map(i -> Optional.ofNullable(argumentMap.get(i)))
                        .collect(ImmutableCollectors.toList());

                if (arguments.stream().anyMatch(a -> !a.isPresent()))
                    return Optional.empty();

                return Optional.of(arguments.stream()
                        .map(Optional::get)
                        .collect(ImmutableCollectors.toList()));
            }

            private Optional<TransferState> tryToTransfer(ImmutableList<ExtensionalDataNode> sameDeterminantKeyNodes,
                                                            FunctionalDependency functionalDependency) {
                ImmutableSet<Attribute> dependentAttributes = functionalDependency.getDependents();
                ImmutableSet<Attribute> determinantAttributes = functionalDependency.getDeterminants();

                ImmutableList<Integer> externalArgumentIndexes = relationDefinition.getAttributes().stream()
                        .filter(a -> !dependentAttributes.contains(a))
                        .filter(a -> !determinantAttributes.contains(a))
                        .map(a -> a.getIndex() - 1)
                        .collect(ImmutableCollectors.toList());

                ImmutableMap<Integer, ImmutableMap<Integer, ? extends VariableOrGroundTerm>> nodeExternalArgumentMap =
                        IntStream.range(0, nodes.size())
                                .boxed()
                                .collect(ImmutableCollectors.toMap(
                                    i -> i,
                                    i -> nodes.get(i).getArgumentMap().entrySet().stream()
                                            .filter(e -> externalArgumentIndexes.contains(e.getKey()))
                                            .collect(ImmutableCollectors.toMap())));

                TransferState initialSubState = new TransferState(relationDefinition, sameDeterminantKeyNodes);
                TransferState finalState = IntStream.range(0, sameDeterminantKeyNodes.size())
                        .boxed()
                        .reduce(initialSubState, (s, i) -> s.simplifyNode(i, functionalDependency, nodeExternalArgumentMap),
                                (s1, s2) -> {
                                    throw new MinorOntopInternalBugException("Merge in // should have been disabled");
                                });
                return Optional.of(finalState)
                        .filter(s -> !s.equals(initialSubState));
            }

            /**
             * Tries to transfer the dependent arguments to another node
             *
             * Preserves positions
             *
             * Returns itself if it cannot simplify the focus node
             */
            private TransferState simplifyNode(int index, FunctionalDependency functionalDependency,
                                                 ImmutableMap<Integer, ImmutableMap<Integer, ? extends VariableOrGroundTerm>> nodeExternalArgumentMap) {
                ExtensionalDataNode focusNode = nodes.get(index);
                ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap = focusNode.getArgumentMap();

                ImmutableSet<Attribute> dependents = functionalDependency.getDependents();

                ImmutableList<Integer> sourceDependentIndexes = dependents.stream()
                        .map(a -> a.getIndex() - 1)
                        .filter(argumentMap::containsKey)
                        .collect(ImmutableCollectors.toList());

                if (sourceDependentIndexes.isEmpty())
                    return this;

                ImmutableMap<Integer, ? extends VariableOrGroundTerm> localExternalArgumentMap = nodeExternalArgumentMap.get(index);

                OptionalInt otherIndex = IntStream.range(0, nodes.size())
                        .filter(j -> j != index)
                        .filter(j -> isSuitableForTransfer(sourceDependentIndexes.size(), localExternalArgumentMap,
                                nodes.get(j), nodeExternalArgumentMap.get(j), dependents))
                        .findFirst();

                if (!otherIndex.isPresent())
                    return this;

                return transferDependentArguments(index, sourceDependentIndexes, argumentMap, otherIndex.getAsInt());
            }

            /**
             * TODO: further explain
             */
            protected boolean isSuitableForTransfer(int sourceDependentArgCount,
                                                  ImmutableMap<Integer, ? extends VariableOrGroundTerm> sourceExternalArgumentMap,
                                                  ExtensionalDataNode candidateNode,
                                                  ImmutableMap<Integer, ? extends VariableOrGroundTerm> candidateExternalArgumentMap,
                                                  ImmutableSet<Attribute> dependents) {
                // First criteria: must contains all the external arguments of the source
                if (!sourceExternalArgumentMap.entrySet().stream()
                        .allMatch(e -> Optional.ofNullable(candidateExternalArgumentMap.get(e.getKey()))
                                .filter(v -> v.equals(e.getValue()))
                                .isPresent()))
                    return false;

                // If it contains additional external arguments, then it is suitable (more constrained)
                if(!sourceExternalArgumentMap.keySet()
                        .containsAll(candidateExternalArgumentMap.keySet()))
                    return true;

                ImmutableMap<Integer, ? extends VariableOrGroundTerm> candidateArgumentMap = candidateNode.getArgumentMap();

                long candidateDependentArgCount = dependents.stream()
                        .map(a -> a.getIndex() - 1)
                        .filter(candidateArgumentMap::containsKey)
                        .count();

                // Last criteria for avoiding loops (i.e. transferring back and forth) between nodes having the same external arguments
                return candidateDependentArgCount >= sourceDependentArgCount;
            }

            private TransferState transferDependentArguments(int sourceIndex, ImmutableList<Integer> sourceDependentIndexes,
                                                             ImmutableMap<Integer, ? extends VariableOrGroundTerm> sourceArgumentMap,
                                                             int targetIndex) {
                ImmutableMap<Integer, ? extends VariableOrGroundTerm> newSourceArgumentMap = sourceArgumentMap.entrySet().stream()
                        .filter(e -> !sourceDependentIndexes.contains(e.getKey()))
                        .collect(ImmutableCollectors.toMap());

                ExtensionalDataNode formerSource = nodes.get(sourceIndex);
                ExtensionalDataNode newSource = iqFactory.createExtensionalDataNode(formerSource.getRelationDefinition(),
                        newSourceArgumentMap);

                // TODO: unify
                throw new RuntimeException("TODO: continue implementing the transfer");
            }

            private TransferState merge(TransferState other) {
                throw new RuntimeException("TODO: implement merge");
            }

            private TransferState appendNodes(Stream<ExtensionalDataNode> otherNodes) {
                throw new RuntimeException("TODO: implement appendNodes");
            }

            private TransferState mergeWithChild(TransferState s) {
                throw new RuntimeException("TODO: merge with child");
            }


            public Optional<IQTree> convert(Collection<ExtensionalDataNode> initialNodes) {
                if (nodes.equals(initialNodes))
                    return Optional.empty();

                throw new RuntimeException("TODO: continue");
            }
        }
    }

}
