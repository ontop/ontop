package org.semanticweb.ontop.pivotalrepr;

/**
 * TODO: describe
 *
 * Can create only one intermediateQuery (to be used once).
 */
public interface IntermediateQueryBuilder {

    void init(QueryNode topQueryNode) throws IntermediateQueryBuilderException;

    void addChild(QueryNode parentNode, QueryNode child) throws IntermediateQueryBuilderException;

    IntermediateQuery build() throws IntermediateQueryBuilderException;
}
