package it.unibz.inf.ontop.spec.mapping.pp;


/**
 * SQL-specific PreProcessedMapping
 */
public interface SQLPPMapping extends PreProcessedMapping<SQLPPTriplesMap> {

    /**
     * Retrieves the pre-processed mapping axiom given its id.
     */
    SQLPPTriplesMap getPPMappingAxiom(String axiomId);
}
