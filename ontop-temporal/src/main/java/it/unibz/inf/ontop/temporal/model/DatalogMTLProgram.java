package it.unibz.inf.ontop.temporal.model;

import it.unibz.inf.ontop.model.DatalogProgram;
import it.unibz.inf.ontop.model.impl.DatalogProgramImpl;

import java.util.List;

public interface DatalogMTLProgram {

    List<DatalogMTLRule> getRules();

    String render();

}
