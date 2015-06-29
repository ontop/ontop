package org.semanticweb.ontop.pivotalrepr.impl;


import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.QueryNode;

/**
 * TODO: explain
 *
 */
public class BasicQueryTreePrinter implements IntermediateQueryPrinter {

    private static final String TAB_STR = "   ";

    @Override
    public String stringify(IntermediateQuery query) {
        return stringifySubTree(query, query.getRootConstructionNode(), "");
    }


    /**
     * Recursive method.
     */
    private String stringifySubTree(IntermediateQuery query, QueryNode subTreeRoot, String rootOffsetString) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(rootOffsetString + subTreeRoot + "\n");

        for (QueryNode child : query.getCurrentSubNodesOf(subTreeRoot)) {
            strBuilder.append(stringifySubTree(query, child, rootOffsetString + TAB_STR));
        }
        return strBuilder.toString();
    }
}
