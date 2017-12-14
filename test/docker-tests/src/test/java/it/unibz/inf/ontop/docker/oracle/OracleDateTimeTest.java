package it.unibz.inf.ontop.docker.oracle;




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
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class OracleDateTimeTest extends AbstractVirtualModeTest {

	static final String owlfile = "/oracle/datetime/dateTimeExampleBooks.owl";
	static final String obdafile = "/oracle/datetime/dateTimeExampleBooks.obda";
	static final String propertyfile = "/oracle/datetime/dateTimeExampleBooks.properties";
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public OracleDateTimeTest() {
		super(owlfile, obdafile, propertyfile);
	}


	private String runTest(OWLStatement st, String query, boolean hasResult) throws Exception {
		String retval;
		TupleOWLResultSet rs = st.executeSelectQuery(query);
		if(hasResult){
			assertTrue(rs.hasNext());
            final OWLBindingSet bindingSet = rs.next();
            OWLObject ind1 = bindingSet.getOWLObject("y")	 ;
			retval = ind1.toString();
		} else {
			assertFalse(rs.hasNext());
			retval = "";
		}

		return retval;
	}

	/**
	 *
	 * @throws Exception
	 */
	@Test
	public void testDateTime() throws Exception {
		OWLStatement st = null;
		try {
			st = conn.createStatement();

				String query = "PREFIX :	<http://meraka/moss/exampleBooks.owl#> \n " +
						" SELECT ?x ?y WHERE " +
						"{?x :dateOfPublication ?y .}";
				String date = runTest(st, query, true);
				log.debug(date);
				
				assertEquals("\"2010-02-18T10:02:30\"^^xsd:dateTime", date);
			
		} catch (Exception e) {
			throw e;
		} finally {
			if (st != null)
				st.close();
		}
	}

}
