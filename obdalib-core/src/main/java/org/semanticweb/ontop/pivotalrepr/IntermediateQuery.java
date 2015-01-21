package org.semanticweb.ontop.pivotalrepr;

import com.google.common.collect.ImmutableList;

/**
 *
 */
public interface IntermediateQuery {


    public ImmutableList<QueryNode> getNodesInAntiTopologicalOrder();

    public ImmutableList<QueryNode> getCurrentSubNodesOf(QueryNode node);

}
