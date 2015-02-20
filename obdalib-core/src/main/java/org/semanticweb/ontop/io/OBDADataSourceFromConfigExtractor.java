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

    public OBDADataSourceFromConfigExtractor(OBDAProperties properties)
            throws InvalidDataSourceException {
        dataSource = extractProperties(properties);
    }

    public OBDADataSource getDataSource() {
        return dataSource;
    }

    private static OBDADataSource extractProperties(OBDAProperties properties)
            throws InvalidDataSourceException {
        if (properties == null)
            throw new IllegalArgumentException("OBDA properties must not be null");

        String id = extractProperty(OBDAProperties.DB_NAME, properties);
        String url = extractProperty(OBDAProperties.JDBC_URL, properties);
        String username = extractProperty(OBDAProperties.DB_USER, properties);
        String password = extractProperty(OBDAProperties.DB_PASSWORD, properties);
        String driver = extractProperty(OBDAProperties.JDBC_DRIVER, properties);

        OBDADataSource source = OBDADataFactoryImpl.getInstance().getDataSource(URI.create(id));
        source.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
        source.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
        source.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
        source.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);

        return source;
    }

    private static String extractProperty(String propertyName, OBDAProperties properties)
            throws InvalidDataSourceException {
        String property = properties.getProperty(propertyName);
        if (property == null) {
            throw new InvalidDataSourceException(String.format("Property %s is missing in the configuration." +
                    "This data source information is required.", propertyName));
        }
        return property;
    }
}

