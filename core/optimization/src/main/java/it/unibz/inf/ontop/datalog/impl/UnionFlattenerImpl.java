package it.unibz.inf.ontop.datalog.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.UnionFlattener;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.visit.impl.DefaultRecursiveIQTreeVisitingTransformerWithVariableGenerator;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;

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

    /**
     * TODO: why a fix point?
     */
    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        IQTreeTransformer transformer = IQTreeTransformer.fixpoint(
                IQTreeTransformer.of(new Transformer(variableGenerator)));

        return transformer.transform(tree);
    }

    private class Transformer extends DefaultRecursiveIQTreeVisitingTransformerWithVariableGenerator {

        Transformer(VariableGenerator variableGenerator) {
            super(UnionFlattenerImpl.this.iqFactory, variableGenerator);
        }

        @Override
        public IQTree transformConstruction(UnaryIQTree tree, ConstructionNode rootCn, IQTree child) {

            IQTree transformedChild = transformChild(child);

            // if the child is a union, lift it
            var union = NaryIQTreeTools.UnionDecomposition.of(transformedChild);
            if (union.isPresent()) {
                return iqTreeTools.createUnionTree(rootCn.getVariables(),
                        NaryIQTreeTools.transformChildren(union.getChildren(),
                                c -> iqFactory.createUnaryIQTree(rootCn, c)));
            }
            // if the child is a construction node, merge it
            var construction = UnaryIQTreeDecomposition.of(transformedChild, ConstructionNode.class);
            if (construction.isPresent()) {
                return iqFactory.createUnaryIQTree(rootCn, transformedChild)
                        .normalizeForOptimization(variableGenerator);
            }
            return iqFactory.createUnaryIQTree(rootCn, transformedChild);
        }

        @Override
        // merge consecutive unions
        public IQTree transformUnion(NaryIQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {

            ImmutableList<IQTree> transformedChildren = NaryIQTreeTools.transformChildren(children, this::transformChild);

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
}