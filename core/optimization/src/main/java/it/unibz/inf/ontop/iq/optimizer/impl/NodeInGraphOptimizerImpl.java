package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.NodeInGraphOptimizer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.atom.*;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;
import static it.unibz.inf.ontop.iq.impl.IQTreeTools.NaryIQTreeDecomposition;


/**
 * Tries to eliminate nodeInGraph atoms where:
 *    - the node is a variable
 *    - all arguments are ground
 */
public class NodeInGraphOptimizerImpl implements NodeInGraphOptimizer {

    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;
    private final SubstitutionFactory substitutionFactory;
    private final IQTreeTools iqTreeTools;

    @Inject
    protected NodeInGraphOptimizerImpl(CoreSingletons coreSingletons, IQTreeTools iqTreeTools) {
        this.iqFactory = coreSingletons.getIQFactory();
        this.termFactory = coreSingletons.getTermFactory();
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
        this.iqTreeTools = iqTreeTools;
    }


    @Override
    public IQ optimize(IQ query) {
        var tree = query.getTree();
        var transformer = new Transformer();
        var newTree = tree.acceptTransformer(transformer)
                .normalizeForOptimization(query.getVariableGenerator());

        return newTree.equals(tree)
                ? query
                : iqFactory.createIQ(query.getProjectionAtom(), newTree);

    }

    protected class Transformer extends DefaultRecursiveIQTreeVisitingTransformer {

        protected Transformer() {
            super(NodeInGraphOptimizerImpl.this.iqFactory);
        }

        /**
         * Taking advantage of an inconsistency of SPARQL 1.1 regarding the usage of the nodes(G) function.
         * For constants, the function is never inserted.
         */
        @Override
        public IQTree transformIntensionalData(IntensionalDataNode dataNode) {
            var atom = dataNode.getProjectionAtom();
            if ((atom.getPredicate() instanceof NodeInGraphPredicate)
                    && atom.getArguments().stream().allMatch(ImmutableTerm::isGround)) {
                return iqFactory.createTrueNode();
            }
            return dataNode;
        }

        @Override
        public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            // Recursive
            var updatedChildren = transformChildren(children);

            var nodeInGraphContextMultimap = extractNodeInGraphContexts(updatedChildren);
            var removableNodeInGraphAtoms = selectRemovableNodeInGraphAtoms(nodeInGraphContextMultimap.keySet(), updatedChildren);
            var newChildren = simplifyChildren(removableNodeInGraphAtoms, nodeInGraphContextMultimap, updatedChildren);
            return newChildren.equals(children)
                    ? tree
                    : newChildren.size() == 1
                        ? iqTreeTools.createOptionalUnaryIQTree(
                                rootNode.getOptionalFilterCondition()
                                        .map(iqFactory::createFilterNode),newChildren.get(0))
                        : iqFactory.createNaryIQTree(rootNode, newChildren);
        }

        private ImmutableMultimap<NodeInGraphContext, IQTree> extractNodeInGraphContexts(ImmutableList<IQTree> children) {
            return children.stream()
                    .flatMap(c -> extractNodeInGraphContextsFromUnionNode(c).stream())
                    .collect(ImmutableCollectors.toMultimap());
        }

        private Optional<Map.Entry<NodeInGraphContext, IQTree>> extractNodeInGraphContextsFromUnionNode(IQTree unionTree) {
            return NaryIQTreeDecomposition.of(unionTree, UnionNode.class)
                    .getOptionalNode()
                    .flatMap(n -> unionTree.getChildren().stream()
                            .flatMap(t -> extractContext(t)
                                    .map(c -> Maps.immutableEntry(c, unionTree))
                                    .stream())
                            .findAny());
        }

        private Optional<NodeInGraphContext> extractContext(IQTree childOfUnion) {
            var construction = UnaryIQTreeDecomposition.of(childOfUnion, ConstructionNode.class);
            return construction.isPresent()
                    ? extractNodeInGraphAtomWithVariable(construction.getChild())
                        .map(a -> extractContext(a, construction.getNode()))
                    : extractNodeInGraphAtomWithVariable(childOfUnion)
                        .map(a -> new NodeInGraphContext(
                            ImmutableSet.of((Variable) a.getPredicate().getNode(a.getArguments())), a));
        }

        private Optional<DataAtom<NodeInGraphPredicate>> extractNodeInGraphAtomWithVariable(IQTree tree) {
            return Optional.of(tree)
                    .filter(t -> t instanceof IntensionalDataNode)
                    .map(t -> (IntensionalDataNode) t)
                    .map(IntensionalDataNode::getProjectionAtom)
                    .filter(a -> a.getPredicate() instanceof NodeInGraphPredicate)
                    .filter(a -> !((NodeInGraphPredicate) a.getPredicate())
                            .getNode(a.getArguments())
                            .isGround())
                    .map(a -> (DataAtom<NodeInGraphPredicate>)(DataAtom<?>) a);
        }

        private NodeInGraphContext extractContext(DataAtom<NodeInGraphPredicate> nodeInGraphAtom,
                                                  ConstructionNode constructionNode) {
            var firstNode = (Variable) nodeInGraphAtom.getPredicate().getNode(nodeInGraphAtom.getArguments());
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
                    .map(c -> (IntensionalDataNode) c)
                    .map(IntensionalDataNode::getProjectionAtom)
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

        /**
         * Test if the triple or quad guarantees that the node is in the default or the named graph.
         * NB: the special case of literals will be filtered out later on.
         */
        private boolean isPresenceInGraphGuaranteed(NodeInGraphContext nodeInGraphContext, DataAtom<RDFAtomPredicate> tripleOrQuadAtom) {
            var tripleOrQuadArguments = tripleOrQuadAtom.getArguments();

            var tripleOrQuadPredicate = tripleOrQuadAtom.getPredicate();

            if (!(nodeInGraphContext.nodeArguments.contains(tripleOrQuadPredicate.getSubject(tripleOrQuadArguments))
                    || nodeInGraphContext.nodeArguments.contains(tripleOrQuadPredicate.getObject(tripleOrQuadArguments))))
                return false;

            if (nodeInGraphContext.isInDefaultGraph())
                return tripleOrQuadPredicate.getArity() == 3;

            return tripleOrQuadPredicate.getGraph(tripleOrQuadArguments)
                    .filter(nodeInGraphContext.graphArguments::contains)
                    .isPresent();
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

            var union = NaryIQTreeDecomposition.of(child, UnionNode.class);
            if (!union.isPresent())
                throw new MinorOntopInternalBugException("Was expecting the child to be a union node");

            var pushedIntensionalNode = iqFactory.createIntensionalDataNode(
                    (DataAtom<AtomPredicate>)(DataAtom<?>)pushedDataAtom);

            return iqFactory.createNaryIQTree(
                    iqFactory.createUnionNode(newProjectedVariables),
                    child.getChildren().stream()
                            .map(c -> pushDataAtomIntoChildOfUnion(c, pushedIntensionalNode))
                            .collect(ImmutableCollectors.toList()));
        }

        private boolean isNodeGraphPredicateNode(IQTree child) {
            return (child instanceof IntensionalDataNode)
                    && (((IntensionalDataNode) child).getProjectionAtom().getPredicate() instanceof NodeInGraphPredicate);
        }

        private IQTree pushDataAtomIntoChildOfUnion(IQTree tree, IntensionalDataNode pushedIntensionalNode) {
            var construction = UnaryIQTreeDecomposition.of(tree, ConstructionNode.class);
            if (construction.isPresent()) {
                if (isNodeGraphPredicateNode(construction.getChild()))
                    return pushDataAtomIntoConstructionTreeWithNodeInGraph(tree, pushedIntensionalNode, construction.getNode());
            }
            else if (isNodeGraphPredicateNode(tree)) {
                return pushedIntensionalNode;
            }

            // Other children: join with the pushed down intentional node
            return iqFactory.createNaryIQTree(
                    iqFactory.createInnerJoinNode(),
                    ImmutableList.of(tree, pushedIntensionalNode));
        }

        private IQTree pushDataAtomIntoConstructionTreeWithNodeInGraph(IQTree tree, IntensionalDataNode pushedIntensionalNode, ConstructionNode rootNode) {
            var intensionalVariables = pushedIntensionalNode.getVariables();
            var treeVariables = tree.getVariables();
            if (intensionalVariables.containsAll(treeVariables)) {
                var filterCondition = termFactory.getConjunction(
                        rootNode.getSubstitution().stream()
                                .map(e -> termFactory.getStrictEquality(e.getKey(), e.getValue())));
                return filterCondition
                        .<IQTree>map(c -> iqFactory.createUnaryIQTree(
                                iqFactory.createFilterNode(c),
                                pushedIntensionalNode))
                        .orElse(pushedIntensionalNode);
            }

            var commonTerm = pushedIntensionalNode.getProjectionAtom().getArguments().stream()
                    // Eliminates the named graph
                    .limit(3)
                    .filter(treeVariables::contains)
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


    /**
     * May have multiple node arguments or graph arguments due to aliases
     */
    private static class NodeInGraphContext {
        private final ImmutableSet<Variable> nodeArguments;
        private final ImmutableSet<? extends VariableOrGroundTerm> graphArguments;

        private NodeInGraphContext(ImmutableSet<Variable> nodeArguments, DataAtom<NodeInGraphPredicate> atom) {
            this.nodeArguments = nodeArguments;
            var firstGraphArgument = atom.getPredicate().getGraph(atom.getArguments());
            this.graphArguments = firstGraphArgument
                    .map(g -> nodeArguments.contains(g)
                            ? nodeArguments
                            : ImmutableSet.of(g))
                    .orElseGet(ImmutableSet::of);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof NodeInGraphContext)) return false;
            NodeInGraphContext that = (NodeInGraphContext) o;
            return Objects.equals(nodeArguments, that.nodeArguments)
                    && Objects.equals(graphArguments, that.graphArguments);
        }

        @Override
        public int hashCode() {
            return Objects.hash(nodeArguments, graphArguments);
        }

        public boolean isInDefaultGraph() {
            return graphArguments.isEmpty();
        }
    }

}
