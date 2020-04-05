package it.unibz.inf.ontop.spec.mapping.pp.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQuery;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;


public abstract class AbstractSQLPPTriplesMap implements SQLPPTriplesMap {

    private final ImmutableList<TargetAtom> targetAtoms;
    private final SQLPPSourceQuery sqlQuery;
    private final String id;

    protected AbstractSQLPPTriplesMap(ImmutableList<TargetAtom> targetAtoms, SQLPPSourceQuery sqlQuery, String id) {
        this.targetAtoms = targetAtoms;
        this.sqlQuery = sqlQuery;
        this.id = id;
    }

    @Override
    public ImmutableList<TargetAtom> getTargetAtoms() {
        return targetAtoms;
    }

    @Override
    public SQLPPSourceQuery getSourceQuery() {
        return sqlQuery;
    }

    @Override
    public String getId() {
        return id;
    }
}
