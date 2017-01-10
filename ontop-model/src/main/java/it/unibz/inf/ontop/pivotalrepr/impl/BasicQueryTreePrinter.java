package it.unibz.inf.ontop.pivotalrepr.impl;


import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;

/**
 * TODO: explain
 *
 */
public class BasicQueryTreePrinter implements IntermediateQueryPrinter {

    private static final String TAB_STR = "   ";

    @Override
    public String stringify(IntermediateQuery query) {
        return  query.getProjectionAtom() + "\n"
                + stringifySubTree(query, query.getRootConstructionNode(), "");
    }


    /**
     * Recursive method.
     */
    private String stringifySubTree(IntermediateQuery query, QueryNode subTreeRoot, String rootOffsetString) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(rootOffsetString + subTreeRoot + "\n");

        for (QueryNode child : query.getChildren(subTreeRoot)) {
            strBuilder.append(stringifySubTree(query, child, rootOffsetString + TAB_STR));
        }
        return strBuilder.toString();
    }
}
