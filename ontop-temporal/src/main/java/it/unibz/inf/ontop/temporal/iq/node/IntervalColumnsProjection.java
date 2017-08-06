package it.unibz.inf.ontop.temporal.iq.node;


import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

public interface IntervalColumnsProjection {
    Variable getBeginInclusive();
    Variable getEndInclusive();
    Variable getBegin();
    Variable getEnd();

    ImmutableSubstitution<ImmutableTerm> getSubstitution();
}
