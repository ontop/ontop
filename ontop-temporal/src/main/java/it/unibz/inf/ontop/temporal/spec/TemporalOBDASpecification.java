package it.unibz.inf.ontop.temporal.spec;


import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.temporal.model.DatalogMTLProgram;

public interface TemporalOBDASpecification extends OBDASpecification {

    DatalogMTLProgram getDatalogMTLProgram();

}
