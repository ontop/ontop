package org.semanticweb.ontop.obda;

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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.ontop.io.ModelIOManager;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.ontology.Ontology;
import org.semanticweb.ontop.owlapi3.OWLAPI3TranslatorUtility;
import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.owlapi3.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Class to test that quotes from table names are removed correctly.
 * We use the npd database.
 * @see TableJSQL
 */
public class QuotedAliasTableTest {

	private OBDADataFactory fac;
	private QuestOWLConnection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;
	private Ontology onto;

	final String owlfile = "src/test/resources/new/extended-npd-v2-ql_a_postgres.owl";
    final String obdafile = "src/test/resources/new/npd-v2.obda";


	private QuestOWL reasonerOBDA;


	@Before
	public void setUp() throws Exception {
		// Loading the OWL file
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager
				.loadOntologyFromOntologyDocument((new File(owlfile)));

		onto = OWLAPI3TranslatorUtility.translate(ontology);

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FULL_METADATA,
				QuestConstants.FALSE);



		loadOBDA(p);
	
	}

	@After
	public void tearDown() throws Exception {
		try {

			if(reasonerOBDA!=null){
			reasonerOBDA.dispose();
			}


		} catch (Exception e) {
			log.debug(e.getMessage());
			assertTrue(false);
		}

	}
	




	/**
	 * Test OBDA table
	 * @throws Exception
	 */
	@Test
	public void test() throws Exception {
		
		// Now we are ready for querying obda
		// npd query 1
		int obdaResult = npdQuery(reasonerOBDA.getConnection());
		assertEquals(52668, obdaResult);



	}



	

	/**
	 * Execute Npd query 1 and give the number of results
	 * @return 
	 */
	private int npdQuery(QuestOWLConnection questOWLConnection) throws OWLException {
		String query = "PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#> SELECT DISTINCT ?x WHERE {"
				+ "?x a npdv:CompanyReserve . "
				+	" }";
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
				assertTrue(false);
			}
			// conn.close();
			st.close();

		}
		return n;

	}



	/**
	 * Create obda model from obda file and prepare the reasoner
	 * 
	 * @param p
	 *            quest preferences for QuestOWL, dataSource for the model
	 */

	private void loadOBDA(QuestPreferences p) throws Exception {
		// Loading the OBDA data
		log.info("Loading obda file");
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



}
