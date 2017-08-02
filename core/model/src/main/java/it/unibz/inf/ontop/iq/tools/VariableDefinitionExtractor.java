package it.unibz.inf.ontop.iq.tools;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;

public interface VariableDefinitionExtractor {
    ImmutableSet<ImmutableTerm> extract(Variable variable, QueryNode node, IntermediateQuery query);

    ImmutableSet<ImmutableTerm> extract(Variable variable, IntermediateQuery query);
}
