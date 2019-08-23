package it.unibz.inf.ontop.iq.tools;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.iq.node.QueryNode;

import java.util.List;

/**
 * Collects all the variables found in the nodes.
 */
public class VariableCollector {

    public static ImmutableSet<Variable> collectVariables(List<QueryNode> nodes) {
        ImmutableSet.Builder<Variable> collectedVariableBuilder = ImmutableSet.builder();

        for (QueryNode node : nodes) {
            collectedVariableBuilder.addAll(node.getLocalVariables());
        }
        return collectedVariableBuilder.build();
    }

}
