package it.unibz.inf.ontop.model.term.impl;

import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.NonFunctionalTerm;

public abstract class AbstractNonFunctionalTerm implements NonFunctionalTerm {

    @Override
    public ImmutableTerm simplify(boolean isInConstructionNodeInOptimizationPhase) {
        return this;
    }
}
