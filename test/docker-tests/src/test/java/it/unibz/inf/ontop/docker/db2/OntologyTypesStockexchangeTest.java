package it.unibz.inf.ontop.docker.db2;

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

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import org.junit.Test;

/**
 * Test if the datatypes are assigned correctly.
 * 
 * NOTE: xsd:string and rdfs:Literal are different.
 * 
 */

public class OntologyTypesStockexchangeTest extends AbstractVirtualModeTest {
    
	static final String owlFile = "/testcases-docker/virtual-mode/stockexchange/simplecq/stockexchange.owl";
	static final String obdaFile = "/testcases-docker/virtual-mode/stockexchange/simplecq/stockexchange-db2.obda";
    static final String propertyFile = "/testcases-docker/virtual-mode/stockexchange/simplecq/stockexchange-db2.properties";

    public OntologyTypesStockexchangeTest() {
        super(owlFile, obdaFile, propertyFile);
    }


    //we need xsd:string to work correctly
    @Test
	public void testQuotedLiteral() throws Exception {
        String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n SELECT DISTINCT ?x ?street WHERE {?x a :Address; :inStreet ?street; :inCity \"Bolzano\".}";

		countResults(query1, 2 );
	}

    @Test
    public void testDatatypeString() throws Exception {
        String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n SELECT DISTINCT ?x ?street WHERE {?x a :Address; :inStreet ?street; :inCity \"Bolzano\"^^xsd:string .}";

        countResults(query1, 2 );
    }

    //we need xsd:string to work correctly
    @Test
    public void testAddressesQuotedLiteral() throws Exception {
        String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n" +
                "SELECT DISTINCT * WHERE {      \n" +
                "\t    $x a :Address . \n" +
                "\t\t$x :addressID $id. \n" +
                "\t\t$x :inStreet \"Via Marconi\". \n" +
                "\t\t$x :inCity \"Bolzano\". \n" +
                "\t\t$x :inCountry $country. \n" +
                "\t\t$x :inState $state. \n" +
                "\t\t$x :hasNumber $number.\n" +
                "}";

        countResults(query1, 1 );
    }

    //in db2 there is no boolean type we refer to it in the database with a smallint 1 for true and a smallint 0 for false
    @Test
    public void testBooleanDatatype() throws Exception {
        String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n SELECT DISTINCT ?x WHERE { ?x a :Stock; :amountOfShares ?amount; :typeOfShares \"1\"^^xsd:integer . }";

        countResults(query1, 5 );
    }

    @Test
    public void testBooleanInteger() throws Exception {
        String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n SELECT DISTINCT ?x WHERE { ?x a :Stock; :amountOfShares ?amount; :typeOfShares 1 . }";

        countResults(query1, 5 );
    }

    //in db2 there is no boolean datatype, it is substitute with smallint
    @Test
    public void testBoolean() throws Exception {
        String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n SELECT DISTINCT ?x WHERE { ?x a :Stock; :amountOfShares ?amount; :typeOfShares TRUE . }";

        countResults(query1, 0 );
    }

    //in db2 there is no boolean datatype, it is substitute with smallint
    @Test
    public void testBooleanTrueDatatype() throws Exception {
        String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n SELECT DISTINCT ?x WHERE { ?x a :Stock; :amountOfShares ?amount; :typeOfShares \"1\"^^xsd:boolean . }";

        countResults(query1, 0 );
    }

    @Test
    public void testFilterBoolean() throws Exception {
        String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n SELECT DISTINCT ?x ?amount WHERE { ?x a :Stock; :amountOfShares ?amount; :typeOfShares ?type. FILTER ( ?type = 1 ). }";

        countResults(query1, 5 );
    }

    @Test
    public void testNotFilterBoolean() throws Exception {
        String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n SELECT DISTINCT ?x ?amount WHERE { ?x a :Stock; :amountOfShares ?amount; :typeOfShares ?type. FILTER ( ?type != 1 ). }";

        countResults(query1, 5 );
    }
    
    //a quoted integer is treated as a literal
    @Test
    public void testQuotedInteger() throws Exception {

          String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n SELECT DISTINCT ?x WHERE { ?x a :Stock; :amountOfShares ?amount; :typeOfShares \"1\" . }";

          countResults(query1, 0 );
    }


    //a quoted datatype is treated as a literal
    @Test
    public void testDatatype() throws Exception {

        String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n SELECT DISTINCT ?x WHERE { ?x a :Transaction; :transactionID ?id; :transactionDate \"2008-04-02T00:00:00\"^^<http://www.w3.org/2001/XMLSchema#dateTime> . }";

        countResults(query1, 1 );
    }

    //a quoted datatype is treated as a literal
    @Test
    public void testQuotedDatatype() throws Exception {
        String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n SELECT DISTINCT ?x WHERE { ?x a :Transaction; :transactionID ?id; :transactionDate \"2008-04-02T00:00:00\" . }";

        countResults(query1, 0 );
    }

    //a quoted datatype is treated as a literal
    @Test
    public void testDatatypeTimezone() throws Exception {
        String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n SELECT DISTINCT ?x WHERE { ?x a :Transaction; :transactionID ?id; :transactionDate \"2008-04-02T00:00:00+06:00\"^^<http://www.w3.org/2001/XMLSchema#dateTime> . }";

        countResults(query1, 1 );
    }
}
