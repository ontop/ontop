package org.semanticweb.ontop.pivotalrepr.transformer;

import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.QueryNodeTransformationException;

/**
 * TODO: explain
 */
public class StopPropagationException extends QueryNodeTransformationException {
    private final QueryNode stoppingNode;

    public StopPropagationException(QueryNode stoppingNode) {
        this.stoppingNode = stoppingNode;
    }

    public QueryNode getStoppingNode() {
        return stoppingNode;
    }
}
