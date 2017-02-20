package it.unibz.inf.ontop.pivotalrepr.impl;

import it.unibz.inf.ontop.model.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.pivotalrepr.*;

import java.util.Optional;

/**
 * Implementation based on a QueryNodeTransformer
 */
public abstract class NodeBasedQueryTransformer
        implements QueryTransformer {

    private final HomogeneousQueryNodeTransformer nodeTransformer;

    protected NodeBasedQueryTransformer(HomogeneousQueryNodeTransformer nodeTransformer) {
        this.nodeTransformer = nodeTransformer;
    }


    @Override
    public IntermediateQuery transform(IntermediateQuery originalQuery) {
        DistinctVariableOnlyDataAtom transformedProjectionDataAtom =
                transformProjectionAtom(originalQuery.getProjectionAtom());
        IntermediateQueryBuilder builder = convertToBuilderAndTransform(originalQuery, nodeTransformer,
                transformedProjectionDataAtom);
        return builder.build();
    }

    protected abstract DistinctVariableOnlyDataAtom transformProjectionAtom(DistinctVariableOnlyDataAtom projectionAtom);


    /**
     * TODO: explain
     * <p>
     * TODO: avoid the use of a recursive method. Use a stack instead.
     */
    private IntermediateQueryBuilder convertToBuilderAndTransform(IntermediateQuery originalQuery,
                                                                  HomogeneousQueryNodeTransformer nodeTransformer,
                                                                  DistinctVariableOnlyDataAtom transformedProjectionAtom) {
        IntermediateQueryBuilder queryBuilder = originalQuery.newBuilder();

        // Clone the original root node and apply the transformer if available.
        ConstructionNode originalRootNode = originalQuery.getRootConstructionNode();
        ConstructionNode newRootNode;
        newRootNode = originalRootNode.acceptNodeTransformer(nodeTransformer);
        queryBuilder.init(transformedProjectionAtom, newRootNode);
        return copyChildrenNodesToBuilder(originalQuery, queryBuilder, originalRootNode, newRootNode, nodeTransformer);
    }

    /**
     * TODO: replace this implementation by a non-recursive one.
     */
    private IntermediateQueryBuilder copyChildrenNodesToBuilder(final IntermediateQuery originalQuery,
                                                                IntermediateQueryBuilder queryBuilder,
                                                                final QueryNode originalParentNode,
                                                                final QueryNode newParentNode,
                                                                HomogeneousQueryNodeTransformer nodeTransformer) {

        for (QueryNode originalChildNode : originalQuery.getChildren(originalParentNode)) {

            QueryNode newChildNode;
            newChildNode = originalChildNode.acceptNodeTransformer(nodeTransformer);
            Optional<BinaryOrderedOperatorNode.ArgumentPosition> optionalPosition = originalQuery.getOptionalPosition(originalParentNode, originalChildNode);
            queryBuilder.addChild(newParentNode, newChildNode, optionalPosition);

            // Recursive call
            queryBuilder = copyChildrenNodesToBuilder(originalQuery, queryBuilder, originalChildNode, newChildNode, nodeTransformer);
        }
        return queryBuilder;
    }

}
