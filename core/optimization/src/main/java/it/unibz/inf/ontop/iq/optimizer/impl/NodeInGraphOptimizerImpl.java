package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
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
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Objects;
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
        var transformer = new Transformer(coreSingletons);
        var newTree = tree.acceptTransformer(transformer);
        return newTree.equals(tree)
                ? query
                : iqFactory.createIQ(query.getProjectionAtom(), newTree);

    }

    protected static class Transformer extends DefaultRecursiveIQTreeVisitingTransformer {
        protected Transformer(CoreSingletons coreSingletons) {
            super(coreSingletons);
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
                    .equals(tripleOrQuadPredicate.getGraph(nodeInGraphArguments));
        }

        private ImmutableList<IQTree> simplifyChildren(ImmutableMap<NodeInGraphContext, DataAtom<RDFAtomPredicate>> removableMap,
                                                       ImmutableMultimap<NodeInGraphContext, IQTree> nodeInGraphAtomMultimap,
                                                       ImmutableList<IQTree> children) {
            if (removableMap.isEmpty())
                return children;
            throw new RuntimeException("TODO: implement the simplification of the children");
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
