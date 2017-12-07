package it.unibz.inf.ontop.temporal.mapping;

import it.unibz.inf.ontop.spec.mapping.pp.PPTriplesMapProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.impl.OntopNativeSQLPPTriplesMap;

import java.util.stream.Collectors;

/**
 * Created by elem on 03/08/2017.
 */
public class SQLPPTemporalTriplesMapProvenance implements PPTriplesMapProvenance {

    private final SQLPPTemporalTriplesMap triplesMap;

    public SQLPPTemporalTriplesMapProvenance(SQLPPTemporalTriplesMap triplesMap) {
        this.triplesMap = triplesMap;
    }

    @Override
    public String getProvenanceInfo() {
        String info = "id: " + triplesMap.getId();
        info += "\ntarget atoms: " + triplesMap.getTargetAtoms().stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));
        info += "\ntemporal interval: "+ triplesMap.getTemporalMappingInterval();
        info += "\nsource query: " + triplesMap.getSourceQuery();
        return info;
    }

    @Override
    public String toString() {
        return getProvenanceInfo();
    }

    public SQLPPTemporalTriplesMap getTriplesMap(){
        return triplesMap;
    }
}

