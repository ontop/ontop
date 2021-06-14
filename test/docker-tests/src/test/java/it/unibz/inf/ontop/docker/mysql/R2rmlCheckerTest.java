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
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import it.unibz.inf.ontop.owlapi.validation.OntopOWLEmptyEntitiesChecker;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.spec.ontology.owlapi.OWLAPITranslatorOWL2QL;
import org.apache.commons.rdf.api.IRI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Class to test that the r2rml file with the mappings give the same results of the corresponding obda file.
 * We use the npd database.
 */

public class R2rmlCheckerTest {
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private static final String owlFile = "/mysql/npd/npd-v2-ql_a.owl";
	private static final String obdaFile = "/mysql/npd/npd-v2-ql_a.obda";
	private static final String propertyFile = "/mysql/npd/npd-v2-ql_a.properties";
	private static final String r2rmlFile = "/mysql/npd/npd-v2-ql_a.ttl";

    private final String owlFileName =  this.getClass().getResource(owlFile).toString();
	private final String obdaFileName =  this.getClass().getResource(obdaFile).toString();
	private final String r2rmlFileName =  this.getClass().getResource(r2rmlFile).toString();
	private final String propertyFileName =  this.getClass().getResource(propertyFile).toString();

	private ClassifiedTBox onto;
	private OntopOWLReasoner reasonerOBDA;
	private OntopOWLReasoner reasonerR2rml;

    @Before
	public void setUp() throws Exception {
		log.info("Loading obda file");
		OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
		OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.nativeOntopMappingFile(obdaFileName)
				.ontologyFile(owlFileName)
				.propertyFile(propertyFileName)
				.enableTestMode()
				.build();
		reasonerOBDA = factory.createReasoner(config);

		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology owl = man.loadOntologyFromOntologyDocument(new File(new URL(owlFileName).getPath()));
		Ontology onto1 = config.getInjector().getInstance(OWLAPITranslatorOWL2QL.class)
				.translateAndClassify(owl);
		onto = onto1.tbox();


		log.info("Loading r2rml file");
		OntopOWLFactory factory1 = OntopOWLFactory.defaultFactory();
		OntopSQLOWLAPIConfiguration config1 = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.r2rmlMappingFile(r2rmlFileName)
				.ontologyFile(owlFileName)
				.propertyFile(propertyFileName)
				.enableTestMode()
				.build();
		reasonerR2rml = factory1.createReasoner(config1);
	}

	@After
	public void tearDown() {
		if (reasonerOBDA != null)
			reasonerOBDA.dispose();
		if (reasonerR2rml != null)
			reasonerR2rml.dispose();
	}

	//TODO:  extract the two OBDA specifications to compare the mapping objects

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

			log.debug("Comparing concepts");
			for (OClass cl : onto.classes()) {
				String concept = cl.getIRI().getIRIString();
				log.debug("class " + concept);
				int conceptOBDA = runSPARQLConceptsQuery("<" + concept + ">", obdaConnection);
				int conceptR2rml = runSPARQLConceptsQuery("<" + concept + ">", r2rmlConnection);
				assertEquals(conceptOBDA, conceptR2rml);
			}

			log.debug("Comparing object properties");
			for (ObjectPropertyExpression prop : onto.objectProperties()) {
				String role = prop.getIRI().getIRIString();
				log.debug("object property " + role);
				int roleOBDA = runSPARQLRolesQuery("<" + role + ">", obdaConnection);
				int roleR2rml = runSPARQLRolesQuery("<" + role + ">", r2rmlConnection);
				assertEquals(roleOBDA, roleR2rml);
			}

			log.debug("Comparing data properties");
			for (DataPropertyExpression prop : onto.dataProperties()) {
				String role = prop.getIRI().getIRIString();
				log.debug("data property " + role);
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
	public void testOBDAEmpties()  {

		OntopOWLEmptyEntitiesChecker empties = new OntopOWLEmptyEntitiesChecker(onto, reasonerOBDA.getConnection());

		List<IRI> emptyConceptsObda = new ArrayList<>();
		for (IRI iri : empties.emptyClasses()) {
			emptyConceptsObda.add(iri);
		}
		log.info("Empty concepts: " + emptyConceptsObda);
		assertEquals(162, emptyConceptsObda.size());

		List<IRI> emptyRolesObda = new ArrayList<>();
		for (IRI iri : empties.emptyProperties()) {
			emptyRolesObda.add(iri);
		}
		log.info("Empty roles: " + emptyRolesObda);
		assertEquals(46, emptyRolesObda.size());
	}

	/**
	 * Test numbers of empty concepts and roles of npd using the r2rml mapping
	 * 
	 * @throws Exception
	 */
//	@Test
	public void testR2rmlEmpties() {

		OntopOWLEmptyEntitiesChecker empties = new OntopOWLEmptyEntitiesChecker(onto, reasonerR2rml.getConnection());

		List<IRI> emptyConceptsR2rml = new ArrayList<>();
		for (IRI iri : empties.emptyClasses()) {
			emptyConceptsR2rml.add(iri);
		}
		log.info("Empty concepts: " + emptyConceptsR2rml);
		assertEquals(162, emptyConceptsR2rml.size());

		List<IRI> emptyRolesR2rml = new ArrayList<>();
		for (IRI iri : empties.emptyProperties()) {
			emptyRolesR2rml.add(iri);
		}
		log.info("Empty roles: " + emptyRolesR2rml);
		assertEquals(46, emptyRolesR2rml.size());
	}

	/**
	 * Compare numbers of result given by the obda file and the r2rml file over an npd query 
	 * 
	 * @throws Exception
	 */
//	@Test
	public void testComparesNpdQuery() throws Exception {
		int obdaResult = npdQuery(reasonerOBDA.getConnection());
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
		log.debug("Comparing roles");
		int roleOBDA = runSPARQLRolesQuery("<http://sws.ifi.uio.no/vocab/npd-v2#utmEW>", reasonerOBDA.getConnection());
		int roleR2rml = runSPARQLRolesQuery("<http://sws.ifi.uio.no/vocab/npd-v2#utmEW>", reasonerR2rml.getConnection());
		assertEquals(roleOBDA, roleR2rml);
	}
	
	/**
	 * Compare the results of r2rml and obda files over one role
	 * Added the filter to give as results only Literals
	 *
	 * @throws Exception
	 */
	@Test
	public void testOneRoleFilterLiterals() throws Exception {
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
		try (OWLStatement st = OWLConnection.createStatement()) {
			TupleOWLResultSet rs = st.executeSelectQuery(query);
			int n = 0;
			while (rs.hasNext()) {
				n++;
			}
			log.debug("number of results of q1: " + n);
			return n;
		}
	}

	private int runSPARQLConceptsQuery(String description,	OWLConnection conn) throws Exception {
		String query = "SELECT ?x WHERE {?x a " + description + ".}";
		try (OWLStatement st = conn.createStatement()) {
			TupleOWLResultSet  rs = st.executeSelectQuery(query);
			int n = 0;
			while (rs.hasNext()) {
				rs.next();
				n++;
			}
			return n;
		}
	}

	private int runSPARQLRolesQuery(String description, OWLConnection conn) throws Exception {
		String query = "SELECT * WHERE {?x " + description + " ?y.}";
		try (OWLStatement st = conn.createStatement()) {
			TupleOWLResultSet  rs = st.executeSelectQuery(query);
			int n = 0;
			while (rs.hasNext()) {
                OWLBindingSet bindingSet = rs.next();
				if (n == 0) {
                    log.debug("result : "  + bindingSet.getOWLObject("x"));
					log.debug("result : "  + bindingSet.getOWLObject("y"));
				}
				n++;
			}
			return n;
		}
	}
	
	private int runSPARQLRoleFilterQuery(String description, OWLConnection connection) throws OWLException {
		String query = "SELECT * WHERE {?x " + description + " ?y. FILTER(isLiteral(?y))}";
		try (OWLStatement st = connection.createStatement()) {
			TupleOWLResultSet  rs = st.executeSelectQuery(query);
			int n = 0;
			while (rs.hasNext()) {
                OWLBindingSet bindingSet = rs.next();
                if (n == 0) {
					log.debug("result : "  + bindingSet.getOWLObject("x"));
					log.debug("result : "  + bindingSet.getOWLLiteral("y"));
				}
				n++;
			}
			return n;
		}
	}
}
