package it.unibz.inf.ontop.obda;

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


import it.unibz.inf.ontop.answering.reformulation.tests.AbstractBindTestWithFunctions;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to test if functions on Strings and Numerics in SPARQL are working properly.
 * Refer in particular to the class {@link it.unibz.inf.ontop.owlrefplatform.core.translator.SparqlAlgebraToDatalogTranslator}
 *
 */

public class BindTestWithFunctionsOracle extends AbstractBindTestWithFunctions {

    private static final String owlfile = "src/test/resources/bindTest/sparqlBind.owl";
    private static final String obdafile = "src/test/resources/bindTest/sparqlBindOracle.obda";

    public BindTestWithFunctionsOracle() {
        super(owlfile, obdafile);
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
    protected List<String> getRoundExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0, 43\"");
        expectedValues.add("\"0, 23\"");
        expectedValues.add("\"0, 34\"");
        expectedValues.add("\"0, 10\"");
        return expectedValues;
    }

    @Override
    protected List<String> getBindWithAfterExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add(null);  // ROMAN (23 Dec 2015): now the language tag is handled correctly
        expectedValues.add("\" Semantic Web\"@en");
        expectedValues.add(null);
        expectedValues.add("\" Logic Book: Introduction, Second Edition\"@en");

        return expectedValues;
    }

    @Override
    protected List<String> getBindWithBeforeExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add(null);  // ROMAN (23 Dec 2015): now the language tag is handled correctly
        expectedValues.add("\"The Seman\"@en");
        expectedValues.add(null);
        expectedValues.add("\"The Logic Book: Introduc\"@en");

        return expectedValues;
    }

}
