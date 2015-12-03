package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.pivotalrepr.*;

import java.util.List;

/**
 * Collects all the variables found in the nodes.
 */
public class VariableCollector {

    public static ImmutableSet<Variable> collectVariables(List<QueryNode> nodes) {
        ImmutableSet.Builder<Variable> collectedVariableBuilder = ImmutableSet.builder();

        for (QueryNode node : nodes) {
            collectedVariableBuilder.addAll(node.getVariables());
        }
        return collectedVariableBuilder.build();
    }

}
