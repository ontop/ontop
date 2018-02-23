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
import org.semanticweb.owlapi.model.OWLIndividual;

import static org.junit.Assert.*;


/***
 * Tests that the system can handle the SPARQL "LIKE" keyword in the oracle setting
 * (i.e. that it is translated to REGEXP_LIKE and not LIKE in oracle sql)
 */
public class OracleRegexpSpaceTest extends AbstractVirtualModeTest {

	static final String owlFile = "/oracle/regex/oracle-regexp.owl";
	static final String obdaFile = "/oracle/regex/oracle-regexp.obda";
	static final String propertyFile = "/oracle/regex/oracle-regexp.properties";

	public OracleRegexpSpaceTest() {
		super(owlFile, obdaFile, propertyFile);
	}


	private String runTest(OWLStatement st, String query, boolean hasResult) throws Exception {
		String retval;
		TupleOWLResultSet rs = st.executeSelectQuery(query);
		if(hasResult){
			assertTrue(rs.hasNext());
            final OWLBindingSet bindingSet = rs.next();
            OWLIndividual ind1 = bindingSet.getOWLIndividual("country")	 ;
			retval = ind1.toString();
		} else {
			assertFalse(rs.hasNext());
			retval = "";
		}

		return retval;
	}

	/**
	 * Tests the use of mapings with regex in subqueries and where with SQL subquery
	 * @throws Exception
	 */
	@Test
	public void testSparql2OracleRegexWhere() throws Exception {
		OWLStatement st = null;
		try {
			st = conn.createStatement();

			
			
			String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?country WHERE {?country a :CountryWithSpace . }";
			String countryName = runTest(st, query, true);
			System.out.println(countryName);
			assertEquals(countryName, "<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-United%20Kingdom>");
		
		
			
		} catch (Exception e) {
			throw e;
		} finally {
			if (st != null)
				st.close();
		}
	}
	
	
	/**
	 * Tests the use of mapings with regex in subqueries without where with SQL subquery
	 * @throws Exception
	 */
	@Test
	public void testSparql2OracleRegexNoWhere() throws Exception {
		OWLStatement st = null;
		try {
			st = conn.createStatement();

			
			
			String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?country ?pos WHERE {?country a :CountriesWithSpaceNoWhere . ?country :position ?pos . FILTER (?pos >0)}";
			String countryName = runTest(st, query, true);
			System.out.println(countryName);
			assertEquals(countryName, "<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-United%20Kingdom>");
		
		
			
		} catch (Exception e) {
			throw e;
		} finally {
			if (st != null)
				st.close();
		}
	}

	/**
	 * Tests the use of mapings with regex in subqueries without where without subquery
	 * @throws Exception
	 */
	@Test
	public void testSparql2OracleRegexNoWhereNoSubquery() throws Exception {
		OWLStatement st = null;
		try {
			st = conn.createStatement();
			
			String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?country ?pos WHERE {?country a :CountriesWithSpaceNoWhereNoSubquery . ?country :position ?pos . FILTER (?pos >0)}";
			String countryName = runTest(st, query, true);
			System.out.println(countryName);
			assertEquals(countryName, "<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-United%20Kingdom>");

		} catch (Exception e) {
			throw e;
		} finally {
			if (st != null)
				st.close();
		}
	}

	

}
