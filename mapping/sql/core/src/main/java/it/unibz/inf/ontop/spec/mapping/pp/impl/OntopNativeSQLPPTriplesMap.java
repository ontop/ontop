package it.unibz.inf.ontop.spec.mapping.pp.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.TargetAtom;
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

    private final OntopNativeSQLPPTriplesMapProvenance triplesMapProvenance;

    private Optional<String> targetString = Optional.empty();

    public OntopNativeSQLPPTriplesMap(String id, OBDASQLQuery sqlQuery, @Nonnull String targetString, ImmutableList<TargetAtom> targetAtoms) {
        super(targetAtoms, sqlQuery, id);
        this.targetString = Optional.of(targetString);
        this.triplesMapProvenance = createProvenance(this);
    }

    public OntopNativeSQLPPTriplesMap(OBDASQLQuery sqlQuery, ImmutableList<TargetAtom> targetAtoms) {
        super(targetAtoms, sqlQuery);
        //this.targetString = null;
        this.triplesMapProvenance = createProvenance(this);
    }

    public OntopNativeSQLPPTriplesMap(String id, OBDASQLQuery sqlQuery, ImmutableList<TargetAtom> targetAtoms) {
        super(targetAtoms, sqlQuery, id);
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

    @Override
    public SQLPPTriplesMap extractPPMappingAssertion(TargetAtom atom) {
        return new OntopNativeSQLPPTriplesMap(getSourceQuery(), ImmutableList.of(atom));
    }

    // currently only used for R2RML conversion, so it is not necessary to keep the targetString
    @Override
    public SQLPPTriplesMap extractPPMappingAssertions(String newId, ImmutableList<TargetAtom> atoms) {
        return new OntopNativeSQLPPTriplesMap(newId, getSourceQuery(), atoms);
    }
}
