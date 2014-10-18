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

    protected void readPropertiesFile(String fileName) throws IOException {
        InputStream in =OBDAProperties.class.getResourceAsStream(fileName);
        readDefaultPropertiesFile(in);
    }

}
