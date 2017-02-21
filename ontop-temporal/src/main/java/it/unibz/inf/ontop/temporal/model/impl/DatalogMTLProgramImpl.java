package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.temporal.model.DatalogMTLProgram;
import it.unibz.inf.ontop.temporal.model.Rule;

import java.util.ArrayList;

public class DatalogMTLProgramImpl implements DatalogMTLProgram {

    ArrayList<Rule> rules = null;

    public DatalogMTLProgramImpl(){
        rules = new ArrayList<>();
    }

    @Override
    public void appendRule(Rule nRule) {
        rules.add(nRule);

    }

    @Override
    public ArrayList<Rule> getRules() {
        return this.rules;
    }
}
