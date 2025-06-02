package it.unibz.inf.ontop.iq.transform.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.BinaryNonCommutativeOperatorNode;
import it.unibz.inf.ontop.iq.node.NaryOperatorNode;
import it.unibz.inf.ontop.iq.node.UnaryOperatorNode;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

/**
 * Applies the transformer to the children
 */
public class ChildTransformer extends DefaultIQTreeVisitingTransformer {

    private final IntermediateQueryFactory iqFactory;
    private final IQTreeTransformer transformer;

    public ChildTransformer(IntermediateQueryFactory iqFactory, IQTreeTransformer transformer) {
        this.iqFactory = iqFactory;
        this.transformer = transformer;
    }

    @Override
    protected IQTree transformLeaf(LeafIQTree leaf){
        return leaf;
    }

    @Override
    protected IQTree transformUnaryNode(UnaryIQTree tree, UnaryOperatorNode rootNode, IQTree child) {
        return iqFactory.createUnaryIQTree(rootNode, transformer.transform(child));
    }

    @Override
    protected IQTree transformNaryCommutativeNode(NaryIQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
        return iqFactory.createNaryIQTree(
                rootNode,
                NaryIQTreeTools.transformChildren(children, transformer::transform));
    }

    @Override
    protected IQTree transformBinaryNonCommutativeNode(BinaryNonCommutativeIQTree tree, BinaryNonCommutativeOperatorNode rootNode, IQTree leftChild, IQTree rightChild) {
        return iqFactory.createBinaryNonCommutativeIQTree(
                rootNode,
                transformer.transform(leftChild),
                transformer.transform(rightChild));
    }
}
