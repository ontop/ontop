package unibz.inf.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableSet;
import unibz.inf.ontop.model.Variable;
import unibz.inf.ontop.pivotalrepr.QueryNode;

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
