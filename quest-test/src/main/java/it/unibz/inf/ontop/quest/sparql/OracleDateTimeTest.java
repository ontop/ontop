package it.unibz.inf.ontop.quest.sparql;




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


import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLResultSet;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLStatement;
import it.unibz.inf.ontop.quest.AbstractVirtualModeTest;
import org.semanticweb.owlapi.model.OWLObject;


/***
 * Tests that the system can handle the SPARQL "LIKE" keyword in the oracle setting
 * (i.e. that it is translated to REGEXP_LIKE and not LIKE in oracle sql)
 */
public class OracleDateTimeTest extends AbstractVirtualModeTest {

	static final String owlfile = "src/test/resources/dateTimeExampleBooks.owl";
	static final String obdafile = "src/test/resources/dateTimeExampleBooks.obda";

	public OracleDateTimeTest() {
		super(owlfile, obdafile);
	}


	private String runTest(OntopOWLStatement st, String query, boolean hasResult) throws Exception {
		String retval;
		QuestOWLResultSet rs = st.executeTuple(query);
		if(hasResult){
			assertTrue(rs.nextRow());
			OWLObject ind1 =	rs.getOWLObject("y")	 ;
			retval = ind1.toString();
		} else {
			assertFalse(rs.nextRow());
			retval = "";
		}

		return retval;
	}

	/**
	 * Tests the use of SPARQL like
	 * @throws Exception
	 */
	public void testSparql2OracleRegex() throws Exception {
		OntopOWLStatement st = null;
		try {
			st = conn.createStatement();

				String query = "PREFIX :	<http://meraka/moss/exampleBooks.owl#> \n " +
						" SELECT ?x ?y WHERE " +
						"{?x :dateOfPublication ?y .}";
				String date = runTest(st, query, true);
				System.out.println(date);
				
				//assertEquals(countryName, "<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-Egypt>");
			
		} catch (Exception e) {
			throw e;
		} finally {
			if (st != null)
				st.close();
		}
	}

}
