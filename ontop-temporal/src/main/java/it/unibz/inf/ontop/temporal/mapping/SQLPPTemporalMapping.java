package it.unibz.inf.ontop.temporal.mapping;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.spec.mapping.pp.PreProcessedMapping;

public interface SQLPPTemporalMapping extends PreProcessedMapping {

    /**
     * Retrieves the pre-processed mapping axiom given its id.
     */
    SQLPPTemporalTriplesMap getPPTemporalMappingAxiom(String axiomId);

    /**
     * Returns all the pre-processed mapping axioms
     */
    ImmutableList<SQLPPTemporalTriplesMap> getPPTemporalMappingAxioms();

}
