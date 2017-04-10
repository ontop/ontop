package it.unibz.inf.ontop.mysql;

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


import it.unibz.inf.ontop.owlrefplatform.owlapi.OntopOWLStatement;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLResultSet;
import it.unibz.inf.ontop.quest.AbstractVirtualModeTest;
import org.semanticweb.owlapi.model.OWLObject;

/**
 * Test
 * CONCAT with table.columnName and string values that need to be change to literal
 * use mysql.
 *
 */

public class ConferenceConcatMySQLTest extends AbstractVirtualModeTest {

    static final String owlFile = "src/test/resources/mysql/conference/ontology3.owl";
    static final String obdaFile = "src/test/resources/mysql/conference/secondmapping-test.obda";
	static final String propertyFile = "src/test/resources/mysql/conference/secondmapping-test.properties";

	public ConferenceConcatMySQLTest() {
		super(owlFile, obdaFile, propertyFile);
	}

	private void runTests(String query1) throws Exception {

		OntopOWLStatement st = conn.createStatement();


		try {
			executeQueryAssertResults(query1, st);
			
		} catch (Exception e) {
            st.close();
            e.printStackTrace();
            assertTrue(false);


		} finally {

			conn.close();
			reasoner.dispose();
		}
	}
	
	private void executeQueryAssertResults(String query, OntopOWLStatement st) throws Exception {
		QuestOWLResultSet rs = st.executeTuple(query);

		OWLObject answer, answer2;
		rs.nextRow();



		answer= rs.getOWLObject("x");
		System.out.print("x =" + answer);
		System.out.print(" ");
		answer2= rs.getOWLObject("y");

		System.out.print("y =" + answer2);
		System.out.print(" ");


		rs.close();
		assertEquals("<http://myproject.org/odbs#tracepaper1>", answer.toString());
		assertEquals("<http://myproject.org/odbs#eventpaper1>", answer2.toString());
	}

	public void testConcat() throws Exception {

        String query1 = "PREFIX : <http://myproject.org/odbs#> SELECT ?x ?y\n" +
                "WHERE {\n" +
                "   ?x :TcontainsE ?y\n" +
				"}";

		runTests(query1);
	}


}
