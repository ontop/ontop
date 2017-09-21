package it.unibz.inf.ontop.docker.mysql;

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
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test if the datatypes xsd:date, xsd:time and xsd:year are returned correctly.
 *
 */

public class OntologyTypesDatatypeTest extends AbstractVirtualModeTest {

    static final String owlfile = "/testcases-docker/datetime/datatypes.owl";
    static final String obdafile = "/testcases-docker/datetime/datatypes-mysql.obda";
    static final String propertiesfile = "/testcases-docker/datetime/datatypes-mysql.properties";

    public OntologyTypesDatatypeTest() {
        super(owlfile, obdafile, propertiesfile);
    }


    //With QuestOWL the results for xsd:date, xsd:time and xsd:year are returned as a plain literal since OWLAPI3 supports only xsd:dateTime
	@Test
    public void testDatatypeDate() throws Exception {

        String query1 = "PREFIX : <http://ontop.inf.unibz.it/test/datatypes#> SELECT ?s ?x\n" +
                "WHERE {\n" +
                "   ?s a :Row; :hasDate ?x\n" +
                "   FILTER ( ?x = \"2013-03-18\"^^xsd:date ) .\n" +
                "}";

        String result = runQueryAndReturnStringOfLiteralX(query1);
		assertEquals("\"2013-03-18\"^^xsd:date",result );
	}

    @Test
    public void testDatatypeTime() throws Exception {

        String query1 = "PREFIX : <http://ontop.inf.unibz.it/test/datatypes#> SELECT ?s ?x\n" +
                "WHERE {\n" +
                "   ?s a :Row; :hasTime ?x\n" +
                "   FILTER ( ?x = \"10:12:10\"^^xsd:time ) .\n" +
                "}";

        String result = runQueryAndReturnStringOfLiteralX(query1);
        assertEquals("\"10:12:10\"^^xsd:time", result );
    }

    @Ignore
    public void testDatatypeTimeTz() throws Exception {

        String query1 = "PREFIX : <http://ontop.inf.unibz.it/test/datatypes#> SELECT ?s ?x\n" +
                "WHERE {\n" +
                "   ?s a :Row; :hasTime ?x\n" +
                "   FILTER ( ?x = \"10:12:10+01:00\"^^xsd:time ) .\n" +
                "}";

        String result = runQueryAndReturnStringOfLiteralX(query1);
        assertEquals("\"10:12:10+01:00\"^^xsd:time", result );
    }

    @Test
    public void testDatatypeYear() throws Exception {
        String query1 = "PREFIX : <http://ontop.inf.unibz.it/test/datatypes#> SELECT ?s ?x\n" +
                "WHERE {\n" +
                "   ?s a :Row; :hasYear ?x\n" +
                "   FILTER ( ?x = \"2013\"^^xsd:gYear ) .\n" +
                "}";
        String result = runQueryAndReturnStringOfLiteralX(query1);
        assertEquals("\"2013\"^^xsd:gYear",result );
    }



}
