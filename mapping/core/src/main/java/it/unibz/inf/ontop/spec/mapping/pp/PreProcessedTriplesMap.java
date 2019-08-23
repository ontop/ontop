package it.unibz.inf.ontop.spec.mapping.pp;

import it.unibz.inf.ontop.model.atom.TargetAtom;

/**
 * TODO: describe
 */
public interface PreProcessedTriplesMap {

    PPMappingAssertionProvenance getMappingAssertionProvenance(TargetAtom targetAtom);

    PPTriplesMapProvenance getTriplesMapProvenance();
}
