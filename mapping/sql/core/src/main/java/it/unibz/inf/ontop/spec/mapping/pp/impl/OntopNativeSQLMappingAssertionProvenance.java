package it.unibz.inf.ontop.spec.mapping.pp.impl;


import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;

public class OntopNativeSQLMappingAssertionProvenance implements PPMappingAssertionProvenance {

    private final TargetAtom targetAtom;
    private final SQLPPTriplesMap triplesMap;

    OntopNativeSQLMappingAssertionProvenance(TargetAtom targetAtom, SQLPPTriplesMap triplesMap) {
        this.targetAtom = targetAtom;
        this.triplesMap = triplesMap;
    }

    @Override
    public String getProvenanceInfo() {
        String info = "id: " + triplesMap.getId();
        info += "\ntarget atom: " + targetAtom.toString();
        info += "\nsource query: " + triplesMap.getSourceQuery();
        return info;
    }

    @Override
    public String toString() {
        return getProvenanceInfo();
    }
}
