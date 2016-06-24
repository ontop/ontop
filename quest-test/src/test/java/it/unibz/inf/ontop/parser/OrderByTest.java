package it.unibz.inf.ontop.parser;

/*
 * #%L
 * ontop-test
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

import it.unibz.inf.ontop.quest.AbstractVirtualModeTest;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests to check if the SQL parser supports ORDER BY properly (SPARQL to SQL).
 */
public class OrderByTest extends AbstractVirtualModeTest {

    static final String owlFile = "src/test/resources/orderBy/stockBolzanoAddress.owl";
    static final String obdaFile = "src/test/resources/orderBy/stockBolzanoAddress.obda";

    protected OrderByTest() {
        super(owlFile, obdaFile);
    }

    public void testBolzanoOrderingAsc() throws Exception {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> "
                + "SELECT ?x ?street WHERE {?x :inState \"Bolzano\"^^<http://www.w3.org/2001/XMLSchema#string>; :inStreet ?street } "
                + "ORDER BY ?street "
                ;

        List<String> expectedUris = new ArrayList<>();
        expectedUris.add("http://www.owl-ontologies.com/Ontology1207768242.owl#Address-995");
        expectedUris.add("http://www.owl-ontologies.com/Ontology1207768242.owl#Address-992");
        checkReturnedUris(query, expectedUris);
    }

    public void testBolzanoOrderingAsc2() throws Exception {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> "
                + "SELECT ?x ?street WHERE {?x :inState \"Bolzano\"^^<http://www.w3.org/2001/XMLSchema#string>; :inStreet ?street } "
                + "ORDER BY ASC(?street)"
                ;

        List<String> expectedUris = new ArrayList<>();
        expectedUris.add("http://www.owl-ontologies.com/Ontology1207768242.owl#Address-995");
        expectedUris.add("http://www.owl-ontologies.com/Ontology1207768242.owl#Address-992");
        checkReturnedUris(query, expectedUris);
    }

    public void testBolzanoOrderingDesc() throws Exception {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> "
                + "SELECT ?x ?street WHERE {?x :inState \"Bolzano\"^^<http://www.w3.org/2001/XMLSchema#string>; :inStreet ?street } "
                + "ORDER BY DESC(?street)"
                ;

        List<String> expectedUris = new ArrayList<>();
        expectedUris.add("http://www.owl-ontologies.com/Ontology1207768242.owl#Address-992");
        expectedUris.add("http://www.owl-ontologies.com/Ontology1207768242.owl#Address-995");
        checkReturnedUris(query, expectedUris);
    }

    public void testBolzanoMultipleOrdering() throws Exception {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> "
                + "SELECT ?x ?street ?country ?number "
                + "WHERE {?x :hasNumber ?number ;"
                + ":inCountry ?country ;"
                + ":inStreet ?street . } "
                + "ORDER BY DESC(?country) ?number DESC(?street)"
                ;

        List<String> expectedUris = new ArrayList<>();
        expectedUris.add("http://www.owl-ontologies.com/Ontology1207768242.owl#Address-993");
        expectedUris.add("http://www.owl-ontologies.com/Ontology1207768242.owl#Address-991");
        expectedUris.add("http://www.owl-ontologies.com/Ontology1207768242.owl#Address-997");
        expectedUris.add("http://www.owl-ontologies.com/Ontology1207768242.owl#Address-992");
        expectedUris.add("http://www.owl-ontologies.com/Ontology1207768242.owl#Address-995");
        expectedUris.add("http://www.owl-ontologies.com/Ontology1207768242.owl#Address-996");
        expectedUris.add("http://www.owl-ontologies.com/Ontology1207768242.owl#Address-998");
        checkReturnedUris(query, expectedUris);
    }

}
