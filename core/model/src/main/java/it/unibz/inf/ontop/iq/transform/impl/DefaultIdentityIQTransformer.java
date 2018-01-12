package it.unibz.inf.ontop.iq.transform.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTransformer;

/**
 * To be extended by overloading the methods of interest.
 */
public class DefaultIdentityIQTransformer implements IQTransformer {

    private final IntermediateQueryFactory iqFactory;

    public DefaultIdentityIQTransformer(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }

    @Override
    public IQTree transformIntensionalData(IntensionalDataNode dataNode) {
        return dataNode;
    }

    @Override
    public IQTree transformExtensionalData(ExtensionalDataNode dataNode) {
        return dataNode;
    }

    @Override
    public IQTree transformEmpty(EmptyNode node) {
        return node;
    }

    @Override
    public IQTree transformTrue(TrueNode node) {
        return node;
    }

    @Override
    public IQTree transformNonStandardLeafNode(LeafIQTree leafNode) {
        return leafNode;
    }

    @Override
    public IQTree transformConstruction(ConstructionNode rootNode, IQTree child) {
        return iqFactory.createUnaryIQTree(rootNode, child);
    }

    @Override
    public IQTree transformFilter(FilterNode rootNode, IQTree child) {
        return iqFactory.createUnaryIQTree(rootNode, child);
    }

    @Override
    public IQTree transformNonStandardUnaryNode(UnaryOperatorNode rootNode, IQTree child) {
        return iqFactory.createUnaryIQTree(rootNode, child);
    }

    @Override
    public IQTree transformLeftJoin(LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        return iqFactory.createBinaryNonCommutativeIQTree(rootNode, leftChild, rightChild);
    }

    @Override
    public IQTree transformNonStandardBinaryNonCommutativeNode(BinaryNonCommutativeOperatorNode rootNode,
                                                               IQTree leftChild, IQTree rightChild) {
        return iqFactory.createBinaryNonCommutativeIQTree(rootNode, leftChild, rightChild);
    }

    @Override
    public IQTree transformInnerJoin(InnerJoinNode rootNode, ImmutableList<IQTree> children) {
        return iqFactory.createNaryIQTree(rootNode, children);
    }

    @Override
    public IQTree transformUnion(UnionNode rootNode, ImmutableList<IQTree> children) {
        return iqFactory.createNaryIQTree(rootNode, children);
    }

    @Override
    public IQTree transformNonStandardNaryNode(NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
        return iqFactory.createNaryIQTree(rootNode, children);
    }
}
