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

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.*;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * Test the simple ontology test-hierarchy-extended for sparql owl entailments.
 * rdfs:subclass, rdfs:subProperty, owl:inverseof owl:equivalentclass owl:equivalentProperty 
 * owl:disjointWith owl:propertyDisjointWith rdfs:domain rdfs:range
 * QuestPreferences has SPARQL_OWL_ENTAILMENT  set to true.
 *
 */
public class LUBMClassicTest {

	private OBDADataFactory fac;
	private Connection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;


	 final String owlfile =
	 "src/test/resources/subclass/LUBM.fss.owl";

	@Before
	public void setUp() throws Exception {

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));


	}

	
	

	private List<String> runTests(Properties p, String query, String function) throws Exception {

		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();

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
	@Test
	public void testSubDescription() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.SPARQL_OWL_ENTAILMENT, "true");

		log.info("Find subProperty");
		List<String> individualsProperty = runTests(p, "PREFIX : <http://example.org/university.owl#> SELECT * WHERE { ?x rdfs:subPropertyOf ?y }", "rdfs:subPropertyOf");
		assertEquals(87, individualsProperty.size());


		log.info("Find subClass");
		List<String> individualsClass = runTests(p, "PREFIX : <http://example.org/university.owl#> SELECT * WHERE { ?x rdfs:subClassOf ?y }", "rdfs:subClassOf");
		assertEquals(99, individualsClass.size());


	}
	@Test
	public void testEquivalences() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.SPARQL_OWL_ENTAILMENT, "true");

		log.info("Find equivalent classes");
		List<String> individualsEquivClass = runTests(p, "PREFIX : <http://example.org/university.owl#> SELECT * WHERE { ?x owl:equivalentClass ?y }", "owl:equivalentClass");
		assertEquals(43, individualsEquivClass.size());


		log.info("Find equivalent properties");
		List<String> individualsEquivProperties = runTests(p, "PREFIX : <http://example.org/university.owl#> SELECT * WHERE { ?x owl:equivalentProperty ?y }", "owl:equivalentProperty");
		assertEquals(65, individualsEquivProperties.size());

		//
	}
	@Test
	public void testDomains() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.SPARQL_OWL_ENTAILMENT, "true");

		log.info("Find domain");
		List<String> individualsDomainClass = runTests(p, "PREFIX : <http://example.org/university.owl#> SELECT * WHERE { ?x rdfs:domain ?y }", "rdfs:domain");
		assertEquals(67, individualsDomainClass.size());
	}

	public void testRanges() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.SPARQL_OWL_ENTAILMENT, "true");

		log.info("Find range");
		List<String> individualsRangeClass = runTests(p, "PREFIX : <http://example.org/university.owl#> SELECT * WHERE { ?x rdfs:range ?y }", "rdfs:range");
		assertEquals(63, individualsRangeClass.size());
	}
	@Test
	public void testDisjoint() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.SPARQL_OWL_ENTAILMENT, "true");

		log.info("Find disjoint classes");
		List<String> individualsDisjClass = runTests(p, "PREFIX : <http://example.org/university.owl#> SELECT * WHERE { ?x owl:disjointWith ?y }", "owl:disjointWith");
		assertEquals(0, individualsDisjClass.size());

		log.info("Find disjoint properties");
		List<String> individualsDisjProp = runTests(p, "PREFIX : <http://example.org/university.owl#> SELECT * WHERE { ?x owl:propertyDisjointWith ?y }", "owl:propertyDisjointWith");
		assertEquals(0, individualsDisjProp.size());

	}
	@Test
	public void testInverseOf() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.SPARQL_OWL_ENTAILMENT, "true");

		log.info("Find inverse");
		List<String> individualsInverse = runTests(p, "PREFIX : <http://example.org/university.owl#> SELECT * WHERE { ?x owl:inverseOf ?y }", "owl:inverseOf");
		assertEquals(58, individualsInverse.size());

	}

}
