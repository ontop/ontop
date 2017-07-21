package it.unibz.inf.ontop.mapping.pp;


import it.unibz.inf.ontop.pp.PreProcessedMapping;

/**
 * SQL-specific PreProcessedMapping
 */
public interface SQLPPMapping extends PreProcessedMapping<SQLPPTriplesMap> {

    /**
     * Retrieves the pre-processed mapping axiom given its id.
     */
    SQLPPTriplesMap getPPMappingAxiom(String axiomId);
}
