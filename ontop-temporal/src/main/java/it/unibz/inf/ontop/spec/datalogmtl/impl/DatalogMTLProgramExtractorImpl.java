package it.unibz.inf.ontop.spec.datalogmtl.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.spec.OBDASpecInput;
import it.unibz.inf.ontop.spec.TOBDASpecInput;
import it.unibz.inf.ontop.spec.datalogmtl.DatalogMTLProgramExtractor;
import it.unibz.inf.ontop.spec.datalogmtl.parser.DatalogMTLNormalizer;
import it.unibz.inf.ontop.spec.datalogmtl.parser.DatalogMTLSyntaxParser;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.temporal.model.DatalogMTLProgram;

import java.io.*;

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

        DatalogMTLProgram program = ruleParser.parse(readFile(specInput.getTemporalRuleFile().get()));
        return ruleNormalizer.normalize(program, staticMapping);
    }

    private String readFile(File file){
        String output = "";
        try {
            BufferedReader bufferedReader =
                    new BufferedReader(new FileReader(file));

            String newLine;
            while ((newLine = bufferedReader.readLine()) != null){
                output += newLine + "\n";
            }
            return output.trim();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
