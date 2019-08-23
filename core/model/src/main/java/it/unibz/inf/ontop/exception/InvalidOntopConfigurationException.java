package it.unibz.inf.ontop.exception;

import it.unibz.inf.ontop.injection.OntopModelConfiguration;

public class InvalidOntopConfigurationException extends RuntimeException {

    public InvalidOntopConfigurationException(String message, OntopModelConfiguration configuration) {
        super(message + "\nConfiguration:\n" + configuration);
    }

    public InvalidOntopConfigurationException(String message) {
        super(message);
    }
}
