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

import it.unibz.krdb.obda.io.QueryIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLConnection;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLFactory;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLResultSet;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLStatement;
import it.unibz.krdb.obda.querymanager.QueryController;
import it.unibz.krdb.obda.querymanager.QueryControllerEntity;
import it.unibz.krdb.obda.querymanager.QueryControllerQuery;

import java.io.File;

import junit.framework.TestCase;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests if QuestOWL can be initialized on top of an existing semantic index
 * created by the SemanticIndexManager.
 */
public class SemanticIndexLUBMHTest extends TestCase {

	String owlfile = "src/test/resources/test/lubm-ex-20-uni1/University0-imports.owl";

	OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	private OWLOntology ontology;
	private OWLOntologyManager manager;

	Logger log = LoggerFactory.getLogger(this.getClass());

	public SemanticIndexLUBMHTest() throws Exception {
		manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument(new File("src/test/resources/test/lubm-ex-20-uni1/LUBM-ex-20.owl"));
	}

	public void test3InitializingQuest() throws Exception {
		long start = System.nanoTime();
	
		QuestOWLFactory fac = new QuestOWLFactory();

		QuestPreferences pref = new QuestPreferences();
		pref.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC_INDEX);
		pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);

		fac.setPreferenceHolder(pref);

		QuestOWL quest = fac.createReasoner(ontology);

		QuestOWLConnection qconn =  quest.getConnection();

		QuestOWLStatement st = qconn.createStatement();
		long end = System.nanoTime();
		double init_time = (end - start) / 1000000;
		start = System.nanoTime();
//		st.insertData(new File("src/test/resources/test/lubm-ex-20-uni1/merge.owl"), 50000, 5000);
		st.insertData(new File("src/test/resources/test/lubm-ex-20-uni1/University0.ttl"), 100000, 50000, "http://swat.cse.lehigh.edu/onto/univ-bench.owl#");
		end = System.nanoTime();
//		double time1 = (end - start) / 1000000000;
//		log.debug("File 1. Total insertion time: {}", time1);
//		st.insertData(new File("src/test/resources/test/lubm-ex-20-uni1/University1.ttl"), 100000, 50000, "http://swat.cse.lehigh.edu/onto/univ-bench.owl#");
//		end = System.nanoTime();
//		time1 = (end - start) / 1000000000;
//		log.debug("File 2. Total insertion time: {}", time1);
//		st.insertData(new File("src/test/resources/test/lubm-ex-20-uni1/University2.ttl"), 100000, 50000, "http://swat.cse.lehigh.edu/onto/univ-bench.owl#");
//		end = System.nanoTime();
//		time1 = (end - start) / 1000000000;
//		log.debug("File 3. Total insertion time: {}", time1);
//		st.insertData(new File("src/test/resources/test/lubm-ex-20-uni1/University3.ttl"), 100000, 50000, "http://swat.cse.lehigh.edu/onto/univ-bench.owl#");
//		end = System.nanoTime();
//		time1 = (end - start) / 1000000000;
//		log.debug("File 4. Total insertion time: {}", time1);
//		st.insertData(new File("src/test/resources/test/lubm-ex-20-uni1/University4.ttl"), 100000, 50000, "http://swat.cse.lehigh.edu/onto/univ-bench.owl#");
//		end = System.nanoTime();
//		time1 = (end - start) / 1000000000;
//		log.debug("File 5. Total insertion time: {}", time1);
//		st.insertData(new File("src/test/resources/test/lubm-ex-20-uni1/University5.ttl"), 100000, 50000, "http://swat.cse.lehigh.edu/onto/univ-bench.owl#");
//		end = System.nanoTime();
//		time1 = (end - start) / 1000000000;
//		log.debug("File 6. Total insertion time: {}", time1);
//		st.insertData(new File("src/test/resources/test/lubm-ex-20-uni1/University6.ttl"), 100000, 50000, "http://swat.cse.lehigh.edu/onto/univ-bench.owl#");
//		end = System.nanoTime();
//		time1 = (end - start) / 1000000000;
//		log.debug("File 7. Total insertion time: {}", time1);
//		st.insertData(new File("src/test/resources/test/lubm-ex-20-uni1/University7.ttl"), 100000, 50000, "http://swat.cse.lehigh.edu/onto/univ-bench.owl#");
//		end = System.nanoTime();
//		time1 = (end - start) / 1000000000;
//		log.debug("File 8. Total insertion time: {}", time1);
//		st.insertData(new File("src/test/resources/test/lubm-ex-20-uni1/University8.ttl"), 100000, 50000, "http://swat.cse.lehigh.edu/onto/univ-bench.owl#");
//		end = System.nanoTime();
//		time1 = (end - start) / 1000000000;
//		log.debug("File 9. Total insertion time: {}", time1);
//		st.insertData(new File("src/test/resources/test/lubm-ex-20-uni1/University9.ttl"), 100000, 50000, "http://swat.cse.lehigh.edu/onto/univ-bench.owl#");
//		end = System.nanoTime();
//		time1 = (end - start) / 1000000000;
//		log.debug("File 10. Total insertion time: {}", time1);
//		st.insertData(new File("src/test/resources/test/lubm-ex-20-uni1/University10.ttl"), 150000, 15000, "http://swat.cse.lehigh.edu/onto/univ-bench.owl#");
//		end = System.nanoTime();
//		time1 = (end - start) / 1000000000;
//		log.debug("File 11. Total insertion time: {}", time1);
//		st.insertData(new File("src/test/resources/test/lubm-ex-20-uni1/University11.ttl"), 150000, 15000, "http://swat.cse.lehigh.edu/onto/univ-bench.owl#");
//		end = System.nanoTime();
//		time1 = (end - start) / 1000000000;
//		log.debug("File 12. Total insertion time: {}", time1);
//		st.insertData(new File("src/test/resources/test/lubm-ex-20-uni1/University12.ttl"), 150000, 15000, "http://swat.cse.lehigh.edu/onto/univ-bench.owl#");
//		end = System.nanoTime();
//		time1 = (end - start) / 1000000000;
//		log.debug("File 13. Total insertion time: {}", time1);
//		st.insertData(new File("src/test/resources/test/lubm-ex-20-uni1/University13.ttl"), 150000, 15000, "http://swat.cse.lehigh.edu/onto/univ-bench.owl#");
//		end = System.nanoTime();
//		time1 = (end - start) / 1000000000;
//		log.debug("File 14. Total insertion time: {}", time1);
//		st.insertData(new File("src/test/resources/test/lubm-ex-20-uni1/University14.ttl"), 150000, 15000, "http://swat.cse.lehigh.edu/onto/univ-bench.owl#");
//		end = System.nanoTime();
//		time1 = (end - start) / 1000000000;
//		log.debug("File 15. Total insertion time: {}", time1);
//		st.insertData(new File("src/test/resources/test/lubm-ex-20-uni1/University15.ttl"), 150000, 15000, "http://swat.cse.lehigh.edu/onto/univ-bench.owl#");
//		end = System.nanoTime();
//		time1 = (end - start) / 1000000000;
//		log.debug("File 16. Total insertion time: {}", time1);
//		st.insertData(new File("src/test/resources/test/lubm-ex-20-uni1/University16.ttl"), 150000, 15000, "http://swat.cse.lehigh.edu/onto/univ-bench.owl#");
//		end = System.nanoTime();
//		time1 = (end - start) / 1000000000;
//		log.debug("File 17. Total insertion time: {}", time1);
//		st.insertData(new File("src/test/resources/test/lubm-ex-20-uni1/University17.ttl"), 150000, 15000, "http://swat.cse.lehigh.edu/onto/univ-bench.owl#");
//		end = System.nanoTime();
//		time1 = (end - start) / 1000000000;
//		log.debug("File 18. Total insertion time: {}", time1);
//		st.insertData(new File("src/test/resources/test/lubm-ex-20-uni1/University18.ttl"), 150000, 15000, "http://swat.cse.lehigh.edu/onto/univ-bench.owl#");
//		end = System.nanoTime();
//		time1 = (end - start) / 1000000000;
//		log.debug("File 19. Total insertion time: {}", time1);
//		st.insertData(new File("src/test/resources/test/lubm-ex-20-uni1/University19.ttl"), 150000, 15000, "http://swat.cse.lehigh.edu/onto/univ-bench.owl#");
//		end = System.nanoTime();
//		time1 = (end - start) / 1000000000;
//		log.debug("File 20. Total insertion time: {}", time1);
//		st.insertData(new File("src/test/resources/test/lubm-ex-20-uni1/University20.ttl"), 150000, 15000, "http://swat.cse.lehigh.edu/onto/univ-bench.owl#");
//		end = System.nanoTime();
//		time1 = (end - start) / 1000000000;
//		log.debug("File 21. Total insertion time: {}", time1);
//		st.insertData(new File("src/test/resources/test/lubm-ex-20-uni1/University21.ttl"), 150000, 15000, "http://swat.cse.lehigh.edu/onto/univ-bench.owl#");
//		end = System.nanoTime();
//		time1 = (end - start) / 1000000000;
//		log.debug("File 22. Total insertion time: {}", time1);
//		st.insertData(new File("src/test/resources/test/lubm-ex-20-uni1/University22.ttl"), 150000, 15000, "http://swat.cse.lehigh.edu/onto/univ-bench.owl#");
//		end = System.nanoTime();
//		time1 = (end - start) / 1000000000;
//		log.debug("File 23. Total insertion time: {}", time1);
//		st.insertData(new File("src/test/resources/test/lubm-ex-20-uni1/University23.ttl"), 150000, 15000, "http://swat.cse.lehigh.edu/onto/univ-bench.owl#");
//		end = System.nanoTime();
//		time1 = (end - start) / 1000000000;
//		log.debug("File 24. Total insertion time: {}", time1);
//		st.insertData(new File("src/test/resources/test/lubm-ex-20-uni1/University24.ttl"), 150000, 15000, "http://swat.cse.lehigh.edu/onto/univ-bench.owl#");
		
		//st.getSIRepository().createIndexes();
		quest.getQuestInstance().getSemanticIndexRepository().createIndexes(qconn.getConnection());
		
		end = System.nanoTime();
		double insert_time = (end - start) / 1000000;
		
		QueryController qc = new QueryController();
		QueryIOManager qman = new QueryIOManager(qc);
		qman.load("src/test/resources/test/treewitness/LUBM-ex-20.q");

		for (QueryControllerEntity e : qc.getElements()) {
			if (!(e instanceof QueryControllerQuery)) {
				continue;
			}
			QueryControllerQuery query = (QueryControllerQuery) e;
			log.debug("Executing query: {}", query.getID());
			log.debug("Query: \n{}", query.getQuery());

			// String query =
			// "PREFIX lubm: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>"
			// + " SELECT ?student ?staff ?course" + " WHERE { "
			// + "	?student a lubm:Student ." +
			// "	?student lubm:advisor ?staff ." + "	?staff a lubm:Faculty ."
			// + "	?student lubm:takesCourse ?course ." +
			// "	?staff lubm:teacherOf ?course ." + "	?course a lubm:Course . "
			// + " }";

			start = System.nanoTime();
			QuestOWLResultSet res = (QuestOWLResultSet) st.executeTuple(query.getQuery());
			end = System.nanoTime();

			double time = (end - start) / 1000000;

			int count = 0;
			while (res.nextRow()) {
				count += 1;
			}
			log.debug("Total result: {}", count);
			log.debug("Initialization time: {} ms", init_time);
			log.debug("Data insertion time: {} ms", insert_time);
			log.debug("Query execution time: {} ms", time);
		}
	}
}
