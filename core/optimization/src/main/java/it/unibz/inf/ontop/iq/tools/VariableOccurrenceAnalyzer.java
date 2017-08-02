package it.unibz.inf.ontop.iq.tools;

import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.model.term.Variable;

/**
 * TODO: explain
 */
public interface VariableOccurrenceAnalyzer {

    boolean isVariableUsedSomewhereElse(IntermediateQuery query, QueryNode originNode, Variable variable);
}
