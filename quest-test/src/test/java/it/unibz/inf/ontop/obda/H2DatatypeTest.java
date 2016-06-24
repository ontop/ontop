package it.unibz.inf.ontop.obda;

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

/***
 * Tests that h2 datatypes
 */
public class H2DatatypeTest extends AbstractVirtualModeTest {
    static final String owlfile = "src/test/resources/datatype/datatypes.owl";
	static final String obdafile = "src/test/resources/datatype/datetime-h2.obda";

	protected H2DatatypeTest() {
		super(owlfile, obdafile);
	}


	/**
	 * Test use of date
	 * @throws Exception
	 */
	public void testDate() throws Exception {
		String query =  "PREFIX : <http://ontop.inf.unibz.it/test/datatypes#> SELECT ?s ?x\n" +
                "WHERE {\n" +
                "   ?s a :Row; :hasDate ?x\n" +
                "   FILTER ( ?x = \"2013-03-18\"^^xsd:date ) .\n" +
                "}";
		String val = runQueryAndReturnStringX(query);
		assertEquals("\"2013-03-18\"", val);
	}

    public void testDate2() throws Exception {
        String query =  "PREFIX : <http://ontop.inf.unibz.it/test/datatypes#> SELECT ?x\n" +
                "WHERE {\n" +
                "   ?x a :Row; :hasDate \"2013-03-18\"^^xsd:date\n" +
                "}";
        String val = runQueryAndReturnStringX(query);
        assertEquals("<http://ontop.inf.unibz.it/test/datatypes#datetime-1>", val);
    }




}

