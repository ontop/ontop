package it.unibz.inf.ontop.mapping.trans;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.mapping.Mapping;

public interface MappingMerger {

    Mapping merge(Mapping... mappings);

    Mapping merge(ImmutableSet<Mapping> mappings);
}
