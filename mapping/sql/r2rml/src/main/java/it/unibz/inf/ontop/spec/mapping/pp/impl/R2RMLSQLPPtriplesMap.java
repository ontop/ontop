package it.unibz.inf.ontop.spec.mapping.pp.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQuery;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.PPTriplesMapProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;

import java.util.Optional;
import java.util.stream.Collectors;

public class R2RMLSQLPPtriplesMap extends AbstractSQLPPTriplesMap {

    private final R2RMLSQLPPtriplesMapProvenance triplesMapProvenance = new R2RMLSQLPPtriplesMapProvenance();

    public R2RMLSQLPPtriplesMap(String id, SQLPPSourceQuery sqlQuery, ImmutableList<TargetAtom> targetAtoms) {
        super(targetAtoms, sqlQuery, id);
    }

    private class R2RMLSQLPPtriplesMapProvenance implements PPTriplesMapProvenance {
        @Override
        public String getProvenanceInfo() {
            return "id: " + getId() +
                    "\ntarget atoms: " + getTargetAtoms().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(", ")) +
                    "\nsource query: " + getSourceQuery();
        }
        @Override
        public String toString() {
            return getProvenanceInfo();
        }
    }


    @Override
    public PPMappingAssertionProvenance getMappingAssertionProvenance(TargetAtom targetAtom) {
        return new PPMappingAssertionProvenance() {
            @Override
            public String getProvenanceInfo() {
                return "id: " + getId() +
                        "\ntarget atom: " + targetAtom.toString() +
                        "\nsource query: " + getSourceQuery();
            }
            @Override
            public String toString() {
                return getProvenanceInfo();
            }
        };
    }

    @Override
    public PPTriplesMapProvenance getTriplesMapProvenance() {
        return triplesMapProvenance;
    }

    @Override
    public String toString() {
        return triplesMapProvenance.getProvenanceInfo();
    }

    @Override
    public Optional<String> getOptionalTargetString() {
        return Optional.empty();
    }
}