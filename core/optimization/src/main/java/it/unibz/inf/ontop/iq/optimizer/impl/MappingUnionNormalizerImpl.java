package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.iq.optimizer.MappingUnionNormalizer;
import it.unibz.inf.ontop.iq.transform.IQTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTransformer;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.stream.Stream;

public class MappingUnionNormalizerImpl implements MappingUnionNormalizer {

    private final IntermediateQueryFactory iqFactory;
    private final MappingUnionNormalizerTreeTransformer transformer;

    private class MappingUnionNormalizerTreeTransformer extends DefaultRecursiveIQTransformer implements IQTransformer {

        @Inject
        private MappingUnionNormalizerTreeTransformer(IntermediateQueryFactory iqFactory) {
            super(iqFactory);
        }

        @Override
        public IQTree transformConstruction(IQTree tree, ConstructionNode rootCn, IQTree child) {

            IQTree transformedChild = child.acceptTransformer(this);
            QueryNode transformedChildRoot = transformedChild.getRootNode();

            // If the child is a union, lift it
            if (transformedChildRoot instanceof UnionNode) {

                UnionNode updatedUnion = iqFactory.createUnionNode(rootCn.getVariables());
                return iqFactory.createNaryIQTree(
                        updatedUnion,
                        transformedChild.getChildren().stream()
                                .map(t -> iqFactory.createUnaryIQTree(rootCn, t))
                                .collect(ImmutableCollectors.toList())
                );
            }

            // if the child is a construction node, merge it
            if (transformedChildRoot instanceof ConstructionNode) {

                ImmutableSubstitution composition = rootCn.getSubstitution().composeWith(
                        ((ConstructionNode) transformedChildRoot).getSubstitution());

                return iqFactory.createUnaryIQTree(
                        iqFactory.createConstructionNode(
                                rootCn.getVariables(),
                                composition
                        ),
                        ((UnaryIQTree) transformedChild).getChild()
                );
            }
            return iqFactory.createUnaryIQTree(rootCn, transformedChild);
        }

        @Override
        // Merge consecutive unions
        public IQTree transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {

            Stream<IQTree> transformedChildren = children.stream().
                    map(t -> t.acceptTransformer(this));
            Stream<IQTree> unionChildren = transformedChildren.
                    filter(t -> t.getRootNode() instanceof UnionNode);
            Stream<IQTree> nonUnionChildren = transformedChildren.
                    filter(t -> !(t.getRootNode() instanceof UnionNode));

            return iqFactory.createNaryIQTree(
                    rootNode,
                    Stream.concat(
                            nonUnionChildren,
                            unionChildren.
                                    flatMap(t -> t.getChildren().stream())
                    ).collect(ImmutableCollectors.toList())
            );
        }
    }

    @Inject
    public MappingUnionNormalizerImpl(IntermediateQueryFactory iqFactory, MappingUnionNormalizerTreeTransformer transformer) {
        this.iqFactory = iqFactory;
        this.transformer = transformer;
    }

    @Override
    public IQ optimize(IQ query) {
        return iqFactory.createIQ(
                query.getProjectionAtom(),
                query.getTree().acceptTransformer(transformer)
        );
    }
}