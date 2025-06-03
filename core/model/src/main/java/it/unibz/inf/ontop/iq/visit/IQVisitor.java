package it.unibz.inf.ontop.iq.visit;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;

/**
 *  For composite IQ trees, the tree itself is passed as the first argument,
 *  which can be used to avoid creating unnecessary new objects in transformers
 *  (if the result is identical to the input).
 */

public interface IQVisitor<T> {

    T transformIntensionalData(IntensionalDataNode dataNode);
    T transformExtensionalData(ExtensionalDataNode dataNode);
    T transformEmpty(EmptyNode node);
    T transformTrue(TrueNode node);
    T transformNative(NativeNode nativeNode);
    T transformValues(ValuesNode valuesNode);

    T transformConstruction(UnaryIQTree tree, ConstructionNode rootNode, IQTree child);
    T transformAggregation(UnaryIQTree tree, AggregationNode aggregationNode, IQTree child);
    T transformFilter(UnaryIQTree tree, FilterNode rootNode, IQTree child);
    T transformFlatten(UnaryIQTree tree, FlattenNode rootNode, IQTree child);
    T transformDistinct(UnaryIQTree tree, DistinctNode rootNode, IQTree child);
    T transformSlice(UnaryIQTree tree, SliceNode sliceNode, IQTree child);
    T transformOrderBy(UnaryIQTree tree, OrderByNode rootNode, IQTree child);

    T transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild);

    T transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children);
    T transformUnion(NaryIQTree tree, UnionNode rootNode, ImmutableList<IQTree> children);

    default T transform(IQTree tree) {
        return tree.acceptVisitor(this);
    }
}
