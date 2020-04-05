package it.unibz.inf.ontop.spec.mapping.pp.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.OBDASQLQuery;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.PPTriplesMapProvenance;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * When the input mapping document is in the native Ontop format
 */
public class OntopNativeSQLPPTriplesMap extends AbstractSQLPPTriplesMap {

    private final Optional<String> targetString;
    private final OntopNativeSQLPPTriplesMapProvenance triplesMapProvenance;

    public OntopNativeSQLPPTriplesMap(String id, OBDASQLQuery sqlQuery, @Nonnull String targetString, ImmutableList<TargetAtom> targetAtoms) {
        super(targetAtoms, sqlQuery, id);
        this.targetString = Optional.of(targetString);
        this.triplesMapProvenance = createProvenance(this);
    }

    public OntopNativeSQLPPTriplesMap(String id, OBDASQLQuery sqlQuery, ImmutableList<TargetAtom> targetAtoms) {
        super(targetAtoms, sqlQuery, id);
        this.targetString = Optional.empty();
        this.triplesMapProvenance = createProvenance(this);
    }

    private static OntopNativeSQLPPTriplesMapProvenance createProvenance(OntopNativeSQLPPTriplesMap triplesMap) {
        return new OntopNativeSQLPPTriplesMapProvenance(triplesMap);
    }

    @Override
    public PPMappingAssertionProvenance getMappingAssertionProvenance(TargetAtom targetAtom) {
        return new OntopNativeSQLMappingAssertionProvenance(targetAtom, this);
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

    // currently only used for R2RML conversion, so it is not necessary to keep the targetString
    @Override
    public SQLPPTriplesMap extractPPMappingAssertions(String newId, ImmutableList<TargetAtom> atoms) {
        return new OntopNativeSQLPPTriplesMap(newId, getSourceQuery(), atoms);
    }
}
