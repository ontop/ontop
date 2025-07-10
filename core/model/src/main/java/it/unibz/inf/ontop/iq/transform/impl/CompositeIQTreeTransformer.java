package it.unibz.inf.ontop.iq.transform.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.BinaryNonCommutativeOperatorNode;
import it.unibz.inf.ontop.iq.node.NaryOperatorNode;
import it.unibz.inf.ontop.iq.node.UnaryOperatorNode;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;

/**
 *
 *  (i) Applies a list of pre-transformers to the current tree,
 *  (ii) recursively applies itself on the children, and
 *  (iii) applies a list of post-transformers to the current tree.
 *
 *  Used only once, in ExplicitEqualityTransformerImpl
 */
public final class CompositeIQTreeTransformer implements IQTreeTransformer {

    private final ImmutableList<IQVisitor<IQTree>> postTransformers;
    private final DefaultIQTreeVisitingTransformer childTransformer;
    private final IntermediateQueryFactory iqFactory;

    public CompositeIQTreeTransformer(ImmutableList<IQVisitor<IQTree>> postTransformers,
                                      IntermediateQueryFactory iqFactory) {
        this.postTransformers = postTransformers;
        this.iqFactory = iqFactory;
        this.childTransformer = new ChildTransformer();
    }

    @Override
    public IQTree transform(IQTree initialTree) {
        //Non-final
        IQTree currentTree = initialTree.acceptVisitor(childTransformer);

        for (IQVisitor<IQTree> visitor : postTransformers) {
            currentTree = currentTree.acceptVisitor(visitor);
        }

        return currentTree;
    }

    private class ChildTransformer extends DefaultIQTreeVisitingTransformer {

        @Override
        protected final IQTree transformLeaf(LeafIQTree leaf) {
            return leaf;
        }

        @Override
        protected final IQTree transformUnaryNode(UnaryIQTree tree, UnaryOperatorNode rootNode, IQTree child) {
            return iqFactory.createUnaryIQTree(rootNode, CompositeIQTreeTransformer.this.transform(child));
        }

        @Override
        protected final IQTree transformNaryCommutativeNode(NaryIQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
            return iqFactory.createNaryIQTree(
                    rootNode,
                    NaryIQTreeTools.transformChildren(children, CompositeIQTreeTransformer.this::transform));
        }

        @Override
        protected final IQTree transformBinaryNonCommutativeNode(BinaryNonCommutativeIQTree tree, BinaryNonCommutativeOperatorNode rootNode, IQTree leftChild, IQTree rightChild) {
            return iqFactory.createBinaryNonCommutativeIQTree(
                    rootNode,
                    CompositeIQTreeTransformer.this.transform(leftChild),
                    CompositeIQTreeTransformer.this.transform(rightChild));
        }
    }
}
