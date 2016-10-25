package it.unibz.inf.ontop.pivotalrepr;

import it.unibz.inf.ontop.model.Variable;

/**
 * TODO: explain
 */
public interface VariableOccurrenceAnalyzer {

    boolean isVariableUsedSomewhereElse(IntermediateQuery query, QueryNode originNode, Variable variable);
}
