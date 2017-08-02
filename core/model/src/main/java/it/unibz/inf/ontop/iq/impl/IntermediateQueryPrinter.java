package it.unibz.inf.ontop.iq.impl;

import it.unibz.inf.ontop.iq.IntermediateQuery;

/**
 * Prints the tree of an IntermediateQuery
 */
public interface IntermediateQueryPrinter {

    String stringify(IntermediateQuery query);
}
