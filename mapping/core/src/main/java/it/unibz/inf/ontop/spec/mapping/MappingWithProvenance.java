package it.unibz.inf.ontop.spec.mapping;

import com.google.common.collect.ImmutableList;
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

    ImmutableList<MappingAssertion> getMappingAssertions();

    /**
     * Conversion -> Provenance info is lost in this new data structure
     */
    MappingInTransformation toRegularMapping();
}
