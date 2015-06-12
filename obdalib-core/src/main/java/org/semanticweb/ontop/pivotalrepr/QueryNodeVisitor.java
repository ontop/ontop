package org.semanticweb.ontop.pivotalrepr;

/**
 * Visits QueryNodes without having effect on them and the intermediate query.
 *
 * If you want to make optimization proposals to the nodes/query, use an Optimizer instead.
 *
 */
public interface QueryNodeVisitor {

    void visit(OrdinaryDataNode ordinaryDataNode);

    void visit(TableNode tableNode);

    void visit(InnerJoinNode innerJoinNode);

    void visit(SimpleFilterNode simpleFilterNode);

    void visit(ProjectionNode projectionNode);

    void visit(UnionNode unionNode);
}
