package it.unibz.inf.ontop.injection;


import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.spec.OBDASpecification;

import java.io.IOException;
import java.util.Optional;

public interface OntopOBDASpecificationConfiguration extends OntopOBDAConfiguration {

    OBDASpecification loadSpecification() throws OBDASpecificationException;
}
