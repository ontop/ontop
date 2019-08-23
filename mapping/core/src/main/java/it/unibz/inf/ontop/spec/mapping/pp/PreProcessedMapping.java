package it.unibz.inf.ontop.spec.mapping.pp;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.spec.mapping.MappingMetadata;

public interface PreProcessedMapping<T extends PreProcessedTriplesMap> {

    MappingMetadata getMetadata();

    ImmutableList<T> getTripleMaps();
}
