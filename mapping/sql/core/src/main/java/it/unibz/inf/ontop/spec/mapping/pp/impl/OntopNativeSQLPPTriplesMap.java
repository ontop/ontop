package it.unibz.inf.ontop.spec.mapping.pp.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.spec.mapping.OBDASQLQuery;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.PPTriplesMapProvenance;

/**
 * When the input mapping document is in the native Ontop format
 */
public class OntopNativeSQLPPTriplesMap extends AbstractSQLPPTriplesMap {

    private final OntopNativeSQLPPTriplesMapProvenance triplesMapProvenance;

    public OntopNativeSQLPPTriplesMap(String id, OBDASQLQuery sqlQuery, ImmutableList<ImmutableFunctionalTerm> targetAtoms) {
        super(targetAtoms, sqlQuery, id);
        this.triplesMapProvenance = createProvenance(this);
    }

    public OntopNativeSQLPPTriplesMap(OBDASQLQuery sqlQuery, ImmutableList<ImmutableFunctionalTerm> targetAtoms) {
        super(targetAtoms, sqlQuery);
        this.triplesMapProvenance = createProvenance(this);
    }

    private static OntopNativeSQLPPTriplesMapProvenance createProvenance(OntopNativeSQLPPTriplesMap triplesMap) {
        return new OntopNativeSQLPPTriplesMapProvenance(triplesMap);
    }

    @Override
    public PPMappingAssertionProvenance getMappingAssertionProvenance(ImmutableFunctionalTerm targetAtom) {
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
    public SQLPPTriplesMap extractPPMappingAssertion(ImmutableFunctionalTerm atom) {
        return new OntopNativeSQLPPTriplesMap(getSourceQuery(), ImmutableList.of(atom));
    }

    @Override
    public SQLPPTriplesMap extractPPMappingAssertions(String newId, ImmutableList<ImmutableFunctionalTerm> atoms) {
        return new OntopNativeSQLPPTriplesMap(newId, getSourceQuery(), atoms);
    }
}
