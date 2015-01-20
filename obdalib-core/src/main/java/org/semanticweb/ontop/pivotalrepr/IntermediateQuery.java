package org.semanticweb.ontop.pivotalrepr;

import com.google.common.collect.ImmutableList;

/**
 *
 */
public interface IntermediateQuery {


    public ImmutableList<NodeView> getNodesInAntiTopologicalOrder();

}
