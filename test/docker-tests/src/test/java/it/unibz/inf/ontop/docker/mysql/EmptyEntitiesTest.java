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

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.spec.ontology.Equivalences;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.spec.ontology.owlapi.OWLAPITranslatorOWL2QL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/***
 * Test returns  empty concepts and roles, based on the mappings.
 * Given an ontology, which is connected to a database via mappings,
 * generate a suitable set of queries that test if there are empty concepts,
 *  concepts that are no populated to anything.
 */

public class EmptyEntitiesTest {

	private OWLConnection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());

    final String owlFile =
	 "/testcases-docker/virtual-mode/stockexchange/simplecq/stockexchange.owl";
    final String obdaFile =
	 "/testcases-docker/virtual-mode/stockexchange/simplecq/stockexchange-mysql.obda";
	final String propertyFile =
			"/testcases-docker/virtual-mode/stockexchange/simplecq/stockexchange-mysql.properties";
	
	private List<String> emptyConcepts = new ArrayList<>();
	private List<String> emptyRoles = new ArrayList<>();
	private Set<ClassExpression> emptyBasicConcepts = new HashSet<>();
	private Set<Description> emptyProperties = new HashSet<>();

	private OntopOWLReasoner reasoner;
	private ClassifiedTBox onto;

	@Before
	public void setUp() throws Exception {

		String owlFileName =  getClass().getResource(owlFile).toString();
		String obdaFileName =  getClass().getResource(obdaFile).toString();
		String propertyFileName = getClass().getResource(propertyFile).toString();

		// Creating a new instance of the reasoner
        // Creating a new instance of the reasoner
        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.ontologyFile(owlFileName)
				.nativeOntopMappingFile(obdaFileName)
				.propertyFile(propertyFileName)
				.enableTestMode()
				.build();
        reasoner = factory.createReasoner(config);

		// Now we are ready for querying
		conn = reasoner.getConnection();

		onto = config.getInjector().getInstance(OWLAPITranslatorOWL2QL.class)
				.translateAndClassify(config.loadProvidedInputOntology()).tbox();
	}

	@After
	public void tearDown() throws Exception {
//			dropTables();
			reasoner.dispose();
//			connection.close();
	}

//	private void dropTables() throws SQLException, IOException {
//
//		Statement st = connection.createStatement();
//
//		FileReader reader = new FileReader("src/test/resources/emptiesDatabase-drop-h2.sql");
//		BufferedReader in = new BufferedReader(reader);
//		StringBuilder bf = new StringBuilder();
//		String line = in.readLine();
//		while (line != null) {
//			bf.append(line);
//			line = in.readLine();
//		}
//
//		st.executeUpdate(bf.toString());
//		st.close();
//		connection.commit();
//	}

	private boolean runSPARQLConceptsQuery(String description) throws Exception {
		String query = "SELECT ?x WHERE {?x a " + description + ".}";
		try (OWLStatement st = conn.createStatement()) {
			TupleOWLResultSet rs = st.executeSelectQuery(query);
			return (rs.hasNext());
		}
	}

	private boolean runSPARQLRolesQuery(String description) throws Exception {
		String query = "SELECT * WHERE {?x " + description + " ?y.}";
		try (OWLStatement st = conn.createStatement()) {
			TupleOWLResultSet  rs = st.executeSelectQuery(query);
			return (rs.hasNext());
		}
	}

	/**
	 * Test numbers of empty concepts
	 * 
	 * @throws Exception
	 */
	@Test
	public void testEmptyConcepts() throws Exception {
		int c = 0; // number of empty concepts
		for (OClass cl : onto.classes()) {
			String concept = cl.getIRI().getIRIString();
			if (!runSPARQLConceptsQuery("<" + concept + ">")) {
				emptyConcepts.add(concept);
				c++;
			}
		}
		log.info(c + " Empty concept/s: " + emptyConcepts);

	}

	/**
	 * Test numbers of empty roles
	 * 
	 * @throws Exception
	 */
	@Test
	public void testEmptyRoles() throws Exception {
		int r = 0; // number of empty roles
		for (ObjectPropertyExpression prop : onto.objectProperties()) {
			String role = prop.getIRI().getIRIString();
			if (!runSPARQLRolesQuery("<" + role + ">")) {
				emptyRoles.add(role);
				r++;
			}
		}
		log.info(r + " Empty role/s: " + emptyRoles);

		r = 0; // number of empty roles
		for (DataPropertyExpression prop : onto.dataProperties()) {
			String role = prop.getIRI().getIRIString();
			if (!runSPARQLRolesQuery("<" + role + ">")) {
				emptyRoles.add(role);
				r++;
			}
		}
		log.info(r + " Empty role/s: " + emptyRoles);
	}

	/**
	 * Test numbers of empty concepts and roles
	 * 
	 * @throws Exception
	 */
	@Test
	public void testEmpties() throws Exception {

		int c = 0; // number of empty concepts
		for (OClass cl : onto.classes()) {
			String concept = cl.getIRI().getIRIString();
			if (!runSPARQLConceptsQuery("<" + concept + ">")) {
				emptyConcepts.add(concept);
				c++;
			}
		}
		log.info(c + " Empty concept/s: " + emptyConcepts);

		int r = 0; // number of empty roles
		for (ObjectPropertyExpression prop : onto.objectProperties()) {
			String role = prop.getIRI().getIRIString();
			if (!runSPARQLRolesQuery("<" + role + ">")) {
				emptyRoles.add(role);
				r++;
			}
		}
		log.info(r + " Empty role/s: " + emptyRoles);

		r = 0; // number of empty roles
		for (DataPropertyExpression prop : onto.dataProperties()) {
			String role = prop.getIRI().getIRIString();
			if (!runSPARQLRolesQuery("<" + role + ">")) {
				emptyRoles.add(role);
				r++;
			}
		}
		log.info(r + " Empty role/s: " + emptyRoles);
	}

	/**
	 * Test numbers of empty concepts and roles considering existential and
	 * inverses
	 * Cannot work until inverses and existentials are considered  in the Abox
	 * @throws Exception
	 */
	// @Test
	public void testEmptiesWithInverses() throws Exception {
		System.out.println();
		System.out.println(onto.objectPropertiesDAG());

		int c = 0; // number of empty concepts
		for (Equivalences<ClassExpression> concept : onto.classesDAG()) {
			ClassExpression representative = concept.getRepresentative();
			if ((!(representative instanceof Datatype)) && !runSPARQLConceptsQuery("<" + concept.getRepresentative() + ">")) {
				emptyBasicConcepts.addAll(concept.getMembers());
				c += concept.size();
			}
		}
		log.info(c + " Empty concept/s: " + emptyConcepts);

		{
			int r = 0; // number of empty roles
			for (Equivalences<ObjectPropertyExpression> properties : onto.objectPropertiesDAG()) {
				if (!runSPARQLRolesQuery("<" + properties.getRepresentative() + ">")) {
					emptyProperties.addAll(properties.getMembers());
					r += properties.size();
				}
			}
			log.info(r + " Empty role/s: " + emptyRoles);
		}
		{
			int r = 0; // number of empty roles
			for (Equivalences<DataPropertyExpression> properties : onto.dataPropertiesDAG()) {
				if (!runSPARQLRolesQuery("<" + properties.getRepresentative() + ">")) {
					emptyProperties.addAll(properties.getMembers());
					r += properties.size();
				}
			}
			log.info(r + " Empty role/s: " + emptyRoles);
		}
	}
}
