package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.NodeInGraphOptimizer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.NodeInGraphPredicate;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class NodeInGraphOptimizerImpl implements NodeInGraphOptimizer {

    private final CoreSingletons coreSingletons;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    protected NodeInGraphOptimizerImpl(CoreSingletons coreSingletons,
                                       IntermediateQueryFactory iqFactory) {
        this.coreSingletons = coreSingletons;
        this.iqFactory = iqFactory;
    }


    @Override
    public IQ optimize(IQ query) {
        var tree = query.getTree();
        // TODO: add a transformer for simplifying node(<cst>) or node(<cst>,g)
        var transformer = new Transformer(coreSingletons);
        var newTree = tree.acceptTransformer(transformer)
                .normalizeForOptimization(query.getVariableGenerator());

        return newTree.equals(tree)
                ? query
                : iqFactory.createIQ(query.getProjectionAtom(), newTree);

    }

    protected static class Transformer extends DefaultRecursiveIQTreeVisitingTransformer {
        private final SubstitutionFactory substitutionFactory;

        protected Transformer(CoreSingletons coreSingletons) {
            super(coreSingletons);
            this.substitutionFactory = coreSingletons.getSubstitutionFactory();
        }

        @Override
        public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            // Recursive
            var updatedChildren = children.stream()
                    .map(t -> t.acceptTransformer(this))
                    .collect(ImmutableCollectors.toList());

            var nodeInGraphContextMultimap = extractNodeInGraphContexts(updatedChildren);
            var removableNodeInGraphAtoms = selectRemovableNodeInGraphAtoms(nodeInGraphContextMultimap.keySet(), updatedChildren);
            var newChildren = simplifyChildren(removableNodeInGraphAtoms, nodeInGraphContextMultimap, updatedChildren);
            return newChildren.equals(children)
                    ? tree
                    : newChildren.size() == 1
                        ? newChildren.iterator().next()
                        : iqFactory.createNaryIQTree(rootNode, newChildren);
        }

        protected ImmutableMultimap<NodeInGraphContext, IQTree> extractNodeInGraphContexts(ImmutableList<IQTree> children) {
            return children.stream()
                    .filter(c -> c.getRootNode() instanceof UnionNode)
                    .flatMap(c -> extractNodeInGraphContextsFromUnionNode(c))
                    .collect(ImmutableCollectors.toMultimap());
        }

        private Stream<Map.Entry<NodeInGraphContext, IQTree>> extractNodeInGraphContextsFromUnionNode(
                IQTree unionTree) {
            return unionTree.getChildren().stream()
                    .filter(t -> t.getRootNode() instanceof ConstructionNode)
                    .flatMap(constructTree -> constructTree.getChildren().stream().findFirst()
                            .filter(t -> t instanceof IntensionalDataNode)
                            .map(t -> ((IntensionalDataNode) t).getProjectionAtom())
                            .filter(a -> a.getPredicate() instanceof NodeInGraphPredicate)
                            .map(a -> Maps.immutableEntry(
                                    extractContext(a, (ConstructionNode) constructTree.getRootNode()),
                                    unionTree))
                            .stream());
        }

        private static NodeInGraphContext extractContext(DataAtom<AtomPredicate> atom,
                                                                     ConstructionNode constructionNode) {
            var nodeInGraphAtom = (DataAtom<NodeInGraphPredicate>)(DataAtom<?>) atom;
            var firstNode = nodeInGraphAtom.getPredicate().getNode(nodeInGraphAtom.getArguments());
            var nodes = Stream.concat(
                    Stream.of(firstNode),
                    constructionNode.getSubstitution().stream()
                            .filter(e -> e.getValue().equals(firstNode))
                            .map(Map.Entry::getKey))
                    .collect(ImmutableSet.toImmutableSet());

            return new NodeInGraphContext(nodes, nodeInGraphAtom);
        }

        private ImmutableMap<NodeInGraphContext, DataAtom<RDFAtomPredicate>> selectRemovableNodeInGraphAtoms(ImmutableSet<NodeInGraphContext> nodeInGraphContexts, ImmutableList<IQTree> updatedChildren) {
            if (nodeInGraphContexts.isEmpty())
                return ImmutableMap.of();

            var triplesOrQuads = updatedChildren.stream()
                    .filter(c -> c instanceof IntensionalDataNode)
                    .map(c -> ((IntensionalDataNode) c).getProjectionAtom())
                    .filter(a -> a.getPredicate() instanceof RDFAtomPredicate)
                    .map(a -> (DataAtom<RDFAtomPredicate>)(DataAtom<?>) a)
                    .collect(ImmutableSet.toImmutableSet());

            return nodeInGraphContexts.stream()
                    .flatMap(a -> triplesOrQuads.stream()
                            .filter(t -> isPresenceInGraphGuaranteed(a, t))
                            .findAny()
                            .map(t -> Maps.immutableEntry(a, t)).stream())
                    .collect(ImmutableCollectors.toMap());

        }

        private boolean isPresenceInGraphGuaranteed(NodeInGraphContext nodeInGraphContext, DataAtom<RDFAtomPredicate> tripleOrQuadAtom) {
            var tripleOrQuadArguments = tripleOrQuadAtom.getArguments();
            var nodeInGraphArguments = nodeInGraphContext.atom.getArguments();

            var nodeInGraphPredicate = nodeInGraphContext.atom.getPredicate();
            var tripleOrQuadPredicate = tripleOrQuadAtom.getPredicate();

            if (!(nodeInGraphContext.nodeArguments.contains(tripleOrQuadPredicate.getSubject(tripleOrQuadArguments))
                    || nodeInGraphContext.nodeArguments.contains(tripleOrQuadPredicate.getObject(tripleOrQuadArguments))))
                return false;

            if (nodeInGraphPredicate.isInDefaultGraph())
                return tripleOrQuadPredicate.getArity() == 3;

            return nodeInGraphPredicate.getGraph(nodeInGraphArguments)
                    .equals(tripleOrQuadPredicate.getGraph(tripleOrQuadArguments));
        }

        private ImmutableList<IQTree> simplifyChildren(ImmutableMap<NodeInGraphContext, DataAtom<RDFAtomPredicate>> removableMap,
                                                       ImmutableMultimap<NodeInGraphContext, IQTree> nodeInGraphTreeMultimap,
                                                       ImmutableList<IQTree> children) {
            if (removableMap.isEmpty())
                return children;

            var pushedDownDataAtoms = ImmutableSet.copyOf(removableMap.values());
            var childrenToUpdate = nodeInGraphTreeMultimap.entries().stream()
                    .filter(e -> removableMap.containsKey(e.getKey()))
                    .map(Map.Entry::getValue)
                    .collect(ImmutableSet.toImmutableSet());

            return children.stream()
                    .flatMap(c -> handleChild(c, pushedDownDataAtoms, removableMap, childrenToUpdate,
                            nodeInGraphTreeMultimap).stream())
                    .collect(ImmutableCollectors.toList());
        }

        private Optional<IQTree> handleChild(IQTree child, ImmutableSet<DataAtom<RDFAtomPredicate>> pushedDownDataAtoms,
                                             ImmutableMap<NodeInGraphContext, DataAtom<RDFAtomPredicate>> removableMap,
                                             ImmutableSet<IQTree> childrenToUpdate,
                                             ImmutableMultimap<NodeInGraphContext, IQTree> nodeInGraphTreeMultimap) {
            if (child instanceof IntensionalDataNode) {
                return Optional.of(child)
                        .filter(c -> !pushedDownDataAtoms.contains(((IntensionalDataNode) child).getProjectionAtom()));
            }
            if (childrenToUpdate.contains(child)) {
                var pushedDataAtom = nodeInGraphTreeMultimap.entries().stream()
                        .filter(e -> e.getValue().equals(child))
                        .map(Map.Entry::getKey)
                        .flatMap(k -> Optional.ofNullable(removableMap.get(k)).stream())
                        .findAny()
                        .orElseThrow(() -> new MinorOntopInternalBugException("Was expecting the tree to have a context to be removed"));

                return Optional.of(pushDataAtom(child, pushedDataAtom));
            }
            return Optional.of(child);
        }

        private IQTree pushDataAtom(IQTree child, DataAtom<RDFAtomPredicate> pushedDataAtom) {
            var newProjectedVariables = Sets.union(child.getVariables(), pushedDataAtom.getVariables())
                    .immutableCopy();

            if (!(child.getRootNode() instanceof UnionNode))
                throw new MinorOntopInternalBugException("Was expecting the child to be a union node");

            var pushedIntensionalNode = iqFactory.createIntensionalDataNode(
                    (DataAtom<AtomPredicate>)(DataAtom<?>)pushedDataAtom);

            var newUnionNode = iqFactory.createUnionNode(newProjectedVariables);
            return iqFactory.createNaryIQTree(
                    newUnionNode,
                    child.getChildren().stream()
                            .map(c -> pushDataAtomIntoChildOfUnion(c, pushedIntensionalNode))
                            .collect(ImmutableCollectors.toList()));
        }

        private IQTree pushDataAtomIntoChildOfUnion(IQTree tree, IntensionalDataNode pushedIntensionalNode) {
            var rootNode = tree.getRootNode();
            if (rootNode instanceof ConstructionNode) {
                var child = ((UnaryIQTree) tree).getChild();
                if ((child instanceof IntensionalDataNode)
                        && (((IntensionalDataNode) child).getProjectionAtom().getPredicate() instanceof NodeInGraphPredicate)) {
                    var intensionalVariables = pushedIntensionalNode.getVariables();
                    var treeVariables = tree.getVariables();
                    if (intensionalVariables.containsAll(treeVariables))
                        // TODO: add a filter with an equality between the vars
                        return pushedIntensionalNode;

                    var commonTerm = pushedIntensionalNode.getProjectionAtom().getArguments().stream()
                            // Eliminates the named graph
                            .limit(3)
                            .filter(t -> treeVariables.contains(t))
                            .findAny()
                            .orElseThrow(() -> new MinorOntopInternalBugException("Was expecting to find a common term"));

                    var newSubstitution = Sets.difference(treeVariables, intensionalVariables).stream()
                            .collect(substitutionFactory.toSubstitution(v -> commonTerm));

                    return iqFactory.createUnaryIQTree(
                            iqFactory.createConstructionNode(Sets.union(intensionalVariables, treeVariables).immutableCopy(),
                                    newSubstitution),
                            pushedIntensionalNode);
                }
            }
            // TODO: consider the case where there is no construction node (?v :p* ?v)
            return iqFactory.createNaryIQTree(
                iqFactory.createInnerJoinNode(),
                    ImmutableList.of(tree, pushedIntensionalNode));
        }
    }


    private static class NodeInGraphContext {
        private final ImmutableSet<VariableOrGroundTerm> nodeArguments;
        private final DataAtom<NodeInGraphPredicate> atom;

        private NodeInGraphContext(ImmutableSet<VariableOrGroundTerm> nodeArguments, DataAtom<NodeInGraphPredicate> atom) {
            this.nodeArguments = nodeArguments;
            this.atom = atom;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof NodeInGraphContext)) return false;
            NodeInGraphContext that = (NodeInGraphContext) o;
            return Objects.equals(nodeArguments, that.nodeArguments) && Objects.equals(atom, that.atom);
        }

        @Override
        public int hashCode() {
            return Objects.hash(nodeArguments, atom);
        }
    }

}
