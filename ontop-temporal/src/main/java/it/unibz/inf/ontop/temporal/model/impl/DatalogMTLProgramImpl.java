package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.temporal.model.DatalogMTLProgram;
import it.unibz.inf.ontop.temporal.model.DatalogMTLRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class DatalogMTLProgramImpl implements DatalogMTLProgram {

    private final List<DatalogMTLRule> rules;

    public DatalogMTLProgramImpl(List<DatalogMTLRule> rules) {
        this.rules = rules;
    }

    public DatalogMTLProgramImpl(DatalogMTLRule... rules) {
        this.rules = Arrays.asList(rules);
    }

    @Override
    public List<DatalogMTLRule> getRules() {
        return rules;
    }

    @Override
    public String render() {
        return rules.stream().map(DatalogMTLRule::render).collect(joining("\n"));
    }

}
