package it.unibz.inf.ontop.spec.mapping.pp.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.OBDASQLQuery;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.PPTriplesMapProvenance;

/**
 * When the input mapping document is in the native Ontop format
 */
public class OntopNativeSQLPPTriplesMap extends AbstractSQLPPTriplesMap {

    private final OntopNativeSQLPPTriplesMapProvenance triplesMapProvenance;

    public OntopNativeSQLPPTriplesMap(String id, OBDASQLQuery sqlQuery, ImmutableList<TargetAtom> targetAtoms) {
        super(targetAtoms, sqlQuery, id);
        this.triplesMapProvenance = createProvenance(this);
    }

    public OntopNativeSQLPPTriplesMap(OBDASQLQuery sqlQuery, ImmutableList<TargetAtom> targetAtoms) {
        super(targetAtoms, sqlQuery);
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
    public SQLPPTriplesMap extractPPMappingAssertion(TargetAtom atom) {
        return new OntopNativeSQLPPTriplesMap(getSourceQuery(), ImmutableList.of(atom));
    }

    @Override
    public SQLPPTriplesMap extractPPMappingAssertions(String newId, ImmutableList<TargetAtom> atoms) {
        return new OntopNativeSQLPPTriplesMap(newId, getSourceQuery(), atoms);
    }
}
