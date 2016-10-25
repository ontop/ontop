package it.unibz.inf.ontop.pivotalrepr.impl;

import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.VariableOccurrenceAnalyzer;

/**
 * Naive implementation: goes over all the nodes of the query
 *
 * TODO: should ignore variable occurrences in other branches of an union
 */
public class NaiveVariableOccurrenceAnalyzerImpl implements VariableOccurrenceAnalyzer {
    @Override
    public boolean isVariableUsedSomewhereElse(IntermediateQuery query, QueryNode originNode, Variable variable) {
        return query.getNodesInTopDownOrder().stream()
                .filter(n -> n != originNode)
                .anyMatch(n -> n.getLocalVariables().contains(variable));
    }
}
