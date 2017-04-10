package it.unibz.inf.ontop.mysql;

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


import it.unibz.inf.ontop.AbstractBindTestWithFunctions;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to test if functions on Strings and Numerics in SPARQL are working properly.
 * Refer in particular to the class {@link it.unibz.inf.ontop.owlrefplatform.core.translator.SparqlAlgebraToDatalogTranslator}
 *
 */

public class BindTestWithFunctionsMySQL extends AbstractBindTestWithFunctions {

	private static final String owlfile = "src/test/resources/mysql/bindTest/sparqlBind.owl";
    private static final String obdafile = "src/test/resources/mysql/bindTest/sparqlBindMySQL.obda";
    private static final String propertyfile = "src/test/resources/mysql/bindTest/sparqlBindMySQL.properties";

    public BindTestWithFunctionsMySQL() {
        super(owlfile, obdafile, propertyfile);
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
        expectedValues.add("\"0, 43\"");
        expectedValues.add("\"0, 23\"");
        expectedValues.add("\"0, 34\"");
        expectedValues.add("\"0, 10\"");
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

    protected List<String> getAbsExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"9\"^^xsd:decimal");
        expectedValues.add("\"6\"^^xsd:decimal");
        expectedValues.add("\"7\"^^xsd:decimal");
        expectedValues.add("\"2\"^^xsd:decimal");
        return expectedValues;
    }
}
