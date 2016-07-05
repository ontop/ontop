package it.unibz.inf.ontop.injection;

public class InvalidOBDAConfigurationException extends RuntimeException {

    public InvalidOBDAConfigurationException(String message, OBDAConfiguration configuration) {
        super(message + "\nConfiguration:\n" + configuration);
    }
}
