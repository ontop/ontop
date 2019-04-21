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

public class BindWithFunctionsSqlServerTest extends AbstractBindTestWithFunctions {
    private static final String owlfile = "/mssql/sparqlBind.owl";
    private static final String obdafile = "/mssql/sparqlBindSqlServer.obda";
    private static final String propertiesfile = "/mssql/sparqlBindSqlServer.properties";

    private static OntopOWLReasoner REASONER;
    private static OWLConnection CONNECTION;

    public BindWithFunctionsSqlServerTest() throws OWLOntologyCreationException {
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
        expectedValues.add("\"8.5000\"^^xsd:decimal");
        expectedValues.add("\"5.7500\"^^xsd:decimal");
        expectedValues.add("\"6.7000\"^^xsd:decimal");
        expectedValues.add("\"1.5000\"^^xsd:decimal");
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

    @Ignore("DATETIME does not have an offset. TODO: update the data source (use DATETIME2 instead)")
    @Test
    public void testTZ() throws Exception {
        super.testTZ();
    }
}
