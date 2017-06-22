package it.unibz.inf.ontop.model;


import it.unibz.inf.ontop.mapping.extraction.PreProcessedMapping;

/**
 * SQL-specific PreProcessedMapping
 */
public interface SQLPPMapping extends PreProcessedMapping<SQLPPTriplesMap> {

    /**
     * Retrieves the pre-processed mapping axiom given its id.
     */
    SQLPPTriplesMap getPPMappingAxiom(String axiomId);
}
