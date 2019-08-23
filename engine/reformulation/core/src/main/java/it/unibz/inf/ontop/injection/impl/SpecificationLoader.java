package it.unibz.inf.ontop.injection.impl;


import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.spec.OBDASpecification;

@FunctionalInterface
public interface SpecificationLoader {

    OBDASpecification load() throws OBDASpecificationException;
}
