package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.DataAtom;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.VariableOrGroundTerm;
import org.semanticweb.ontop.model.impl.DataAtomImpl;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.VariableImpl;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.VariableDispatcher;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.BinaryAsymmetricOperatorNode.ArgumentPosition;

import java.util.List;

/**
 * TODO: explain
 */
public class IntermediateQueryUtils {

    private static final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();


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

        DataAtom headAtom = createTopProjectionAtom(firstDefinition.getRootConstructionNode().getProjectionAtom());

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
     *
     */
    private static DataAtom createTopProjectionAtom(DataAtom firstRuleProjectionAtom) {
        ImmutableList.Builder<VariableImpl> argBuilder = ImmutableList.builder();

        VariableDispatcher variableDispatcher = new VariableDispatcher();
        for (VariableOrGroundTerm argument : firstRuleProjectionAtom.getVariablesOrGroundTerms()) {
            /**
             * Variable: keeps it if not already used in the atom or rename it otherwise.
             */
            if (argument instanceof VariableImpl) {
                argBuilder.add(variableDispatcher.renameDataAtomVariable((VariableImpl) argument));
            }
            /**
             * Ground term: create a new variable.
             */
            else {
                argBuilder.add(variableDispatcher.generateNewVariable());
            }
        }

        return DATA_FACTORY.getDataAtom(firstRuleProjectionAtom.getPredicate(), argBuilder.build());
    }

    /**
     * TODO: explain
     */
    private static IntermediateQuery initMergedDefinition(DataAtom headAtom) throws QueryMergingException {
        ConstructionNode rootNode = new ConstructionNodeImpl(headAtom);
        UnionNode unionNode = new UnionNodeImpl();
        OrdinaryDataNode dataNode = new OrdinaryDataNodeImpl(headAtom);

        IntermediateQueryBuilder queryBuilder = new JgraphtIntermediateQueryBuilder();
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
            ConstructionNode rootConstructionNode = queryBuilder.getRootConstructionNode();
            DataAtom dataAtom = rootConstructionNode.getProjectionAtom();

            UnionNode unionNode = extractUnionNode(queryBuilder, rootConstructionNode);

            OrdinaryDataNode dataNode = new OrdinaryDataNodeImpl(dataAtom);
            queryBuilder.addChild(unionNode, dataNode);

            return queryBuilder.build();
        } catch (IntermediateQueryBuilderException e) {
            throw new QueryMergingException(e.getLocalizedMessage());
        }
    }

    private static UnionNode extractUnionNode(IntermediateQueryBuilder queryBuilder,
                                              ConstructionNode rootConstructionNode)
            throws IntermediateQueryBuilderException {
        ImmutableList<QueryNode> rootChildren = queryBuilder.getSubNodesOf(rootConstructionNode);
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
        IntermediateQueryBuilder queryBuilder = new JgraphtIntermediateQueryBuilder();

        // Clone of the original root node (because is mutable)
        ConstructionNode originalRootNode = originalQuery.getRootConstructionNode();
        ConstructionNode newRootNode = originalRootNode.clone();

        queryBuilder.init(newRootNode);


        return copyChildrenNodesToBuilder(originalQuery, queryBuilder, originalRootNode, newRootNode);
    }

    /**
     * TODO: replace this implementation by a non-recursive one.
     */
    private static IntermediateQueryBuilder copyChildrenNodesToBuilder(final IntermediateQuery originalQuery,
                                                                       IntermediateQueryBuilder queryBuilder,
                                                                       final QueryNode originalParentNode,
                                                                       final QueryNode newParentNode)
            throws IntermediateQueryBuilderException {
        for(QueryNode originalChildNode : originalQuery.getCurrentSubNodesOf(originalParentNode)) {

            // QueryNode are mutable
            QueryNode newChildNode = originalChildNode.clone();

            Optional<ArgumentPosition> optionalPosition = originalQuery.getOptionalPosition(originalParentNode, originalChildNode);
            queryBuilder.addChild(newParentNode, newChildNode, optionalPosition);

            // Recursive call
            queryBuilder = copyChildrenNodesToBuilder(originalQuery, queryBuilder, originalChildNode, newChildNode);
        }

        return queryBuilder;
    }

    /**
     * TODO: explain
     *
     */
    private static void checkDefinitionRootProjections(IntermediateQuery definition1, IntermediateQuery definition2)
            throws QueryMergingException {
        ConstructionNode root1 = definition1.getRootConstructionNode();
        ConstructionNode root2 = definition2.getRootConstructionNode();

        DataAtom headAtom1 = root1.getProjectionAtom();
        DataAtom headAtom2 = root2.getProjectionAtom();

        if (!headAtom1.hasSamePredicateAndArity(headAtom2)) {
            throw new QueryMergingException("Two definitions of different things: "
                    + headAtom1 + " != " + headAtom2);
        }

        /**
         * We do not check the query modifiers
         * TODO: should we?
         */
    }
}
