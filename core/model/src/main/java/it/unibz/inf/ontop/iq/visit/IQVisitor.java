package it.unibz.inf.ontop.iq.visit;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;


public interface IQVisitor<T> {

    T transformIntensionalData(IntensionalDataNode dataNode);
    T transformExtensionalData(ExtensionalDataNode dataNode);
    T transformEmpty(EmptyNode node);
    T transformTrue(TrueNode node);
    T transformNative(NativeNode nativeNode);
    T transformValues(ValuesNode valuesNode);

    T transformConstruction(IQTree tree, ConstructionNode rootNode, IQTree child);
    T transformAggregation(IQTree tree, AggregationNode aggregationNode, IQTree child);
    T transformFilter(IQTree tree, FilterNode rootNode, IQTree child);
    T transformFlatten(IQTree tree, FlattenNode rootNode, IQTree child);
    T transformDistinct(IQTree tree, DistinctNode rootNode, IQTree child);
    T transformSlice(IQTree tree, SliceNode sliceNode, IQTree child);
    T transformOrderBy(IQTree tree, OrderByNode rootNode, IQTree child);

    T transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild);

    T transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children);
    T transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children);
}
