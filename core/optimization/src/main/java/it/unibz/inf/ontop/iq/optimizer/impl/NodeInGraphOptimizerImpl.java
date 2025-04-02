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
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.NodeInGraphPredicate;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
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

            var nodeInGraphAtomMultimap = extractNodeInGraphAtoms(updatedChildren);
            var removableNodeInGraphAtoms = selectRemovableNodeInGraphAtoms(nodeInGraphAtomMultimap.keySet(), updatedChildren);
            var newChildren = simplifyChildren(removableNodeInGraphAtoms, nodeInGraphAtomMultimap, updatedChildren);
            return newChildren.equals(children)
                    ? tree
                    : iqFactory.createNaryIQTree(rootNode, newChildren);
        }

        protected ImmutableMultimap<DataAtom<NodeInGraphPredicate>, IQTree> extractNodeInGraphAtoms(ImmutableList<IQTree> children) {
            return children.stream()
                    .filter(c -> c.getRootNode() instanceof UnionNode)
                    .flatMap(c -> extractNodeInGraphAtomsFromUnionNode(c))
                    .collect(ImmutableCollectors.toMultimap());
        }

        private Stream<Map.Entry<DataAtom<NodeInGraphPredicate>, IQTree>> extractNodeInGraphAtomsFromUnionNode(
                IQTree unionTree) {
            return unionTree.getChildren().stream()
                    .filter(t -> t.getRootNode() instanceof ConstructionNode)
                    .map(t -> ((UnaryIQTree) t).getChild())
                    .filter(t -> t instanceof IntensionalDataNode)
                    .map(t -> ((IntensionalDataNode) t).getProjectionAtom())
                    .filter(a -> a.getPredicate() instanceof NodeInGraphPredicate)
                    .map(a -> Maps.immutableEntry((DataAtom<NodeInGraphPredicate>) (DataAtom<?>) a, unionTree));
        }

        private ImmutableMap<DataAtom<NodeInGraphPredicate>, DataAtom<RDFAtomPredicate>> selectRemovableNodeInGraphAtoms(ImmutableSet<DataAtom<NodeInGraphPredicate>> nodeInGraphAtoms, ImmutableList<IQTree> updatedChildren) {
            if (nodeInGraphAtoms.isEmpty())
                return ImmutableMap.of();

            var triplesOrQuads = updatedChildren.stream()
                    .filter(c -> c instanceof IntensionalDataNode)
                    .map(c -> ((IntensionalDataNode) c).getProjectionAtom())
                    .filter(a -> a.getPredicate() instanceof RDFAtomPredicate)
                    .map(a -> (DataAtom<RDFAtomPredicate>)(DataAtom<?>) a)
                    .collect(ImmutableCollectors.toSet());

            return nodeInGraphAtoms.stream()
                    .flatMap(a -> triplesOrQuads.stream()
                            .filter(t -> isPresenceInGraphGuaranteed(a, t))
                            .findAny()
                            .map(t -> Maps.immutableEntry(a, t)).stream())
                    .collect(ImmutableCollectors.toMap());

        }

        private boolean isPresenceInGraphGuaranteed(DataAtom<NodeInGraphPredicate> nodeInGraphAtom, DataAtom<RDFAtomPredicate> tripleOrQuadAtom) {
            var tripleOrQuadArguments = tripleOrQuadAtom.getArguments();
            var nodeInGraphArguments = nodeInGraphAtom.getArguments();
            if (!tripleOrQuadArguments.containsAll(nodeInGraphArguments))
                return false;

            var nodeInGraphPredicate = nodeInGraphAtom.getPredicate();
            var tripleOrQuadPredicate = tripleOrQuadAtom.getPredicate();
            var node = nodeInGraphPredicate.getNode(nodeInGraphAtom.getArguments());

            if (!(tripleOrQuadPredicate.getSubject(tripleOrQuadArguments).equals(node)
                    || tripleOrQuadPredicate.getObject(tripleOrQuadArguments).equals(node)))
                return false;

            if (nodeInGraphPredicate.isInDefaultGraph())
                return tripleOrQuadPredicate.getArity() == 3;

            return nodeInGraphPredicate.getGraph(nodeInGraphArguments)
                    .equals(tripleOrQuadPredicate.getGraph(nodeInGraphArguments));
        }

        private ImmutableList<IQTree> simplifyChildren(ImmutableMap<DataAtom<NodeInGraphPredicate>, DataAtom<RDFAtomPredicate>> removableMap,
                                                       ImmutableMultimap<DataAtom<NodeInGraphPredicate>, IQTree> nodeInGraphAtomMultimap,
                                                       ImmutableList<IQTree> children) {
            if (removableMap.isEmpty())
                return children;
            throw new RuntimeException("TODO: implement the simplification of the children");
        }
    }
}
