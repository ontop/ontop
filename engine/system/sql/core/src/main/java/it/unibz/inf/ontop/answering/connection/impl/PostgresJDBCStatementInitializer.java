package it.unibz.inf.ontop.answering.connection.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.OntopSystemSQLSettings;

import java.sql.SQLException;
import java.sql.Statement;

@Singleton
public class PostgresJDBCStatementInitializer extends DefaultJDBCStatementInitializer {

    @Inject
    protected PostgresJDBCStatementInitializer(OntopSystemSQLSettings settings) {
        super(settings);
    }

    @Override
    protected Statement init(Statement statement) throws SQLException {
        int fetchSize = settings.getFetchSize();
        if (fetchSize > 0) {
            statement.getConnection().setAutoCommit(false);
            statement.setFetchSize(fetchSize);
        }
        return statement;
    }

    @Override
    public void closeStatement(Statement statement) throws SQLException {
        if (!statement.isClosed()) {
            if (settings.getFetchSize() > 0)
                statement.execute("COMMIT");
            statement.close();
        }
    }
}
