package it.unibz.krdb.obda.sparql.entailments;

/*
 * #%L
 * ontop-quest-owlapi3
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

import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLConnection;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLFactory;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLResultSet;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLStatement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * A simple test that check if the system is able to handle Mappings for
 * classes/roles and attributes even if there are no URI templates. i.e., the
 * database stores URI's directly.
 * 
 * We are going to create an H2 DB, the .sql file is fixed. We will map directly
 * there and then query on top.
 */
public class HierarchyVirtualTest extends TestCase {

	// TODO We need to extend this test to import the contents of the mappings
	// into OWL and repeat everything taking form OWL

	private OBDADataFactory fac;
	private Connection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	// final String owlfile = "src/test/resources/subclass/test-hierarchy.owl";
	// final String obdafile =
	// "src/test/resources/subclass/test-hierarchy.obda";
	final String owlfile = "src/test/resources/subclass/test-hierarchy2.owl";
	final String obdafile = "src/test/resources/subclass/test-hierarchy2.obda";

	// final String owlfile =
	// "src/test/resources/subclass/test-hierarchy-extended.owl";
	// final String obdafile =
	// "src/test/resources/subclass/test-hierarchy-extended.obda";

	@Override
	public void setUp() throws Exception {

		/*
		 * Initializing and H2 database with the stock exchange data
		 */
		// String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:questjunitdb";
		String username = "sa";
		String password = "";

		fac = OBDADataFactoryImpl.getInstance();

		conn = DriverManager.getConnection(url, username, password);
		Statement st = conn.createStatement();

		FileReader reader = new FileReader("src/test/resources/subclass/create-h2.sql");
		BufferedReader in = new BufferedReader(reader);
		StringBuilder bf = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			bf.append(line);
			line = in.readLine();
		}

		st.executeUpdate(bf.toString());
		conn.commit();

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));

		// Loading the OBDA data
		obdaModel = fac.getOBDAModel();

		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(obdafile);

	}

	@Override
	public void tearDown() throws Exception {
		try {
			dropTables();
			conn.close();
		} catch (Exception e) {
			log.debug(e.getMessage());
		}
	}

	private void dropTables() throws SQLException, IOException {

		Statement st = conn.createStatement();

		FileReader reader = new FileReader("src/test/resources/subclass/drop-h2.sql");
		BufferedReader in = new BufferedReader(reader);
		StringBuilder bf = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			bf.append(line);
			line = in.readLine();
		}

		st.executeUpdate(bf.toString());
		st.close();
		conn.commit();
	}

	private List<String> runTests(Properties p, String query, String function) throws Exception {

		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);

		factory.setPreferenceHolder(p);

		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

		// Now we are ready for querying
		QuestOWLConnection conn = reasoner.getConnection();
		QuestOWLStatement st = conn.createStatement();
		List<String> individuals = new LinkedList<String>();

		StringBuilder bf = new StringBuilder(query);
		try {

			QuestOWLResultSet rs = st.executeTuple(query);
			while (rs.nextRow())
			{
				OWLIndividual ind1 = rs.getOWLIndividual("x");
				OWLIndividual ind2 = rs.getOWLIndividual("y");

				System.out.println(ind1 + " " + function + " " + ind2);
				individuals.add(ind1 + " " + function + " " + ind2);

			}
			return individuals;

		} catch (Exception e) {
			throw e;
		} finally {
			try {

			} catch (Exception e) {
				st.close();
			}
			conn.close();
			reasoner.dispose();
		}
	}

	private List<String> runSingleNamedIndividualTests(Properties p, String query, String function) throws Exception {

		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);

		factory.setPreferenceHolder(p);

		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

		// Now we are ready for querying
		QuestOWLConnection conn = reasoner.getConnection();
		QuestOWLStatement st = conn.createStatement();
		List<String> individuals = new LinkedList<String>();

		StringBuilder bf = new StringBuilder(query);
		try {

			QuestOWLResultSet rs = st.executeTuple(query);
			while (rs.nextRow())
			{
				OWLIndividual ind2 = rs.getOWLIndividual("x");

				System.out.println(ind2);
				if (ind2.isNamed())
					individuals.add(ind2.toString());

			}
			return individuals;

		} catch (Exception e) {
			throw e;
		} finally {
			try {

			} catch (Exception e) {
				st.close();
			}
			conn.close();
			reasoner.dispose();
		}
	}

	public void testSubDescription() throws Exception {
		
		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.SPARQL_OWL_ENTAILMENT, "true");

		log.info("Find subProperty");
		List<String> individualsProperty = runTests(p, "PREFIX : <http://obda.inf.unibz.it/sparql/test-hierarchy.owl/> SELECT * WHERE { ?x rdfs:subPropertyOf ?y }", "rdfs:subPropertyOf");
		assertEquals(22, individualsProperty.size());

		log.info("Find subProperty");
		List<String> property = runSingleNamedIndividualTests(p, "PREFIX : <http://obda.inf.unibz.it/sparql/test-hierarchy.owl/> SELECT * WHERE { :isSibling rdfs:subPropertyOf ?x }",
				"rdfs:subPropertyOf");
		assertEquals(4, property.size());

		log.info("Find subClass");
		List<String> individualsClass = runTests(p, "PREFIX : <http://obda.inf.unibz.it/sparql/test-hierarchy.owl/> SELECT * WHERE { ?x rdfs:subClassOf ?y }", "rdfs:subClassOf");
		assertEquals(59, individualsClass.size());

		log.info("Find subClass");
		List<String> classes = runSingleNamedIndividualTests(p, "PREFIX : <http://obda.inf.unibz.it/sparql/test-hierarchy.owl/> SELECT * WHERE { ?x rdfs:subClassOf :Man }", "rdfs:subClassOf");
		assertEquals(2, classes.size());

	}

	public void testEquivalences() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.SPARQL_OWL_ENTAILMENT, "true");

		log.info("Find equivalences");
		List<String> individualsEquivClass = runTests(p, "PREFIX : <http://obda.inf.unibz.it/sparql/test-hierarchy.owl/> SELECT * WHERE { ?x owl:equivalentClass ?y }", "owl:equivalentClass");
		assertEquals(23, individualsEquivClass.size());

		List<String> equivClass = runSingleNamedIndividualTests(p,
				"PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX : <http://obda.inf.unibz.it/sparql/test-hierarchy.owl/> select * where {?x owl:equivalentClass :Girl }", "owl:equivalentClass");
		assertEquals(3, equivClass.size());
	}

	public void testDomains() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.SPARQL_OWL_ENTAILMENT, "true");

		log.info("Find domain");
		List<String> individualsDomainClass = runTests(p, "PREFIX : <http://obda.inf.unibz.it/sparql/test-hierarchy.owl/> SELECT * WHERE { ?x rdfs:domain ?y }", "rdfs:domain");
		assertEquals(2, individualsDomainClass.size());
	}

	public void testRanges() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.SPARQL_OWL_ENTAILMENT, "true");

		log.info("Find range");
		List<String> individualsRangeClass = runTests(p, "PREFIX : <http://obda.inf.unibz.it/sparql/test-hierarchy.owl/> SELECT * WHERE { ?x rdfs:range ?y }", "rdfs:range");
		assertEquals(0, individualsRangeClass.size());
	}

	public void testDisjointClasses() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.SPARQL_OWL_ENTAILMENT, "true");

		log.info("Find disjoint classes");
		List<String> individualsDisjClass = runTests(p, "PREFIX : <http://obda.inf.unibz.it/sparql/test-hierarchy.owl/> SELECT * WHERE { ?x owl:disjointWith ?y }", "owl:disjointWith");
		assertEquals(2, individualsDisjClass.size());

	}
	
	public void testInverseOf() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.SPARQL_OWL_ENTAILMENT, "true");

		log.info("Find inverse");
		List<String> individualsInverse = runTests(p, "PREFIX : <http://obda.inf.unibz.it/sparql/test-hierarchy.owl/> SELECT * WHERE { ?x owl:inverseOf ?y }", "owl:inverseOf");
		assertEquals(0, individualsInverse.size());

	}

}
