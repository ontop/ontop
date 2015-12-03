package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.pivotalrepr.*;

import java.util.List;

/**
 * Collects all the variables found in the nodes.
 *
 * TODO: refactor it NOT AS A QueryNodeVisitor
 * but by calling queryNode.getVariables()
 */
public class VariableCollector {

    public static ImmutableSet<Variable> collectVariables(IntermediateQuery query) {
        return collectVariables(query.getNodesInBottomUpOrder());
    }

    public static ImmutableSet<Variable> collectVariables(List<QueryNode> nodes) {
        ImmutableSet.Builder<Variable> collectedVariableBuilder = ImmutableSet.builder();

        for (QueryNode node : nodes) {
            collectedVariableBuilder.addAll(node.getVariables());
        }
        return collectedVariableBuilder.build();
    }

}
