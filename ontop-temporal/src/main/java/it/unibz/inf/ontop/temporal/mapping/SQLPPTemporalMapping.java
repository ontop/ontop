package it.unibz.inf.ontop.temporal.mapping;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.mapping.extraction.PreProcessedMapping;
import it.unibz.inf.ontop.model.SQLPPMappingAxiom;

public interface SQLPPTemporalMapping extends PreProcessedMapping {

    /**
     * Retrieves the pre-processed mapping axiom given its id.
     */
    SQLPPTemporalMappingAxiom getPPTemporalMappingAxiom(String axiomId);

    /**
     * Returns all the pre-processed mapping axioms
     */
    ImmutableList<SQLPPTemporalMappingAxiom> getPPTemporalMappingAxioms();

}
