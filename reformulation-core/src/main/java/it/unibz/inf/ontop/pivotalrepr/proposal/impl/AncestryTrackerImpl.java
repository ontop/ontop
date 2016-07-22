package it.unibz.inf.ontop.pivotalrepr.proposal.impl;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
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
    private final BiMap<QueryNode, QueryNode> ancestorUpdate;
    /**
     * Original ancestor -> closest ancestor
     * (this was holding at some point in time).
     *
     * Only filled when the ancestor node and its possible
     * (transitive) replacement are not in the query anymore
     *
     */
    private final Map<QueryNode, QueryNode> closestAncestorMap;

    /**
     * Original ancestor -> next sibling
     * (this was holding at some point in time).
     *
     * Only filled when the ancestor node and its possible
     * (transitive) replacement are not in the query anymore
     *
     */
    private final Map<QueryNode, QueryNode> nextSiblingMap;

    /**
     * Ancestors that have explicitly been declared as useless (e.g. empty) before being removed.
     */
    private final Set<QueryNode> uselessAncestors;

    public AncestryTrackerImpl(IntermediateQuery query, QueryNode descendantNode) {
        originalDescendantNode = descendantNode;
        originalAncestors = query.getAncestors(descendantNode);
        childReplacement = new HashMap<>();
        ancestorUpdate = HashBiMap.create();
        closestAncestorMap = new HashMap<>();
        uselessAncestors = new HashSet<>();
        nextSiblingMap = new HashMap<>();
    }

    @Override
    public void recordReplacementByChild(QueryNode ancestorNode, QueryNode replacingChildNode) {
        if (ancestorUpdate.containsKey(ancestorNode)) {
            throw new IllegalArgumentException("This ancestor node (" + ancestorNode
                    + ") has already been updated");
        }

        childReplacement.put(ancestorNode, replacingChildNode);

        // TODO: should we update the other maps?
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
    public void recordEmptinessDeclaration(QueryNode ancestorNode, Optional<QueryNode> optionalNextSibling,
                                           Optional<QueryNode> optionalClosestAncestor) {
        recordUselessnessDeclaration(ancestorNode, optionalNextSibling, optionalClosestAncestor);
    }

    private void recordUselessnessDeclaration(QueryNode focusNode,
                                              Optional<QueryNode> optionalNextSibling,
                                              Optional<QueryNode> optionalClosestAncestor) {
        if (childReplacement.containsKey(focusNode)) {
            throw new IllegalArgumentException("This ancestor node (" + focusNode
                    + ") has already been replaced by a child");
        }
        else if (ancestorUpdate.containsKey(focusNode)) {
            throw new IllegalArgumentException("This ancestor node (" + focusNode
                    + ") has already been updated");
        }
        uselessAncestors.add(focusNode);
        optionalNextSibling.ifPresent(s -> nextSiblingMap.put(focusNode, s));

        // NB: if the closest ancestor is not present, an EmptyQueryException is expected to be generated soon
        optionalClosestAncestor
                .ifPresent(a -> closestAncestorMap.put(focusNode, a));
    }

    @Override
    public void recordResults(QueryNode originalFocusNode, NodeCentricOptimizationResults<? extends QueryNode> propagationResults) {
        Optional<? extends QueryNode> optionalNewNode = propagationResults.getOptionalNewNode();
        /**
         * Standard replacement
         */
        if (optionalNewNode.isPresent()) {
            QueryNode newNode = optionalNewNode.get();
            if (newNode != originalFocusNode) {
                recordReplacement(originalFocusNode, newNode);
            }
        }
        /**
         * Replacement by a child
         */
        else if (propagationResults.getOptionalReplacingChild().isPresent()) {
            recordReplacementByChild(originalFocusNode, propagationResults.getOptionalReplacingChild().get());
        }
        /**
         * Otherwise, we interpret this removal as an uselessness declaration.
         */
        else {
            recordUselessnessDeclaration(originalFocusNode, propagationResults.getOptionalNextSibling(),
                    propagationResults.getOptionalClosestAncestor());
        }
    }

//    private QueryNode getRelatedOriginalNode(QueryNode node) {
//        if (originalAncestors.contains(node)) {
//            return node;
//        }
//        else if (ancestorUpdate.containsValue(node)) {
//            // Recursive
//            return getRelatedOriginalNode(ancestorUpdate.inverse().get(node));
//        }
//        else if (childReplacement.containsValue(node)) {
//            // Recursive
//            return getRelatedOriginalNode(childReplacement.inverse().get(node));
//        }
//        else {
//            throw new IllegalArgumentException("Unknown node, not appearing in the ancestry: " + node);
//        }
//
//    }

//    @Override
//    public boolean hasChanged(QueryNode ancestorNode) {
//        throw new RuntimeException("TODO: implement");
//    }

    @Override
    public <N extends QueryNode> Optional<N> getCurrentNode(N focusNode) {
        throw new RuntimeException("TODO: implement getCurrentNode()");
    }
}
