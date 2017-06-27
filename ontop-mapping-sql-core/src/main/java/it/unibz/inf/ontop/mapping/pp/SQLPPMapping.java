package it.unibz.inf.ontop.mapping.pp;


/**
 * SQL-specific PreProcessedMapping
 */
public interface SQLPPMapping extends PreProcessedMapping<SQLPPTriplesMap> {

    /**
     * Retrieves the pre-processed mapping axiom given its id.
     */
    SQLPPTriplesMap getPPMappingAxiom(String axiomId);
}
