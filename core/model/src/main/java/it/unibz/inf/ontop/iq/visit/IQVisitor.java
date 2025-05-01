package it.unibz.inf.ontop.iq.visit;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;


public interface IQVisitor<T> {

    T visitIntensionalData(IntensionalDataNode dataNode);
    T visitExtensionalData(ExtensionalDataNode dataNode);
    T visitEmpty(EmptyNode node);
    T visitTrue(TrueNode node);
    T visitNative(NativeNode nativeNode);
    T visitValues(ValuesNode valuesNode);

    T visitConstruction(IQTree tree, ConstructionNode rootNode, IQTree child);
    T visitAggregation(IQTree tree, AggregationNode aggregationNode, IQTree child);
    T visitFilter(IQTree tree, FilterNode rootNode, IQTree child);
    T visitFlatten(IQTree tree, FlattenNode rootNode, IQTree child);
    T visitDistinct(IQTree tree, DistinctNode rootNode, IQTree child);
    T visitSlice(IQTree tree, SliceNode sliceNode, IQTree child);
    T visitOrderBy(IQTree tree, OrderByNode rootNode, IQTree child);

    T visitLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild);

    T visitInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children);
    T visitUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children);
}
