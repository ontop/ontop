package org.semanticweb.ontop.pivotalrepr.impl;

import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;

/**
 * Prints the tree of an IntermediateQuery
 */
public interface IntermediateQueryPrinter {

    String stringify(IntermediateQuery query);
}
