package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.answering.connection.JDBCStatementInitializer;
import it.unibz.inf.ontop.injection.OntopSystemSQLSettings;
import it.unibz.inf.ontop.answering.connection.pool.JDBCConnectionPool;


public class OntopSystemSQLModule extends OntopAbstractModule {

    private final OntopSystemSQLSettings settings;

    public OntopSystemSQLModule(OntopSystemSQLSettings settings) {
        super(settings);
        this.settings = settings;
    }

    @Override
    protected void configure() {
        bind(OntopSystemSQLSettings.class).toInstance(settings);
        bindFromSettings(JDBCConnectionPool.class);
        bindFromSettings(JDBCStatementInitializer.class);
    }
}
