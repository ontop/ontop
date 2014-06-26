package it.unibz.krdb.r2rml;


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
import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLConnection;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLEmptyEntitiesChecker;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLFactory;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLResultSet;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLStatement;
import it.unibz.krdb.obda.sesame.r2rml.R2RMLReader;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use the class EmptiesAboxCheck to test the return of empty concepts and
 * roles, based on the mappings. Given ontology, which is connected to a
 * database via mappings, generate a suitable set of queries that test if there
 * are empty concepts, concepts that are no populated to anything.
 */
public class R2rmlChecker {

	private OBDADataFactory fac;
	private QuestOWLConnection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	 final String owlfile =
	 "src/test/resources/r2rml/npd-v2-ql_a.owl";
	 final String obdafile =
	 "src/test/resources/r2rml/npd-v2-ql_a.obda";

//	 final String r2rmlfile =
//			 "src/test/resources/r2rml/npd-v2-ql_a.ttl";
	 final String r2rmlfile =
			 "src/test/resources/r2rml/npd.ttl";
	private List<Predicate> emptyConceptsObda = new ArrayList<Predicate>();
	private List<Predicate> emptyRolesObda = new ArrayList<Predicate>();
	private List<Predicate> emptyConceptsR2rml = new ArrayList<Predicate>();
	private List<Predicate> emptyRolesR2rml = new ArrayList<Predicate>();

	private QuestOWL reasoner;

	@Before
	public void setUp() throws Exception {


		

	}

	@After
	public void tearDown() throws Exception {
		try {

			reasoner.dispose();
			
		} catch (Exception e) {
			log.debug(e.getMessage());
		}

	}

	/**
	 * Test numbers of empty concepts and roles
	 * 
	 * @throws Exception
	 */
	@Test
	public void testOBDA() throws Exception {

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager
				.loadOntologyFromOntologyDocument((new File(owlfile)));

		// Loading the OBDA data
		fac = OBDADataFactoryImpl.getInstance();
		obdaModel = fac.getOBDAModel();

		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(obdafile);

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FULL_METADATA,
				QuestConstants.FALSE);
		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);

		factory.setPreferenceHolder(p);

		reasoner = (QuestOWL) factory.createReasoner(ontology,
				new SimpleConfiguration());

		// Now we are ready for querying
		conn = reasoner.getConnection();

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
		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();

		factory.setPreferenceHolder(p);

		R2RMLReader reader = new R2RMLReader(r2rmlfile);
		
		OBDADataFactory f = OBDADataFactoryImpl.getInstance();
		
		String sourceUrl = "http://example.org/customOBDA";
		OBDADataSource dataSource = f.getJDBCDataSource(sourceUrl, jdbcurl, username, password, driverclass);
		
		obdaModel = reader.readModel(dataSource);
		
		factory.setOBDAController(obdaModel);

		reasoner = (QuestOWL) factory.createReasoner(ontology,
				new SimpleConfiguration());

		// Now we are ready for querying
		conn = reasoner.getConnection();
		
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
	
	@Test
	public void testCompares() throws Exception {
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
				// Creating a new instance of the reasoner
				QuestOWLFactory factory = new QuestOWLFactory();

				factory.setPreferenceHolder(p);

				R2RMLReader reader = new R2RMLReader(r2rmlfile);
				
				OBDADataFactory f = OBDADataFactoryImpl.getInstance();
				
				String sourceUrl = "http://example.org/customOBDA";
				OBDADataSource dataSource = f.getJDBCDataSource(sourceUrl, jdbcurl, username, password, driverclass);
				
				obdaModel = reader.readModel(dataSource);
				
				factory.setOBDAController(obdaModel);

				reasoner = (QuestOWL) factory.createReasoner(ontology,
						new SimpleConfiguration());

				// Now we are ready for querying
				conn = reasoner.getConnection();
				
				String query = "PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#> SELECT DISTINCT ?licenceURI WHERE { ?licenceURI a npdv:ProductionLicence .}";
				QuestOWLStatement st = conn.createStatement();
				int n=0;
				try {
					QuestOWLResultSet rs = st.executeTuple(query);
					while(rs.nextRow()){
						n++;
					}
					log.info("q1: " + n);

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
		
	}
}
