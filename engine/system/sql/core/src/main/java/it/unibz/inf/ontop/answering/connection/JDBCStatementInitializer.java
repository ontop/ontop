package it.unibz.inf.ontop.answering.connection;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public interface JDBCStatementInitializer extends JDBCStatementFinalizer {

    Statement createAndInitStatement(Connection connection) throws SQLException;

}
