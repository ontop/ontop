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
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;


/**
 * Tries to eliminate nodeInGraph atoms where the node is a variable
 */
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
        var newTree = tree.acceptTransformer(transformer)
                .normalizeForOptimization(query.getVariableGenerator());

        return newTree.equals(tree)
                ? query
                : iqFactory.createIQ(query.getProjectionAtom(), newTree);

    }

    /**
     * Only deals with nodeInGraph atoms where the node is a variable
     */
    protected static class Transformer extends DefaultRecursiveIQTreeVisitingTransformer {
        private final SubstitutionFactory substitutionFactory;
        private final TermFactory termFactory;
        private final FunctionSymbolFactory functionSymbolFactory;

        protected Transformer(CoreSingletons coreSingletons) {
            super(coreSingletons);
            this.substitutionFactory = coreSingletons.getSubstitutionFactory();
            this.termFactory = coreSingletons.getTermFactory();
            this.functionSymbolFactory = coreSingletons.getFunctionSymbolFactory();
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

        private ImmutableMultimap<NodeInGraphContext, IQTree> extractNodeInGraphContexts(ImmutableList<IQTree> children) {
            return children.stream()
                    .filter(c -> c.getRootNode() instanceof UnionNode)
                    .flatMap(c -> extractNodeInGraphContextsFromUnionNode(c).stream())
                    .collect(ImmutableCollectors.toMultimap());
        }

        private Optional<Map.Entry<NodeInGraphContext, IQTree>> extractNodeInGraphContextsFromUnionNode(
                IQTree unionTree) {
            return unionTree.getChildren().stream()
                    .flatMap(t -> extractContext(t)
                            .map(c -> Maps.immutableEntry(c, unionTree))
                            .stream())
                    .findAny();
        }

        private Optional<NodeInGraphContext> extractContext(IQTree childOfUnion) {
            var rootNode = childOfUnion.getRootNode();
            if (rootNode instanceof ConstructionNode) {
                return childOfUnion.getChildren().stream()
                        .findFirst()
                        .flatMap(this::extractNodeInGraphAtomWithVariable)
                        .map(a -> extractContext(a, (ConstructionNode) rootNode));
            }
            return extractNodeInGraphAtomWithVariable(childOfUnion)
                    .map(a -> new NodeInGraphContext(
                            ImmutableSet.of((Variable) a.getPredicate().getNode(a.getArguments())),
                            a));
        }

        private Optional<DataAtom<NodeInGraphPredicate>> extractNodeInGraphAtomWithVariable(IQTree tree) {
            return Optional.of(tree)
                    .filter(t -> t instanceof IntensionalDataNode)
                    .map(t -> ((IntensionalDataNode) t).getProjectionAtom())
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
                        && (((IntensionalDataNode) child).getProjectionAtom().getPredicate() instanceof NodeInGraphPredicate))
                    return pushDataAtomIntoConstructionTreeWithNodeInGraph(tree, pushedIntensionalNode, (ConstructionNode) rootNode);
            }
            else if ((rootNode instanceof IntensionalDataNode)
                    && (((IntensionalDataNode)rootNode).getProjectionAtom().getPredicate() instanceof NodeInGraphPredicate)) {

                return iqFactory.createUnaryIQTree(
                        createNotLiteralFilterNode(((IntensionalDataNode) rootNode).getProjectionAtom().getTerm(0)),
                        pushedIntensionalNode);
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
                        .map(c -> (IQTree) iqFactory.createUnaryIQTree(
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

            var constructionTree = iqFactory.createUnaryIQTree(
                    iqFactory.createConstructionNode(Sets.union(intensionalVariables, treeVariables).immutableCopy(),
                            newSubstitution),
                    pushedIntensionalNode);

            return iqFactory.createUnaryIQTree(
                    createNotLiteralFilterNode(commonTerm),
                    constructionTree);
        }

        /**
         * Makes sure the node term is never a literal
         */
        private FilterNode createNotLiteralFilterNode(VariableOrGroundTerm nodeTerm) {
            var condition = termFactory.getDBNot(
                    termFactory.getRDF2DBBooleanFunctionalTerm(
                            termFactory.getImmutableFunctionalTerm(
                                    functionSymbolFactory.getSPARQLFunctionSymbol(SPARQL.IS_LITERAL, 1)
                                            .orElseThrow(),
                                    nodeTerm)));
            return iqFactory.createFilterNode(condition);
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
