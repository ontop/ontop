package org.semanticweb.ontop.pivotalrepr;

import com.google.common.collect.ImmutableList;

/**
 * TODO: describe
 *
 * Can create only one intermediateQuery (to be used once).
 */
public interface IntermediateQueryBuilder {

    void init(ProjectionNode rootProjectionNode) throws IntermediateQueryBuilderException;

    void addChild(QueryNode parentNode, QueryNode child) throws IntermediateQueryBuilderException;

    IntermediateQuery build() throws IntermediateQueryBuilderException;

    ProjectionNode getRootProjectionNode() throws IntermediateQueryBuilderException;

    ImmutableList<QueryNode> getSubNodesOf(QueryNode node) throws IntermediateQueryBuilderException;
}
