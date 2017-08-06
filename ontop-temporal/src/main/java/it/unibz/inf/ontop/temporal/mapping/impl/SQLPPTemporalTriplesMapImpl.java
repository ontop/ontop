package it.unibz.inf.ontop.temporal.mapping.impl;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.spec.mapping.OBDASQLQuery;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.PPTriplesMapProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.AbstractSQLPPTriplesMap;
import it.unibz.inf.ontop.temporal.mapping.SQLPPTemporalTriplesMap;
import it.unibz.inf.ontop.temporal.mapping.SQLPPTemporalTriplesMapProvenance;
import it.unibz.inf.ontop.temporal.mapping.TemporalMappingInterval;

public class SQLPPTemporalTriplesMapImpl extends AbstractSQLPPTriplesMap implements SQLPPTemporalTriplesMap {

    private final SQLPPTemporalTriplesMapProvenance triplesMapProvenance;
    private TemporalMappingInterval temporalMappingInterval;

    public SQLPPTemporalTriplesMapImpl(String id, OBDASQLQuery sqlQuery, ImmutableList<ImmutableFunctionalTerm> targetAtoms, TemporalMappingInterval temporalMappingInterval) {
        super(targetAtoms, sqlQuery, id);
        this.triplesMapProvenance = createProvenance(this);
        setTemporalMappingInterval(temporalMappingInterval);
    }

    public SQLPPTemporalTriplesMapImpl(OBDASQLQuery sqlQuery, ImmutableList<ImmutableFunctionalTerm> targetAtoms) {
        super(targetAtoms, sqlQuery);
        this.triplesMapProvenance = createProvenance(this);
        setTemporalMappingInterval(temporalMappingInterval);
    }

    private static SQLPPTemporalTriplesMapProvenance createProvenance(SQLPPTemporalTriplesMapImpl triplesMap) {
        return new SQLPPTemporalTriplesMapProvenance(triplesMap);
    }

    @Override
    public void setTemporalMappingInterval(TemporalMappingInterval temporalMappingInterval) {
        this.temporalMappingInterval = temporalMappingInterval;

    }

    @Override
    public TemporalMappingInterval getTemporalMappingInterval() {
        return temporalMappingInterval;
    }

    @Override
    public String getId() {
        return super.getId();
    }

    @Override
    public SQLPPTriplesMap extractPPMappingAssertion(ImmutableFunctionalTerm atom) {
        return null;
    }

    @Override
    public SQLPPTriplesMap extractPPMappingAssertions(String newId, ImmutableList<ImmutableFunctionalTerm> atoms) {
        return null;
    }

    @Override
    public SQLPPTriplesMap clone() {
        return null;
    }

    @Override
    public PPMappingAssertionProvenance getMappingAssertionProvenance(ImmutableFunctionalTerm targetAtom) {
        return null;
    }

    @Override
    public PPTriplesMapProvenance getTriplesMapProvenance() {
        return null;
    }
}
