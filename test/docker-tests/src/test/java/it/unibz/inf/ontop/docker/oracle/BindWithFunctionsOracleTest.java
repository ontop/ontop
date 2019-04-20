package it.unibz.inf.ontop.docker.oracle;

/*
 * #%L
 * ontop-quest-owlapi
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */




import it.unibz.inf.ontop.answering.reformulation.input.translation.impl.SparqlAlgebraToDatalogTranslator;
import it.unibz.inf.ontop.docker.AbstractBindTestWithFunctions;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to test if functions on Strings and Numerics in SPARQL are working properly.
 * Refer in particular to the class {@link SparqlAlgebraToDatalogTranslator}
 *
 */

public class BindWithFunctionsOracleTest extends AbstractBindTestWithFunctions {

    private static final String owlfile = "/oracle/bindTest/sparqlBind.owl";
    private static final String obdafile = "/oracle/bindTest/sparqlBindOracle.obda";
    private static final String propertiesfile = "/oracle/oracle.properties";
    
    private static OntopOWLReasoner REASONER;
    private static OWLConnection CONNECTION;

    public BindWithFunctionsOracleTest() throws OWLOntologyCreationException {
        super(createReasoner(owlfile, obdafile, propertiesfile));
        REASONER = getReasoner();
        CONNECTION = getConnection();
    }

    @AfterClass
    public static void after() throws OWLException {
        CONNECTION.close();
        REASONER.dispose();
    }

    @Override
    protected List<String> getAbsExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"8.5\"^^xsd:decimal");
        expectedValues.add("\"5.75\"^^xsd:decimal");
        expectedValues.add("\"6.7\"^^xsd:decimal");
        expectedValues.add("\"1.5\"^^xsd:decimal");
        return expectedValues;
    }

    /*
	 * Tests for hash functions. Oracle does not support any hash functions if DBMS CRYPTO is not enabled
	 */
    @Ignore("Not yet supported")
    @Test
    @Override
    public void testHash() {
    }

    @Override
    protected List<String> getHoursExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"10\"^^xsd:integer");
        expectedValues.add("\"11\"^^xsd:integer");
        expectedValues.add("\"9\"^^xsd:integer");
        expectedValues.add("\"6\"^^xsd:integer");
        return expectedValues;
    }

    @Override
    protected List<String> getDivideExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"21.25\"^^xsd:decimal");
        expectedValues.add("\"11.5\"^^xsd:decimal");
        expectedValues.add("\"16.75\"^^xsd:decimal");
        expectedValues.add("\"5\"^^xsd:decimal");
        return expectedValues;    }

    @Override
    protected List<String> getRoundExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0, 43\"^^xsd:string");
        expectedValues.add("\"0, 23\"^^xsd:string");
        expectedValues.add("\"0, 34\"^^xsd:string");
        expectedValues.add("\"0, 10\"^^xsd:string");
        return expectedValues;
    }

    //Note: in specification of SPARQL function if the string doesn't contain the specified string empty string has to be returned,
    //here instead return null value
    @Override
    protected List<String> getBindWithAfter1ExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add(null);  // ROMAN (23 Dec 2015): now the language tag is handled correctly
        expectedValues.add("\" Semantic Web\"@en");
        expectedValues.add(null);
        expectedValues.add("\" Logic Book: Introduction, Second Edition\"@en");

        return expectedValues;
    }

    //Note: in specification of SPARQL function if the string doesn't contain the specified string empty string has to be returned,
    //here instead return null value

    @Override
    protected List<String> getBindWithBefore1ExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add(null);  // ROMAN (23 Dec 2015): now the language tag is handled correctly
        expectedValues.add("\"The Seman\"@en");
        expectedValues.add(null);
        expectedValues.add("\"The Logic Book: Introduc\"@en");

        return expectedValues;
    }

    @Override
    protected List<String> getTZExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"8:0\"");
        expectedValues.add("\"1:0\"");
        expectedValues.add("\"0:0\"");
        expectedValues.add("\"1:0\"");

        return expectedValues;
    }
}
