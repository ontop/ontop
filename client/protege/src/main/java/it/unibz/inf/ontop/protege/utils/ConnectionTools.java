package it.unibz.inf.ontop.protege.utils;

import it.unibz.inf.ontop.protege.core.OBDADataSource;
import it.unibz.inf.ontop.protege.core.impl.RDBMSourceParameterConstants;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionTools {

    public static Connection getConnection(OBDADataSource source) throws SQLException {
        JDBCConnectionManager man = JDBCConnectionManager.getJDBCConnectionManager();
        String url = source.getParameter(RDBMSourceParameterConstants.DATABASE_URL);
        String username = source.getParameter(RDBMSourceParameterConstants.DATABASE_USERNAME);
        String password = source.getParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD);

        return man.getConnection(url, username, password);

    }

}
