package it.unibz.krdb.obda.reformulation.tests;

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
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test the simple ontology animals for sparql owl entailments.
 * rdfs:subclass, rdfs:subProperty, owl:inverseof owl:equivalentclass owl:equivalentProperty 
 * owl:disjointWith owl:propertyDisjointWith rdfs:domain rdfs:range
 * QuestPreferences has SPARQL_OWL_ENTAILMENT  set to true.
 *
 */
public class OWLEntailmentsinSPARQL extends TestCase {

	private OBDADataFactory fac;
	private Connection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;


	 final String owlfile =
	 "src/test/resources/test/entailments/animals.owl";
	 final String obdafile =
	 "src/test/resources/test/entailments/animals.obda";

	@Override
	public void setUp() throws Exception {

		/*
		 * Initializing and H2 database with the stock exchange data
		 */
	
		String url = "jdbc:h2:mem:questjunitdb";
		String username = "sa";
		String password = "";

		fac = OBDADataFactoryImpl.getInstance();

		conn = DriverManager.getConnection(url, username, password);
		Statement st = conn.createStatement();

		FileReader reader = new FileReader("src/test/resources/test/entailments/create-animals.sql");
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

		FileReader reader = new FileReader("src/test/resources/test/entailments/animals-drop.sql");
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
		List<String> individualsProperty = runTests(p, "PREFIX : <http://www.semanticweb.org/sarah/ontologies/2014/3/untitled-ontology-35#> SELECT * WHERE { ?x rdfs:subPropertyOf ?y }", "rdfs:subPropertyOf");
		assertEquals(36, individualsProperty.size());

		log.info("Find subProperty");
		List<String> property = runSingleNamedIndividualTests(p, "PREFIX : <http://www.semanticweb.org/sarah/ontologies/2014/3/untitled-ontology-35#> SELECT * WHERE { :likesPet rdfs:subPropertyOf ?x }",
				"rdfs:subPropertyOf");
		assertEquals(3, property.size());

		log.info("Find subClass");
		List<String> individualsClass = runTests(p, "PREFIX : <http://www.semanticweb.org/sarah/ontologies/2014/3/untitled-ontology-35#> SELECT * WHERE { ?x rdfs:subClassOf ?y }", "rdfs:subClassOf");
		assertEquals(86, individualsClass.size());

		log.info("Find subClass");
		List<String> classes = runSingleNamedIndividualTests(p, "PREFIX : <http://www.semanticweb.org/sarah/ontologies/2014/3/untitled-ontology-35#> SELECT * WHERE { ?x rdfs:subClassOf :Person }", "rdfs:subClassOf");
		assertEquals(6, classes.size());

	}

	public void testEquivalences() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.SPARQL_OWL_ENTAILMENT, "true");

		log.info("Find equivalent classes");
		List<String> individualsEquivClass = runTests(p, "PREFIX : <http://www.semanticweb.org/sarah/ontologies/2014/3/untitled-ontology-35#> SELECT * WHERE { ?x owl:equivalentClass ?y }", "owl:equivalentClass");
		assertEquals(50, individualsEquivClass.size());

		List<String> equivClass = runSingleNamedIndividualTests(p,
				"PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX : <http://www.semanticweb.org/sarah/ontologies/2014/3/untitled-ontology-35#> select * where {?x owl:equivalentClass :Person }", "owl:equivalentClass");
		assertEquals(1, equivClass.size());
		
		log.info("Find equivalent properties");
		List<String> individualsEquivProperties = runTests(p, "PREFIX : <http://www.semanticweb.org/sarah/ontologies/2014/3/untitled-ontology-35#> SELECT * WHERE { ?x owl:equivalentProperty ?y }", "owl:equivalentProperty");
		assertEquals(30, individualsEquivProperties.size());

		List<String> equivProperty = runSingleNamedIndividualTests(p,
				"PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX : <http://www.semanticweb.org/sarah/ontologies/2014/3/untitled-ontology-35#> select * where {?x owl:equivalentProperty :likesPet }", "owl:equivalentProperty");
		assertEquals(3, equivProperty.size());
	}

	public void testDomains() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.SPARQL_OWL_ENTAILMENT, "true");

		log.info("Find domain");
		List<String> individualsDomainClass = runTests(p, "PREFIX : <http://www.semanticweb.org/sarah/ontologies/2014/3/untitled-ontology-35#> SELECT * WHERE { ?x rdfs:domain ?y }", "rdfs:domain");
		assertEquals(10, individualsDomainClass.size());
		
		log.info("Find one domain");
		List<String> domain = runSingleNamedIndividualTests(p,
				"PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX : <http://www.semanticweb.org/sarah/ontologies/2014/3/untitled-ontology-35#> SELECT * WHERE { ?x rdfs:domain :AnimalLover }", "rdfs:domain");
		assertEquals(4, domain.size());
	}

	public void testRanges() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.SPARQL_OWL_ENTAILMENT, "true");

		log.info("Find range");
		List<String> individualsRangeClass = runTests(p, "PREFIX : <http://www.semanticweb.org/sarah/ontologies/2014/3/untitled-ontology-35#> SELECT * WHERE { ?x rdfs:range ?y }", "rdfs:range");
		assertEquals(10, individualsRangeClass.size());
		
		log.info("Find one range");
		List<String> range = runSingleNamedIndividualTests(p,
				"PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX : <http://www.semanticweb.org/sarah/ontologies/2014/3/untitled-ontology-35#> SELECT * WHERE { :ssn rdfs:range ?x }", "rdfs:range");
		assertEquals(1, range.size());
	}

	public void testDisjoint() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.SPARQL_OWL_ENTAILMENT, "true");

		log.info("Find disjoint classes");
		List<String> individualsDisjClass = runTests(p, "PREFIX : <http://www.semanticweb.org/sarah/ontologies/2014/3/untitled-ontology-35#> SELECT * WHERE { ?x owl:disjointWith ?y }", "owl:disjointWith");
		assertEquals(84, individualsDisjClass.size());
		
		log.info("Find a disjoint class");
		List<String> disjClass = runSingleNamedIndividualTests(p,
				"PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX : <http://www.semanticweb.org/sarah/ontologies/2014/3/untitled-ontology-35#> SELECT * WHERE { :Animal owl:disjointWith ?x }", "owl:disjointWith");
		assertEquals(2, disjClass.size());
		
		log.info("Find disjoint properties");
		List<String> individualsDisjProp = runTests(p, "PREFIX : <http://www.semanticweb.org/sarah/ontologies/2014/3/untitled-ontology-35#> SELECT * WHERE { ?x owl:propertyDisjointWith ?y }", "owl:propertyDisjointWith");
		assertEquals(26, individualsDisjProp.size());
		
		log.info("Find a disjoint property");
		List<String> disjProp = runSingleNamedIndividualTests(p,
				"PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX : <http://www.semanticweb.org/sarah/ontologies/2014/3/untitled-ontology-35#> SELECT * WHERE { ?x owl:propertyDisjointWith :eats }", "owl:propertyDisjointWith");
		assertEquals(4, disjProp.size());

	}
	
	public void testInverseOf() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.SPARQL_OWL_ENTAILMENT, "true");

		log.info("Find inverse");
		List<String> individualsInverse = runTests(p, "PREFIX : <http://www.semanticweb.org/sarah/ontologies/2014/3/untitled-ontology-35#> SELECT * WHERE { ?x owl:inverseOf ?y }", "owl:inverseOf");
		assertEquals(30, individualsInverse.size());
		
		log.info("Find a disjoint property");
		List<String> inverse = runSingleNamedIndividualTests(p,
				"PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX : <http://www.semanticweb.org/sarah/ontologies/2014/3/untitled-ontology-35#>  SELECT * WHERE { ?x owl:inverseOf :isPetOf }", "owl:inverseOf");
		assertEquals(3, inverse.size());

	}

}
