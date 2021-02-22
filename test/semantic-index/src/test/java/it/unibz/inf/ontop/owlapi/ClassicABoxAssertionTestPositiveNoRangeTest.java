package it.unibz.inf.ontop.owlapi;

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

import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import it.unibz.inf.ontop.si.OntopSemanticIndexLoader;
import junit.framework.TestCase;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * This test check proper handling of ABox assertions, including handling of the
 * supported data types. We check that each ABox assertion is inserted in the
 * database and the data is taken into account in relevant queries. Typing is
 * important in that although all data will be entered, not all data
 * participates in all queries.
 */
public class ClassicABoxAssertionTestPositiveNoRangeTest  {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClassicABoxAssertionTestPositiveNoRangeTest.class);

	private final OWLConnection conn;

	public ClassicABoxAssertionTestPositiveNoRangeTest() throws Exception {
		Properties p = new Properties();
		p.setProperty(OntopModelSettings.CARDINALITY_MODE, "STRICT");

		String owlfile = "src/test/resources/test/owl-types-simple-split.owl";

		OntopOWLReasoner reasoner;
		try(OntopSemanticIndexLoader siLoader = OntopSemanticIndexLoader.loadOntologyIndividuals(owlfile, p)) {
			OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
			reasoner = factory.createReasoner(siLoader.getConfiguration());
		}

		conn = reasoner.getConnection();
	}

	private static final String prefix =
			"PREFIX : <http://it.unibz.inf/obda/ontologies/quest-typing-test.owl#> \n"
					+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n"
					+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
					+"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>";

	private int executeQuery(String q) throws OWLException {
		String query = prefix + " " + q;

		try(OWLStatement st = conn.createStatement();
			TupleOWLResultSet res = st.executeSelectQuery(query)) {
			int count = 0;
			while (res.hasNext()) {
				final OWLBindingSet bindingSet = res.next();
				LOGGER.info(bindingSet.toString());
				count += 1;
			}
			res.close();
			return count;
		}
	}

	@Test
	public void testClassAssertions() throws OWLException {
		String query = "SELECT ?x WHERE {?x a :class}";
		int count = executeQuery(query);
		assertEquals(1, count);
	}

	@Test
	public void testObjectPropertyAssertions() throws OWLException{
		String query = "SELECT ?x ?y WHERE {?x :oproperty ?y}";
		int count = executeQuery(query);
		assertEquals(1, count);
	}

	@Test
	public void testDataPropertyAssertionsLiteral() throws OWLException{
		String query = "SELECT ?x WHERE {?x :uliteral ?y}";
		int count = executeQuery(query);
		assertEquals(2, count);
	}

	@Test
	public void testDataPropertyAssertionsBoolean() throws OWLException{
		String query = "SELECT ?x WHERE {?x :uboolean ?y}";
		int count = executeQuery(query);
		// asserting 0 and false will result in only one axiom 
		// same for 1 and true
		// the result is a set of axioms
		// hence we expect only 2
		assertEquals(2, count);
	}

	@Test
	public void testDataPropertyAssertionsDatetime() throws OWLException{
		String query = "SELECT ?x WHERE {?x :udateTime ?y}";
		int count = executeQuery(query);
		assertEquals(5, count);
	}

	@Test
	public void testDataPropertyAssertionsDecimal() throws OWLException{
		String query = "SELECT ?x WHERE {?x :udecimal ?y}";
		int count = executeQuery(query);
		assertEquals(8, count);
	}

	@Test
	public void testDataPropertyAssertionsDouble() throws OWLException{
		String query = "SELECT ?y WHERE {?x :udouble ?y}";
		int count = executeQuery(query);
		// values 0 and -0 produce equivalent axioms
		assertEquals(6, count);
	}

	@Test
	public void testDataPropertyAssertionsFloat() throws OWLException{
		String query = "SELECT ?x WHERE {?x :ufloat ?y}";
		int count = executeQuery(query);
		// values 0 and -0 produce equivalent axioms
		assertEquals(6, count);
	}

	@Test
	public void testDataPropertyAssertionsInt() throws OWLException{
		String query = "SELECT ?x ?y WHERE {?x :uint ?y}";
		int count = executeQuery(query);
		assertEquals(6, count);
		
		query = "SELECT ?x ?y WHERE {?x :uint ?y FILTER (?y > 0)}";
		count = executeQuery(query);
		assertEquals(3, count);
	}

	@Test
	public void testDataPropertyAssertionsInteger()throws OWLException {
		String query = "SELECT ?y WHERE {?x :uinteger ?y}";
		int count = executeQuery(query);
		assertEquals(4, count);
	}

	@Test
	public void testDataPropertyAssertionsLong() throws OWLException{
		String query = "SELECT ?x WHERE {?x :ulong ?y}";
		int count = executeQuery(query);
		assertEquals(6, count);
	}
}
