package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.temporal.model.DatalogMTLProgram;
import it.unibz.inf.ontop.temporal.model.DatalogMTLRule;

import java.util.*;

import static java.util.stream.Collectors.joining;

public class DatalogMTLProgramImpl implements DatalogMTLProgram {

    private final List<DatalogMTLRule> rules;
    private String base;

    private Map<String, String> prefixes;

    public DatalogMTLProgramImpl(Map<String, String> prefixes, String base, List<DatalogMTLRule> rules) {
        this.prefixes = prefixes;
        this.base = base;
        this.rules = rules;
    }

    public DatalogMTLProgramImpl(Map<String, String> prefixes, String base, DatalogMTLRule... rules) {
        this.prefixes = prefixes;
        this.base = base;
        this.rules = Arrays.asList(rules);
    }

    @Override
    public void setBase(String base){
        this.base = base;
    }

    @Override
    public String getBase(){
        return base;
    }

    @Override
    public Map<String, String> getPrefixes() {
        return prefixes;
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
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        prefixes.forEach((key, value) -> stringBuilder.append("PREFIX ").append(key).append("\t<").append(value).append(">\n"));
        String renderedRules = render();
        ArrayList<String> prefixList = new ArrayList<>(prefixes.values());
        prefixList.sort(Collections.reverseOrder());
        for(String uri : prefixList) {
            Optional<Map.Entry<String, String>> prefix =
                    prefixes.entrySet().stream().filter(v ->
                            v.getValue().equals(uri)).findFirst();
            if(prefix.isPresent()){
                Map.Entry<String, String> entry = prefix.get();
                renderedRules = renderedRules.replaceAll(entry.getValue(), entry.getKey());
            }
        }
        stringBuilder.append(renderedRules);
        return stringBuilder.toString();
    }
}
