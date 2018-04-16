package it.unibz.inf.ontop.iq.tools;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;

public interface VariableDefinitionExtractor {
    ImmutableSet<ImmutableTerm> extract(Variable variable, IQTree tree);

    ImmutableSet<ImmutableTerm> extract(Variable variable, IQ query);
}
