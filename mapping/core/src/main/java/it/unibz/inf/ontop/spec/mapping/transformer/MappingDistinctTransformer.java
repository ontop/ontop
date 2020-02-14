package it.unibz.inf.ontop.spec.mapping.transformer;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;

public interface MappingDistinctTransformer {

    /**
     * TODO: rename the method (too low-level)
     */
    ImmutableList<MappingAssertion> addDistinct(ImmutableList<MappingAssertion> mapping);
}
