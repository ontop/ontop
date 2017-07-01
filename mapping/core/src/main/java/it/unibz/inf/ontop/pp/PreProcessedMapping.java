package it.unibz.inf.ontop.pp;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.mapping.MappingMetadata;

public interface PreProcessedMapping<T extends PreProcessedTriplesMap> {

    MappingMetadata getMetadata();

    ImmutableList<T> getTripleMaps();
}
