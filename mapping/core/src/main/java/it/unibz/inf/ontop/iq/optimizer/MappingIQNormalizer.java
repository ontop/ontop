package it.unibz.inf.ontop.iq.optimizer;

import it.unibz.inf.ontop.iq.IntermediateQuery;

public interface MappingIQNormalizer {
    IntermediateQuery normalize(IntermediateQuery query);
}
