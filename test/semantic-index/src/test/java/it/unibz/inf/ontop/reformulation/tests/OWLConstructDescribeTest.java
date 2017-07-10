package it.unibz.inf.ontop.reformulation.tests;

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

import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import it.unibz.inf.ontop.si.OntopSemanticIndexLoader;
import org.junit.*;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.List;
import java.util.Properties;

/**
 * This unit test is for testing correctness of construct and describe queries
 * in ontop from the owl api. It is the same as SesameConstructDescribe for the
 * Sesame API, with the only difference that all abox data comes from the owl
 * file as declared named individuals and axioms, AND a property cannot have
 * both constant and uri objects. It must be clear if it's a data property or
 * object property.
 */
public class OWLConstructDescribeTest{

	QuestOWL reasoner = null;
	OntopOWLConnection conn = null;
	OntopOWLStatement st = null;
	String owlFile = "src/test/resources/describeConstruct.owl";
	
	@Before
	public void setUp() throws Exception {
		try(OntopSemanticIndexLoader loader = OntopSemanticIndexLoader.loadOntologyIndividuals(owlFile, new Properties())) {
			QuestOWLFactory factory = new QuestOWLFactory();
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
		List<OWLAxiom> rs = st.executeGraph(query);
		Assert.assertEquals(4, rs.size());
	}
	
	@Test
	public void testDescribeUri0() throws Exception {
		String query = "DESCRIBE <http://www.semanticweb.org/ontologies/test#p1>";
		List<OWLAxiom> rs = st.executeGraph(query);
		Assert.assertEquals(0, rs.size());
	}
	
	@Test
	public void testDescribeUri1() throws Exception {
		String query = "DESCRIBE <http://example.org/D>";
		List<OWLAxiom> rs = st.executeGraph(query);
		Assert.assertEquals(1, rs.size());
	}
	
	@Test
	public void testDescribeUri2() throws Exception {
		String query = "DESCRIBE <http://example.org/C>";
		List<OWLAxiom> rs = st.executeGraph(query);
		Assert.assertEquals(2, rs.size());
	}
	
	@Test
	public void testDescribeVar0() throws Exception {
		String query = "DESCRIBE ?x WHERE {<http://example.org/C> ?x ?y }";
		List<OWLAxiom> rs = st.executeGraph(query);
		Assert.assertEquals(0, rs.size());
	}
	
	@Test
	public void testDescribeVar1() throws Exception {
		String query = "DESCRIBE ?x WHERE {?x <http://www.semanticweb.org/ontologies/test#p2> <http://example.org/A>}";
		List<OWLAxiom> rs = st.executeGraph(query);
		Assert.assertEquals(1, rs.size());
	}
	
	@Test
	public void testDescribeVar2() throws Exception {
		String query = "DESCRIBE ?x WHERE {?x <http://www.semanticweb.org/ontologies/test#p1> ?y}";
		List<OWLAxiom> rs = st.executeGraph(query);
		Assert.assertEquals(2, rs.size());
	}
	
	@Test
	public void testConstruct0() throws Exception {
		String query = "CONSTRUCT {?s ?p <http://www.semanticweb.org/ontologies/test/p1>} WHERE {?s ?p <http://www.semanticweb.org/ontologies/test/p1>}";
		List<OWLAxiom> rs = st.executeGraph(query);
		Assert.assertEquals(0, rs.size());
	}
	
	@Test
	public void testConstruct1() throws Exception {
		String query = "CONSTRUCT { ?s ?p <http://example.org/D> } WHERE { ?s ?p <http://example.org/D>}";
		List<OWLAxiom> rs = st.executeGraph(query);
		Assert.assertEquals(1, rs.size());
	}
	
	@Test
	public void testConstruct2() throws Exception {
		String query = "CONSTRUCT {<http://example.org/C> ?p ?o} WHERE {<http://example.org/C> ?p ?o}";
		List<OWLAxiom> rs = st.executeGraph(query);
		Assert.assertEquals(2, rs.size());
	}
}
