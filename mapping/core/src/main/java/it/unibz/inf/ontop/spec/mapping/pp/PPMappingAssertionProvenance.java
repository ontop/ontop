package it.unibz.inf.ontop.spec.mapping.pp;


/**
 * Mapping assertion: ONE target atom.
 *
 * By contrast, a triples map may have several target atoms
 * for the same source query.
 *
 */
public interface PPMappingAssertionProvenance {

    /**
     * To be displayed in exceptions
     */
    String getProvenanceInfo();
}
