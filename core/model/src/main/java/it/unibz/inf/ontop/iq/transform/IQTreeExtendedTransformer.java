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
public interface IQTreeExtendedTransformer {

    IQTree transformIntensionalData(IntensionalDataNode rootNode, VariableGenerator variableGenerator);
    IQTree transformExtensionalData(ExtensionalDataNode rootNode, VariableGenerator variableGenerator);
    IQTree transformEmpty(EmptyNode rootNode, VariableGenerator variableGenerator);
    IQTree transformTrue(TrueNode rootNode, VariableGenerator variableGenerator);
    IQTree transformValues(ValuesNode valuesNode, VariableGenerator variableGenerator);
    IQTree transformNonStandardLeafNode(LeafIQTree rootNode, VariableGenerator variableGenerator);

    IQTree transformConstruction(IQTree tree, ConstructionNode rootNode, IQTree child, VariableGenerator variableGenerator);
    IQTree transformAggregation(IQTree tree, AggregationNode aggregationNode, IQTree child, VariableGenerator variableGenerator);
    IQTree transformFilter(IQTree tree, FilterNode rootNode, IQTree child, VariableGenerator variableGenerator);
    IQTree transformDistinct(IQTree tree, DistinctNode rootNode, IQTree child, VariableGenerator variableGenerator);
    IQTree transformSlice(IQTree tree, SliceNode rootNode, IQTree child, VariableGenerator variableGenerator);
    IQTree transformOrderBy(IQTree tree, OrderByNode rootNode, IQTree child, VariableGenerator variableGenerator);
    IQTree transformNonStandardUnaryNode(IQTree tree, UnaryOperatorNode rootNode, IQTree child, VariableGenerator variableGenerator);

    IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild, VariableGenerator variableGenerator);
    IQTree transformNonStandardBinaryNonCommutativeNode(IQTree tree, BinaryNonCommutativeOperatorNode rootNode,
                                                        IQTree leftChild, IQTree rightChild, VariableGenerator variableGenerator);

    IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children, VariableGenerator variableGenerator);
    IQTree transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children, VariableGenerator variableGenerator);
    IQTree transformNonStandardNaryNode(IQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children, VariableGenerator variableGenerator);

    default IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return tree.acceptTransformer(this, variableGenerator);
    }
}
