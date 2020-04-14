package it.unibz.inf.ontop.spec.mapping.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQueryFactory;

public class SQLPPSourceQueryFactoryImpl implements SQLPPSourceQueryFactory {

    @Inject
    private SQLPPSourceQueryFactoryImpl() {
    }

    @Override
    public SQLPPSourceQueryImpl createSourceQuery(String query) {
        return new SQLPPSourceQueryImpl(query);
    }
}
