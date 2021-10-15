package it.unibz.inf.ontop.iq.transform;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.utils.VariableGenerator;


/**
 *  For composite IQ trees, the tree itself is passed as a first argument,
 *  in case the transformer does not transform the tree,
 *  so as to avoid creating unnecessary new objects.
 */
public interface IQTreeExtendedTransformer<T> {

    IQTree transformIntensionalData(IntensionalDataNode rootNode, T context);
    IQTree transformExtensionalData(ExtensionalDataNode rootNode, T context);
    IQTree transformEmpty(EmptyNode rootNode, T context);
    IQTree transformTrue(TrueNode rootNode, T context);
    IQTree transformValues(ValuesNode valuesNode, T context);
    IQTree transformNonStandardLeafNode(LeafIQTree rootNode, T context);

    IQTree transformConstruction(IQTree tree, ConstructionNode rootNode, IQTree child, T context);
    IQTree transformAggregation(IQTree tree, AggregationNode aggregationNode, IQTree child, T context);
    IQTree transformFilter(IQTree tree, FilterNode rootNode, IQTree child, T context);
    IQTree transformDistinct(IQTree tree, DistinctNode rootNode, IQTree child, T context);
    IQTree transformSlice(IQTree tree, SliceNode rootNode, IQTree child, T context);
    IQTree transformOrderBy(IQTree tree, OrderByNode rootNode, IQTree child, T context);
    IQTree transformNonStandardUnaryNode(IQTree tree, UnaryOperatorNode rootNode, IQTree child, T context);

    IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild, T context);
    IQTree transformNonStandardBinaryNonCommutativeNode(IQTree tree, BinaryNonCommutativeOperatorNode rootNode,
                                                        IQTree leftChild, IQTree rightChild, T context);

    IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children, T context);
    IQTree transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children, T context);
    IQTree transformNonStandardNaryNode(IQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children, T context);

    default IQTree transform(IQTree tree, T context) {
        return tree.acceptTransformer(this, context);
    }
}
