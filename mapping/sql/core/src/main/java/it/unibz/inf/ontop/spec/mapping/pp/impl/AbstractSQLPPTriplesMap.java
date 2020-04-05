package it.unibz.inf.ontop.spec.mapping.pp.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.OBDASQLQuery;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.utils.IDGenerator;


public abstract class AbstractSQLPPTriplesMap implements SQLPPTriplesMap {

    private final ImmutableList<TargetAtom> targetAtoms;
    private final OBDASQLQuery sqlQuery;
    private final String id;

    protected AbstractSQLPPTriplesMap(ImmutableList<TargetAtom> targetAtoms, OBDASQLQuery sqlQuery, String id) {
        this.targetAtoms = targetAtoms;
        this.sqlQuery = sqlQuery;
        this.id = id;
    }

    @Override
    public ImmutableList<TargetAtom> getTargetAtoms() {
        return targetAtoms;
    }

    @Override
    public OBDASQLQuery getSourceQuery() {
        return sqlQuery;
    }

    @Override
    public String getId() {
        return id;
    }
}
