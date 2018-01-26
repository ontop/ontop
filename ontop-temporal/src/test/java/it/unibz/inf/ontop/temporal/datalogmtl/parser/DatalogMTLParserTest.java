package it.unibz.inf.ontop.temporal.datalogmtl.parser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import it.unibz.inf.ontop.injection.OntopMappingConfiguration;
import it.unibz.inf.ontop.injection.OntopTemporalSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.spec.datalogmtl.parser.DatalogMTLSyntaxParser;
import it.unibz.inf.ontop.spec.datalogmtl.parser.impl.DatalogMTLSyntaxParserImpl;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.temporal.model.DatalogMTLProgram;
import junit.framework.TestCase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import static it.unibz.inf.ontop.spec.impl.TestingTools.*;

public class DatalogMTLParserTest {

    private final static Logger log = LoggerFactory.getLogger(DatalogMTLParserTest.class);
    private final SpecificationFactory specificationFactory;

    public DatalogMTLParserTest() {
        OntopMappingConfiguration defaultConfiguration = OntopMappingConfiguration.defaultBuilder()
                .enableTestMode()
                .build();
        Injector injector = defaultConfiguration.getInjector();
        this.specificationFactory = injector.getInstance(SpecificationFactory.class);
    }

    @Test
    public void test() {
        final boolean result = parse(readFile("src/test/resources/rule.dmtl"));
        TestCase.assertTrue(result);
    }

    @Test
    public void test2() {
        final boolean result = parse(readFile("src/test/resources/rule2.dmtl"));
        TestCase.assertTrue(result);
    }

    @Test
    public void test3() {
        final boolean result = parse(readFile("src/test/resources/rule3.dmtl"));
        TestCase.assertTrue(result);
    }

    private boolean parse(String input) {
        DatalogMTLSyntaxParser parser = new DatalogMTLSyntaxParserImpl(getPrefixManager().getPrefixMap(), ATOM_FACTORY,
                TERM_FACTORY);

        DatalogMTLProgram program;
        try {
            program = parser.parse(input);
            log.debug("mapping " + program);
        } catch (Exception e) {
            log.debug(e.getMessage());
            return false;
        }
        return true;
    }

    private PrefixManager getPrefixManager() {
        return specificationFactory.createPrefixManager(ImmutableMap.of(
                PrefixManager.DEFAULT_PREFIX, "http://obda.inf.unibz.it/testcase#",
                "ex:", "http://www.example.org/"
        ));
    }

    private String readFile(String path){
        String output = "";
        try {
            BufferedReader bufferedReader =
                    new BufferedReader(new FileReader(path));

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
