package it.unibz.inf.ontop.temporal.mapping.impl;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
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
    private Predicate provenanceTemporalPredicate;

    public SQLPPTemporalTriplesMapImpl(String id, OBDASQLQuery sqlQuery, ImmutableList<ImmutableFunctionalTerm> targetAtoms, TemporalMappingInterval temporalMappingInterval) {
        super(targetAtoms, sqlQuery, id);
        this.triplesMapProvenance = createProvenance(this);
        setTemporalMappingInterval(temporalMappingInterval);
        provenanceTemporalPredicate = null;
    }

    public SQLPPTemporalTriplesMapImpl(OBDASQLQuery sqlQuery, ImmutableList<ImmutableFunctionalTerm> targetAtoms) {
        super(targetAtoms, sqlQuery);
        this.triplesMapProvenance = createProvenance(this);
        setTemporalMappingInterval(temporalMappingInterval);
    }

    public SQLPPTemporalTriplesMapImpl(SQLPPTemporalTriplesMap oldTriplesMap, ImmutableList<ImmutableFunctionalTerm> targetAtoms, Predicate provenanceTemporalPredicate) {
        super(targetAtoms, oldTriplesMap.getSourceQuery());
        this.triplesMapProvenance = (SQLPPTemporalTriplesMapProvenance) oldTriplesMap.getTriplesMapProvenance();
        setTemporalMappingInterval(oldTriplesMap.getTemporalMappingInterval());
        this.provenanceTemporalPredicate = provenanceTemporalPredicate;
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
    public Predicate getProvenanceTemporalPredicate() {
        return provenanceTemporalPredicate;
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
        return new SQLTemporalMappingAssertionProvenance(targetAtom, this);
    }

    @Override
    public PPTriplesMapProvenance getTriplesMapProvenance() {
        return triplesMapProvenance;
    }
}
