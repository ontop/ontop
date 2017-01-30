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


import it.unibz.inf.ontop.reformulation.tests.AbstractBindTestWithFunctions;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to test if functions on Strings and Numerics in SPARQL are working properly.
 * Refer in particular to the class {@link it.unibz.inf.ontop.owlrefplatform.core.translator.SparqlAlgebraToDatalogTranslator}
 *
 */

public class BindTestWithFunctionsDb2 extends AbstractBindTestWithFunctions {
	private static final String owlfile = "src/test/resources/bindTest/sparqlBind.owl";
	private static final String obdafile = "src/test/resources/bindTest/sparqlBindDb2.obda";

    public BindTestWithFunctionsDb2() {
        super(owlfile, obdafile);
    }

    @Ignore("Not yet supported")
    @Test
    @Override
    public void testHash() {
    }

    @Ignore("Not yet supported")
    @Test
    @Override
    public void testUuid() {
    }

    @Ignore("Not yet supported")
    @Test
    @Override
    public void testStrUuid() {
    }

    @Ignore("Not yet supported")
    @Test
    @Override
    public void testTZ() {
    }


    @Override
    protected List<String> getAbsExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"8.5000\"^^xsd:decimal");
        expectedValues.add("\"5.7500\"^^xsd:decimal");
        expectedValues.add("\"6.7000\"^^xsd:decimal");
        expectedValues.add("\"1.5000\"^^xsd:decimal");
        return expectedValues;
    }

    @Override
    protected List<String> getRoundExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\".00, 43.00\"");
        expectedValues.add("\".00, 23.00\"");
        expectedValues.add("\".00, 34.00\"");
        expectedValues.add("\".00, 10.00\"");
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
    protected List<String> getSecondsExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"52.000000\"^^xsd:decimal");
        expectedValues.add("\"0.000000\"^^xsd:decimal");
        expectedValues.add("\"6.000000\"^^xsd:decimal");
        expectedValues.add("\"0.000000\"^^xsd:decimal");

        return expectedValues;
    }
}
