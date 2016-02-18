package unibz.inf.ontop.pivotalrepr.impl;

import unibz.inf.ontop.pivotalrepr.IntermediateQuery;

/**
 * Prints the tree of an IntermediateQuery
 */
public interface IntermediateQueryPrinter {

    String stringify(IntermediateQuery query);
}
