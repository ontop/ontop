package org.semanticweb.ontop.io;

import org.semanticweb.ontop.injection.OBDAProperties;
import org.semanticweb.ontop.model.OBDADataSource;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.RDBMSourceParameterConstants;

import java.net.URI;

/**
 * Creates an OBDADataSource by looking at the properties.
 */
public class OBDADataSourceFromConfigExtractor {

    private final OBDADataSource dataSource;

    public OBDADataSourceFromConfigExtractor(OBDAProperties properties) {
        dataSource = extract(properties);
    }

    public OBDADataSource getDataSource() {
        return dataSource;
    }

    /**
     * TODO: manage exceptions!
     */
    private static OBDADataSource extract(OBDAProperties properties) {
        String id = properties.get(OBDAProperties.DB_NAME).toString();
        String url = properties.get(OBDAProperties.JDBC_URL).toString();
        String username = properties.get(OBDAProperties.DB_USER).toString();
        String password = properties.get(OBDAProperties.DB_PASSWORD).toString();
        String driver = properties.get(OBDAProperties.JDBC_DRIVER).toString();

        OBDADataSource source = OBDADataFactoryImpl.getInstance().getDataSource(URI.create(id));
        source.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
        source.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
        source.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
        source.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);

        return source;
    }
}

