package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.base.Optional;
import fj.P2;
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
    private static IntermediateQuery mergeDefinitions(IntermediateQuery accumulatedQuery,
                                                      IntermediateQuery newDefinition)
        throws QueryMergingException {
        checkDefinitionProjections(accumulatedQuery, newDefinition);

        /**
         * TODO: explain the strategy
         */
        IntermediateQuery mergedDefinition = prepareNewQueryForMerging(accumulatedQuery);
        mergedDefinition.mergeSubQuery(newDefinition);

        return mergedDefinition;
    }

    /**
     * TODO: explain
     *
     */
    private static IntermediateQuery prepareNewQueryForMerging(IntermediateQuery originalQuery)
            throws QueryMergingException {
        try {
            P2<IntermediateQueryBuilder, UnionNode> preparationPair = prepareUnion(originalQuery);
            IntermediateQueryBuilder newQueryBuilder = preparationPair._1();
            UnionNode unionNode = preparationPair._2();

            /**
             * TODO: explain
             */
            DataAtom headAtom = originalQuery.getRootProjectionNode().getHeadAtom();
            DataAtom intentionalDataAtom = detypeDataAtom(headAtom, newQueryBuilder);
            DataNode intentionalDataNode = new OrdinaryDataNodeImpl(intentionalDataAtom);

            newQueryBuilder.addChild(unionNode, intentionalDataNode);

            return newQueryBuilder.build();
        } catch (IntermediateQueryBuilderException e) {
            throw new QueryMergingException(e.getLocalizedMessage());
        }
    }

    /**
     * TODO: explain
     *
     * TODO: keep it?
     *
     *
     */
    private static DataAtom detypeDataAtom(DataAtom atom, IntermediateQueryBuilder newQueryBuilder) {
        // TODO: implement it;
        return null;
    }

    /**
     * TODO: explain and find a better name
     */
    private static P2<IntermediateQueryBuilder, UnionNode> prepareUnion(IntermediateQuery originalQuery) {
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

        if (!headAtom1.referSameAbstraction(headAtom2)) {
            throw new QueryMergingException("Two definitions of different things: "
                    + headAtom1 + " != " + headAtom2);
        }

        /**
         * We do not check the query modifiers
         * TODO: should we?
         */
    }
}
