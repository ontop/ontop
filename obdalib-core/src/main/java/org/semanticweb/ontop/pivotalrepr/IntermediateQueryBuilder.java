package org.semanticweb.ontop.pivotalrepr;

import com.google.common.collect.ImmutableList;

/**
 * TODO: describe
 *
 * Can create only one intermediateQuery (to be used once).
 */
public interface IntermediateQueryBuilder {

    void init(ConstructionNode rootConstructionNode) throws IntermediateQueryBuilderException;

    void addChild(QueryNode parentNode, QueryNode child) throws IntermediateQueryBuilderException;

    IntermediateQuery build() throws IntermediateQueryBuilderException;

    ConstructionNode getRootConstructionNode() throws IntermediateQueryBuilderException;

    ImmutableList<QueryNode> getSubNodesOf(QueryNode node) throws IntermediateQueryBuilderException;
}
