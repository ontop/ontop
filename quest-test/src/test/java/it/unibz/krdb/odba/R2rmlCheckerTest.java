package it.unibz.krdb.odba;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlapi3.OWLAPI3Translator;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLConnection;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLEmptyEntitiesChecker;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLFactory;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLResultSet;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLStatement;
import it.unibz.krdb.obda.r2rml.R2RMLReader;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to test that the r2rml file with the mappings give the same results of the corresponding obda file.
 * We use the npd database.
 */
public class R2rmlCheckerTest {

	private OBDADataFactory fac;
	private QuestOWLConnection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;
	private Ontology onto;

	final String owlfile = "src/test/resources/r2rml/npd-v2-ql_a.owl";
	final String obdafile = "src/test/resources/r2rml/npd-v2-ql_a.obda";

//	final String r2rmlfile = "src/test/resources/r2rml/npd-v2_uglyVersion.ttl";
	final String r2rmlfile = "src/test/resources/r2rml/npd-v2-ql_a_IRI.ttl";
//	final String r2rmlfile = "src/test/resources/r2rml/npd-v2_pretty.ttl";

	private List<Predicate> emptyConceptsObda = new ArrayList<Predicate>();
	private List<Predicate> emptyRolesObda = new ArrayList<Predicate>();
	private List<Predicate> emptyConceptsR2rml = new ArrayList<Predicate>();
	private List<Predicate> emptyRolesR2rml = new ArrayList<Predicate>();

	private QuestOWL reasonerOBDA;
	private QuestOWL reasonerR2rml;

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {
		try {

			if(reasonerOBDA!=null){
			reasonerOBDA.dispose();
			}
			if(reasonerR2rml!=null){
			reasonerR2rml.dispose();
			}

		} catch (Exception e) {
			log.debug(e.getMessage());
			assertTrue(false);
		}

	}

	/**
	 * Check the number of descriptions retrieved by the obda mapping and the
	 * r2rml mapping is the same
	 * 
	 * @throws Exception
	 */

	@Test
	public void testDescriptionsCheck() throws Exception {
		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager
				.loadOntologyFromOntologyDocument((new File(owlfile)));

		OWLAPI3Translator translator = new OWLAPI3Translator();

		onto = translator.translate(ontology);

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FULL_METADATA,
				QuestConstants.FALSE);

		loadOBDA(p);

		String jdbcurl = "jdbc:mysql://10.7.20.39/npd";
		String username = "fish";
		String password = "fish";
		String driverclass = "com.mysql.jdbc.Driver";

		OBDADataFactory f = OBDADataFactoryImpl.getInstance();
//		String sourceUrl = "http://example.org/customOBDA";
		URI obdaURI =  new File(r2rmlfile).toURI();
		String sourceUrl =obdaURI.toString();
		OBDADataSource dataSource = f.getJDBCDataSource(sourceUrl, jdbcurl,
				username, password, driverclass);

		loadR2rml(p, dataSource);

		// Now we are ready for querying
		log.debug("Comparing concepts");
		for (Predicate concept : onto.getConcepts()) {

			int conceptOBDA = runSPARQLConceptsQuery("<" + concept.getName()
					+ ">", reasonerOBDA.getConnection());
			int conceptR2rml = runSPARQLConceptsQuery("<" + concept.getName()
					+ ">", reasonerR2rml.getConnection());

			assertEquals(conceptOBDA, conceptR2rml);
		}

		log.debug("Comparing roles");
		for (Predicate role : onto.getRoles()) {

			log.debug("description " + role);
			int roleOBDA = runSPARQLRolesQuery("<" + role.getName() + ">",
					reasonerOBDA.getConnection());
			int roleR2rml = runSPARQLRolesQuery("<" + role.getName() + ">",
					reasonerR2rml.getConnection());

			assertEquals(roleOBDA, roleR2rml);
			
		}

	}

	/**
	 * Test numbers of empty concepts and roles of npd using the obda mapping
	 * 
	 * @throws Exception
	 */
	@Test
	public void testOBDA() throws Exception {

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager
				.loadOntologyFromOntologyDocument((new File(owlfile)));

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FULL_METADATA,
				QuestConstants.FALSE);

		loadOBDA(p);

		// Now we are ready for querying
		conn = reasonerOBDA.getConnection();

		QuestOWLEmptyEntitiesChecker empties = new QuestOWLEmptyEntitiesChecker(
				ontology, conn);
		emptyConceptsObda = empties.getEmptyConcepts();
		log.info(empties.toString());
		log.info("Empty concept/s: " + emptyConceptsObda);
		assertEquals(162, emptyConceptsObda.size());

		emptyRolesObda = empties.getEmptyRoles();
		log.info("Empty role/s: " + emptyRolesObda);
		assertEquals(46, emptyRolesObda.size());

	}

	/**
	 * Test numbers of empty concepts and roles of npd using the r2rml mapping
	 * 
	 * @throws Exception
	 */
	@Test
	public void testR2rml() throws Exception {
		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager
				.loadOntologyFromOntologyDocument((new File(owlfile)));

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FULL_METADATA,
				QuestConstants.FALSE);

		String jdbcurl = "jdbc:mysql://10.7.20.39/npd";
		String username = "fish";
		String password = "fish";
		String driverclass = "com.mysql.jdbc.Driver";

		OBDADataFactory f = OBDADataFactoryImpl.getInstance();

//		String sourceUrl = "http://example.org/customOBDA";
		URI obdaURI =  new File(r2rmlfile).toURI();
		String sourceUrl =obdaURI.toString();
		OBDADataSource dataSource = f.getJDBCDataSource(sourceUrl, jdbcurl,
				username, password, driverclass);

		loadR2rml(p, dataSource);

		// Now we are ready for querying
		conn = reasonerR2rml.getConnection();

		QuestOWLEmptyEntitiesChecker empties = new QuestOWLEmptyEntitiesChecker(
				ontology, conn);
		emptyConceptsR2rml = empties.getEmptyConcepts();
		log.info(empties.toString());
		log.info("Empty concept/s: " + emptyConceptsR2rml);
		assertEquals(162, emptyConceptsR2rml.size());

		emptyRolesR2rml = empties.getEmptyRoles();
		log.info("Empty role/s: " + emptyRolesR2rml);
		assertEquals(46, emptyRolesR2rml.size());
	}

	/**
	 * Compare numbers of result given by the obda file and the r2rml file over an npd query 
	 * 
	 * @throws Exception
	 */
	@Test
	public void testComparesNpdQuery() throws Exception {

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager
				.loadOntologyFromOntologyDocument((new File(owlfile)));

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FULL_METADATA,
				QuestConstants.FALSE);

		loadOBDA(p);
		// Now we are ready for querying
		// npd query 1
		int obdaResult = npdQuery(reasonerOBDA.getConnection());
		// reasoner.dispose();

		String jdbcurl = "jdbc:mysql://10.7.20.39/npd";
		String username = "fish";
		String password = "fish";
		String driverclass = "com.mysql.jdbc.Driver";

		OBDADataFactory f = OBDADataFactoryImpl.getInstance();
//		String sourceUrl = "http://example.org/customOBDA";
		URI obdaURI =  new File(r2rmlfile).toURI();
		String sourceUrl =obdaURI.toString();

		OBDADataSource dataSource = f.getJDBCDataSource(sourceUrl, jdbcurl,
				username, password, driverclass);

		loadR2rml(p, dataSource);

		// Now we are ready for querying
		// npd query 1
		int r2rmlResult = npdQuery(reasonerR2rml.getConnection());
		
		assertEquals(obdaResult, r2rmlResult);

	}

	/**
	 * Compare the results of r2rml and obda files over the role <http://sws.ifi.uio.no/vocab/npd-v2#factMapURL>
	 * @throws Exception
	 */
	@Test
	public void testOneRole() throws Exception {

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager
				.loadOntologyFromOntologyDocument((new File(owlfile)));

		OWLAPI3Translator translator = new OWLAPI3Translator();

		onto = translator.translate(ontology);

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FULL_METADATA,
				QuestConstants.FALSE);

		loadOBDA(p);

		String jdbcurl = "jdbc:mysql://10.7.20.39/npd";
		String username = "fish";
		String password = "fish";
		String driverclass = "com.mysql.jdbc.Driver";

		OBDADataFactory f = OBDADataFactoryImpl.getInstance();
//		String sourceUrl = "http://example.org/customOBDA";
		URI obdaURI =  new File(r2rmlfile).toURI();
		String sourceUrl =obdaURI.toString();

		OBDADataSource dataSource = f.getJDBCDataSource(sourceUrl, jdbcurl,
				username, password, driverclass);

		loadR2rml(p, dataSource);

		// Now we are ready for querying
		log.debug("Comparing roles");

			int roleOBDA = runSPARQLRolesQuery("<http://sws.ifi.uio.no/vocab/npd-v2#factMapURL>",
					reasonerOBDA.getConnection());
			int roleR2rml = runSPARQLRolesQuery("<http://sws.ifi.uio.no/vocab/npd-v2#factMapURL>",
					reasonerR2rml.getConnection());

			assertEquals(roleOBDA, roleR2rml);

		
	}
	/**
	 * Execute Npd query 1 and give the number of results
	 * @return 
	 */
	private int npdQuery(QuestOWLConnection questOWLConnection) throws OWLException {
		String query = "PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#> SELECT DISTINCT ?licenceURI WHERE { ?licenceURI a npdv:ProductionLicence ."
				+ "[ ] a npdv:ProductionLicenceLicensee ; "
				+ "npdv:dateLicenseeValidFrom ?date ;"
				+ "npdv:licenseeInterest ?interest ;"
				+ "npdv:licenseeForLicence ?licenceURI . "
				+ "FILTER(?date > '1979-12-31T00:00:00')	}";
		QuestOWLStatement st = questOWLConnection.createStatement();
		int n = 0;
		try {
			QuestOWLResultSet rs = st.executeTuple(query);
			while (rs.nextRow()) {
				n++;
			}
			log.debug("number of results of q1: " + n);

		} catch (Exception e) {
			throw e;
		} finally {
			try {

			} catch (Exception e) {
				st.close();
			}
			// conn.close();
			st.close();

		}
		return n;

	}

	/**
	 * create obda model from r2rml and prepare the reasoner
	 * 
	 * @param p
	 *            quest preferences for QuestOWL, dataSource for the model
	 */
	private void loadR2rml(QuestPreferences p, OBDADataSource dataSource) {
		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();

		factory.setPreferenceHolder(p);

		R2RMLReader reader = new R2RMLReader(r2rmlfile);

		obdaModel = reader.readModel(dataSource);

		factory.setOBDAController(obdaModel);

		reasonerR2rml = (QuestOWL) factory.createReasoner(ontology,
				new SimpleConfiguration());

	}

	/**
	 * Create obda model from obda file and prepare the reasoner
	 * 
	 * @param p
	 *            quest preferences for QuestOWL, dataSource for the model
	 */

	private void loadOBDA(QuestPreferences p) throws Exception {
		// Loading the OBDA data
		fac = OBDADataFactoryImpl.getInstance();
		obdaModel = fac.getOBDAModel();

		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(obdafile);
		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);
		factory.setPreferenceHolder(p);

		reasonerOBDA = (QuestOWL) factory.createReasoner(ontology,
				new SimpleConfiguration());

	}

	private int runSPARQLConceptsQuery(String description,	QuestOWLConnection conn) throws Exception {
		String query = "SELECT ?x WHERE {?x a " + description + ".}";
		QuestOWLStatement st = conn.createStatement();
		int n = 0;
		try {
			QuestOWLResultSet rs = st.executeTuple(query);
			while (rs.nextRow()) {
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
			}
			st.close();
			// conn.close();

		}

	}

	private int runSPARQLRolesQuery(String description, QuestOWLConnection conn) throws Exception {
		String query = "SELECT * WHERE {?x " + description + " ?y.  FILTER isLiteral(?y)}";
		QuestOWLStatement st = conn.createStatement();
		int n = 0;
		try {
			QuestOWLResultSet rs = st.executeTuple(query);
			while (rs.nextRow()) {
				log.debug("result : "  + rs.getOWLObject("x"));
				log.debug("result : "  + rs.getOWLObject("y"));
				n++;
			}
			// log.info("description: " + n);
			return n;

		} catch (Exception e) {
			assertTrue(false);
			throw e;

		} finally {
			try {

			} catch (Exception e) {
				st.close();
			}
			// conn.close();
			st.close();

		}

	}

}
