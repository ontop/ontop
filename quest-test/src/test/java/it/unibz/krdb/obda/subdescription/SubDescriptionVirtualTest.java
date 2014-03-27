package it.unibz.krdb.obda.subdescription;

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
public class SubDescriptionVirtualTest extends TestCase {

	// TODO We need to extend this test to import the contents of the mappings
	// into OWL and repeat everything taking form OWL

	private OBDADataFactory fac;
	private Connection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	final String owlfile = "src/test/resources/subclass/test-hierarchy-extended.owl";
	final String obdafile = "src/test/resources/subclass/test-hierarchy-extended.obda";

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

	private List<OWLIndividual> runTests(Properties p, String query, String function) throws Exception {

		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);

		factory.setPreferenceHolder(p);

		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

		// Now we are ready for querying
		QuestOWLConnection conn = reasoner.getConnection();
		QuestOWLStatement st = conn.createStatement();
		List<OWLIndividual> individuals = new LinkedList<OWLIndividual>();

		StringBuilder bf = new StringBuilder(query);
		try {

			QuestOWLResultSet rs = st.executeTuple(query);
			while (rs.nextRow())
			{
				OWLIndividual ind1 = rs.getOWLIndividual("x");
				OWLIndividual ind2 = rs.getOWLIndividual("y");
				System.out.println(ind1 + " "+ function + " "+ ind2);
				individuals.add(ind1);
				individuals.add(ind2);
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

	public void testViEqSig() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.SPARQL_OWL_ENTAILMENT, "true");
		
		log.info("Find subProperty");
		List<OWLIndividual> individualsProperty=runTests(p, "PREFIX : <http://it.unibz.krdb/obda/test/simple#> SELECT * WHERE { ?x rdfs:subPropertyOf ?y }", "rdfs:subPropertyOf");
		assertEquals(48, individualsProperty.size());
		
		log.info("Find subClass");
		List<OWLIndividual> individualsClass= runTests(p, "PREFIX : <http://it.unibz.krdb/obda/test/simple#> SELECT * WHERE { ?x rdfs:subClassOf ?y }", "rdfs:subClassOf");
//		
//		assertEquals(10, individualsClass.size());
//		assertEquals(individualsClass.get(0).toStringID(),"http://obda.inf.unibz.it/sparql/test-hierarchy.owl#Woman");
//		assertEquals(individualsClass.get(1).toStringID(),"http://obda.inf.unibz.it/sparql/test-hierarchy.owl#Person");
//		assertEquals(individualsClass.get(2).toStringID(),"http://obda.inf.unibz.it/sparql/test-hierarchy.owl#Man");
//		assertEquals(individualsClass.get(3).toStringID(),"http://obda.inf.unibz.it/sparql/test-hierarchy.owl#Man");
//		assertEquals(individualsClass.get(8).toStringID(),"http://obda.inf.unibz.it/sparql/test-hierarchy.owl#Man");
//		assertEquals(individualsClass.get(9).toStringID(),"http://obda.inf.unibz.it/sparql/test-hierarchy.owl#Person");
		
		log.info("Find equivalences");
		List<OWLIndividual> individualsEquivClass= runTests(p, "PREFIX : <http://it.unibz.krdb/obda/test/simple#> SELECT * WHERE { ?x owl:equivalentClass ?y }", "owl:equivalentClass");
		assertEquals(4, individualsEquivClass.size());
	}

	

}
