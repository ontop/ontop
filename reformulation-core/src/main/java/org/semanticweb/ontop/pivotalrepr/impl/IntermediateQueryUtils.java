package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.model.impl.VariableImpl;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.NeutralSubstitution;
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

        IntermediateQuery firstDefinition = predicateDefinitions.get(0);
        if (predicateDefinitions.size() == 1) {
            return Optional.of(firstDefinition);
        }

        PureDataAtom headAtom = firstDefinition.getRootProjectionNode().getHeadAtom();

        // Non final definition
        IntermediateQuery mergedDefinition = null;

        for (IntermediateQuery definition : predicateDefinitions) {
            if (mergedDefinition == null) {
                mergedDefinition = initMergedDefinition(headAtom);
            } else {
                mergedDefinition = prepareForMergingNewDefinition(mergedDefinition);
            }

            checkDefinitionRootProjections(mergedDefinition, definition);
            mergedDefinition.mergeSubQuery(definition);
        }
        return Optional.of(mergedDefinition);
    }

    /**
     * TODO: explain
     */
    private static IntermediateQuery initMergedDefinition(PureDataAtom headAtom) throws QueryMergingException {
        ProjectionNode rootNode = new ProjectionNodeImpl(headAtom, new NeutralSubstitution());
        UnionNode unionNode = new UnionNodeImpl();
        OrdinaryDataNode dataNode = new OrdinaryDataNodeImpl(headAtom);

        IntermediateQueryBuilder queryBuilder = new IntermediateQueryBuilderImpl();
        try {
            queryBuilder.init(rootNode);
            queryBuilder.addChild(rootNode, unionNode);
            queryBuilder.addChild(unionNode, dataNode);
            return queryBuilder.build();
        } catch (IntermediateQueryBuilderException e) {
            throw new QueryMergingException(e.getLocalizedMessage());
        }
    }

    /**
     * TODO: explain
     */
    private static IntermediateQuery prepareForMergingNewDefinition(IntermediateQuery mergedDefinition)
            throws QueryMergingException {
        try {
            IntermediateQueryBuilder queryBuilder = convertToBuilder(mergedDefinition);
            ProjectionNode rootProjectionNode = queryBuilder.getRootProjectionNode();
            PureDataAtom dataAtom = rootProjectionNode.getHeadAtom();

            UnionNode unionNode = extractUnionNode(queryBuilder, rootProjectionNode);

            OrdinaryDataNode dataNode = new OrdinaryDataNodeImpl(dataAtom);
            queryBuilder.addChild(unionNode, dataNode);

            return queryBuilder.build();
        } catch (IntermediateQueryBuilderException e) {
            throw new QueryMergingException(e.getLocalizedMessage());
        }
    }

    private static UnionNode extractUnionNode(IntermediateQueryBuilder queryBuilder,
                                              ProjectionNode rootProjectionNode)
            throws IntermediateQueryBuilderException {
        ImmutableList<QueryNode> rootChildren = queryBuilder.getSubNodesOf(rootProjectionNode);
        if (rootChildren.size() != 1) {
            throw new RuntimeException("BUG: merged definition query without a unique UNION" +
                    " below the root projection node");
        }
        QueryNode rootChild = rootChildren.get(0);

        if (!(rootChild instanceof UnionNode)) {
            throw new RuntimeException("BUG: the root child of a merged definition is not a UNION");
        }
        return (UnionNode) rootChild;
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


        return copyChildrenNodesToBuilder(originalQuery, queryBuilder, newRootNode);
    }

    /**
     * TODO: replace this implementation by a non-recursive one.
     */
    private static IntermediateQueryBuilder copyChildrenNodesToBuilder(final IntermediateQuery originalQuery,
                                                                       IntermediateQueryBuilder queryBuilder,
                                                                       final QueryNode parentNode)
            throws IntermediateQueryBuilderException {
        for(QueryNode originalChildNode : originalQuery.getCurrentSubNodesOf(parentNode)) {

            // QueryNode are mutable
            QueryNode newChildNode = originalChildNode.clone();

            queryBuilder.addChild(parentNode, newChildNode);

            // Recursive call
            queryBuilder = copyChildrenNodesToBuilder(originalQuery, queryBuilder, newChildNode);
        }

        return queryBuilder;
    }

    /**
     * TODO: explain
     *
     */
    private static void checkDefinitionRootProjections(IntermediateQuery definition1, IntermediateQuery definition2)
            throws QueryMergingException {
        ProjectionNode root1 = definition1.getRootProjectionNode();
        ProjectionNode root2 = definition2.getRootProjectionNode();

        PureDataAtom headAtom1 = root1.getHeadAtom();
        PureDataAtom headAtom2 = root2.getHeadAtom();

        if (!headAtom1.isEquivalent(headAtom2)) {
            throw new QueryMergingException("Two definitions of different things: "
                    + headAtom1 + " != " + headAtom2);
        }

        /**
         * We do not check the query modifiers
         * TODO: should we?
         */
    }
}
