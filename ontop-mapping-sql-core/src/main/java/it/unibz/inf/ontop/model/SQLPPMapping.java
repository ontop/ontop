package it.unibz.inf.ontop.model;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.mapping.MappingMetadata;
import it.unibz.inf.ontop.mapping.extraction.PreProcessedMapping;

/**
 * SQL-specific PreProcessedMapping
 */
public interface SQLPPMapping extends PreProcessedMapping {

    /**
     * Retrieves the pre-processed mapping axiom given its id.
     */
    SQLPPMappingAxiom getPPMappingAxiom(String axiomId);

    /**
     * Returns all the pre-processed mapping axioms
     */
    ImmutableList<SQLPPMappingAxiom> getPPMappingAxioms();
}
