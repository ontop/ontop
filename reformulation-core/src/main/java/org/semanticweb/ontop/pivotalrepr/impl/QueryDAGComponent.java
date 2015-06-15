package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableList;
import org.jgraph.graph.DefaultEdge;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.ProjectionNode;
import org.semanticweb.ontop.pivotalrepr.QueryNode;

import java.util.List;

/**
 * Mutable component used for internal implementations of IntermediateQuery.
 */
public interface QueryDAGComponent {

    ImmutableList<QueryNode> getCurrentSubNodesOf(QueryNode node);

    ProjectionNode getRootProjectionNode() throws IllegalDAGException;

    ImmutableList<QueryNode> getNodesInBottomUpOrder() throws IllegalDAGException;

    boolean contains(QueryNode node);

    void replaceNode(QueryNode previousNode, QueryNode replacingNode);

    void removeDependency(DefaultEdge dependencyEdge);

    void addSubTree(IntermediateQuery subQuery, QueryNode parentNode);

    /**
     * Makes sure all the children nodes in the DAG are the listed ones.
     */
    void setChildrenNodes(QueryNode parentNode, List<QueryNode> allChildrenNodes) throws IllegalDAGException;

    ImmutableList<QueryNode> getSubTreeNodesInTopDownOrder(QueryNode currentNode);
}
