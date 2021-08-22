package it.unibz.inf.ontop.answering.connection;

import java.sql.SQLException;
import java.sql.Statement;

public interface JDBCStatementFinalizer {

    void closeStatement(Statement statement) throws SQLException;
}
