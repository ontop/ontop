package it.unibz.inf.ontop.docker.lightweight.redshift;

import it.unibz.inf.ontop.docker.lightweight.AbstractConstraintTest;
import it.unibz.inf.ontop.docker.lightweight.RedshiftLightweightTest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@RedshiftLightweightTest
public class ConstraintRedshiftTest extends AbstractConstraintTest {

    private static final String PROPERTIES_FILE = "/dbconstraints/dbconstraints-redshift.properties";

    public ConstraintRedshiftTest(String method) {
        super(method, PROPERTIES_FILE);
    }

    @Override
    protected Connection createConnection() throws SQLException {
        Properties info = new Properties();
        info.put("AccessKeyID", properties.get("jdbc.property.AccessKeyID"));
        info.put("SecretAccessKey", properties.get("jdbc.property.SecretAccessKey"));
        return DriverManager.getConnection(getConnectionString(), info);
    }
}
