package it.unibz.inf.ontop.temporal.mapping.impl;

import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.temporal.mapping.SQLPPTemporalTriplesMap;

public class SQLTemporalMappingAssertionProvenance implements PPMappingAssertionProvenance {

    private final ImmutableFunctionalTerm targetAtom;
    private final SQLPPTemporalTriplesMap triplesMap;

    SQLTemporalMappingAssertionProvenance(ImmutableFunctionalTerm targetAtom, SQLPPTemporalTriplesMap triplesMap) {
        this.targetAtom = targetAtom;
        this.triplesMap = triplesMap;
    }

    @Override
    public String getProvenanceInfo() {
        String info = "id: " + triplesMap.getId();
        info += "\ntarget atom: " + targetAtom.toString();
        info += "\ninterval: " + triplesMap.getTemporalMappingInterval();
        info += "\nsource query: " + triplesMap.getSourceQuery();
        return info;
    }

    @Override
    public String toString() {
        return getProvenanceInfo();
    }
}
