package it.unibz.inf.ontop.spec.datalogmtl.parser.impl;

import it.unibz.inf.ontop.temporal.model.DatalogMTLFactory;
import it.unibz.inf.ontop.temporal.model.DatalogMTLProgram;
import it.unibz.inf.ontop.temporal.model.DatalogMTLRule;

import java.util.List;

public class DatalogMTLNormalizer {

    private final DatalogMTLFactory datalogMTLFactory;

    public DatalogMTLNormalizer(DatalogMTLFactory datalogMTLFactory){
        this.datalogMTLFactory = datalogMTLFactory;
    }

    public DatalogMTLProgram normalize(DatalogMTLProgram program){
        List<DatalogMTLRule> rules = correctStaticExpressions(program.getRules());
        rules = correctStaticJoins(rules);
        return datalogMTLFactory.createProgram(program.getPrefixes(), rules);
    }

    public List<DatalogMTLRule> correctStaticExpressions(List<DatalogMTLRule> rules){
        return null;
    }

    public List<DatalogMTLRule> correctStaticJoins(List<DatalogMTLRule> rules){
        return null;
    }

}
