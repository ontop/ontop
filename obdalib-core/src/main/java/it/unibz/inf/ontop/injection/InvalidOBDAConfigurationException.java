package it.unibz.inf.ontop.injection;

public class InvalidOBDAConfigurationException extends RuntimeException {

    public InvalidOBDAConfigurationException(String message, OBDACoreConfiguration configuration) {
        super(message + "\nConfiguration:\n" + configuration);
    }

    public InvalidOBDAConfigurationException(String message) {
        super(message);
    }
}
