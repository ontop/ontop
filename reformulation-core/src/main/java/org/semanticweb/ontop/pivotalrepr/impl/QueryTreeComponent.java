package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableList;
import org.jgraph.graph.DefaultEdge;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.ConstructionNode;
import org.semanticweb.ontop.pivotalrepr.QueryNode;

import java.util.List;

/**
 * Mutable component used for internal implementations of IntermediateQuery.
 */
public interface QueryTreeComponent {

    ImmutableList<QueryNode> getCurrentSubNodesOf(QueryNode node);

    ConstructionNode getRootConstructionNode() throws IllegalTreeException;

    ImmutableList<QueryNode> getNodesInBottomUpOrder() throws IllegalTreeException;

    boolean contains(QueryNode node);

    void replaceNode(QueryNode previousNode, QueryNode replacingNode);

    void removeDependency(DefaultEdge dependencyEdge);

    void addSubTree(IntermediateQuery subQuery, QueryNode parentNode);

    /**
     * Makes sure all the children nodes in the DAG are the listed ones.
     */
    void setChildrenNodes(QueryNode parentNode, List<QueryNode> allChildrenNodes) throws IllegalTreeException;

    ImmutableList<QueryNode> getSubTreeNodesInTopDownOrder(QueryNode currentNode);
}
