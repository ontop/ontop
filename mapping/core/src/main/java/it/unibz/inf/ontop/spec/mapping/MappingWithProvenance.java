package it.unibz.inf.ontop.spec.mapping;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;

/**
 * TODO: find a better name!
 *
 * Composed of mapping assertions for which the provenance is known
 *
 * Immutable
 *
 * See ProvenanceMappingFactory for creating a new instance.
 *
 */
public interface MappingWithProvenance {

    ImmutableSet<IQ> getMappingAssertions();

    ImmutableMap<IQ, PPMappingAssertionProvenance> getProvenanceMap();

    PPMappingAssertionProvenance getProvenance(IQ mappingAssertion);

    /**
     * Conversion -> Provenance info is lost in this new data structure
     */
    MappingInTransformation toRegularMapping();
}
