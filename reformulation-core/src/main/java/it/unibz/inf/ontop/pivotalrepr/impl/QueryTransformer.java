package it.unibz.inf.ontop.pivotalrepr.impl;

import it.unibz.inf.ontop.model.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.tree.DefaultIntermediateQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public abstract class QueryTransformer {


    Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * TODO: explain
     */
    public IntermediateQuery transform(IntermediateQuery originalQuery,
                                       HomogeneousQueryNodeTransformer nodeTransformer)
            throws IntermediateQueryBuilderException, QueryNodeTransformationException, NotNeededNodeException {
        return convertToBuilderAndTransform(originalQuery, nodeTransformer).build();
    }

    public IntermediateQuery transform(IntermediateQuery originalQuery,
                                       Optional<HomogeneousQueryNodeTransformer> optionalNodeTransformer)
            throws IntermediateQueryBuilderException, QueryNodeTransformationException, NotNeededNodeException {
        if (optionalNodeTransformer.isPresent()){
            return transform(originalQuery, optionalNodeTransformer.get());
        }
        return IntermediateQueryUtils.convertToBuilder(originalQuery).build();
    }



    protected IntermediateQueryBuilder convertToBuilderAndTransform(IntermediateQuery originalQuery,
                                                                  HomogeneousQueryNodeTransformer nodeTransformer)
            throws NotNeededNodeException {
        return convertToBuilderAndTransform(originalQuery, nodeTransformer, originalQuery.getProjectionAtom());
    }

    /**
     * TODO: explain
     *
     * TODO: avoid the use of a recursive method. Use a stack instead.
     */
    protected IntermediateQueryBuilder convertToBuilderAndTransform(IntermediateQuery originalQuery,
                                                                  HomogeneousQueryNodeTransformer nodeTransformer,
                                                                  DistinctVariableOnlyDataAtom projectionAtom)
            throws NotNeededNodeException {
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(originalQuery.getMetadata());

        // Clone the original root node and apply the transformer if available.
        ConstructionNode originalRootNode = originalQuery.getRootConstructionNode();
        ConstructionNode newRootNode;
        newRootNode = originalRootNode.acceptNodeTransformer(nodeTransformer);
        queryBuilder.init(projectionAtom, newRootNode);
        return copyChildrenNodesToBuilder(originalQuery, queryBuilder, originalRootNode, newRootNode, nodeTransformer);
    }

    /**
     * TODO: replace this implementation by a non-recursive one.
     */
    private IntermediateQueryBuilder copyChildrenNodesToBuilder(final IntermediateQuery originalQuery,
                                                                IntermediateQueryBuilder queryBuilder,
                                                                final QueryNode originalParentNode,
                                                                final QueryNode newParentNode,
                                                                HomogeneousQueryNodeTransformer nodeTransformer)
            throws NotNeededNodeException {

        log.debug(originalParentNode.toString());
        log.debug(originalQuery.toString());

        for (QueryNode originalChildNode : originalQuery.getChildren(originalParentNode)) {

            QueryNode newChildNode;
                newChildNode = originalChildNode.acceptNodeTransformer(nodeTransformer);
            Optional<NonCommutativeOperatorNode.ArgumentPosition> optionalPosition = originalQuery.getOptionalPosition(originalParentNode, originalChildNode);
            queryBuilder.addChild(newParentNode, newChildNode, optionalPosition);

            // Recursive call
            queryBuilder = copyChildrenNodesToBuilder(originalQuery, queryBuilder, originalChildNode, newChildNode, nodeTransformer);
        }
        return queryBuilder;
    }

}
