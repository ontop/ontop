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

import it.unibz.inf.ontop.injection.OntopReformulationSettings;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.GraphOWLResultSet;
import it.unibz.inf.ontop.si.OntopSemanticIndexLoader;
import org.junit.*;
import org.semanticweb.owlapi.model.OWLException;

import java.util.Properties;

/**
 * This unit test is for testing correctness of construct and describe queries
 * in ontop from the owl api. It is the same as SesameConstructDescribe for the
 * Sesame API, with the only difference that all abox data comes from the owl
 * file as declared named individuals and axioms, AND a property cannot have
 * both constant and uri objects. It must be clear if it's a data property or
 * object property.
 */
public class OWLConstructDescribeTest {

	private OntopOWLReasoner reasoner;
	private OWLConnection conn;
	private OWLStatement st;
	private static final String owlFile = "src/test/resources/describeConstruct.owl";
	
	@Before
	public void setUp() throws Exception {
		Properties properties = new Properties();
		properties.setProperty(OntopReformulationSettings.INCLUDE_FIXED_OBJECT_POSITION_IN_DESCRIBE, "true");

		try(OntopSemanticIndexLoader loader = OntopSemanticIndexLoader.loadOntologyIndividuals(owlFile, properties)) {
			OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
			reasoner = factory.createReasoner(loader.getConfiguration());
			conn = reasoner.getConnection();
			st = conn.createStatement();
		}
	}
	
	@After
	public void tearDown() throws Exception {
		st.close();
		conn.close();
		reasoner.dispose();	
	}
	
	@Test
	public void testAInsertData() throws Exception {
		String query = "CONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o}";
		Assert.assertEquals(4, countResult(query));
	}
	
	@Test
	public void testDescribeUri0() throws Exception {
		String query = "DESCRIBE <http://www.semanticweb.org/ontologies/test#p1>";
		Assert.assertEquals(0, countResult(query));
	}
	
	@Test
	public void testDescribeUri1() throws Exception {
		String query = "DESCRIBE <http://example.org/D>";
		Assert.assertEquals(1, countResult(query));
	}
	
	@Test
	public void testDescribeUri2() throws Exception {
		String query = "DESCRIBE <http://example.org/C>";
		Assert.assertEquals(2, countResult(query));
	}
	
	@Test
	public void testDescribeVar0() throws Exception {
		String query = "DESCRIBE ?x WHERE {<http://example.org/C> ?x ?y }";
		Assert.assertEquals(0, countResult(query));
	}
	
	@Test
	public void testDescribeVar1() throws Exception {
		String query = "DESCRIBE ?x WHERE {?x <http://www.semanticweb.org/ontologies/test#p2> <http://example.org/A>}";
		Assert.assertEquals(1, countResult(query));
	}

	@Test
	public void testDescribeVar2() throws Exception {
		String query = "DESCRIBE ?x WHERE {?x <http://www.semanticweb.org/ontologies/test#p1> ?y}";
		Assert.assertEquals(2, countResult(query));
	}

	@Test
	public void testDescribeVar3() throws Exception {
		String query = "DESCRIBE ?x ?y WHERE {<http://example.org/C> ?x ?y }";
		Assert.assertEquals(0, countResult(query));
	}
	
	@Test
	public void testConstruct0() throws Exception {
		String query = "CONSTRUCT {?s ?p <http://www.semanticweb.org/ontologies/test/p1>} WHERE {?s ?p <http://www.semanticweb.org/ontologies/test/p1>}";
		Assert.assertEquals(0, countResult(query));
	}
	
	@Test
	public void testConstruct1() throws Exception {
		String query = "CONSTRUCT { ?s ?p <http://example.org/D> } WHERE { ?s ?p <http://example.org/D>}";
		Assert.assertEquals(1, countResult(query));
	}
	
	@Test
	public void testConstruct2() throws Exception {
		String query = "CONSTRUCT {<http://example.org/C> ?p ?o} WHERE {<http://example.org/C> ?p ?o}";
		Assert.assertEquals(2, countResult(query));
	}

	private int countResult(String graphQuery) throws OWLException {
		int count = 0;
		try (GraphOWLResultSet rs = st.executeGraphQuery(graphQuery)) {
			while(rs.hasNext()) {
				rs.next();
				count++;
			}
		}
		return count;
	}
}
