package it.unibz.inf.ontop.docker.mysql;

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

public class BindWithFunctionsMySQLTest extends AbstractBindTestWithFunctions {

	private static final String owlfile = "/mysql/bindTest/sparqlBind.owl";
    private static final String obdafile = "/mysql/bindTest/sparqlBindMySQL.obda";
    private static final String propertyfile = "/mysql/bindTest/sparqlBindMySQL.properties";

    private static OntopOWLReasoner REASONER;
    private static OWLConnection CONNECTION;

    public BindWithFunctionsMySQLTest() throws OWLOntologyCreationException {
        super(createReasoner(owlfile, obdafile, propertyfile));
        REASONER = getReasoner();
        CONNECTION = getConnection();
    }

    @AfterClass
    public static void after() throws OWLException {
        CONNECTION.close();
        REASONER.dispose();
    }

    @Ignore("Not yet supported")
    @Test
    @Override
    public void testHash() {
    }

    @Ignore("Not yet supported")
    @Test
    @Override
    public void testTZ() {
    }

    @Override
    protected List<String> getRoundExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0, 43\"^^xsd:string");
        expectedValues.add("\"0, 23\"^^xsd:string");
        expectedValues.add("\"0, 34\"^^xsd:string");
        expectedValues.add("\"0, 10\"^^xsd:string");
        return expectedValues;
    }

    @Override
    protected List<String> getYearExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"2014\"^^xsd:integer");
        expectedValues.add("\"2011\"^^xsd:integer");
        expectedValues.add("\"2015\"^^xsd:integer");
        expectedValues.add("\"1970\"^^xsd:integer");

        return expectedValues;
    }

    @Override
    protected List<String> getAbsExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"9\"^^xsd:decimal");
        expectedValues.add("\"6\"^^xsd:decimal");
        expectedValues.add("\"7\"^^xsd:decimal");
        expectedValues.add("\"2\"^^xsd:decimal");
        return expectedValues;
    }

    @Override
    protected List<String> getHoursExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"16\"^^xsd:integer");
        expectedValues.add("\"11\"^^xsd:integer");
        expectedValues.add("\"7\"^^xsd:integer");
        expectedValues.add("\"6\"^^xsd:integer");
        return expectedValues;
    }

}
