package it.unibz.inf.ontop.model.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.mapping.extraction.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.OBDASQLQuery;
import it.unibz.inf.ontop.model.SQLPPTriplesMap;

/**
 * When the input mapping document is in the native Ontop format
 */
public class OntopNativeSQLPPTriplesMap extends AbstractSQLPPTriplesMap {

    private final PPMappingAssertionProvenance provenance;

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
    private static PPMappingAssertionProvenance createProvenance(OntopNativeSQLPPTriplesMap triplesMap) {
        return null;
    }

    @Override
    public PPMappingAssertionProvenance getProvenance(ImmutableFunctionalTerm targetAtom) {
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
