package it.unibz.inf.ontop.datalog.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.UnionFlattener;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;
import static it.unibz.inf.ontop.iq.impl.IQTreeTools.NaryIQTreeDecomposition;

/**
 * Lifts unions above projections, until a fixed point is reached.
 * Also merges consecutive unions and projections.
 * <p>
 * This normalization may be needed for datalog-based mapping optimizers.
 */
public class UnionFlattenerImpl implements UnionFlattener {

    private final IntermediateQueryFactory iqFactory;
    private final IQTreeTools iqTreeTools;

    @Inject
    private UnionFlattenerImpl(IntermediateQueryFactory iqFactory, IQTreeTools iqTreeTools) {
        this.iqFactory = iqFactory;
        this.iqTreeTools = iqTreeTools;
    }

    private class TreeTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        private final VariableGenerator variableGenerator;

        private TreeTransformer(VariableGenerator variableGenerator) {
            super(UnionFlattenerImpl.this.iqFactory);
            this.variableGenerator = variableGenerator;
        }

        @Override
        public IQTree transformConstruction(UnaryIQTree tree, ConstructionNode rootCn, IQTree child) {

            IQTree transformedChild = transformChild(child);

            // if the child is a union, lift it
            var union = NaryIQTreeDecomposition.of(transformedChild, UnionNode.class);
            if (union.isPresent()) {
                return iqTreeTools.createUnionTree(rootCn.getVariables(),
                            iqTreeTools.createUnaryOperatorChildren(rootCn, union.getChildren()));
            }
            // if the child is a construction node, merge it
            var construction = UnaryIQTreeDecomposition.of(transformedChild, ConstructionNode.class);
            if (construction.isPresent()) {
                return rootCn.normalizeForOptimization(transformedChild, variableGenerator, iqFactory.createIQTreeCache());
            }
            return iqFactory.createUnaryIQTree(rootCn, transformedChild);
        }

        @Override
        // merge consecutive unions
        public IQTree transformUnion(NaryIQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {

            ImmutableList<IQTree> transformedChildren = transformChildren(children);

            ImmutableList<IQTree> unionGrandChildren = transformedChildren.stream()
                    .filter(t -> t.getRootNode() instanceof UnionNode)
                    .flatMap(t -> t.getChildren().stream())
                    .collect(ImmutableCollectors.toList());

            return iqFactory.createNaryIQTree(
                    rootNode,
                    Stream.concat(
                            transformedChildren.stream()
                                    .filter(t -> !(t.getRootNode() instanceof UnionNode)),
                            unionGrandChildren.stream())
                            .collect(ImmutableCollectors.toList()));
        }
    }

    /**
     * TODO: why a fix point?
     */
    @Override
    public IQ optimize(IQ query) {
        return iqFactory.createIQ(query.getProjectionAtom(), optimize(query.getTree(), query.getVariableGenerator()));
    }

    @Override
    public IQTree optimize(IQTree tree, VariableGenerator variableGenerator) {
        TreeTransformer treeTransformer = new TreeTransformer(variableGenerator);
        IQTree prev;
        do {
            prev = tree;
            tree = tree.acceptTransformer(treeTransformer);
        } while (!prev.equals(tree));
        return prev;
    }
}