package it.unibz.inf.ontop.iq.tools.impl;

import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.tools.VariableOccurrenceAnalyzer;

/**
 * Naive implementation: goes over all the nodes of the query
 *
 * TODO: should ignore variable occurrences in other branches of an union
 */
public class NaiveVariableOccurrenceAnalyzerImpl implements VariableOccurrenceAnalyzer {
    @Override
    public boolean isVariableUsedSomewhereElse(IntermediateQuery query, QueryNode originNode, Variable variable) {
        if (query.getProjectionAtom().getArguments().contains(variable))
            return true;
        return query.getNodesInTopDownOrder().stream()
                .filter(n -> n != originNode)
                .anyMatch(n -> n.getLocalVariables().contains(variable));
    }
}
