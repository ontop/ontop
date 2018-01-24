package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.temporal.model.DatalogMTLProgram;
import it.unibz.inf.ontop.temporal.model.DatalogMTLRule;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;

public class DatalogMTLProgramImpl implements DatalogMTLProgram {

    private final List<DatalogMTLRule> rules;

    Map<String, String> prefixes;

    public DatalogMTLProgramImpl(Map<String, String> prefixes, List<DatalogMTLRule> rules) {
        this.prefixes = prefixes;
        this.rules = rules;
    }

    public DatalogMTLProgramImpl(Map<String, String> prefixes, DatalogMTLRule... rules) {
        this.prefixes = prefixes;
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

    @Override
    public Map<String, String> getPrefixes() {
        return prefixes;
    }

}
