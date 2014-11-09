package org.semanticweb.ontop.injection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * General properties.
 *
 * Focuses on implementation class declaration
 * for the core module of Ontop.
 *
 */
public class OBDAProperties extends Properties {

    public static final String JDBC_URL = "JDBC_URL";
    public static final String DB_NAME = "DB_NAME";
    public static final String DB_USER = "DBUSER";
    public static final String DB_PASSWORD = "DBPASSWORD";
    public static final String JDBC_DRIVER = "JDBC_DRIVER";

    public static final String OBTAIN_FULL_METADATA = "OBTAIN_FULL_METADATA";

    public static final String DEFAULT_OBDA_PROPERTIES_FILE = "default_implementations.properties";
    private static Logger LOG = LoggerFactory.getLogger(OBDAProperties.class);

    public OBDAProperties() {
        super();
        try {
            readPropertiesFile(DEFAULT_OBDA_PROPERTIES_FILE);
        } catch (IOException e1) {
            LOG.error("Error reading default OBDA properties.");
            LOG.debug(e1.getMessage(), e1);
        }
    }

    public OBDAProperties(Properties values) {
        this();
        this.putAll(values);
    }


    /**
     * Reads the properties from the input stream and sets them as default.
     *
     * @param in
     *            The input stream.
     */
    public void readDefaultPropertiesFile(InputStream in) throws IOException {
        this.load(in);
    }

    private void readPropertiesFile(String fileName) throws IOException {
        InputStream in =OBDAProperties.class.getResourceAsStream(fileName);
        readDefaultPropertiesFile(in);
    }

}
