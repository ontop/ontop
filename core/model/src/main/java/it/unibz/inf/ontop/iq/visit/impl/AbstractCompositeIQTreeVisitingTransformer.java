package it.unibz.inf.ontop.iq.visit.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.*;

import java.util.function.Function;

public abstract class AbstractCompositeIQTreeVisitingTransformer extends AbstractIQVisitor<IQTree> {
    protected final IntermediateQueryFactory iqFactory;

    protected AbstractCompositeIQTreeVisitingTransformer(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }

    @Override
    public final IQTree transformIntensionalData(IntensionalDataNode leaf) {
        return leaf;
    }

    @Override
    public final IQTree transformExtensionalData(ExtensionalDataNode leaf) {
        return leaf;
    }

    @Override
    public final IQTree transformEmpty(EmptyNode leaf) {
        return leaf;
    }

    @Override
    public final IQTree transformTrue(TrueNode leaf) {
        return leaf;
    }

    @Override
    public final IQTree transformNative(NativeNode leaf) {
        return leaf;
    }

    @Override
    public final IQTree transformValues(ValuesNode leaf) {
        return leaf;
    }

    protected abstract IQTree postTransformation(IQTree tree);

    protected final IQTree transformNaryCommutativeNode(NaryIQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children,
                                                  Function<IQTree, IQTree> childTransformation) {

        ImmutableList<IQTree> newChildren = NaryIQTreeTools.transformChildren(children, childTransformation);
        return newChildren.equals(children)
                ? tree
                : postTransformation(iqFactory.createNaryIQTree(rootNode, newChildren));
    }

    protected final IQTree transformBinaryNonCommutativeNode(BinaryNonCommutativeIQTree tree, BinaryNonCommutativeOperatorNode rootNode,
                                                       IQTree leftChild, IQTree rightChild,
                                                       Function<IQTree, IQTree> childTransformation) {

        IQTree newLeftChild = childTransformation.apply(leftChild);
        IQTree newRightChild = childTransformation.apply(rightChild);
        return newLeftChild.equals(leftChild) && newRightChild.equals(rightChild)
                ? tree
                : postTransformation(iqFactory.createBinaryNonCommutativeIQTree(rootNode, newLeftChild, newRightChild));
    }

    protected final IQTree transformUnaryNode(IQTree tree, UnaryOperatorNode rootNode, IQTree child,
                                        Function<IQTree, IQTree> childTransformation) {

        IQTree newChild = childTransformation.apply(child);
        return newChild.equals(child)
                ? tree
                : postTransformation(iqFactory.createUnaryIQTree(rootNode, newChild));
    }
}
