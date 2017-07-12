package it.unibz.inf.ontop.mapping;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.pp.PPTriplesMapProvenance;

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

    ImmutableMap<IntermediateQuery, PPTriplesMapProvenance> getProvenanceMap();

    PPTriplesMapProvenance getProvenance(IntermediateQuery mappingAssertion);

    /**
     * Conversion -> Provenance info is lost in this new data structure
     */
    Mapping toMapping();
}
