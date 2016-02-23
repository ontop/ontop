package org.protege.osgi.jdbc.prefs;

import java.io.File;

public class DriverInfo {

    private String description;
    private String className;
    private File   driverLocation;
    
    public DriverInfo(String description, String className, File driverLocation) {
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

    public File getDriverLocation() {
        return driverLocation;
    }
    
}
