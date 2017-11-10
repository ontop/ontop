package it.unibz.inf.ontop.spec.mapping.parser;

import it.unibz.inf.ontop.injection.OntopSQLCoreSettings;
import it.unibz.inf.ontop.injection.OntopSQLCredentialSettings;
import it.unibz.inf.ontop.protege.core.OBDADataSource;
import it.unibz.inf.ontop.protege.core.impl.RDBMSourceParameterConstants;

import java.util.Properties;

/**
 * TODO: explain
 */
public class DataSource2PropertiesConvertor {

    /**
     * These properties are compatible with OBDAProperties' keys.
     */
    public static Properties convert(OBDADataSource source) {

        String id = source.getSourceID().toString();
        String url = source.getParameter(RDBMSourceParameterConstants.DATABASE_URL);
        String username = source.getParameter(RDBMSourceParameterConstants.DATABASE_USERNAME);
        String password = source.getParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD);
        String driver =  source.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER);

        Properties p = new Properties();
        p.put(OntopSQLCoreSettings.JDBC_NAME, id);
        p.put(OntopSQLCoreSettings.JDBC_URL, url);
        p.put(OntopSQLCredentialSettings.JDBC_USER, username);
        p.put(OntopSQLCredentialSettings.JDBC_PASSWORD, password);
        p.put(OntopSQLCoreSettings.JDBC_DRIVER, driver);

        return p;
    }
}
