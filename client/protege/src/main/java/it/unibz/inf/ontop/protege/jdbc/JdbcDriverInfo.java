package it.unibz.inf.ontop.protege.jdbc;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class JdbcDriverInfo {

    private final String description;
    private final String className;
    private final File   driverLocation;
    
    public JdbcDriverInfo(String description, String className, File driverLocation) {
        this.description = description;
        this.className = className;
        this.driverLocation = driverLocation;
    }

    public String getDescription() {
        return description;
    }

    public String getClassName() {
        return className;
    }

    public String getDriverPath() {
        return driverLocation.getAbsolutePath();
    }

    public URL getDriverURL() throws MalformedURLException {
        return driverLocation.toURI().toURL();
    }

}
