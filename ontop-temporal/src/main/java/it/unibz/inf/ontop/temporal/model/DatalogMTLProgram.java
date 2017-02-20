package it.unibz.inf.ontop.temporal.model;

import it.unibz.inf.ontop.model.DatalogProgram;
import it.unibz.inf.ontop.model.impl.DatalogProgramImpl;

import java.util.List;

public interface DatalogMTLProgram /*extends DatalogProgramImpl implements  DatalogProgram*/ {

    public void appendRule(NormalizedRule nRule);

    public List<NormalizedRule> getRules();



}
