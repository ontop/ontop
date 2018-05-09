package it.unibz.inf.ontop.spec.datalogmtl.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.spec.TOBDASpecInput;
import it.unibz.inf.ontop.spec.datalogmtl.DatalogMTLProgramExtractor;
import it.unibz.inf.ontop.spec.datalogmtl.parser.DatalogMTLNormalizer;
import it.unibz.inf.ontop.spec.datalogmtl.parser.DatalogMTLSyntaxParser;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.temporal.model.DatalogMTLProgram;

public class DatalogMTLProgramExtractorImpl implements DatalogMTLProgramExtractor {

    private final DatalogMTLSyntaxParser ruleParser;
    private final DatalogMTLNormalizer ruleNormalizer;

    @Inject
    public DatalogMTLProgramExtractorImpl(DatalogMTLSyntaxParser ruleParser, DatalogMTLNormalizer ruleNormalizer){

        this.ruleParser = ruleParser;
        this.ruleNormalizer = ruleNormalizer;
    }

    @Override
    public DatalogMTLProgram extract(TOBDASpecInput specInput, Mapping staticMapping) {

        //DatalogMTLProgram program = ruleParser.parse(readFile(specInput.getTemporalRuleFile().get()));
        return ruleNormalizer.normalize(specInput.getTemporalRuleProgram().get(), staticMapping);
    }
}
