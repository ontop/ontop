package it.unibz.inf.ontop.spec.mapping.impl;

import it.unibz.inf.ontop.spec.mapping.SQLMappingFactory;

public class SQLMappingFactoryImpl implements SQLMappingFactory {

    private static SQLMappingFactory INSTANCE = null;

    private SQLMappingFactoryImpl() {
    }

    public static SQLMappingFactory getInstance() {
        if (INSTANCE == null)
            INSTANCE = new SQLMappingFactoryImpl();
        return INSTANCE;
    }

    @Override
    public SQLQueryImpl getSQLQuery(String query) {
        return new SQLQueryImpl(query);
    }
}
