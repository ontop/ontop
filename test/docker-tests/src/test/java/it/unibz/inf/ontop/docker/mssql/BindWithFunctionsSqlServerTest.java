package it.unibz.inf.ontop.docker.mssql;

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

import java.util.ArrayList;
import java.util.List;

/**
 * Class to test if functions on Strings and Numerics in SPARQL are working properly.
 * Refer in particular to the class {@link SparqlAlgebraToDatalogTranslator}
 *
 */

public class BindWithFunctionsSqlServerTest extends AbstractBindTestWithFunctions {
    private static final String owlfile = "/mssql/sparqlBind.owl";
    private static final String obdafile = "/mssql/sparqlBindSqlServer.obda";
    private static final String propertiesfile = "/mssql/sparqlBindSqlServer.properties";

    public BindWithFunctionsSqlServerTest() {
        super(owlfile, obdafile, propertiesfile );
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
    protected List<String> getFloorExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0.0000\"^^xsd:decimal");
        expectedValues.add("\"0.0000\"^^xsd:decimal");
        expectedValues.add("\"0.0000\"^^xsd:decimal");
        expectedValues.add("\"0.0000\"^^xsd:decimal");
        return expectedValues;
    }

    @Override
    protected List<String> getRoundExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0.00, 43.00\"^^xsd:string");
        expectedValues.add("\"0.00, 23.00\"^^xsd:string");
        expectedValues.add("\"0.00, 34.00\"^^xsd:string");
        expectedValues.add("\"0.00, 10.00\"^^xsd:string");
        return expectedValues;
    }

    @Override
    protected List<String> getCeilExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"1.0000\"^^xsd:decimal");
        expectedValues.add("\"1.0000\"^^xsd:decimal");
        expectedValues.add("\"1.0000\"^^xsd:decimal");
        expectedValues.add("\"1.0000\"^^xsd:decimal");

        return expectedValues;
    }

    @Override
    protected List<String> getSecondsExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"52.0000\"^^xsd:decimal");
        expectedValues.add("\"0.0000\"^^xsd:decimal");
        expectedValues.add("\"6.0000\"^^xsd:decimal");
        expectedValues.add("\"0.0000\"^^xsd:decimal");

        return expectedValues;
    }

    @Override
    protected List<String> getTZExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"08:00\"");
        expectedValues.add("\"01:00\"");
        expectedValues.add("\"00:00\"");
        expectedValues.add("\"01:00\"");

        return expectedValues;
    }
}
