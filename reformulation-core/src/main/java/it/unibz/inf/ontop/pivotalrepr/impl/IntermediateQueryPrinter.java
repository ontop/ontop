package it.unibz.inf.ontop.pivotalrepr.impl;

import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;

/**
 * Prints the tree of an IntermediateQuery
 */
public interface IntermediateQueryPrinter {

    String stringify(IntermediateQuery query);
}
