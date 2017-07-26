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
import it.unibz.inf.ontop.ontology.*;
import it.unibz.inf.ontop.owlapi.OWLAPITranslatorUtility;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
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

	private OntopOWLConnection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());

//	final String owlFile = "src/test/resources/emptiesDatabase.owl";
//	final String obdaFile = "src/test/resources/emptiesDatabase.obda";
	
	 final String owlFile =
	 "/testcases-docker/virtual-mode/stockexchange/simplecq/stockexchange.owl";
	 final String obdaFile =
	 "/testcases-docker/virtual-mode/stockexchange/simplecq/stockexchange-mysql.obda";
	final String propertyFile =
			"/testcases-docker/virtual-mode/stockexchange/simplecq/stockexchange-mysql.properties";
	
	private List<String> emptyConcepts = new ArrayList<String>();
	private List<String> emptyRoles = new ArrayList<String>();
	private Set<ClassExpression> emptyBasicConcepts = new HashSet<ClassExpression>();
	private Set<Description> emptyProperties = new HashSet<Description>();

	private OntopOWLReasoner reasoner;
	private Ontology onto;

	@Before
	public void setUp() throws Exception {

		String owlFileName =  this.getClass().getResource(owlFile).toString();
		String obdaFileName =  this.getClass().getResource(obdaFile).toString();
		String propertyFileName =  this.getClass().getResource(propertyFile).toString();

		// Creating a new instance of the reasoner
        // Creating a new instance of the reasoner
        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.enableFullMetadataExtraction(false)
				.ontologyFile(owlFileName)
				.nativeOntopMappingFile(obdaFileName)
				.propertyFile(propertyFileName)
				.enableTestMode()
				.build();
        reasoner = factory.createReasoner(config);


		// Now we are ready for querying
		conn = reasoner.getConnection();

		onto = OWLAPITranslatorUtility.translate(config.loadProvidedInputOntology());
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
		OntopOWLStatement st = conn.createStatement();
		try {
			QuestOWLResultSet rs = st.executeTuple(query);
			return (rs.hasNext());

		} catch (Exception e) {
			throw e;
		} finally {
			try {

			} catch (Exception e) {
				st.close();
				throw e;
			}
			st.close();
			// conn.close();

		}

	}

	private boolean runSPARQLRolesQuery(String description) throws Exception {
		String query = "SELECT * WHERE {?x " + description + " ?y.}";
		OntopOWLStatement st = conn.createStatement();
		try {
			QuestOWLResultSet rs = st.executeTuple(query);
			return (rs.hasNext());

		} catch (Exception e) {
			throw e;
		} finally {
			try {

			} catch (Exception e) {
				st.close();
				throw e;
			}
			// conn.close();
			st.close();

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
		for (OClass cl : onto.getVocabulary().getClasses()) {
			String concept = cl.getName();
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
		for (ObjectPropertyExpression prop : onto.getVocabulary().getObjectProperties()) {
			String role = prop.getName();
			if (!runSPARQLRolesQuery("<" + role + ">")) {
				emptyRoles.add(role);
				r++;
			}
		}
		log.info(r + " Empty role/s: " + emptyRoles);

		r = 0; // number of empty roles
		for (DataPropertyExpression prop : onto.getVocabulary().getDataProperties()) {
			String role = prop.getName();
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
		for (OClass cl : onto.getVocabulary().getClasses()) {
			String concept = cl.getName();
			if (!runSPARQLConceptsQuery("<" + concept + ">")) {
				emptyConcepts.add(concept);
				c++;
			}
		}
		log.info(c + " Empty concept/s: " + emptyConcepts);

		int r = 0; // number of empty roles
		for (ObjectPropertyExpression prop : onto.getVocabulary().getObjectProperties()) {
			String role = prop.getName();
			if (!runSPARQLRolesQuery("<" + role + ">")) {
				emptyRoles.add(role);
				r++;
			}
		}
		log.info(r + " Empty role/s: " + emptyRoles);

		r = 0; // number of empty roles
		for (DataPropertyExpression prop : onto.getVocabulary().getDataProperties()) {
			String role = prop.getName();
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
		TBoxReasoner tboxreasoner = TBoxReasonerImpl.create(onto);
		System.out.println();
		System.out.println(tboxreasoner.getObjectPropertyDAG());

		int c = 0; // number of empty concepts
		for (Equivalences<ClassExpression> concept : tboxreasoner.getClassDAG()) {
			ClassExpression representative = concept.getRepresentative();
			if ((!(representative instanceof Datatype)) && !runSPARQLConceptsQuery("<" + concept.getRepresentative().toString() + ">")) {
				emptyBasicConcepts.addAll(concept.getMembers());
				c += concept.size();
			}
		}
		log.info(c + " Empty concept/s: " + emptyConcepts);

		{
			int r = 0; // number of empty roles
			for (Equivalences<ObjectPropertyExpression> properties : tboxreasoner.getObjectPropertyDAG()) {
				if (!runSPARQLRolesQuery("<" + properties.getRepresentative().toString() + ">")) {
					emptyProperties.addAll(properties.getMembers());
					r += properties.size();
				}
			}
			log.info(r + " Empty role/s: " + emptyRoles);
		}
		{
			int r = 0; // number of empty roles
			for (Equivalences<DataPropertyExpression> properties : tboxreasoner.getDataPropertyDAG()) {
				if (!runSPARQLRolesQuery("<" + properties.getRepresentative().toString() + ">")) {
					emptyProperties.addAll(properties.getMembers());
					r += properties.size();
				}
			}
			log.info(r + " Empty role/s: " + emptyRoles);
		}
	}

}
