package it.unibz.inf.ontop.pivotalrepr.proposal.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.AncestryTracker;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;

import java.util.*;


public class AncestryTrackerImpl implements AncestryTracker {

    private final QueryNode originalDescendantNode;

    /**
     * From the parent to the oldest ancestor
     */
    private final ImmutableList<QueryNode> originalAncestors;

    // Original parent -> replacing child
    private final Map<QueryNode, QueryNode> childReplacement;
    private final Map<QueryNode, QueryNode> ancestorUpdate;
    private final Set<QueryNode> removedAncestors;

    public AncestryTrackerImpl(IntermediateQuery query, QueryNode descendantNode) {
        originalDescendantNode = descendantNode;
        originalAncestors = query.getAncestors(descendantNode);
        childReplacement = new HashMap<>();
        ancestorUpdate = new HashMap<>();
        removedAncestors = new HashSet<>();
    }

    @Override
    public void recordReplacementByChild(QueryNode ancestorNode, QueryNode replacingChildNode) {
        childReplacement.put(ancestorNode, replacingChildNode);
        ancestorUpdate.remove(ancestorNode);
    }

    @Override
    public void recordReplacement(QueryNode ancestorNode, QueryNode newNode) {
        if (childReplacement.containsKey(ancestorNode)) {
            throw new IllegalArgumentException("This ancestor node (" + ancestorNode
                    + ") has already been replaced by a child");
        }
        ancestorUpdate.put(ancestorNode, newNode);
    }

    @Override
    public void recordRemoval(QueryNode ancestorNode) {
        if (childReplacement.containsKey(ancestorNode)) {
            throw new IllegalArgumentException("This ancestor node (" + ancestorNode
                    + ") has already been replaced by a child");
        }
        else if (ancestorUpdate.containsKey(ancestorNode)) {
            throw new IllegalArgumentException("This ancestor node (" + ancestorNode
                    + ") has already been updated");
        }
        removedAncestors.add(ancestorNode);
    }

    @Override
    public void recordResults(NodeCentricOptimizationResults<QueryNode> propagationResults) {
        throw new RuntimeException("TODO: implement");
    }

    @Override
    public boolean hasChanged(QueryNode ancestorNode) {
        throw new RuntimeException("TODO: implement");
    }

    @Override
    public <N extends QueryNode> Optional<N> getCurrentNode(N ancestorNode) {
        throw new RuntimeException("TODO: implement");
    }
}
