package it.unibz.inf.ontop.spec.mapping.transformer;

import it.unibz.inf.ontop.constraints.ImmutableCQContainmentCheck;
import it.unibz.inf.ontop.iq.IQ;

public interface MappingCQCOptimizer {
    IQ optimize(ImmutableCQContainmentCheck cqContainmentCheck, IQ query);
}
