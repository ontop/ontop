package it.unibz.inf.ontop.spec.mapping.transformer;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.spec.mapping.Mapping;

public interface MappingMerger {

    Mapping merge(Mapping... mappings);

    Mapping merge(ImmutableSet<Mapping> mappings);
}
