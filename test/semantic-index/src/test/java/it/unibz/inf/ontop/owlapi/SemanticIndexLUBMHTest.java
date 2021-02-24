package it.unibz.inf.ontop.owlapi;

/*
 * #%L
 * ontop-quest-owlapi
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

import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import it.unibz.inf.ontop.si.OntopSemanticIndexLoader;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static it.unibz.inf.ontop.injection.OntopReformulationSettings.EXISTENTIAL_REASONING;

/**
 * Tests if QuestOWL can be initialized on top of an existing semantic index
 * created by the SemanticIndexManager.
 */
@Ignore("Failing on most of the queries")
public class SemanticIndexLUBMHTest {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private static final String owlFilePath = "src/test/resources/test/lubm-ex-20-uni1/LUBM-ex-20.owl";

	@Test
	public void test3InitializingQuest() throws Exception {
		long start = System.nanoTime();

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology mainOntology = manager.loadOntologyFromOntologyDocument(new File(owlFilePath));
		Set<OWLAxiom> axioms = new HashSet<>();
		axioms.addAll(mainOntology.getTBoxAxioms(Imports.INCLUDED));
		axioms.addAll(mainOntology.getABoxAxioms(Imports.INCLUDED));

		long end = System.nanoTime();
		long init_time = (end - start) / 1_000_000;
		start = System.nanoTime();
//		st.insertData(new File("src/test/resources/test/lubm-ex-20-uni1/merge.owl"), 50000, 5000);
		axioms.addAll(manager.loadOntologyFromOntologyDocument(new File("src/test/resources/test/lubm-ex-20-uni1/University0.ttl"))
				.getABoxAxioms(Imports.EXCLUDED));
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

		end = System.nanoTime();

		OWLOntologyManager newManager = OWLManager.createOWLOntologyManager();
		OWLOntology completeOntology = newManager.createOntology(axioms);

		long insert_time = (end - start) / 1_000_000;

		Properties p = new Properties();
		p.setProperty(EXISTENTIAL_REASONING, "true");
		OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
		try(OntopSemanticIndexLoader loader = OntopSemanticIndexLoader.loadOntologyIndividuals(completeOntology, p);
			OntopOWLReasoner reasoner = factory.createReasoner(loader.getConfiguration());
			OWLConnection connection = reasoner.getConnection();
			OWLStatement st = connection.createStatement()) {

			/**
			 *  @see src/test/resources/test/treewitness/LUBM-ex-20.q for queries
			 */

            //QueryController qc = new QueryController();
            //QueryIOManager qman = new QueryIOManager(qc);
            // qman.load(new File(""));

            //for (QueryControllerEntity e : qc.getElements()) {
            //    if (e instanceof QueryControllerQuery) {
			//		QueryControllerQuery query = (QueryControllerQuery) e;
			//		log.debug("Executing query: {}", query.getID());
			//		log.debug("Query: \n{}", query.getQuery());

					// String query =
					// "PREFIX lubm: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>"
					// + " SELECT ?student ?staff ?course" + " WHERE { "
					// + "	?student a lubm:Student ." +
					// "	?student lubm:advisor ?staff ." + "	?staff a lubm:Faculty ."
					// + "	?student lubm:takesCourse ?course ." +
					// "	?staff lubm:teacherOf ?course ." + "	?course a lubm:Course . "
					// + " }";

					start = System.nanoTime();
			//		TupleOWLResultSet res = st.executeSelectQuery(query.getQuery());
					end = System.nanoTime();

					long time = (end - start) / 1_000_000;

					int count = 0;
			//		while (res.hasNext()) {
			//			count += 1;
			//		}
					log.debug("Total result: {}", count);
					log.debug("Initialization time: {} ms", init_time);
					log.debug("Data insertion time: {} ms", insert_time);
					log.debug("Query execution time: {} ms", time);
			//	}
            //}
		}
	}
}
