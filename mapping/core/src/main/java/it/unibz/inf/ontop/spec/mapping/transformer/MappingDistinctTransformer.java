package it.unibz.inf.ontop.spec.mapping.transformer;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.spec.mapping.MappingAssertionIndex;

public interface MappingDistinctTransformer {

    /**
     * TODO: rename the method (too low-level)
     */
    ImmutableMap<MappingAssertionIndex, IQ> addDistinct(ImmutableMap<MappingAssertionIndex, IQ> mapping);
}
