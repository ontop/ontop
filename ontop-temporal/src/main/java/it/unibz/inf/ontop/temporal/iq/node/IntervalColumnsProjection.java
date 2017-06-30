package it.unibz.inf.ontop.temporal.iq.node;

import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.Variable;

public interface IntervalColumnsProjection {
    Variable getBeginInclusive();
    Variable getEndInclusive();
    Variable getBegin();
    Variable getEnd();

    ImmutableSubstitution<ImmutableTerm> getSubstitution();
}
