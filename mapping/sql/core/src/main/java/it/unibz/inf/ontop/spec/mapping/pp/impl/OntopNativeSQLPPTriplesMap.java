package it.unibz.inf.ontop.spec.mapping.pp.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQuery;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.PPTriplesMapProvenance;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * When the input mapping document is in the native Ontop format
 */
public class OntopNativeSQLPPTriplesMap extends AbstractSQLPPTriplesMap {

    private final Optional<String> targetString;
    private final OntopNativeSQLPPTriplesMapProvenance triplesMapProvenance = new OntopNativeSQLPPTriplesMapProvenance();

    public OntopNativeSQLPPTriplesMap(String id, SQLPPSourceQuery sqlQuery, @Nonnull String targetString, ImmutableList<TargetAtom> targetAtoms) {
        super(targetAtoms, sqlQuery, id);
        this.targetString = Optional.of(targetString);
    }

    public OntopNativeSQLPPTriplesMap(String id, SQLPPSourceQuery sqlQuery, ImmutableList<TargetAtom> targetAtoms) {
        super(targetAtoms, sqlQuery, id);
        this.targetString = Optional.empty();
    }

    private class OntopNativeSQLPPTriplesMapProvenance implements PPTriplesMapProvenance {
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
        return targetString;
    }
}
