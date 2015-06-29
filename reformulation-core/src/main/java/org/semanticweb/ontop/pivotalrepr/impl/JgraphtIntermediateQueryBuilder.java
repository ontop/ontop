package org.semanticweb.ontop.pivotalrepr.impl;


import com.google.common.collect.ImmutableList;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgraph.graph.DefaultEdge;
import org.semanticweb.ontop.pivotalrepr.*;

public class JgraphtIntermediateQueryBuilder implements IntermediateQueryBuilder {

    private DirectedAcyclicGraph<QueryNode,DefaultEdge> queryDAG;
    private ConstructionNode rootConstructionNode;
    private boolean canEdit;
    private boolean hasBeenInitialized;

    /**
     * TODO: construct with Guice?
     */
    public JgraphtIntermediateQueryBuilder() {
        queryDAG = new DirectedAcyclicGraph<>(DefaultEdge.class);
        rootConstructionNode = null;
        canEdit = false;
        hasBeenInitialized = false;
    }

    @Override
    public void init(ConstructionNode rootConstructionNode){
        if (hasBeenInitialized)
            throw new IllegalArgumentException("Already initialized IntermediateQueryBuilder.");
        hasBeenInitialized = true;

        queryDAG.addVertex(rootConstructionNode);
        this.rootConstructionNode = rootConstructionNode;
        canEdit = true;
    }

    @Override
    public void addChild(QueryNode parentNode, QueryNode childNode) throws IntermediateQueryBuilderException {
        checkEditMode();

        if (!queryDAG.addVertex(childNode)) {
            throw new IntermediateQueryBuilderException("Node " + childNode + " already in the graph");
        }
        try {
            // child --> parent!!
            queryDAG.addDagEdge(childNode, parentNode);
        } catch (DirectedAcyclicGraph.CycleFoundException e) {
            throw new IntermediateQueryBuilderException(e.getMessage());
        }
    }

    @Override
    public IntermediateQuery build() throws IntermediateQueryBuilderException{
        checkInitialization();

        IntermediateQuery query;
        try {
            query = new IntermediateQueryImpl(new JgraphtQueryTreeComponent(queryDAG));
        } catch (IllegalTreeException e) {
            throw new IntermediateQueryBuilderException(e.getMessage());
        }
        canEdit = false;
        return query;
    }

    private void checkInitialization() throws IntermediateQueryBuilderException {
        if (!hasBeenInitialized)
            throw new IntermediateQueryBuilderException("Not initialized!");
    }

    private void checkEditMode() throws IntermediateQueryBuilderException {
        checkInitialization();

        if (!canEdit)
            throw new IllegalArgumentException("Cannot be edited anymore (the query has already been built).");
    }

    @Override
    public ConstructionNode getRootConstructionNode() throws IntermediateQueryBuilderException {
        checkInitialization();
        return rootConstructionNode;
    }

    @Override
    public ImmutableList<QueryNode> getSubNodesOf(QueryNode node)
            throws IntermediateQueryBuilderException {
        checkInitialization();
        return JgraphtQueryTreeComponent.getSubNodesOf(queryDAG, node);
    }
}
