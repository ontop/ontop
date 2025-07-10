package it.unibz.inf.ontop.iq.visit.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.*;

import java.util.function.Function;

public abstract class AbstractCompositeIQTreeVisitingTransformer extends AbstractIQVisitor<IQTree> {
    protected final IntermediateQueryFactory iqFactory;
    protected final Function<IQTree, IQTree> postTransformer;

    protected AbstractCompositeIQTreeVisitingTransformer(IntermediateQueryFactory iqFactory, Function<IQTree, IQTree> postTransformer) {
        this.iqFactory = iqFactory;
        this.postTransformer = postTransformer;
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

    protected final IQTree transformNaryCommutativeNode(NaryIQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children,
                                                  Function<IQTree, IQTree> childTransformation) {
        return withTransformedChildren(tree, NaryIQTreeTools.transformChildren(children, childTransformation));
    }

    protected final IQTree withTransformedChildren(NaryIQTree tree, ImmutableList<IQTree> transformedChildren) {
        return transformedChildren.equals(tree.getChildren())
                ? tree
                : postTransformer.apply(iqFactory.createNaryIQTree(tree.getRootNode(), transformedChildren));
    }

    protected final IQTree transformBinaryNonCommutativeNode(BinaryNonCommutativeIQTree tree, BinaryNonCommutativeOperatorNode rootNode,
                                                       IQTree leftChild, IQTree rightChild,
                                                       Function<IQTree, IQTree> childTransformation) {
        return withTransformedChildren(tree, childTransformation.apply(leftChild), childTransformation.apply(rightChild));
    }

    protected final IQTree withTransformedChildren(BinaryNonCommutativeIQTree tree, IQTree transformedLeftChild, IQTree transformedRightChild) {
        return transformedLeftChild.equals(tree.getLeftChild()) && transformedRightChild.equals(tree.getRightChild())
                ? tree
                : postTransformer.apply(iqFactory.createBinaryNonCommutativeIQTree(tree.getRootNode(), transformedLeftChild, transformedRightChild));
    }

    protected final IQTree transformUnaryNode(UnaryIQTree tree, UnaryOperatorNode rootNode, IQTree child,
                                        Function<IQTree, IQTree> childTransformation) {
        return withTransformedChild(tree, childTransformation.apply(child));
    }

    protected final IQTree withTransformedChild(UnaryIQTree tree, IQTree transformedChild) {
        return transformedChild.equals(tree.getChild())
                ? tree
                : postTransformer.apply(iqFactory.createUnaryIQTree(tree.getRootNode(), transformedChild));
    }
}
