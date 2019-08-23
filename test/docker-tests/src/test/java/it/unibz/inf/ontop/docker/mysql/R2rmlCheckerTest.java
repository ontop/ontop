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

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import it.unibz.inf.ontop.owlapi.validation.QuestOWLEmptyEntitiesChecker;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.spec.ontology.owlapi.OWLAPITranslatorOWL2QL;
import org.apache.commons.rdf.api.IRI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static it.unibz.inf.ontop.docker.utils.DockerTestingTools.OWLAPI_TRANSLATOR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Class to test that the r2rml file with the mappings give the same results of the corresponding obda file.
 * We use the npd database.
 */

public class R2rmlCheckerTest {
	private OWLConnection conn;

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private ClassifiedTBox onto;

	final String owlFile = "/mysql/npd/npd-v2-ql_a.owl";
	final String obdaFile = "/mysql/npd/npd-v2-ql_a.obda";
	final String propertyFile = "/mysql/npd/npd-v2-ql_a.properties";
	final String r2rmlFile = "/mysql/npd/npd-v2-ql_a.ttl";

    final String owlFileName =  this.getClass().getResource(owlFile).toString();
	final String obdaFileName =  this.getClass().getResource(obdaFile).toString();
	final String r2rmlFileName =  this.getClass().getResource(r2rmlFile).toString();
	final String propertyFileName =  this.getClass().getResource(propertyFile).toString();

	private List<IRI> emptyConceptsObda = new ArrayList<>();
	private List<IRI> emptyRolesObda = new ArrayList<>();
	private List<IRI> emptyConceptsR2rml = new ArrayList<>();
	private List<IRI> emptyRolesR2rml = new ArrayList<>();

	private OntopOWLReasoner reasonerOBDA;
	private OntopOWLReasoner reasonerR2rml;

    @Before
	public void setUp() throws Exception {
		onto = loadOntologyFromFileAndClassify(new URL(owlFileName).getPath());

		loadOBDA();
		loadR2rml();
	}

	@After
	public void tearDown() {
		try {
			if (reasonerOBDA!=null) {
				reasonerOBDA.dispose();
			}
			if (reasonerR2rml!=null) {
				reasonerR2rml.dispose();
			}
		}
		catch (Exception e) {
			log.debug(e.getMessage());
			assertTrue(false);
		}
	}

	//TODO:  extract the two OBDA specifications to compare the mapping objects
//	@Test Cannot get anymore the unfolder rules,
//	public void testMappings() throws Exception {
//		for (CQIE q : reasonerOBDA.getQuestInstance().getUnfolderRules()) {
//			if (!reasonerR2rml.getQuestInstance().getUnfolderRules().contains(q))
//				System.out.println("NOT IN R2RML: " + q);
//		}
//		for (CQIE q : reasonerR2rml.getQuestInstance().getUnfolderRules()) {
//			if (!reasonerOBDA.getQuestInstance().getUnfolderRules().contains(q))
//				System.out.println("NOT IN OBDA: " + q);
//		}
//	}

	/**
	 * Check the number of descriptions retrieved by the obda mapping and the
	 * r2rml mapping is the same
	 * 
	 * @throws Exception
	 */



	@Test
	public void testDescriptionsCheck() throws Exception {

		try (OWLConnection obdaConnection = reasonerOBDA.getConnection();
			 OWLConnection r2rmlConnection = reasonerR2rml.getConnection()) {

			// Now we are ready for querying
			log.debug("Comparing concepts");
			for (OClass cl : onto.classes()) {
				String concept = cl.getIRI().getIRIString();

				int conceptOBDA = runSPARQLConceptsQuery("<" + concept + ">", obdaConnection);
				int conceptR2rml = runSPARQLConceptsQuery("<" + concept + ">", r2rmlConnection);

				assertEquals(conceptOBDA, conceptR2rml);
			}

			log.debug("Comparing object properties");
			for (ObjectPropertyExpression prop : onto.objectProperties()) {
				String role = prop.getIRI().getIRIString();

				log.debug("description " + role);
				int roleOBDA = runSPARQLRolesQuery("<" + role + ">", obdaConnection);
				int roleR2rml = runSPARQLRolesQuery("<" + role + ">", r2rmlConnection);

				assertEquals(roleOBDA, roleR2rml);
			}

			log.debug("Comparing data properties");
			for (DataPropertyExpression prop : onto.dataProperties()) {
				String role = prop.getIRI().getIRIString();

				log.debug("description " + role);
				int roleOBDA = runSPARQLRolesQuery("<" + role + ">", obdaConnection);
				int roleR2rml = runSPARQLRolesQuery("<" + role + ">", r2rmlConnection);

				assertEquals(roleOBDA, roleR2rml);
			}
		}
	}

	/**
	 * Test numbers of empty concepts and roles of npd using the obda mapping
	 * 
	 * @throws Exception
	 */
//	@Test
	public void testOBDAEmpties() throws Exception {

		// Now we are ready for querying
		conn = reasonerOBDA.getConnection();
		QuestOWLEmptyEntitiesChecker empties = new QuestOWLEmptyEntitiesChecker(onto, conn);
		Iterator<IRI> iteratorC = empties.iEmptyConcepts();
		while (iteratorC.hasNext()){
			emptyConceptsObda.add(iteratorC.next());
		}
		log.info(empties.toString());
		log.info("Empty concept/s: " + emptyConceptsObda);
		assertEquals(162, emptyConceptsObda.size());

		Iterator<IRI> iteratorR = empties.iEmptyRoles();
		while (iteratorR.hasNext()){
			emptyRolesObda.add(iteratorR.next());
		}
		log.info("Empty role/s: " + emptyRolesObda);
		assertEquals(46, emptyRolesObda.size());

	}

	/**
	 * Test numbers of empty concepts and roles of npd using the r2rml mapping
	 * 
	 * @throws Exception
	 */
//	@Test
	public void testR2rmlEmpties() throws Exception {

		// Now we are ready for querying
		conn = reasonerR2rml.getConnection();
		QuestOWLEmptyEntitiesChecker empties = new QuestOWLEmptyEntitiesChecker(onto, conn);
		Iterator<IRI> iteratorC = empties.iEmptyConcepts();
		while (iteratorC.hasNext()){
			emptyConceptsR2rml.add(iteratorC.next());
		}
		log.info(empties.toString());
		log.info("Empty concept/s: " + emptyConceptsR2rml);
		assertEquals(162, emptyConceptsR2rml.size());

		Iterator<IRI> iteratorR = empties.iEmptyRoles();
		while (iteratorR.hasNext()){
			emptyRolesR2rml.add(iteratorR.next());
		}
		log.info("Empty role/s: " + emptyRolesR2rml);
		assertEquals(46, emptyRolesR2rml.size());
	}

	/**
	 * Compare numbers of result given by the obda file and the r2rml file over an npd query 
	 * 
	 * @throws Exception
	 */
//	@Test
	public void testComparesNpdQuery() throws Exception {
		
		// Now we are ready for querying obda
		// npd query 1
		int obdaResult = npdQuery(reasonerOBDA.getConnection());
		// reasoner.dispose();


		// Now we are ready for querying r2rml
		// npd query 1
		int r2rmlResult = npdQuery(reasonerR2rml.getConnection());
		
		assertEquals(obdaResult, r2rmlResult);

	}

	/**
	 * Compare the results of r2rml and obda files over one role
	 * Try <http://sws.ifi.uio.no/vocab/npd-v2#factMapURL> for the case of termtype set to IRI
	 * Try <http://sws.ifi.uio.no/vocab/npd-v2#dateSyncNPD> or <http://sws.ifi.uio.no/vocab/npd-v2#dateBaaLicenseeValidTo>  to test typed literal
	 * Try <http://sws.ifi.uio.no/vocab/npd-v2#sensorLength>, <http://sws.ifi.uio.no/vocab/npd-v2#wellboreHoleDiameter> or <http://sws.ifi.uio.no/vocab/npd-v2#isMultilateral> for a plain Literal
	 *
	 * @throws Exception
	 */
	@Test
	public void testOneRole() throws Exception {

		// Now we are ready for querying
		log.debug("Comparing roles");

			int roleOBDA = runSPARQLRolesQuery("<http://sws.ifi.uio.no/vocab/npd-v2#utmEW>",
					reasonerOBDA.getConnection());
			int roleR2rml = runSPARQLRolesQuery("<http://sws.ifi.uio.no/vocab/npd-v2#utmEW>",
					reasonerR2rml.getConnection());

			assertEquals(roleOBDA, roleR2rml);

		
	}
	
	/**
	 * Compare the results of r2rml and obda files over one role
	 * Added the filter to give as results only Literals
	 * 
	 *
	 * @throws Exception
	 */
	@Test
	public void testOneRoleFilterLiterals() throws Exception {

		// Now we are ready for querying
		log.debug("Comparing roles");

		int roleOBDA = runSPARQLRoleFilterQuery("<http://sws.ifi.uio.no/vocab/npd-v2#name>", reasonerOBDA.getConnection());
		int roleR2rml = runSPARQLRoleFilterQuery("<http://sws.ifi.uio.no/vocab/npd-v2#name>", reasonerR2rml.getConnection());

		assertEquals(roleOBDA, roleR2rml);

	}
	

	/**
	 * Execute Npd query 1 and give the number of results
	 * @return 
	 */
	private int npdQuery(OWLConnection OWLConnection) throws OWLException {
		String query = "PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#> SELECT DISTINCT ?licenceURI WHERE { ?licenceURI a npdv:ProductionLicence ."
				+ "[ ] a npdv:ProductionLicenceLicensee ; "
				+ "npdv:dateLicenseeValidFrom ?date ;"
				+ "npdv:licenseeInterest ?interest ;"
				+ "npdv:licenseeForLicence ?licenceURI . "
				+ "FILTER(?date > '1979-12-31T00:00:00')	}";
		OWLStatement st = OWLConnection.createStatement();
		int n = 0;
		try {
			TupleOWLResultSet rs = st.executeSelectQuery(query);
			while (rs.hasNext()) {
				n++;
			}
			log.debug("number of results of q1: " + n);

		} catch (Exception e) {
			throw e;
		} finally {
			try {

			} catch (Exception e) {
				st.close();
				assertTrue(false);
			}
			// conn.close();
			st.close();

		}
		return n;

	}

	/**
	 * create obda model from r2rml and prepare the reasoner
	 * 
	 *
	 * @throws Exception
	 */
	private void loadR2rml() throws OWLOntologyCreationException {
		log.info("Loading r2rml file");
		OntopOWLFactory factory = OntopOWLFactory.defaultFactory();

		OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.r2rmlMappingFile(r2rmlFileName)
				.ontologyFile(owlFileName)
				.propertyFile(propertyFileName)
				.enableTestMode()
				.build();
        reasonerR2rml = factory.createReasoner(config);
	}

	/**
	 * Create obda model from obda file and prepare the reasoner
	 * 
	 *
	 */

	private void loadOBDA() throws Exception {
		// Loading the OBDA data
		log.info("Loading obda file");

		// Creating a new instance of the reasoner
		OntopOWLFactory factory = OntopOWLFactory.defaultFactory();

		OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.nativeOntopMappingFile(obdaFileName)
				.propertyFile(propertyFileName)
				.ontologyFile(owlFileName)
				.enableTestMode()
				.build();
		reasonerOBDA = factory.createReasoner(config);
	}

	private int runSPARQLConceptsQuery(String description,	OWLConnection conn) throws Exception {
		String query = "SELECT ?x WHERE {?x a " + description + ".}";
		OWLStatement st = conn.createStatement();
		int n = 0;
		try {
			TupleOWLResultSet  rs = st.executeSelectQuery(query);
			while (rs.hasNext()) {
				rs.next();
				n++;
			}
			// log.info("description: " + n);
			return n;

		} catch (Exception e) {
			throw e;
		} finally {
			try {

			} catch (Exception e) {
				st.close();
				assertTrue(false);
			}
			st.close();
			// conn.close();

		}

	}

	private int runSPARQLRolesQuery(String description, OWLConnection conn) throws Exception {
		String query = "SELECT * WHERE {?x " + description + " ?y.}";
		OWLStatement st = conn.createStatement();
		int n = 0;
		try {
			TupleOWLResultSet  rs = st.executeSelectQuery(query);
			while (rs.hasNext()) {
//				log.debug("result : "  + rs.getOWLObject("x"));
//				log.debug("result : "  + rs.getOWLObject("y"));
//				log.debug("result : "  + rs.getOWLLiteral("y"));
				
                final OWLBindingSet bindingSet = rs.next();
				if(n==0){
                    log.debug("result : "  + bindingSet.getOWLObject("x"));
					log.debug("result : "  + bindingSet.getOWLObject("y"));
				}
				n++;
			}
			
			return n;

		} catch (Exception e) {
			log.debug(e.toString());
			throw e;

		} finally {
			try {

			} catch (Exception e) {
				st.close();
				assertTrue(false);
			}
			// conn.close();
			st.close();

		}

	}
	
	private int runSPARQLRoleFilterQuery(String description, OWLConnection connection) throws OWLException {
		String query = "SELECT * WHERE {?x " + description + " ?y. FILTER(isLiteral(?y))}";
		OWLStatement st = connection.createStatement();
		int n = 0;
		try {
			TupleOWLResultSet  rs = st.executeSelectQuery(query);
			while (rs.hasNext()) {
                final OWLBindingSet bindingSet = rs.next();
                if(n==0){
					log.debug("result : "  + bindingSet.getOWLObject("x"));
					log.debug("result : "  + bindingSet.getOWLLiteral("y"));
				
				}
				n++;
			}
			
			return n;

		} catch (Exception e) {
			log.debug(e.toString());
			throw e;

		} finally {
			try {

			} catch (Exception e) {
				st.close();
				assertTrue(false);
			}
			// conn.close();
			st.close();
		}
	}

	/**
	 * USE FOR TESTS ONLY
	 *
	 * @param filename
	 * @return
	 * @throws OWLOntologyCreationException
	 */

	public static ClassifiedTBox loadOntologyFromFileAndClassify(String filename) throws OWLOntologyCreationException {
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology owl = man.loadOntologyFromOntologyDocument(new File(filename));
		Ontology onto = OWLAPI_TRANSLATOR.translateAndClassify(owl);
		return onto.tbox();
	}

}
