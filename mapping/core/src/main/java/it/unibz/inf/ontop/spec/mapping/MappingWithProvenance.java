package it.unibz.inf.ontop.spec.mapping;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;

/**
 * TODO: find a better name!
 *
 * Composed of mapping assertions for which the provenance is known
 *
 * Immutable
 *
 */
public interface MappingWithProvenance {

    ImmutableSet<IntermediateQuery> getMappingAssertions();

    ImmutableMap<IntermediateQuery, PPMappingAssertionProvenance> getProvenanceMap();

    PPMappingAssertionProvenance getProvenance(IntermediateQuery mappingAssertion);

    /**
     * Conversion -> Provenance info is lost in this new data structure
     */
    Mapping toRegularMapping();

    ExecutorRegistry getExecutorRegistry();

    MappingMetadata getMetadata();
}
