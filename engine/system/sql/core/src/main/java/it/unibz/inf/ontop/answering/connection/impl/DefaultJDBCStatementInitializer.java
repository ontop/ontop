package it.unibz.inf.ontop.answering.connection.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.answering.connection.JDBCStatementInitializer;
import it.unibz.inf.ontop.injection.OntopSystemSQLSettings;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DefaultJDBCStatementInitializer implements JDBCStatementInitializer {

    protected final OntopSystemSQLSettings settings;

    @Inject
    protected DefaultJDBCStatementInitializer(OntopSystemSQLSettings settings) {
        this.settings = settings;
    }

    @Override
    public Statement createAndInitStatement(Connection connection) throws SQLException {
        return init(create(connection));
    }

    @Override
    public void closeStatement(Statement statement) throws SQLException {
        statement.close();
    }

    protected Statement create(Connection connection) throws SQLException {
        return connection.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
    }

    protected Statement init(Statement statement) throws SQLException {
        int fetchSize = settings.getFetchSize();
        if (fetchSize > 0)
            statement.setFetchSize(fetchSize);
        return statement;
    }
}
