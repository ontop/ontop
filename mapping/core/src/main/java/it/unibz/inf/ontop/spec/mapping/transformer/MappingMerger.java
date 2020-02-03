package it.unibz.inf.ontop.spec.mapping.transformer;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.MappingInTransformation;

public interface MappingMerger {

    MappingInTransformation merge(MappingInTransformation... mappings);

    MappingInTransformation merge(ImmutableSet<MappingInTransformation> mappings);
}
