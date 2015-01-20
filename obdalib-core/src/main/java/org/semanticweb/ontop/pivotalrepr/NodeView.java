package org.semanticweb.ontop.pivotalrepr;

import com.google.common.collect.ImmutableList;

/**
 * TODO: find a better name
 *
 * Strongly connected to the DAG.
 *
 * Only use them iteratively in a given ordered list.
 */
public interface NodeView {

    /**
     *
     */
    public QueryNode getCurrentNode();

    public ImmutableList<QueryNode> getSubNodes();

    //TODO: apply update proposal given by the optimizer

}
