package it.unibz.inf.ontop.iq.transform;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.node.*;


/**
 *  For composite IQ trees, the tree itself is passed as a first argument,
 *  in case the transformer does not transform the tree,
 *  so as to avoid creating unnecessary new objects.
 */
public interface IQTreeVisitingTransformer extends IQTreeTransformer {

    IQTree transformIntensionalData(IntensionalDataNode rootNode);
    IQTree transformExtensionalData(ExtensionalDataNode rootNode);
    IQTree transformEmpty(EmptyNode rootNode);
    IQTree transformTrue(TrueNode rootNode);
    IQTree transformValues(ValuesNode valuesNode);
    IQTree transformNonStandardLeafNode(LeafIQTree rootNode);

    IQTree transformConstruction(IQTree tree, ConstructionNode rootNode, IQTree child);
    IQTree transformAggregation(IQTree tree, AggregationNode aggregationNode, IQTree child);
    IQTree transformFilter(IQTree tree, FilterNode rootNode, IQTree child);
    IQTree transformDistinct(IQTree tree, DistinctNode rootNode, IQTree child);
    IQTree transformSlice(IQTree tree, SliceNode rootNode, IQTree child);
    IQTree transformOrderBy(IQTree tree, OrderByNode rootNode, IQTree child);
    IQTree transformNonStandardUnaryNode(IQTree tree, UnaryOperatorNode rootNode, IQTree child);

    IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild);
    IQTree transformNonStandardBinaryNonCommutativeNode(IQTree tree, BinaryNonCommutativeOperatorNode rootNode,
                                                        IQTree leftChild, IQTree rightChild);

    IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children);
    IQTree transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children);
    IQTree transformNonStandardNaryNode(IQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children);

    default IQTree transform(IQTree tree) {
        return tree.acceptTransformer(this);
    }
}
