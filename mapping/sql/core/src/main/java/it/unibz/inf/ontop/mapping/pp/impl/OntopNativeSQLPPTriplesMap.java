package it.unibz.inf.ontop.mapping.pp.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.OBDASQLQuery;
import it.unibz.inf.ontop.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;

/**
 * When the input mapping document is in the native Ontop format
 */
public class OntopNativeSQLPPTriplesMap extends AbstractSQLPPTriplesMap {

    private final OntopNativeSQLPPTriplesMapProvenance provenance;

    public OntopNativeSQLPPTriplesMap(String id, OBDASQLQuery sqlQuery, ImmutableList<ImmutableFunctionalTerm> targetAtoms) {
        super(targetAtoms, sqlQuery, id);
        this.provenance = createProvenance(this);
    }

    public OntopNativeSQLPPTriplesMap(OBDASQLQuery sqlQuery, ImmutableList<ImmutableFunctionalTerm> targetAtoms) {
        super(targetAtoms, sqlQuery);
        this.provenance = createProvenance(this);
    }

    /**
     * TODO: create it (same object for all the target atoms)
     */
    private static OntopNativeSQLPPTriplesMapProvenance createProvenance(OntopNativeSQLPPTriplesMap triplesMap) {
        return new OntopNativeSQLPPTriplesMapProvenance(triplesMap);
    }

    @Override
    public OntopNativeSQLPPTriplesMapProvenance getProvenance(ImmutableFunctionalTerm targetAtom) {
        return provenance;
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
