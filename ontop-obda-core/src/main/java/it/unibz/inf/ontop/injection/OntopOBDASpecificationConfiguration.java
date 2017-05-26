package it.unibz.inf.ontop.injection;


import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.spec.OBDASpecification;

import java.io.IOException;
import java.util.Optional;

public interface OntopOBDASpecificationConfiguration extends OntopOBDAConfiguration {

    /**
     * TODO: is it necessary?
     *
     */
    Optional<OBDASpecification> loadSpecification() throws OBDASpecificationException;

    /**
     * Only call it if you are sure that mapping assertions have been provided
     */
    default OBDASpecification loadProvidedSpecification() throws OBDASpecificationException {
        return loadSpecification()
                .orElseThrow(() -> new IllegalStateException("No OBDA specification has been provided. " +
                        "Do not call this method unless you are sure of the specification provision."));
    }
}
