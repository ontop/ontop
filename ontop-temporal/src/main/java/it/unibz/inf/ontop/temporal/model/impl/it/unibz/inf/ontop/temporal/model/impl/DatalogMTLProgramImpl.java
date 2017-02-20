package it.unibz.inf.ontop.temporal.model.impl.it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.temporal.model.DatalogMTLProgram;
import it.unibz.inf.ontop.temporal.model.NormalizedRule;

import java.util.ArrayList;

public class DatalogMTLProgramImpl implements DatalogMTLProgram {

    ArrayList<NormalizedRule> rules = null;

    public DatalogMTLProgramImpl(){
        rules = new ArrayList<>();
    }

    @Override
    public void appendRule(NormalizedRule nRule) {
        rules.add(nRule);

    }

    @Override
    public ArrayList<NormalizedRule> getRules() {
        return null;
    }
}
