package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.base.Optional;
import org.semanticweb.ontop.pivotalrepr.*;

import java.util.List;

/**
 * TODO: explain
 */
public class IntermediateQueryUtils {

    /**
     * TODO: describe
     */
    public static Optional<IntermediateQuery> mergeDefinitions(List<IntermediateQuery> predicateDefinitions)
            throws QueryMergingException {
        if (predicateDefinitions.isEmpty())
            return Optional.absent();

        // Non final definition
        IntermediateQuery mergedDefinition = null;

        for (IntermediateQuery definition : predicateDefinitions) {
            if (mergedDefinition == null) {
                mergedDefinition = definition;
                continue;
            }
            mergedDefinition = mergeDefinitions(mergedDefinition, definition);
        }
        return Optional.of(mergedDefinition);
    }

    /**
     * TODO: explain
     * TODO: find a better name
     *
     * TODO: avoid the use of a recursive method. Use a stack instead.
     *
     */
    public static IntermediateQueryBuilder convertToBuilder(IntermediateQuery originalQuery)
            throws IntermediateQueryBuilderException {
        IntermediateQueryBuilder queryBuilder = new IntermediateQueryBuilderImpl();

        // Clone of the original root node (because is mutable)
        ProjectionNode newRootNode = originalQuery.getRootProjectionNode().clone();

        queryBuilder.init(newRootNode);


        return copyNodesToBuilder(originalQuery, queryBuilder, newRootNode);
    }

    /**
     * TODO: replace this implementation by a non-recursive one.
     */
    private static IntermediateQueryBuilder copyNodesToBuilder(final IntermediateQuery originalQuery,
                                                               IntermediateQueryBuilder queryBuilder,
                                                               final QueryNode parentNode)
            throws IntermediateQueryBuilderException {
        for(QueryNode originalChildNode : originalQuery.getCurrentSubNodesOf(parentNode)) {

            // QueryNode are mutable
            QueryNode newChildNode = originalChildNode.clone();

            queryBuilder.addChild(parentNode, newChildNode);

            // Recursive call
            queryBuilder = copyNodesToBuilder(originalQuery, queryBuilder, newChildNode);
        }

        return queryBuilder;
    }

    /**
     * TODO: implement it
     */
    private static IntermediateQuery mergeDefinitions(IntermediateQuery definition1, IntermediateQuery definition2)
        throws QueryMergingException {
        checkDefinitionProjections(definition1, definition2);

        // TODO: continue
        return null;
    }

    /**
     * TODO: explain
     *
     */
    private static void checkDefinitionProjections(IntermediateQuery definition1, IntermediateQuery definition2)
            throws QueryMergingException {
        ProjectionNode root1 = definition1.getRootProjectionNode();
        ProjectionNode root2 = definition2.getRootProjectionNode();

        DataAtom headAtom1 = root1.getHeadAtom();
        DataAtom headAtom2 = root2.getHeadAtom();

        if (!headAtom1.shareReferenceToTheSameAbstraction(headAtom2)) {
            throw new QueryMergingException("Two definitions of different things: "
                    + headAtom1 + " != " + headAtom2);
        }

        /**
         * We do not check the query modifiers
         * TODO: should we?
         */
    }
}
