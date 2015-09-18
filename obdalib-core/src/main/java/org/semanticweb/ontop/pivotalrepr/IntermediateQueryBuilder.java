package org.semanticweb.ontop.pivotalrepr;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

/**
 * TODO: describe
 *
 * Can create only one intermediateQuery (to be used once).
 */
public interface IntermediateQueryBuilder {

    void init(ConstructionNode rootConstructionNode) throws IntermediateQueryBuilderException;

    /**
     * When the parent is NOT a BinaryAsymetricOperatorNode
     */
    void addChild(QueryNode parentNode, QueryNode child) throws IntermediateQueryBuilderException;

    /**
     * When the parent is a BinaryAsymetricOperatorNode.
     */
    void addChild(QueryNode parentNode, QueryNode child, BinaryAsymmetricOperatorNode.ArgumentPosition position)
            throws IntermediateQueryBuilderException;

    /**
     * For commodity
     */
    void addChild(QueryNode parentNode, QueryNode child,
                  Optional<BinaryAsymmetricOperatorNode.ArgumentPosition> optionalPosition)
            throws IntermediateQueryBuilderException;


    IntermediateQuery build() throws IntermediateQueryBuilderException;

    ConstructionNode getRootConstructionNode() throws IntermediateQueryBuilderException;

    ImmutableList<QueryNode> getSubNodesOf(QueryNode node) throws IntermediateQueryBuilderException;
}
